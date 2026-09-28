package com.flashlearn.domain.usecase

import com.flashlearn.domain.repository.ContentRepository
import com.flashlearn.domain.repository.ConceptRepository
import com.flashlearn.domain.repository.DataVersionRepository
import com.flashlearn.domain.repository.FlashLearnDatabase
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

data class DataMigrationResult(
    val conceptFrom: Int,
    val conceptTo: Int,
    val contentFrom: Int,
    val contentTo: Int,
    val changedContentRows: Int,
    val changedConceptRows: Int = 0
)

class RefreshDataUseCase @Inject constructor(
    private val versions: DataVersionRepository,
    private val conceptRepository: ConceptRepository,
    private val contentRepository: ContentRepository,
    private val database: FlashLearnDatabase
) {
    companion object {
        const val CURRENT_CONCEPT_DATA_VERSION = 2
        const val CURRENT_CONTENT_DATA_VERSION = 3
    }

    suspend operator fun invoke(): DataMigrationResult = database.withTransaction {
        var conceptVersion = versions.getConceptDataVersion()
        var contentVersion = versions.getContentDataVersion()
        val conceptFrom = conceptVersion
        val contentFrom = contentVersion
        var changedConcept = 0
        var changedContent = 0

        while (conceptVersion < CURRENT_CONCEPT_DATA_VERSION) {
            val result = migrateConceptToNextVersion(conceptVersion)
            conceptVersion = result.first
            changedConcept += result.second
            versions.setConceptDataVersion(conceptVersion)
        }
        while (contentVersion < CURRENT_CONTENT_DATA_VERSION) {
            val result = migrateContentToNextVersion(contentVersion)
            contentVersion = result.first
            changedContent += result.second
            versions.setContentDataVersion(contentVersion)
        }

        require(conceptVersion == CURRENT_CONCEPT_DATA_VERSION) {
            "Unsupported concept data version: $conceptVersion"
        }
        require(contentVersion == CURRENT_CONTENT_DATA_VERSION) {
            "Unsupported content data version: $contentVersion"
        }

        DataMigrationResult(
            conceptFrom = conceptFrom,
            conceptTo = conceptVersion,
            contentFrom = contentFrom,
            contentTo = contentVersion,
            changedContentRows = changedContent,
            changedConceptRows = changedConcept
        )
    }

    private suspend fun migrateConceptToNextVersion(current: Int): Pair<Int, Int> = when (current) {
        0 -> migrateConceptToVersion1()
        1 -> migrateConceptToVersion2()
        else -> error("No concept migration path from version $current")
    }

    private suspend fun migrateConceptToVersion1(): Pair<Int, Int> {
        var checked = 0
        val now = Instant.now()
        conceptRepository.getAllActive().forEach { concept ->
            require(concept.createdAt <= now) { "INVALID_CONCEPT_CREATED_AT:${concept.id}" }
            checked++
        }
        return 1 to checked
    }

    private suspend fun migrateConceptToVersion2(): Pair<Int, Int> {
        var checked = 0
        conceptRepository.getAllActive().forEach { concept ->
            require(concept.updatedAt >= concept.createdAt) { "INVALID_CONCEPT_TIMELINE:${concept.id}" }
            checked++
        }
        return 2 to checked
    }

    private suspend fun migrateContentToNextVersion(current: Int): Pair<Int, Int> = when (current) {
        0 -> migrateContentToVersion1()
        1 -> migrateContentToVersion2()
        2 -> migrateContentToVersion3()
        else -> error("No content migration path from version $current")
    }

    private suspend fun migrateContentToVersion1(): Pair<Int, Int> = canonicalizeContent()
        .let { 1 to it }

    private suspend fun migrateContentToVersion2(): Pair<Int, Int> = canonicalizeContent()
        .let { 2 to it }

    private suspend fun migrateContentToVersion3(): Pair<Int, Int> {
        var changed = 0
        val groups = contentRepository.getAll().groupBy { it.conceptId to it.languageCode }
        groups.forEach { (key, rows) ->
            val language = key.second
            if (!language.equals("fa", true)) return@forEach
            val ordered = rows.sortedWith(compareBy({ it.translationIndex }, { it.id }))
            val parts = ordered.flatMap { row ->
                row.text.split(Regex("\\s*/\\s*|\\s*؛\\s*|\\s*;\\s*"))
                    .map(String::trim).filter(String::isNotBlank).distinctBy(::computeCanonicalKey)
                    .map { text -> row to text }
            }
            val normalized = parts.distinctBy { computeCanonicalKey(it.second) }
            val needsRewrite = ordered.map { computeCanonicalKey(it.text) to it.translationIndex } != normalized.mapIndexed { i, p -> computeCanonicalKey(p.second) to i }
            if (needsRewrite) {
                contentRepository.deleteByConceptAndLanguage(key.first, language)
                normalized.forEachIndexed { index, pair ->
                    val row = pair.first
                    val text = pair.second
                    contentRepository.upsert(row.copy(id = if (index == 0) row.id else UUID.randomUUID(), text = text, canonicalKey = computeCanonicalKey(text), translationIndex = index))
                    changed++
                }
            } else {
                ordered.forEachIndexed { index, row ->
                    if (row.translationIndex != index) {
                        contentRepository.upsert(row.copy(translationIndex = index))
                        changed++
                    }
                }
            }
        }
        return 3 to changed
    }

    private suspend fun canonicalizeContent(): Int {
        var changed = 0
        contentRepository.getAll().forEach { content ->
            val canonical = computeCanonicalKey(content.text)
            if (content.canonicalKey != canonical) {
                contentRepository.upsert(content.copy(canonicalKey = canonical))
                changed++
            }
        }
        return changed
    }
}
