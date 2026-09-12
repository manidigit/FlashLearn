package com.flashlearn.data.backup

import com.flashlearn.database.*
import androidx.room.withTransaction
import com.flashlearn.domain.repository.*
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomBackupRepository @Inject constructor(
    private val db: RoomFlashLearnDatabase
) : BackupRepository {

    override suspend fun exportFull(): String = db.withTransaction {
        val root = JSONObject()
        root.put("schemaVersion", 1)
        root.put("exportedAt", Instant.now().toString())
        root.put("backupType", "FULL")
        root.put("concepts", JSONArray(db.conceptDao().getAll().map { JSONObject()
            .put("id", it.id.toString()).put("entryType", it.entryType).put("categoryId", it.categoryId?.toString())
            .put("favorite", it.favorite).put("active", it.active).put("createdAt", it.createdAt.toString()).put("updatedAt", it.updatedAt.toString()) }))
        root.put("contents", JSONArray(db.contentDao().getAll().map { JSONObject()
            .put("id", it.id.toString()).put("conceptId", it.conceptId.toString()).put("languageCode", it.languageCode)
            .put("text", it.text).put("canonicalKey", it.canonicalKey).put("notes", it.notes)
            .put("pronunciation", it.pronunciation).put("example", it.example) }))
        root.put("learningStates", JSONArray(db.learningStateDao().getAll().map { JSONObject()
            .put("id", it.id.toString()).put("conceptId", it.conceptId.toString()).put("stage", it.stage)
            .put("nextReviewAt", it.nextReviewAt?.toString()).put("monthlyWrongCount", it.monthlyWrongCount)
            .put("hasPathFailure", it.hasPathFailure).put("totalCorrect", it.totalCorrect).put("totalWrong", it.totalWrong)
            .put("lastReviewedAt", it.lastReviewedAt?.toString()) }))
        root.put("difficultyStates", JSONArray(db.difficultyStateDao().getAll().map { JSONObject()
            .put("id", it.id.toString()).put("conceptId", it.conceptId.toString()).put("current", it.current)
            .put("consecutiveCorrect", it.consecutiveCorrect).put("consecutiveWrong", it.consecutiveWrong)
            .put("hasReachedVeryHard", it.hasReachedVeryHard) }))
        root.put("tags", JSONArray(db.tagDao().getAll().map { JSONObject().put("id", it.id.toString()).put("name", it.name) }))
        root.put("conceptTags", JSONArray(db.conceptTagDao().getAll().map { JSONObject()
            .put("conceptId", it.conceptId.toString()).put("tagId", it.tagId.toString()) }))
        root.put("reviewSessions", JSONArray(db.reviewSessionDao().getAll().map { JSONObject()
            .put("id", it.id.toString()).put("startedAt", it.startedAt.toString()).put("endedAt", it.endedAt?.toString())
            .put("reviewType", it.reviewType) }))
        root.put("reviewHistory", JSONArray(db.reviewHistoryDao().getAll().map { JSONObject()
            .put("id", it.id.toString()).put("sessionId", it.sessionId.toString()).put("reviewAttemptId", it.reviewAttemptId.toString())
            .put("conceptId", it.conceptId.toString()).put("reviewedAt", it.reviewedAt.toString())
            .put("isCorrect", it.isCorrect).put("reviewType", it.reviewType) }))
        root.put("settings", JSONArray(db.settingsDao().getAll().map { JSONObject()
            .put("key", it.key).put("value", it.value).put("updatedAt", it.updatedAt.toString()) }))
        root.put("categories", JSONArray(db.categoryDao().getAll().map { JSONObject().put("id", it.id.toString()).put("name", it.name) }))
        root.toString()
    }

    override suspend fun restoreFull(json: String): RestoreResult {
        val root = try {
            JSONObject(json)
        } catch (_: Exception) {
            return RestoreResult(0, 0, listOf("INVALID_JSON"))
        }
        if (root.optInt("schemaVersion", -1) != 1 || root.optString("backupType") != "FULL") {
            return RestoreResult(0, 0, listOf("UNSUPPORTED_BACKUP"))
        }

        fun uuid(o: JSONObject, k: String) = UUID.fromString(o.getString(k))
        fun instant(o: JSONObject, k: String): Instant? =
            o.optString(k).takeIf { it.isNotBlank() && it != "null" }?.let(Instant::parse)

        return try {
            db.withTransaction {
                // Parse the complete payload before mutating the database. Any parsing or
                // persistence failure escapes the transaction so Room rolls back all writes.
                val concepts = root.arr("concepts").map {
                    ConceptEntity(uuid(it, "id"), it.getString("entryType"),
                        it.optString("categoryId").takeIf { v -> v.isNotBlank() && v != "null" }?.let(UUID::fromString),
                        it.getBoolean("favorite"), it.getBoolean("active"),
                        Instant.parse(it.getString("createdAt")), Instant.parse(it.getString("updatedAt")))
                }
                val contents = root.arr("contents").map {
                    ContentEntity(uuid(it, "id"), uuid(it, "conceptId"), it.getString("languageCode"),
                        it.getString("text"), it.getString("canonicalKey"),
                        it.optString("notes").takeIf { v -> v != "null" },
                        it.optString("pronunciation").takeIf { v -> v != "null" },
                        it.optString("example").takeIf { v -> v != "null" })
                }
                val learning = root.arr("learningStates").map {
                    LearningStateEntity(uuid(it, "id"), uuid(it, "conceptId"), it.getString("stage"),
                        instant(it, "nextReviewAt"), it.getInt("monthlyWrongCount"), it.getBoolean("hasPathFailure"),
                        it.getInt("totalCorrect"), it.getInt("totalWrong"), instant(it, "lastReviewedAt"))
                }
                val difficulty = root.arr("difficultyStates").map {
                    DifficultyStateEntity(uuid(it, "id"), uuid(it, "conceptId"), it.getString("current"),
                        it.getInt("consecutiveCorrect"), it.getInt("consecutiveWrong"), it.getBoolean("hasReachedVeryHard"))
                }
                val tags = root.arr("tags").map { TagEntity(uuid(it, "id"), it.getString("name")) }
                val ct = root.arr("conceptTags").map { ConceptTagEntity(uuid(it, "conceptId"), uuid(it, "tagId")) }
                val sessions = root.arr("reviewSessions").map {
                    ReviewSessionEntity(uuid(it, "id"), Instant.parse(it.getString("startedAt")),
                        instant(it, "endedAt"), it.getString("reviewType"))
                }
                val history = root.arr("reviewHistory").map {
                    ReviewHistoryEntity(uuid(it, "id"), uuid(it, "sessionId"), uuid(it, "reviewAttemptId"),
                        uuid(it, "conceptId"), Instant.parse(it.getString("reviewedAt")),
                        it.getBoolean("isCorrect"), it.getString("reviewType"))
                }
                val settings = root.arr("settings").map {
                    SettingsEntity(it.getString("key"), it.getString("value"), Instant.parse(it.getString("updatedAt")))
                }
                val categories = root.arr("categories").map { CategoryEntity(uuid(it, "id"), it.getString("name")) }

                fun <T> requireUniqueIds(items: List<T>, id: (T) -> UUID, label: String) {
                    require(items.groupingBy(id).eachCount().values.all { it == 1 }) { "DUPLICATE_UUID:$label" }
                }
                requireUniqueIds(concepts, { it.id }, "concepts")
                requireUniqueIds(contents, { it.id }, "contents")
                requireUniqueIds(learning, { it.id }, "learningStates")
                requireUniqueIds(difficulty, { it.id }, "difficultyStates")
                requireUniqueIds(tags, { it.id }, "tags")
                requireUniqueIds(sessions, { it.id }, "reviewSessions")
                requireUniqueIds(history, { it.id }, "reviewHistory")
                requireUniqueIds(categories, { it.id }, "categories")
                require(history.groupingBy { it.sessionId to it.reviewAttemptId }.eachCount().values.all { it == 1 }) { "DUPLICATE_ATTEMPT:reviewHistory" }
                require(settings.all { it.key.isNotBlank() }) { "INVALID_VALUE:blank_setting_key" }

                val conceptIds = concepts.map { it.id }.toSet()
                val categoryIds = categories.map { it.id }.toSet()
                val tagIds = tags.map { it.id }.toSet()
                val sessionIds = sessions.map { it.id }.toSet()
                require(concepts.all { it.categoryId == null || it.categoryId in categoryIds }) { "INVALID_REFERENCE:category" }
                require(contents.all { it.conceptId in conceptIds }) { "INVALID_REFERENCE:content_concept" }
                require(learning.all { it.conceptId in conceptIds }) { "INVALID_REFERENCE:learning_concept" }
                require(difficulty.all { it.conceptId in conceptIds }) { "INVALID_REFERENCE:difficulty_concept" }
                require(ct.all { it.conceptId in conceptIds && it.tagId in tagIds }) { "INVALID_REFERENCE:concept_tag" }
                require(history.all { it.conceptId in conceptIds && it.sessionId in sessionIds }) { "INVALID_REFERENCE:history" }
                require(settings.map { it.key }.distinct().size == settings.size) { "DUPLICATE_KEY:settings" }
                require(ct.distinctBy { it.conceptId to it.tagId }.size == ct.size) { "DUPLICATE_RELATION:concept_tag" }
                require(contents.groupingBy { it.conceptId to it.languageCode }.eachCount().values.all { it == 1 }) { "DUPLICATE_CONTENT_LANGUAGE" }

                // A FULL restore replaces the local snapshot. This keeps restore semantics
                // deterministic and avoids primary/unique-key conflicts when restoring into
                // a non-empty database. All deletes and inserts are in the same transaction.
                db.conceptTagDao().deleteAll()
                db.reviewHistoryDao().deleteAll()
                db.reviewSessionDao().deleteAll()
                db.learningStateDao().deleteAll()
                db.difficultyStateDao().deleteAll()
                db.contentDao().deleteAll()
                db.conceptDao().deleteAll()
                db.tagDao().deleteAll()
                db.categoryDao().deleteAll()
                db.settingsDao().deleteAll()

                var imported = 0
                db.categoryDao().insertAll(categories); imported += categories.size
                db.tagDao().insertAll(tags); imported += tags.size
                db.conceptDao().insertAll(concepts); imported += concepts.size
                db.contentDao().insertAll(contents); imported += contents.size
                db.learningStateDao().upsertAll(learning); imported += learning.size
                db.difficultyStateDao().upsertAll(difficulty); imported += difficulty.size
                db.conceptTagDao().insertAll(ct); imported += ct.size
                db.reviewSessionDao().insertAll(sessions); imported += sessions.size
                db.reviewHistoryDao().insertAll(history); imported += history.size
                db.settingsDao().putAll(settings); imported += settings.size
                RestoreResult(imported, 0, emptyList())
            }
        } catch (e: Exception) {
            RestoreResult(0, 0, listOf("RESTORE_FAILED:${e.message ?: "unknown"}"))
        }
    }

    private fun JSONObject.arr(key: String): List<JSONObject> {
        val a = optJSONArray(key) ?: JSONArray()
        return (0 until a.length()).map { a.getJSONObject(it) }
    }
}
