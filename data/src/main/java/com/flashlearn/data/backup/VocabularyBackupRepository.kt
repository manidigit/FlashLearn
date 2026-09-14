package com.flashlearn.data.backup

import androidx.room.withTransaction
import com.flashlearn.database.CategoryEntity
import com.flashlearn.database.ContentEntity
import com.flashlearn.database.ConceptEntity
import com.flashlearn.database.DifficultyStateEntity
import com.flashlearn.database.LearningStateEntity
import com.flashlearn.database.RoomFlashLearnDatabase
import com.flashlearn.domain.model.EntryType
import com.flashlearn.domain.model.Stage
import com.flashlearn.domain.model.VocabularyDifficulty
import com.flashlearn.domain.repository.RestoreResult
import com.flashlearn.domain.usecase.computeCanonicalKey
import org.json.JSONObject
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/** Restores the legacy vocabulary-only backup format used by FlashLearn v5.x. */
@Singleton
class VocabularyBackupRepository @Inject constructor(
    private val db: RoomFlashLearnDatabase
) {
    companion object {
        private const val SCHEMA_VERSION = 1
        private const val BACKUP_MODE = "VOCABULARY"
    }

    suspend fun restore(json: String): RestoreResult {
        val root = try { JSONObject(json) } catch (_: Exception) {
            return RestoreResult(0, 0, listOf("INVALID_JSON"))
        }
        if (root.optInt("schemaVersion", -1) != SCHEMA_VERSION || root.optString("backupMode") != BACKUP_MODE) {
            return RestoreResult(0, 0, listOf("UNSUPPORTED_VOCABULARY_BACKUP"))
        }
        if (!root.has("concepts") || !root.has("languages") || !root.has("categories")) {
            return RestoreResult(0, 0, listOf("MISSING_VOCABULARY_SECTION"))
        }

        return try {
            val conceptsJson = root.getJSONArray("concepts")
            val categoryNames = linkedSetOf<String>()
            val categoriesJson = root.getJSONArray("categories")
            for (i in 0 until categoriesJson.length()) {
                val name = categoriesJson.getJSONObject(i).optString("name").trim()
                if (name.isNotEmpty()) categoryNames += name
            }

            data class Incoming(
                val concept: ConceptEntity,
                val contents: List<ContentEntity>
            )

            val now = Instant.now()
            val incoming = ArrayList<Incoming>(conceptsJson.length())
            val seenConcepts = HashSet<UUID>(conceptsJson.length())
            val validEntryTypes = EntryType.entries.map { it.name }.toSet()

            for (i in 0 until conceptsJson.length()) {
                val item = conceptsJson.getJSONObject(i)
                val id = UUID.fromString(item.getString("uuid"))
                require(seenConcepts.add(id)) { "DUPLICATE_UUID:concepts" }
                val entryType = item.optString("contentType", "WORD").uppercase()
                require(entryType in validEntryTypes) { "INVALID_VALUE:contentType" }

                val byLanguage = linkedMapOf<String, MutableList<String>>()
                val contentsJson = item.getJSONArray("contents")
                for (j in 0 until contentsJson.length()) {
                    val content = contentsJson.getJSONObject(j)
                    val language = content.optString("languageCode").trim()
                    val text = content.optString("text").trim()
                    require(language.isNotEmpty() && text.isNotEmpty()) { "INVALID_VALUE:content" }
                    byLanguage.getOrPut(language) { mutableListOf() }.let { values ->
                        if (text !in values) values += text
                    }
                }
                require(byLanguage.isNotEmpty()) { "INVALID_VALUE:contents" }

                val note = item.optString("notes").takeIf { it.isNotBlank() && it != "null" }
                val concept = ConceptEntity(id, entryType, null, false, true, now, now)
                val contents = byLanguage.map { (language, values) ->
                    val mergedText = values.joinToString(" / ")
                    ContentEntity(UUID.randomUUID(), id, language, mergedText, computeCanonicalKey(mergedText), note, null, null)
                }
                incoming += Incoming(concept, contents)
            }

            db.withTransaction {
                val existingConcepts = db.conceptDao().getAll().associateBy { it.id }
                val existingContents = db.contentDao().getAll().groupBy { it.conceptId }

                val categoryEntities = categoryNames.mapNotNull { name ->
                    if (db.categoryDao().findByName(name) != null) null else CategoryEntity(UUID.randomUUID(), name)
                }
                if (categoryEntities.isNotEmpty()) db.categoryDao().insertAll(categoryEntities)

                val conceptEntities = ArrayList<ConceptEntity>(incoming.size)
                val contentEntities = ArrayList<ContentEntity>()
                val learningEntities = ArrayList<LearningStateEntity>()
                val difficultyEntities = ArrayList<DifficultyStateEntity>()
                var newCount = 0
                var mergedCount = 0

                for (item in incoming) {
                    val existing = existingConcepts[item.concept.id]
                    if (existing == null) {
                        conceptEntities += item.concept
                        learningEntities += LearningStateEntity(UUID.randomUUID(), item.concept.id, Stage.DAILY.name, now, 0, false, 0, 0, null)
                        difficultyEntities += DifficultyStateEntity(UUID.randomUUID(), item.concept.id, VocabularyDifficulty.EASY.name, 0, 0, false)
                        newCount++
                    } else {
                        // Restoring a vocabulary item should make it visible again, while keeping user progress.
                        conceptEntities += existing.copy(entryType = item.concept.entryType, active = true, updatedAt = now)
                        mergedCount++
                    }

                    val oldByLanguage = existingContents[item.concept.id].orEmpty().associateBy { it.languageCode }
                    for (content in item.contents) {
                        val old = oldByLanguage[content.languageCode]
                        if (old == null) {
                            contentEntities += content
                        } else {
                            contentEntities += content.copy(
                                id = old.id,
                                notes = content.notes ?: old.notes,
                                pronunciation = old.pronunciation,
                                example = old.example
                            )
                        }
                    }
                }

                if (conceptEntities.isNotEmpty()) db.conceptDao().insertAll(conceptEntities)
                if (contentEntities.isNotEmpty()) db.contentDao().insertAll(contentEntities)
                if (learningEntities.isNotEmpty()) db.learningStateDao().upsertAll(learningEntities)
                if (difficultyEntities.isNotEmpty()) db.difficultyStateDao().upsertAll(difficultyEntities)

                RestoreResult(newCount, mergedCount, emptyList())
            }
        } catch (e: Exception) {
            RestoreResult(0, 0, listOf(e.message ?: "VOCABULARY_RESTORE_FAILED"))
        }
    }
}
