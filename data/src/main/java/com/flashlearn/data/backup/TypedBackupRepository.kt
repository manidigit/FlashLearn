package com.flashlearn.data.backup

import com.flashlearn.database.*
import com.flashlearn.domain.backup.BackupType
import com.flashlearn.domain.repository.RestoreResult
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TypedBackupRepository @Inject constructor(
    private val db: RoomFlashLearnDatabase,
    private val fullBackupRepository: RoomBackupRepository,
    private val vocabularyBackupRepository: VocabularyBackupRepository
) {
    suspend fun export(type: BackupType): String {
        val root = JSONObject().put("schemaVersion", 2).put("exportedAt", Instant.now().toString()).put("backupType", type.name)
        when (type) {
            BackupType.VOCABULARY -> {
                root.put("concepts", JSONArray(db.conceptDao().getAll().map { JSONObject().put("id", it.id.toString()).put("entryType", it.entryType).put("categoryId", it.categoryId?.toString()).put("favorite", it.favorite).put("active", it.active).put("createdAt", it.createdAt.toString()).put("updatedAt", it.updatedAt.toString()) }))
                root.put("contents", JSONArray(db.contentDao().getAll().map { JSONObject().put("id", it.id.toString()).put("conceptId", it.conceptId.toString()).put("languageCode", it.languageCode).put("text", it.text).put("canonicalKey", it.canonicalKey).put("notes", it.notes).put("grammarNote", it.grammarNote).put("possibleCorrection", it.possibleCorrection).put("translationIndex", it.translationIndex).put("pronunciation", it.pronunciation).put("example", it.example) }))
                root.put("tags", JSONArray(db.tagDao().getAll().map { JSONObject().put("id", it.id.toString()).put("name", it.name) }))
                root.put("categories", JSONArray(db.categoryDao().getAll().map { JSONObject().put("id", it.id.toString()).put("name", it.name) }))
                root.put("relations", JSONArray(db.vocabularyRelationDao().getAll().map { JSONObject().put("id", it.id.toString()).put("sourceConceptId", it.sourceConceptId.toString()).put("targetConceptId", it.targetConceptId?.toString()).put("relationType", it.relationType).put("unresolvedText", it.unresolvedText) }))
                root.put("variants", JSONArray(db.vocabularyVariantDao().getAll().map { JSONObject().put("id", it.id.toString()).put("conceptId", it.conceptId.toString()).put("text", it.text).put("variantType", it.variantType) }))
                root.put("languages", JSONArray(db.languageDao().getAll().map { JSONObject().put("code", it.code).put("name", it.name).put("active", it.active) }))
                root.put("languagePairs", JSONArray(db.languagePairDao().getAll().map { JSONObject().put("sourceLanguageCode", it.sourceLanguageCode).put("targetLanguageCode", it.targetLanguageCode).put("active", it.active) }))
            }
            BackupType.PROGRESS -> {
                root.put("learningStates", JSONArray(db.learningStateDao().getAll().map { JSONObject().put("id", it.id.toString()).put("conceptId", it.conceptId.toString()).put("stage", it.stage).put("nextReviewAt", it.nextReviewAt?.toString()).put("monthlyWrongCount", it.monthlyWrongCount).put("hasPathFailure", it.hasPathFailure).put("totalCorrect", it.totalCorrect).put("totalWrong", it.totalWrong).put("lastReviewedAt", it.lastReviewedAt?.toString()) }))
                root.put("difficultyStates", JSONArray(db.difficultyStateDao().getAll().map { JSONObject().put("id", it.id.toString()).put("conceptId", it.conceptId.toString()).put("current", it.current).put("consecutiveCorrect", it.consecutiveCorrect).put("consecutiveWrong", it.consecutiveWrong).put("hasReachedVeryHard", it.hasReachedVeryHard) }))
                root.put("reviewSessions", JSONArray(db.reviewSessionDao().getAll().map { JSONObject().put("id", it.id.toString()).put("startedAt", it.startedAt.toString()).put("endedAt", it.endedAt?.toString()).put("reviewType", it.reviewType) }))
                root.put("reviewHistory", JSONArray(db.reviewHistoryDao().getAll().map { JSONObject().put("id", it.id.toString()).put("sessionId", it.sessionId.toString()).put("reviewAttemptId", it.reviewAttemptId.toString()).put("conceptId", it.conceptId.toString()).put("reviewedAt", it.reviewedAt.toString()).put("isCorrect", it.isCorrect).put("reviewType", it.reviewType) }))
            }
            BackupType.FULL -> return fullBackupRepository.exportFull()
        }
        return root.toString()
    }

    suspend fun restore(type: BackupType, json: String): RestoreResult = try {
        val root = JSONObject(json)
        if (root.optInt("schemaVersion", -1) != 2 || root.optString("backupType") != type.name) {
            RestoreResult(0, 0, listOf("UNSUPPORTED_TYPED_${type.name}_BACKUP"))
        } else when (type) {
            BackupType.VOCABULARY -> restoreVocabulary(root)
            BackupType.PROGRESS -> restoreProgress(root)
            BackupType.FULL -> fullBackupRepository.restoreFull(json)
        }
    } catch (_: Exception) {
        RestoreResult(0, 0, listOf("INVALID_JSON"))
    }

    private suspend fun restoreVocabulary(root: JSONObject): RestoreResult {
        val concepts = root.optJSONArray("concepts") ?: return RestoreResult(0, 0, listOf("MISSING_TYPED_VOCABULARY_SECTION:concepts"))
        val contents = root.optJSONArray("contents") ?: return RestoreResult(0, 0, listOf("MISSING_TYPED_VOCABULARY_SECTION:contents"))
        val categories = root.optJSONArray("categories") ?: return RestoreResult(0, 0, listOf("MISSING_TYPED_VOCABULARY_SECTION:categories"))
        val categoryNames = mutableMapOf<String, String>()
        for (i in 0 until categories.length()) {
            val o = categories.optJSONObject(i) ?: return RestoreResult(0, 0, listOf("INVALID_ENTRY:categories[$i]"))
            val id = o.optString("id")
            val name = o.optString("name").trim()
            if (id.isBlank() || runCatching { UUID.fromString(id) }.isFailure) return RestoreResult(0, 0, listOf("INVALID_UUID:categories"))
            if (name.isBlank()) return RestoreResult(0, 0, listOf("INVALID_VALUE:category_name"))
            categoryNames[id] = name
        }
        val nested = JSONObject().put("schemaVersion", 1).put("backupMode", "VOCABULARY").put("exportedAt", System.currentTimeMillis())
        nested.put("languages", root.optJSONArray("languages") ?: JSONArray())
        nested.put("categories", JSONArray(categoryNames.values.distinct().map { JSONObject().put("name", it) }))
        val contentByConcept = mutableMapOf<String, MutableList<JSONObject>>()
        for (i in 0 until contents.length()) {
            val o = contents.optJSONObject(i) ?: return RestoreResult(0, 0, listOf("INVALID_ENTRY:contents[$i]"))
            val conceptId = o.optString("conceptId")
            if (runCatching { UUID.fromString(conceptId) }.isFailure) return RestoreResult(0, 0, listOf("INVALID_UUID:contents"))
            if (o.optString("languageCode").isBlank() || o.optString("text").isBlank()) return RestoreResult(0, 0, listOf("INVALID_VALUE:contents"))
            contentByConcept.getOrPut(conceptId) { mutableListOf() }.add(JSONObject().put("languageCode", o.optString("languageCode")).put("text", o.optString("text")))
        }
        val nestedConcepts = JSONArray()
        val seenConceptIds = mutableSetOf<String>()
        for (i in 0 until concepts.length()) {
            val o = concepts.optJSONObject(i) ?: return RestoreResult(0, 0, listOf("INVALID_ENTRY:concepts[$i]"))
            val id = o.optString("id")
            if (runCatching { UUID.fromString(id) }.isFailure || !seenConceptIds.add(id)) return RestoreResult(0, 0, listOf("INVALID_UUID:concepts"))
            if (contentByConcept[id].isNullOrEmpty()) return RestoreResult(0, 0, listOf("INVALID_REFERENCE:concept_contents"))
            val categoryId = o.optString("categoryId")
            if (categoryId.isNotBlank() && categoryId !in categoryNames) return RestoreResult(0, 0, listOf("INVALID_REFERENCE:concept_category"))
            nestedConcepts.put(JSONObject()
                .put("uuid", id)
                .put("contentType", o.optString("entryType", "WORD"))
                .put("favorite", o.optBoolean("favorite", false))
                .put("active", o.optBoolean("active", true))
                .put("categoryName", categoryNames[categoryId])
                .put("contents", JSONArray(contentByConcept[id]!!)))
        }
        nested.put("concepts", nestedConcepts)
        return vocabularyBackupRepository.restore(nested.toString())
    }

    private suspend fun restoreProgress(root: JSONObject): RestoreResult {
        val learning = root.optJSONArray("learningStates") ?: return RestoreResult(0, 0, listOf("MISSING_TYPED_PROGRESS_SECTION:learningStates"))
        val difficulty = root.optJSONArray("difficultyStates") ?: return RestoreResult(0, 0, listOf("MISSING_TYPED_PROGRESS_SECTION:difficultyStates"))
        val sessions = root.optJSONArray("reviewSessions") ?: return RestoreResult(0, 0, listOf("MISSING_TYPED_PROGRESS_SECTION:reviewSessions"))
        val history = root.optJSONArray("reviewHistory") ?: return RestoreResult(0, 0, listOf("MISSING_TYPED_PROGRESS_SECTION:reviewHistory"))
        val issues = mutableListOf<String>()
        val conceptIds = db.conceptDao().getAll().map { it.id }.toSet()
        val sessionIds = mutableSetOf<UUID>()
        val attemptKeys = mutableSetOf<String>()
        val learningIds = mutableSetOf<UUID>()
        val difficultyIds = mutableSetOf<UUID>()
        val incomingSessions = mutableListOf<ReviewSessionEntity>()
        val incomingHistory = mutableListOf<ReviewHistoryEntity>()
        for (i in 0 until learning.length()) {
            val o = learning.optJSONObject(i) ?: return RestoreResult(0, 0, listOf("INVALID_ENTRY:learningStates[$i]"))
            val id = parseUuid(o, "id", "learningStates", issues) ?: continue
            val conceptId = parseUuid(o, "conceptId", "learningStates", issues) ?: continue
            if (!learningIds.add(id)) issues += "DUPLICATE_UUID:learningStates"
            if (conceptId !in conceptIds) issues += "INVALID_REFERENCE:learningStates_concept"
            if (o.optString("stage") !in setOf("DAILY", "WEEKLY", "MONTHLY", "LEARNED")) issues += "INVALID_VALUE:stage"
            if (o.optString("nextReviewAt").isNotBlank() && o.optString("nextReviewAt") != "null") runCatching { Instant.parse(o.optString("nextReviewAt")) }.onFailure { issues += "INVALID_VALUE:nextReviewAt" }
            if (o.optString("lastReviewedAt").isNotBlank() && o.optString("lastReviewedAt") != "null") runCatching { Instant.parse(o.optString("lastReviewedAt")) }.onFailure { issues += "INVALID_VALUE:lastReviewedAt" }
        }
        for (i in 0 until difficulty.length()) {
            val o = difficulty.optJSONObject(i) ?: return RestoreResult(0, 0, listOf("INVALID_ENTRY:difficultyStates[$i]"))
            val id = parseUuid(o, "id", "difficultyStates", issues) ?: continue
            val conceptId = parseUuid(o, "conceptId", "difficultyStates", issues) ?: continue
            if (!difficultyIds.add(id)) issues += "DUPLICATE_UUID:difficultyStates"
            if (conceptId !in conceptIds) issues += "INVALID_REFERENCE:difficultyStates_concept"
        }
        for (i in 0 until sessions.length()) {
            val o = sessions.optJSONObject(i) ?: return RestoreResult(0, 0, listOf("INVALID_ENTRY:reviewSessions[$i]"))
            val id = parseUuid(o, "id", "reviewSessions", issues) ?: continue
            if (!sessionIds.add(id)) issues += "DUPLICATE_UUID:reviewSessions"
            val start = runCatching { Instant.parse(o.optString("startedAt")) }.getOrNull()
            val end = if (o.isNull("endedAt")) null else runCatching { Instant.parse(o.optString("endedAt")) }.getOrNull()
            if (start == null || (!o.isNull("endedAt") && end == null) || (start != null && end != null && end.isBefore(start))) issues += "INVALID_VALUE:reviewSession_time"
            if (o.optString("reviewType") !in setOf("DAILY", "WEEKLY", "MONTHLY", "LEARNED", "RANDOM")) issues += "INVALID_VALUE:reviewSession_reviewType"
            if (start != null && (o.isNull("endedAt") || end != null)) incomingSessions += ReviewSessionEntity(id, start, end, o.optString("reviewType"))
        }
        for (i in 0 until history.length()) {
            val o = history.optJSONObject(i) ?: return RestoreResult(0, 0, listOf("INVALID_ENTRY:reviewHistory[$i]"))
            val id = parseUuid(o, "id", "reviewHistory", issues) ?: continue
            val sessionId = parseUuid(o, "sessionId", "reviewHistory", issues) ?: continue
            val attemptId = parseUuid(o, "reviewAttemptId", "reviewHistory", issues) ?: continue
            val conceptId = parseUuid(o, "conceptId", "reviewHistory", issues) ?: continue
            if (sessionId !in sessionIds) issues += "INVALID_REFERENCE:history_session"
            if (conceptId !in conceptIds) issues += "INVALID_REFERENCE:history_concept"
            if (!attemptKeys.add("$sessionId:$attemptId")) issues += "DUPLICATE_ATTEMPT:reviewHistory"
            if (o.optString("reviewType") !in setOf("DAILY", "WEEKLY", "MONTHLY", "LEARNED", "RANDOM")) issues += "INVALID_VALUE:history_reviewType"
            val reviewedAt = runCatching { Instant.parse(o.optString("reviewedAt")) }.getOrNull()
            if (reviewedAt == null) issues += "INVALID_VALUE:reviewHistory_reviewedAt"
            if (reviewedAt != null) incomingHistory += ReviewHistoryEntity(id, sessionId, attemptId, conceptId, reviewedAt, o.optBoolean("isCorrect"), o.optString("reviewType"))
        }
        if (issues.isNotEmpty()) return RestoreResult(0, 0, issues.distinct())
        return db.withTransaction {
            var added = 0
            var merged = 0
            for (i in 0 until learning.length()) {
                val o = learning.getJSONObject(i); val conceptId = UUID.fromString(o.getString("conceptId")); val old = db.learningStateDao().getByConceptId(conceptId)
                val entity = LearningStateEntity(UUID.fromString(o.getString("id")), conceptId, o.getString("stage"), o.optString("nextReviewAt").takeIf { it.isNotBlank() && it != "null" }?.let(Instant::parse), o.optInt("monthlyWrongCount"), o.optBoolean("hasPathFailure"), o.optInt("totalCorrect"), o.optInt("totalWrong"), o.optString("lastReviewedAt").takeIf { it.isNotBlank() && it != "null" }?.let(Instant::parse))
                db.learningStateDao().upsert(if (old == null) entity else entity.copy(id = old.id)); if (old == null) added++ else merged++
            }
            for (i in 0 until difficulty.length()) {
                val o = difficulty.getJSONObject(i); val conceptId = UUID.fromString(o.getString("conceptId")); val old = db.difficultyStateDao().getByConceptId(conceptId)
                val entity = DifficultyStateEntity(UUID.fromString(o.getString("id")), conceptId, o.getString("current"), o.optInt("consecutiveCorrect"), o.optInt("consecutiveWrong"), o.optBoolean("hasReachedVeryHard"))
                db.difficultyStateDao().upsert(if (old == null) entity else entity.copy(id = old.id)); if (old == null) added++ else merged++
            }
            incomingSessions.forEach { if (db.reviewSessionDao().getById(it.id) == null) { db.reviewSessionDao().insert(it); added++ } else { db.reviewSessionDao().update(it); merged++ } }
            incomingHistory.forEach { if (!db.reviewHistoryDao().existsByAttemptId(it.sessionId, it.reviewAttemptId)) { db.reviewHistoryDao().insert(it); added++ } else merged++ }
            RestoreResult(added, merged, emptyList())
        }
    }

    private fun parseUuid(o: JSONObject, key: String, section: String, issues: MutableList<String>): UUID? =
        runCatching { UUID.fromString(o.optString(key)) }.getOrElse { issues += "INVALID_UUID:$section"; null }
}
