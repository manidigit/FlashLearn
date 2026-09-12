package com.flashlearn.domain.backup

import com.flashlearn.domain.model.*
import java.time.Instant
import java.util.UUID

enum class BackupType { VOCABULARY, PROGRESS, FULL }

data class BackupData(
    val schemaVersion: Int,
    val exportedAt: Instant,
    val backupType: BackupType,
    val languages: List<BackupLanguage> = emptyList(),
    val categories: List<Category> = emptyList(),
    val tags: List<BackupTag> = emptyList(),
    val languagePairs: List<BackupLanguagePair> = emptyList(),
    val concepts: List<Concept> = emptyList(),
    val contents: List<Content> = emptyList(),
    val settings: Map<String, String> = emptyMap(),
    val reviewSessions: List<ReviewSession> = emptyList(),
    val reviewHistory: List<ReviewHistory> = emptyList(),
    val learningStates: List<LearningState> = emptyList(),
    val difficultyStates: List<DifficultyState> = emptyList(),
    val achievements: List<BackupAchievement> = emptyList(),
    val conceptReferences: Set<UUID> = emptySet()
)

data class BackupLanguage(val id: UUID, val code: String, val name: String)
data class BackupTag(val id: UUID, val name: String)
data class BackupLanguagePair(val id: UUID, val sourceLanguageId: UUID, val targetLanguageId: UUID)
data class BackupAchievement(val id: UUID, val key: String, val unlockedAt: Instant?)

data class BackupValidationIssue(val code: String, val message: String)
data class BackupValidationResult(val valid: Boolean, val issues: List<BackupValidationIssue>)

object BackupValidator {
    fun validate(data: BackupData, supportedSchemaVersion: Int): BackupValidationResult {
        val issues = mutableListOf<BackupValidationIssue>()
        if (data.schemaVersion !in 1..supportedSchemaVersion)
            issues += BackupValidationIssue("UNSUPPORTED_SCHEMA", "Unsupported schemaVersion=${data.schemaVersion}")

        fun <T> duplicateIds(items: List<T>, id: (T) -> UUID, label: String) {
            val duplicates = items.groupingBy(id).eachCount().filterValues { it > 1 }.keys
            if (duplicates.isNotEmpty())
                issues += BackupValidationIssue("DUPLICATE_UUID", "$label contains duplicate UUIDs: $duplicates")
        }

        duplicateIds(data.languages, BackupLanguage::id, "languages")
        duplicateIds(data.categories, Category::id, "categories")
        duplicateIds(data.tags, BackupTag::id, "tags")
        duplicateIds(data.languagePairs, BackupLanguagePair::id, "languagePairs")
        duplicateIds(data.concepts, Concept::id, "concepts")
        duplicateIds(data.contents, Content::id, "contents")
        duplicateIds(data.learningStates, LearningState::id, "learningStates")
        duplicateIds(data.difficultyStates, DifficultyState::id, "difficultyStates")
        duplicateIds(data.reviewSessions, ReviewSession::id, "reviewSessions")
        duplicateIds(data.reviewHistory, ReviewHistory::id, "reviewHistory")
        duplicateIds(data.achievements, BackupAchievement::id, "achievements")

        val conceptIds = data.concepts.map { it.id }.toSet()
        val categoryIds = data.categories.map { it.id }.toSet()
        val sessionIds = data.reviewSessions.map { it.id }.toSet()
        data.concepts.forEach {
            if (it.categoryId != null && it.categoryId !in categoryIds)
                issues += BackupValidationIssue("BROKEN_REFERENCE", "Concept ${it.id} references missing Category ${it.categoryId}")
        }
        data.contents.forEach { if (it.conceptId !in conceptIds) issues += BackupValidationIssue("BROKEN_REFERENCE", "Content ${it.id} references missing Concept ${it.conceptId}") }
        data.learningStates.forEach { if (it.conceptId !in conceptIds) issues += BackupValidationIssue("BROKEN_REFERENCE", "LearningState ${it.id} references missing Concept ${it.conceptId}") }
        data.difficultyStates.forEach { if (it.conceptId !in conceptIds) issues += BackupValidationIssue("BROKEN_REFERENCE", "DifficultyState ${it.id} references missing Concept ${it.conceptId}") }
        data.reviewHistory.forEach {
            if (it.conceptId !in conceptIds) issues += BackupValidationIssue("BROKEN_REFERENCE", "ReviewHistory ${it.id} references missing Concept ${it.conceptId}")
            if (it.sessionId !in sessionIds) issues += BackupValidationIssue("BROKEN_REFERENCE", "ReviewHistory ${it.id} references missing ReviewSession ${it.sessionId}")
        }
        data.conceptReferences.forEach {
            if (it !in conceptIds) issues += BackupValidationIssue("BROKEN_REFERENCE", "Concept reference points to missing Concept $it")
        }

        data.tags.let { tags ->
            val duplicateNames = tags.groupingBy { it.name.trim() }.eachCount().filterValues { it > 1 }.keys
            if (duplicateNames.isNotEmpty()) issues += BackupValidationIssue("DUPLICATE_VALUE", "tags contain duplicate names: $duplicateNames")
        }
        if (data.settings.keys.any { it.isBlank() })
            issues += BackupValidationIssue("INVALID_VALUE", "settings contain a blank key")
        data.reviewHistory.let { history ->
            val duplicateAttempts = history.groupingBy { it.sessionId to it.reviewAttemptId }.eachCount().filterValues { it > 1 }.keys
            if (duplicateAttempts.isNotEmpty()) issues += BackupValidationIssue("DUPLICATE_ATTEMPT", "reviewHistory contains duplicate attempts: $duplicateAttempts")
        }

        val languageIds = data.languages.map { it.id }.toSet()
        data.languagePairs.forEach {
            if (it.sourceLanguageId !in languageIds || it.targetLanguageId !in languageIds)
                issues += BackupValidationIssue("BROKEN_REFERENCE", "LanguagePair ${it.id} references missing language")
        }

        if (data.backupType == BackupType.PROGRESS && data.concepts.isNotEmpty())
            issues += BackupValidationIssue("TYPE_MISMATCH", "PROGRESS backup must not contain Concepts")
        if (data.backupType == BackupType.VOCABULARY &&
            (data.learningStates.isNotEmpty() || data.difficultyStates.isNotEmpty() || data.reviewHistory.isNotEmpty()))
            issues += BackupValidationIssue("TYPE_MISMATCH", "VOCABULARY backup must not contain progress data")

        return BackupValidationResult(issues.isEmpty(), issues)
    }
}
