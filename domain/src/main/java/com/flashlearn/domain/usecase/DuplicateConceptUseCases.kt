package com.flashlearn.domain.usecase

import com.flashlearn.domain.model.Content
import com.flashlearn.domain.repository.ConceptRepository
import com.flashlearn.domain.repository.ContentRepository
import com.flashlearn.domain.repository.FlashLearnDatabase
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

/**
 * Consolidates active concepts that represent the same source word.
 * The oldest concept is retained; every distinct target meaning is preserved on it,
 * and redundant concepts are soft-deleted. Exact source+translation copies therefore
 * collapse to one while different translations remain as separate meanings.
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
        val groups = activeConcepts
            .mapNotNull { concept ->
                val sourceKey = contentsByConcept[concept.id].orEmpty()
                    .firstOrNull { it.languageCode == sourceLanguage }
                    ?.canonicalKey
                    ?.takeIf { it.isNotBlank() }
                sourceKey?.let { it to concept }
            }
            .groupBy({ it.first }, { it.second })
            .values
            .filter { it.size > 1 }

        var removed = 0
        for (group in groups) {
            val ordered = group.sortedBy { it.createdAt }
            val survivor = ordered.first()
            val survivorTargets = contentRepository.findAll(survivor.id, targetLanguage)
            val existingTargetKeys = survivorTargets.map { it.canonicalKey }.toMutableSet()
            var nextIndex = (survivorTargets.maxOfOrNull { it.translationIndex } ?: -1) + 1

            ordered.drop(1).forEach { duplicate ->
                contentsByConcept[duplicate.id].orEmpty()
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
                conceptRepository.softDelete(duplicate.id, Instant.now())
                removed++
            }
        }
        removed
    }
}
