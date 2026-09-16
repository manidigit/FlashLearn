package com.flashlearn.domain.usecase

import com.flashlearn.domain.model.Content
import com.flashlearn.domain.repository.ConceptRepository
import com.flashlearn.domain.repository.ContentRepository
import com.flashlearn.domain.repository.FlashLearnDatabase
import java.util.UUID
import javax.inject.Inject

/**
 * Removes exact duplicate vocabulary concepts while preserving distinct meanings.
 *
 * Exact duplicate identity is source canonical text + target canonical text. When two
 * active concepts share that pair, the oldest concept is kept. Any additional target
 * meanings that exist only on a duplicate concept are copied to the survivor before
 * the duplicate concept is soft-deleted.
 */
class RemoveExactDuplicateConceptsUseCase @Inject constructor(
    private val conceptRepository: ConceptRepository,
    private val contentRepository: ContentRepository,
    private val database: FlashLearnDatabase
) {
    suspend operator fun invoke(sourceLanguage: String = "es", targetLanguage: String = "fa"): Int = database.withTransaction {
        val activeConcepts = conceptRepository.getAllActive()
        if (activeConcepts.size < 2) return@withTransaction 0

        val contentsByConcept = contentRepository.getAll().groupBy(Content::conceptId)
        val conceptsById = activeConcepts.associateBy { it.id }
        val pairToConceptIds = linkedMapOf<Pair<String, String>, MutableList<UUID>>()

        activeConcepts.sortedBy { it.createdAt }.forEach { concept ->
            val cc = contentsByConcept[concept.id].orEmpty()
            val sourceKeys = cc.asSequence()
                .filter { it.languageCode == sourceLanguage }
                .map { it.canonicalKey }
                .filter { it.isNotBlank() }
                .distinct()
                .toList()
            val targetKeys = cc.asSequence()
                .filter { it.languageCode == targetLanguage }
                .map { it.canonicalKey }
                .filter { it.isNotBlank() }
                .distinct()
                .toList()
            for (sourceKey in sourceKeys) for (targetKey in targetKeys) {
                pairToConceptIds.getOrPut(sourceKey to targetKey) { mutableListOf() }.add(concept.id)
            }
        }

        val duplicatePairs = pairToConceptIds.filterValues { it.distinct().size > 1 }
        if (duplicatePairs.isEmpty()) return@withTransaction 0

        val survivorIds = mutableSetOf<UUID>()
        val duplicateIds = mutableSetOf<UUID>()
        duplicatePairs.values.forEach { ids ->
            val ordered = ids.distinct().mapNotNull(conceptsById::get).sortedBy { it.createdAt }
            ordered.firstOrNull()?.let { survivorIds += it.id }
            ordered.drop(1).forEach { duplicateIds += it.id }
        }

        var removed = 0
        for (duplicateId in duplicateIds) {
            val duplicate = conceptsById[duplicateId] ?: continue
            val duplicateContents = contentsByConcept[duplicateId].orEmpty()
            val sourceKeys = duplicateContents.filter { it.languageCode == sourceLanguage }.map { it.canonicalKey }.toSet()
            val survivor = activeConcepts
                .filter { it.id in survivorIds && it.id != duplicateId }
                .firstOrNull { survivorConcept ->
                    val survivorContents = contentsByConcept[survivorConcept.id].orEmpty()
                    survivorContents.any { it.languageCode == sourceLanguage && it.canonicalKey in sourceKeys }
                }
                ?: continue

            val survivorTargets = contentRepository.findAll(survivor.id, targetLanguage).toMutableList()
            val existingTargetKeys = survivorTargets.map { it.canonicalKey }.toMutableSet()
            var nextIndex = (survivorTargets.maxOfOrNull { it.translationIndex } ?: -1) + 1
            duplicateContents
                .asSequence()
                .filter { it.languageCode == targetLanguage }
                .sortedBy { it.translationIndex }
                .forEach { content ->
                    if (existingTargetKeys.add(content.canonicalKey)) {
                        contentRepository.insertTranslation(
                            Content(
                                id = UUID.randomUUID(),
                                conceptId = survivor.id,
                                languageCode = targetLanguage,
                                text = content.text,
                                canonicalKey = content.canonicalKey,
                                notes = content.notes,
                                pronunciation = content.pronunciation,
                                example = content.example,
                                translationIndex = nextIndex++,
                                grammarNote = content.grammarNote,
                                possibleCorrection = content.possibleCorrection
                            )
                        )
                    }
                }

            conceptRepository.softDelete(duplicate.id, java.time.Instant.now())
            removed++
        }
        removed
    }
}
