package com.flashlearn.domain.usecase

import com.flashlearn.domain.model.Content
import com.flashlearn.domain.repository.ContentRepository
import com.flashlearn.domain.repository.DataVersionRepository
import com.flashlearn.domain.repository.FlashLearnDatabase
import javax.inject.Inject

data class DataMigrationResult(
    val conceptFrom: Int,
    val conceptTo: Int,
    val contentFrom: Int,
    val contentTo: Int,
    val changedContentRows: Int
)

class RefreshDataUseCase @Inject constructor(
    private val versions: DataVersionRepository,
    private val contentRepository: ContentRepository,
    private val database: FlashLearnDatabase
) {
    companion object {
        const val CURRENT_CONCEPT_DATA_VERSION = 1
        const val CURRENT_CONTENT_DATA_VERSION = 1
    }

    suspend operator fun invoke(): DataMigrationResult = database.withTransaction {
        var conceptVersion = versions.getConceptDataVersion()
        var contentVersion = versions.getContentDataVersion()
        val conceptFrom = conceptVersion
        val contentFrom = contentVersion
        var changedContent = 0
        while (conceptVersion < CURRENT_CONCEPT_DATA_VERSION) {
            conceptVersion = migrateConceptToVersion1(conceptVersion)
            versions.setConceptDataVersion(conceptVersion)
        }
        while (contentVersion < CURRENT_CONTENT_DATA_VERSION) {
            val result = migrateContentToVersion1(contentVersion)
            contentVersion = result.first
            changedContent += result.second
            versions.setContentDataVersion(contentVersion)
        }
        require(conceptVersion == CURRENT_CONCEPT_DATA_VERSION) { "Unsupported concept data version: $conceptVersion" }
        require(contentVersion == CURRENT_CONTENT_DATA_VERSION) { "Unsupported content data version: $contentVersion" }
        DataMigrationResult(conceptFrom, conceptVersion, contentFrom, contentVersion, changedContent)
    }

    private suspend fun migrateConceptToVersion1(current: Int): Int {
        require(current == 0) { "No concept migration path from version $current" }
        return 1
    }

    private suspend fun migrateContentToVersion1(current: Int): Pair<Int, Int> {
        require(current == 0) { "No content migration path from version $current" }
        var changed = 0
        contentRepository.getAll().forEach { content ->
            val canonical = computeCanonicalKey(content.text)
            if (content.canonicalKey != canonical) {
                contentRepository.upsert(content.copy(canonicalKey = canonical))
                changed++
            }
        }
        return 1 to changed
    }
}
