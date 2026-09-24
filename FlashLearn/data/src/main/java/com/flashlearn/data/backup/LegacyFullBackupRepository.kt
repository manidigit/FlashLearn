package com.flashlearn.data.backup

import androidx.room.withTransaction
import com.flashlearn.database.CategoryEntity
import com.flashlearn.database.ContentEntity
import com.flashlearn.database.ConceptEntity
import com.flashlearn.database.DifficultyStateEntity
import com.flashlearn.database.LearningStateEntity
import com.flashlearn.database.ReviewHistoryEntity
import com.flashlearn.database.ReviewSessionEntity
import com.flashlearn.database.RoomFlashLearnDatabase
import com.flashlearn.database.SettingsEntity
import com.flashlearn.domain.model.EntryType
import com.flashlearn.domain.model.ReviewType
import com.flashlearn.domain.model.Stage
import com.flashlearn.domain.model.VocabularyDifficulty
import com.flashlearn.domain.repository.RestoreResult
import com.flashlearn.domain.usecase.computeCanonicalKey
import org.json.JSONObject
import java.time.Duration
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/** Restores the legacy FULL backup format exported by pre-v5.69 FlashLearn builds. */
@Singleton
class LegacyFullBackupRepository @Inject constructor(
    private val db: RoomFlashLearnDatabase
) {
    companion object {
        private const val SCHEMA_VERSION = 1
        private const val BACKUP_MODE = "FULL"
        private const val NAMESPACE = "flashlearn-legacy-full-v1"

        private fun stableId(vararg parts: String): UUID =
            UUID.nameUUIDFromBytes((NAMESPACE + "|" + parts.joinToString("|")).toByteArray(Charsets.UTF_8))

        private fun epochInstant(value: Long?, fallback: Instant): Instant =
            value?.let { Instant.ofEpochMilli(it) } ?: fallback

        private fun intervalFor(stage: String): Duration = when (stage) {
            Stage.DAILY.name -> Duration.ofDays(1)
            Stage.WEEKLY.name -> Duration.ofDays(7)
            Stage.MONTHLY.name -> Duration.ofDays(30)
            else -> Duration.ZERO
        }
    }

    suspend fun restore(json: String): RestoreResult {
        val root = try { JSONObject(json) } catch (_: Exception) {
            return RestoreResult(0, 0, listOf("INVALID_JSON"))
        }
        if (root.optInt("schemaVersion", -1) != SCHEMA_VERSION || root.optString("backupMode") != BACKUP_MODE) {
            return RestoreResult(0, 0, listOf("UNSUPPORTED_LEGACY_FULL_BACKUP"))
        }
        if (!root.has("concepts") || !root.has("learningStates") || !root.has("reviewHistory")) {
            return RestoreResult(0, 0, listOf("MISSING_LEGACY_FULL_SECTION"))
        }

        return try {
            val exportedAt = epochInstant(root.optLong("exportedAt").takeIf { root.has("exportedAt") }, Instant.now())
            val conceptsJson = root.getJSONArray("concepts")
            val statesJson = root.getJSONArray("learningStates")
            val historyJson = root.getJSONArray("reviewHistory")

            data class IncomingConcept(
                val entity: ConceptEntity,
                val contents: List<ContentEntity>,
                val categoryName: String?
            )
            data class LegacyState(
                val conceptId: UUID,
                val stage: String,
                val difficulty: String,
                val monthlyWrongCount: Int,
                val totalCorrect: Int,
                val totalWrong: Int,
                val everFailed: Boolean,
                val consecutiveCorrect: Int,
                val consecutiveIncorrect: Int,
                val difficultyScore: Int
            )

            val now = Instant.now()
            val incoming = ArrayList<IncomingConcept>(conceptsJson.length())
            val seenConcepts = HashSet<UUID>(conceptsJson.length())
            val validEntryTypes = EntryType.entries.map { it.name }.toSet()
            val languages = root.optJSONArray("languages")
            val validLanguages = if (languages == null) emptySet() else buildSet {
                for (i in 0 until languages.length()) add(languages.getJSONObject(i).optString("code").trim())
            }

            for (i in 0 until conceptsJson.length()) {
                val item = conceptsJson.getJSONObject(i)
                val id = UUID.fromString(item.getString("uuid"))
                require(seenConcepts.add(id)) { "DUPLICATE_UUID:concepts" }
                val entryType = item.optString("contentType", EntryType.WORD.name).uppercase()
                require(entryType in validEntryTypes) { "INVALID_VALUE:contentType" }
                val categoryName = item.optString("categoryName").trim().takeIf { it.isNotEmpty() }
                val byLanguage = linkedMapOf<String, MutableList<String>>()
                val contentsJson = item.getJSONArray("contents")
                for (j in 0 until contentsJson.length()) {
                    val content = contentsJson.getJSONObject(j)
                    val language = content.optString("languageCode").trim()
                    val text = content.optString("text").trim()
                    // Some legacy exports contain placeholder empty translations. Ignore those rows;
                    // a concept is still required to have at least one real content value.
                    if (language.isEmpty() || text.isEmpty()) continue
                    if (validLanguages.isNotEmpty()) require(language in validLanguages) { "INVALID_VALUE:languageCode" }
                    byLanguage.getOrPut(language) { mutableListOf() }.let { values -> if (text !in values) values += text }
                }
                require(byLanguage.isNotEmpty()) { "INVALID_VALUE:contents" }
                val createdAt = epochInstant(item.optLong("createdAt").takeIf { item.has("createdAt") }, exportedAt)
                val updatedAt = epochInstant(item.optLong("updatedAt").takeIf { item.has("updatedAt") }, createdAt).coerceAtLeast(createdAt)
                val concept = ConceptEntity(id, entryType, null, item.optBoolean("favorite", false), item.optBoolean("active", true), createdAt, updatedAt)
                val note = item.optString("notes").takeIf { it.isNotBlank() && it != "null" }
                val contents = byLanguage.map { (language, values) ->
                    val mergedText = values.joinToString(" / ")
                    ContentEntity(stableId("content", id.toString(), language), id, language, mergedText, computeCanonicalKey(mergedText), note, null, null)
                }
                incoming += IncomingConcept(concept, contents, categoryName)
            }

            val states = HashMap<UUID, LegacyState>(statesJson.length())
            for (i in 0 until statesJson.length()) {
                val item = statesJson.getJSONObject(i)
                val id = UUID.fromString(item.getString("conceptUuid"))
                require(!states.containsKey(id)) { "DUPLICATE_UUID:learningStates" }
                val stage = item.optString("stage").uppercase()
                val difficulty = item.optString("difficulty").uppercase()
                require(stage in Stage.entries.map { it.name }) { "INVALID_VALUE:stage" }
                require(difficulty in VocabularyDifficulty.entries.map { it.name }) { "INVALID_VALUE:difficulty" }
                val correct = item.optInt("consecutiveCorrect", 0).coerceAtLeast(0)
                val incorrect = item.optInt("consecutiveIncorrect", 0).coerceAtLeast(0)
                require(!(correct > 0 && incorrect > 0)) { "INVALID_VALUE:difficulty_streak" }
                states[id] = LegacyState(id, stage, difficulty, item.optInt("monthlyWrongCount", 0).coerceAtLeast(0),
                    item.optInt("totalCorrect", 0).coerceAtLeast(0), item.optInt("totalWrong", 0).coerceAtLeast(0),
                    item.optBoolean("everFailed", false), correct, incorrect, item.optInt("difficultyScore", 0))
            }
            require(states.keys.containsAll(incoming.map { it.entity.id })) { "INVALID_REFERENCE:learning_state_missing" }

            data class LegacyHistory(
                val conceptId: UUID, val sessionId: String, val stage: String, val reviewedAt: Instant,
                val correct: Boolean
            )
            val history = ArrayList<LegacyHistory>(historyJson.length())
            for (i in 0 until historyJson.length()) {
                val item = historyJson.getJSONObject(i)
                val conceptId = UUID.fromString(item.getString("conceptUuid"))
                require(conceptId in states) { "INVALID_REFERENCE:history_concept" }
                val stage = item.optString("reviewStage").uppercase()
                require(stage in ReviewType.entries.map { it.name }) { "INVALID_VALUE:reviewStage" }
                val reviewedAt = epochInstant(item.optLong("reviewDate").takeIf { item.has("reviewDate") }, exportedAt)
                history += LegacyHistory(conceptId, item.optString("sessionId").ifBlank { "legacy-$i" }, stage, reviewedAt, item.optBoolean("isCorrect", false))
            }

            db.withTransaction {
                val existingConcepts = db.conceptDao().getAll().associateBy { it.id }
                val existingContents = db.contentDao().getAll().groupBy { it.conceptId }
                val existingCategories = db.categoryDao().getAll().associateBy { it.name.trim().lowercase(java.util.Locale.ROOT) }
                val categoryIds = HashMap<String, UUID>()
                var newCount = 0
                var mergedCount = 0

                incoming.mapNotNull { it.categoryName }.distinct().forEach { name ->
                    val key = name.trim().lowercase(java.util.Locale.ROOT)
                    val existing = existingCategories[key]
                    val id = existing?.id ?: UUID.randomUUID().also { db.categoryDao().insert(CategoryEntity(it, name)) }
                    categoryIds[key] = id
                    if (existing == null) newCount++ else mergedCount++
                }

                val historyByConcept = history.groupBy { it.conceptId }
                for (item in incoming) {
                    val categoryId = item.categoryName?.let { categoryIds[it.trim().lowercase(java.util.Locale.ROOT)] }
                    val restoredConcept = item.entity.copy(categoryId = categoryId ?: existingConcepts[item.entity.id]?.categoryId)
                    val existing = existingConcepts[item.entity.id]
                    if (existing == null) { db.conceptDao().insert(restoredConcept); newCount++ }
                    else { db.conceptDao().update(restoredConcept); mergedCount++ }

                    val oldByLanguage = existingContents[item.entity.id].orEmpty().associateBy { it.languageCode }
                    for (content in item.contents) {
                        val old = oldByLanguage[content.languageCode]
                        if (old == null) { db.contentDao().insert(content); newCount++ }
                        else {
                            db.contentDao().update(content.copy(id = old.id, notes = content.notes ?: old.notes, pronunciation = old.pronunciation, example = old.example))
                            mergedCount++
                        }
                    }

                    val state = states[item.entity.id]!!
                    val conceptHistory = historyByConcept[item.entity.id].orEmpty()
                    val lastReviewed = conceptHistory.maxOfOrNull { it.reviewedAt }
                    val nextReview = if (state.stage == Stage.LEARNED.name) null else (lastReviewed ?: now).plus(intervalFor(state.stage))
                    val existingLearning = db.learningStateDao().getByConceptId(item.entity.id)
                    val learningEntity = LearningStateEntity(
                        existingLearning?.id ?: stableId("learning", item.entity.id.toString()), item.entity.id, state.stage,
                        nextReview, state.monthlyWrongCount, state.everFailed, state.totalCorrect, state.totalWrong, lastReviewed
                    )
                    db.learningStateDao().upsert(learningEntity)
                    if (existingLearning == null) newCount++ else mergedCount++

                    val existingDifficulty = db.difficultyStateDao().getByConceptId(item.entity.id)
                    val difficultyEntity = DifficultyStateEntity(
                        existingDifficulty?.id ?: stableId("difficulty", item.entity.id.toString()), item.entity.id, state.difficulty,
                        state.consecutiveCorrect, state.consecutiveIncorrect, state.difficulty == VocabularyDifficulty.VERY_HARD.name || state.difficultyScore >= 10
                    )
                    db.difficultyStateDao().upsert(difficultyEntity)
                    if (existingDifficulty == null) newCount++ else mergedCount++
                }

                // Legacy sessions could contain both DAILY and WEEKLY attempts. Split them by stage so
                // the current schema's session reviewType invariant remains valid without dropping history.
                history.groupBy { it.sessionId to it.stage }.forEach { (key, attempts) ->
                    val sessionId = stableId("session", key.first, key.second)
                    val started = attempts.minOf { it.reviewedAt }
                    val ended = attempts.maxOf { it.reviewedAt }
                    val existingSession = db.reviewSessionDao().getById(sessionId)
                    val session = ReviewSessionEntity(sessionId, started, ended, key.second)
                    if (existingSession == null) { db.reviewSessionDao().insert(session); newCount++ }
                    else { db.reviewSessionDao().update(session); mergedCount++ }
                    attempts.forEachIndexed { index, attempt ->
                        val attemptId = stableId("attempt", attempt.conceptId.toString(), key.first, key.second, attempt.reviewedAt.toEpochMilli().toString(), index.toString())
                        val historyId = stableId("history", attemptId.toString())
                        if (!db.reviewHistoryDao().existsByAttemptId(sessionId, attemptId)) {
                            db.reviewHistoryDao().insert(ReviewHistoryEntity(historyId, sessionId, attemptId, attempt.conceptId, attempt.reviewedAt, attempt.correct, key.second))
                            newCount++
                        } else mergedCount++
                    }
                }

                root.optJSONArray("settings")?.let { settingsJson ->
                    for (i in 0 until settingsJson.length()) {
                        val item = settingsJson.getJSONObject(i)
                        val key = item.optString("key").trim()
                        if (key.isBlank()) continue
                        val value = item.optString("value")
                        val updatedAt = epochInstant(item.optLong("updatedAt").takeIf { item.has("updatedAt") }, exportedAt)
                        db.settingsDao().put(SettingsEntity(key, value, updatedAt))
                        mergedCount++
                    }
                }

                RestoreResult(newCount, mergedCount, emptyList())
            }
        } catch (e: Exception) {
            RestoreResult(0, 0, listOf(e.message ?: "LEGACY_FULL_RESTORE_FAILED"))
        }
    }
}
