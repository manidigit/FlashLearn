package com.flashlearn.data.backup

import android.content.Context
import androidx.room.withTransaction
import com.flashlearn.database.*
import com.flashlearn.domain.model.EntryType
import com.flashlearn.domain.model.ReviewType
import com.flashlearn.domain.model.Stage
import com.flashlearn.domain.model.VocabularyDifficulty
import com.flashlearn.domain.repository.BackupRepository
import com.flashlearn.domain.repository.RestoreResult
import com.flashlearn.domain.usecase.computeCanonicalKey
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.util.UUID
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption.ATOMIC_MOVE
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomBackupRepository @Inject constructor(
    private val db: RoomFlashLearnDatabase,
    @ApplicationContext private val context: Context
) : BackupRepository {

    companion object {
        private const val BACKUP_SCHEMA_VERSION = 1
        private const val PRE_RESTORE_BACKUP_FILE = "flashlearn-pre-restore-backup.json"
        private const val PRE_RESTORE_BACKUP_TMP_FILE = "flashlearn-pre-restore-backup.json.tmp"
        private val REQUIRED_ARRAYS = listOf(
            "concepts", "contents", "learningStates", "difficultyStates", "tags", "conceptTags",
            "reviewSessions", "reviewHistory", "settings", "categories", "achievements", "parserMetadata"
        )
    }

    override suspend fun exportFull(): String = db.withTransaction {
        JSONObject().apply {
            put("schemaVersion", BACKUP_SCHEMA_VERSION)
            put("exportedAt", Instant.now().toString())
            put("backupType", "FULL")
            put("concepts", JSONArray(db.conceptDao().getAll().map { JSONObject()
                .put("id", it.id.toString()).put("entryType", it.entryType).put("categoryId", it.categoryId?.toString())
                .put("favorite", it.favorite).put("active", it.active).put("createdAt", it.createdAt.toString()).put("updatedAt", it.updatedAt.toString()) }))
            put("contents", JSONArray(db.contentDao().getAll().map { JSONObject()
                .put("id", it.id.toString()).put("conceptId", it.conceptId.toString()).put("languageCode", it.languageCode)
                .put("text", it.text).put("canonicalKey", it.canonicalKey).put("notes", it.notes)
                .put("pronunciation", it.pronunciation).put("example", it.example) }))
            put("learningStates", JSONArray(db.learningStateDao().getAll().map { JSONObject()
                .put("id", it.id.toString()).put("conceptId", it.conceptId.toString()).put("stage", it.stage)
                .put("nextReviewAt", it.nextReviewAt?.toString()).put("monthlyWrongCount", it.monthlyWrongCount)
                .put("hasPathFailure", it.hasPathFailure).put("totalCorrect", it.totalCorrect).put("totalWrong", it.totalWrong)
                .put("lastReviewedAt", it.lastReviewedAt?.toString()) }))
            put("difficultyStates", JSONArray(db.difficultyStateDao().getAll().map { JSONObject()
                .put("id", it.id.toString()).put("conceptId", it.conceptId.toString()).put("current", it.current)
                .put("consecutiveCorrect", it.consecutiveCorrect).put("consecutiveWrong", it.consecutiveWrong)
                .put("hasReachedVeryHard", it.hasReachedVeryHard) }))
            put("tags", JSONArray(db.tagDao().getAll().map { JSONObject().put("id", it.id.toString()).put("name", it.name) }))
            put("conceptTags", JSONArray(db.conceptTagDao().getAll().map { JSONObject()
                .put("conceptId", it.conceptId.toString()).put("tagId", it.tagId.toString()) }))
            put("reviewSessions", JSONArray(db.reviewSessionDao().getAll().map { JSONObject()
                .put("id", it.id.toString()).put("startedAt", it.startedAt.toString()).put("endedAt", it.endedAt?.toString())
                .put("reviewType", it.reviewType) }))
            put("reviewHistory", JSONArray(db.reviewHistoryDao().getAll().map { JSONObject()
                .put("id", it.id.toString()).put("sessionId", it.sessionId.toString()).put("reviewAttemptId", it.reviewAttemptId.toString())
                .put("conceptId", it.conceptId.toString()).put("reviewedAt", it.reviewedAt.toString())
                .put("isCorrect", it.isCorrect).put("reviewType", it.reviewType) }))
            put("settings", JSONArray(db.settingsDao().getAll().map { JSONObject()
                .put("key", it.key).put("value", it.value).put("updatedAt", it.updatedAt.toString()) }))
            put("categories", JSONArray(db.categoryDao().getAll().map { JSONObject().put("id", it.id.toString()).put("name", it.name) }))
            put("achievements", JSONArray(db.achievementDao().getAll().map { JSONObject().put("achievementId", it.achievementId).put("unlocked", it.unlocked) }))
            put("parserMetadata", JSONArray(db.parserMetadataDao().getAll().map { JSONObject()
                .put("conceptId", it.conceptId.toString()).put("breakdownJson", it.breakdownJson)
                .put("relationshipsJson", it.relationshipsJson).put("variantsJson", it.variantsJson)
                .put("confidence", it.confidence) }))
        }.toString()
    }

    override suspend fun restoreFull(json: String): RestoreResult {
        val root = try { JSONObject(json) } catch (_: Exception) {
            return RestoreResult(0, 0, listOf("INVALID_JSON"))
        }
        if (root.optInt("schemaVersion", -1) != BACKUP_SCHEMA_VERSION || root.optString("backupType") != "FULL") {
            return RestoreResult(0, 0, listOf("UNSUPPORTED_BACKUP"))
        }
        if (root.optString("exportedAt").isBlank()) return RestoreResult(0, 0, listOf("INVALID_VALUE:exportedAt"))
        try { Instant.parse(root.getString("exportedAt")) } catch (_: Exception) {
            return RestoreResult(0, 0, listOf("INVALID_VALUE:exportedAt"))
        }
        val missing = REQUIRED_ARRAYS.filterNot(root::has)
        if (missing.isNotEmpty()) return RestoreResult(0, 0, listOf("MISSING_SECTION:${missing.joinToString(",")}"))

        fun uuid(o: JSONObject, k: String): UUID = UUID.fromString(o.getString(k))
        fun instant(o: JSONObject, k: String): Instant? =
            if (!o.has(k) || o.isNull(k)) null else Instant.parse(o.getString(k))
        fun requiredText(o: JSONObject, k: String): String {
            require(o.has(k) && !o.isNull(k)) { "INVALID_VALUE:$k" }
            return o.getString(k).also { require(it.isNotBlank()) { "INVALID_VALUE:$k" } }
        }
        fun <T> unique(items: List<T>, key: (T) -> Any, code: String) {
            require(items.groupingBy(key).eachCount().values.all { it == 1 }) { code }
        }

        return try {
            // The v4.20 restore contract requires an automatic snapshot of the current
            // device state immediately before any restore mutation. Write it atomically
            // to app-private storage; if this fails, do not touch the database.
            val preRestoreBackup = exportFull()
            val backupFile = File(context.filesDir, PRE_RESTORE_BACKUP_FILE)
            val tempFile = File(context.filesDir, PRE_RESTORE_BACKUP_TMP_FILE)
            tempFile.writeText(preRestoreBackup, Charsets.UTF_8)
            try {
                Files.move(tempFile.toPath(), backupFile.toPath(), ATOMIC_MOVE, REPLACE_EXISTING)
            } catch (_: Exception) {
                tempFile.delete()
                throw IllegalStateException("PRE_RESTORE_BACKUP_WRITE_FAILED")
            }

            val parsed = db.withTransaction {
                val concepts = root.arr("concepts").map {
                    ConceptEntity(uuid(it, "id"), requiredText(it, "entryType").also { v -> require(v in EntryType.entries.map { it.name }) { "INVALID_VALUE:entryType" } },
                        if (it.has("categoryId") && !it.isNull("categoryId")) uuid(it, "categoryId") else null,
                        it.getBoolean("favorite"), it.getBoolean("active"), Instant.parse(it.getString("createdAt")), Instant.parse(it.getString("updatedAt")))
                }
                val contents = root.arr("contents").map {
                    val text = requiredText(it, "text")
                    val languageCode = requiredText(it, "languageCode")
                    requiredText(it, "canonicalKey")
                    ContentEntity(uuid(it, "id"), uuid(it, "conceptId"), languageCode, text,
                        computeCanonicalKey(text), it.optString("notes").takeIf { v -> v.isNotBlank() && v != "null" },
                        it.optString("pronunciation").takeIf { v -> v.isNotBlank() && v != "null" }, it.optString("example").takeIf { v -> v.isNotBlank() && v != "null" })
                }
                val learning = root.arr("learningStates").map {
                    val stage = requiredText(it, "stage")
                    require(stage in Stage.entries.map { it.name }) { "INVALID_VALUE:stage" }
                    LearningStateEntity(uuid(it, "id"), uuid(it, "conceptId"), stage, instant(it, "nextReviewAt"),
                        it.getInt("monthlyWrongCount"), it.getBoolean("hasPathFailure"), it.getInt("totalCorrect"), it.getInt("totalWrong"), instant(it, "lastReviewedAt"))
                }
                val difficulty = root.arr("difficultyStates").map {
                    val current = requiredText(it, "current")
                    require(current in VocabularyDifficulty.entries.map { it.name }) { "INVALID_VALUE:difficulty" }
                    val correct = it.getInt("consecutiveCorrect")
                    val wrong = it.getInt("consecutiveWrong")
                    require(correct >= 0 && wrong >= 0 && !(correct > 0 && wrong > 0)) { "INVALID_VALUE:difficulty_streak" }
                    DifficultyStateEntity(uuid(it, "id"), uuid(it, "conceptId"), current, correct, wrong, it.getBoolean("hasReachedVeryHard"))
                }
                val tags = root.arr("tags").map { TagEntity(uuid(it, "id"), requiredText(it, "name")) }
                val ct = root.arr("conceptTags").map { ConceptTagEntity(uuid(it, "conceptId"), uuid(it, "tagId")) }
                val sessions = root.arr("reviewSessions").map {
                    val type = requiredText(it, "reviewType")
                    require(type in ReviewType.entries.map { it.name }) { "INVALID_VALUE:reviewType" }
                    val started = Instant.parse(it.getString("startedAt"))
                    val ended = instant(it, "endedAt")
                    require(ended == null || ended >= started) { "INVALID_VALUE:reviewSession_time" }
                    ReviewSessionEntity(uuid(it, "id"), started, ended, type)
                }
                val history = root.arr("reviewHistory").map {
                    val type = requiredText(it, "reviewType")
                    require(type in ReviewType.entries.map { it.name }) { "INVALID_VALUE:history_reviewType" }
                    ReviewHistoryEntity(uuid(it, "id"), uuid(it, "sessionId"), uuid(it, "reviewAttemptId"), uuid(it, "conceptId"),
                        Instant.parse(it.getString("reviewedAt")), it.getBoolean("isCorrect"), type)
                }
                val settings = root.arr("settings").map {
                    SettingsEntity(requiredText(it, "key"), it.getString("value"), Instant.parse(it.getString("updatedAt")))
                }
                val categories = root.arr("categories").map { CategoryEntity(uuid(it, "id"), requiredText(it, "name")) }
                val achievements = root.arr("achievements").map { AchievementEntity(requiredText(it, "achievementId"), it.getBoolean("unlocked")) }
                require(categories.all { it.name.trim().isNotEmpty() }) { "INVALID_VALUE:category_name" }
                val parserMetadata = root.arr("parserMetadata").map {
                    val confidence = it.getDouble("confidence")
                    require(confidence.isFinite() && confidence in 0.0..1.0) { "INVALID_VALUE:parserMetadata_confidence" }
                    ParserMetadataEntity(uuid(it, "conceptId"), it.getString("breakdownJson"), it.getString("relationshipsJson"), it.getString("variantsJson"), confidence)
                }

                unique(concepts, { it.id }, "DUPLICATE_UUID:concepts")
                unique(contents, { it.id }, "DUPLICATE_UUID:contents")
                unique(learning, { it.id }, "DUPLICATE_UUID:learningStates")
                unique(difficulty, { it.id }, "DUPLICATE_UUID:difficultyStates")
                unique(tags, { it.id }, "DUPLICATE_UUID:tags")
                unique(sessions, { it.id }, "DUPLICATE_UUID:reviewSessions")
                unique(history, { it.id }, "DUPLICATE_UUID:reviewHistory")
                unique(categories, { it.id }, "DUPLICATE_UUID:categories")
                unique(parserMetadata, { it.conceptId }, "DUPLICATE_UUID:parserMetadata")
                unique(settings, { it.key }, "DUPLICATE_KEY:settings")
                unique(achievements, { it.achievementId }, "DUPLICATE_KEY:achievements")
                unique(ct, { it.conceptId to it.tagId }, "DUPLICATE_RELATION:concept_tag")
                unique(contents, { it.conceptId to it.languageCode }, "DUPLICATE_CONTENT_LANGUAGE")
                unique(history, { it.sessionId to it.reviewAttemptId }, "DUPLICATE_ATTEMPT:reviewHistory")

                val conceptIds = concepts.map { it.id }.toSet()
                val categoryIds = categories.map { it.id }.toSet()
                val tagIds = tags.map { it.id }.toSet()
                val sessionById = sessions.associateBy { it.id }
                require(concepts.all { it.categoryId == null || it.categoryId in categoryIds }) { "INVALID_REFERENCE:category" }
                require(contents.all { it.conceptId in conceptIds }) { "INVALID_REFERENCE:content_concept" }
                require(learning.all { it.conceptId in conceptIds }) { "INVALID_REFERENCE:learning_concept" }
                require(difficulty.all { it.conceptId in conceptIds }) { "INVALID_REFERENCE:difficulty_concept" }
                require(parserMetadata.all { it.conceptId in conceptIds }) { "INVALID_REFERENCE:parserMetadata_concept" }
                require(ct.all { it.conceptId in conceptIds && it.tagId in tagIds }) { "INVALID_REFERENCE:concept_tag" }
                require(history.all { it.conceptId in conceptIds && it.sessionId in sessionById.keys }) { "INVALID_REFERENCE:history" }
                require(history.all { it.reviewType == sessionById[it.sessionId]!!.reviewType }) { "INVALID_VALUE:history_session_reviewType" }
                require(learning.all { it.monthlyWrongCount >= 0 && it.totalCorrect >= 0 && it.totalWrong >= 0 }) { "INVALID_VALUE:learning_counts" }
                require(concepts.all { it.updatedAt >= it.createdAt }) { "INVALID_VALUE:concept_time" }
                require(history.all {
                    val session = sessionById[it.sessionId]!!
                    it.reviewedAt >= session.startedAt && (session.endedAt == null || it.reviewedAt <= session.endedAt)
                }) { "INVALID_VALUE:history_time" }
                require(concepts.all { concept -> learning.count { it.conceptId == concept.id } == 1 }) { "INVALID_REFERENCE:learning_state_missing_or_duplicate" }
                require(concepts.all { concept -> difficulty.count { it.conceptId == concept.id } == 1 }) { "INVALID_REFERENCE:difficulty_state_missing_or_duplicate" }
                require(categories.map { it.name.trim().lowercase(java.util.Locale.ROOT) }.distinct().size == categories.size) { "DUPLICATE_KEY:categories" }

                var newCount = 0
                var mergedCount = 0
                suspend fun restoreConcept(entity: ConceptEntity) {
                    val existing = db.conceptDao().getByIdIncludingInactive(entity.id)
                    if (existing == null) { db.conceptDao().insert(entity); newCount++ }
                    else { db.conceptDao().update(entity); mergedCount++ }
                }
                suspend fun restoreCategory(entity: CategoryEntity) {
                    val existing = db.categoryDao().getById(entity.id)
                    if (existing == null) { db.categoryDao().insert(entity); newCount++ }
                    else { db.categoryDao().update(entity); mergedCount++ }
                }
                suspend fun restoreTag(entity: TagEntity) {
                    val existing = db.tagDao().getById(entity.id)
                    if (existing == null) { db.tagDao().insert(entity); newCount++ }
                    else { db.tagDao().update(entity); mergedCount++ }
                }

                // UUID is the stable restore identity. No existing destination row is deleted.
                categories.forEach { restoreCategory(it) }
                tags.forEach { restoreTag(it) }
                concepts.forEach { restoreConcept(it) }

                contents.forEach { incoming ->
                    val existingByUuid = db.contentDao().getById(incoming.id)
                    if (existingByUuid != null) {
                        db.contentDao().update(incoming); mergedCount++
                    } else {
                        val existingByConceptLanguage = db.contentDao().getByConceptIdAndLanguage(incoming.conceptId, incoming.languageCode)
                        when {
                            existingByConceptLanguage == null -> { db.contentDao().insert(incoming); newCount++ }
                            existingByConceptLanguage.text == incoming.text -> Unit
                            else -> { db.contentDao().update(incoming.copy(id = existingByConceptLanguage.id)); mergedCount++ }
                        }
                    }
                }

                learning.forEach { incoming ->
                    val existing = db.learningStateDao().getByConceptId(incoming.conceptId)
                    if (existing == null) { db.learningStateDao().upsert(incoming); newCount++ }
                    else { db.learningStateDao().upsert(incoming.copy(id = existing.id)); mergedCount++ }
                }
                difficulty.forEach { incoming ->
                    val existing = db.difficultyStateDao().getByConceptId(incoming.conceptId)
                    if (existing == null) { db.difficultyStateDao().upsert(incoming); newCount++ }
                    else { db.difficultyStateDao().upsert(incoming.copy(id = existing.id)); mergedCount++ }
                }
                ct.forEach { incoming ->
                    val existing = db.conceptTagDao().getAll().any { it.conceptId == incoming.conceptId && it.tagId == incoming.tagId }
                    if (!existing) { db.conceptTagDao().insert(incoming); newCount++ }
                }
                sessions.forEach { incoming ->
                    val existing = db.reviewSessionDao().getById(incoming.id)
                    if (existing == null) { db.reviewSessionDao().insert(incoming); newCount++ }
                    else { db.reviewSessionDao().update(incoming); mergedCount++ }
                }
                history.forEach { incoming ->
                    val existing = db.reviewHistoryDao().getAll().firstOrNull { it.id == incoming.id }
                    if (existing == null) { db.reviewHistoryDao().insert(incoming); newCount++ }
                    else { db.reviewHistoryDao().update(incoming); mergedCount++ }
                }
                settings.forEach { incoming ->
                    val existing = db.settingsDao().getByKey(incoming.key)
                    if (existing == null) { db.settingsDao().put(incoming); newCount++ }
                    else { db.settingsDao().put(incoming); mergedCount++ }
                }
                achievements.forEach { incoming ->
                    val existing = db.achievementDao().getAll().any { it.achievementId == incoming.achievementId }
                    db.achievementDao().upsert(incoming)
                    if (existing) mergedCount++ else newCount++
                }
                parserMetadata.forEach { incoming ->
                    val existing = db.parserMetadataDao().getByConceptId(incoming.conceptId)
                    db.parserMetadataDao().upsert(incoming)
                    if (existing != null) mergedCount++ else newCount++
                }

                require(db.conceptDao().getAll().count { it.id in conceptIds } == concepts.size) { "RESTORE_INTEGRITY:concept_count" }
                require(db.contentDao().getAll().count { it.conceptId in conceptIds } >= contents.size) { "RESTORE_INTEGRITY:content_count" }
                require(db.learningStateDao().getAll().count { it.conceptId in conceptIds } == learning.size) { "RESTORE_INTEGRITY:learning_count" }
                require(db.difficultyStateDao().getAll().count { it.conceptId in conceptIds } == difficulty.size) { "RESTORE_INTEGRITY:difficulty_count" }
                RestoreResult(newCount, mergedCount, emptyList())
            }
            parsed
        } catch (e: Exception) {
            RestoreResult(0, 0, listOf(e.message?.takeIf { it.isNotBlank() } ?: "RESTORE_FAILED:unknown"))
        }
    }

    private fun JSONObject.arr(key: String): List<JSONObject> {
        val array = getJSONArray(key)
        return (0 until array.length()).map { array.getJSONObject(it) }
    }
}
