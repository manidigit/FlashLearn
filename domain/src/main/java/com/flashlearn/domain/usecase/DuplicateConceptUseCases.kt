package com.flashlearn.domain.usecase

import com.flashlearn.domain.model.Content
import com.flashlearn.domain.model.Concept
import com.flashlearn.domain.repository.ConceptRepository
import com.flashlearn.domain.repository.ContentRepository
import com.flashlearn.domain.repository.FlashLearnDatabase
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

/**
 * Cleans the active library by source word. One active concept survives for each
 * normalized source; every distinct target meaning is preserved on that concept.
 * Comparisons are derived from Content.text rather than persisted canonicalKey so
 * legacy/imported rows with stale canonical keys are cleaned too.
 */
class RemoveExactDuplicateConceptsUseCase @Inject constructor(
    private val conceptRepository: ConceptRepository,
    private val contentRepository: ContentRepository,
    private val database: FlashLearnDatabase
) {
    suspend operator fun invoke(sourceLanguage: String = "es", targetLanguage: String = "fa"): Int = database.withTransaction {
        require(sourceLanguage.isNotBlank() && targetLanguage.isNotBlank() && sourceLanguage != targetLanguage) {
            "زبان‌های مبدأ و مقصد باید متفاوت باشند"
        }

        val activeConcepts = conceptRepository.getAllActive()
        if (activeConcepts.size < 2) return@withTransaction 0

        val contentsByConcept = contentRepository.getAll().groupBy(Content::conceptId)
        val groups: Collection<List<Concept>> = activeConcepts
            .mapNotNull { concept ->
                val source = contentsByConcept[concept.id].orEmpty()
                    .firstOrNull { it.languageCode == sourceLanguage && it.text.isNotBlank() }
                source?.let { computeCanonicalKey(it.text) to concept }
            }
            .groupBy({ it.first }, { it.second })
            .values
            .filter { it.size > 1 }

        var removed = 0
        for (group in groups) {
            val ordered = group.sortedWith(compareBy<Concept> { it.createdAt }.thenBy { it.id.toString() })
            val survivor = ordered.first()
            val survivorTargets = contentRepository.findAll(survivor.id, targetLanguage)
                .sortedBy { it.translationIndex }
            val existingTargetKeys = survivorTargets
                .map { computeCanonicalKey(it.text) }
                .filter(String::isNotBlank)
                .toMutableSet()
            var nextIndex = (survivorTargets.maxOfOrNull { it.translationIndex } ?: -1) + 1

            ordered.drop(1).forEach { duplicate ->
                contentsByConcept[duplicate.id].orEmpty()
                    .asSequence()
                    .filter { it.languageCode == targetLanguage && it.text.isNotBlank() }
                    .sortedBy { it.translationIndex }
                    .forEach { content ->
                        val targetKey = computeCanonicalKey(content.text)
                        if (targetKey.isNotBlank() && existingTargetKeys.add(targetKey)) {
                            contentRepository.insertTranslation(
                                Content(
                                    id = UUID.randomUUID(),
                                    conceptId = survivor.id,
                                    languageCode = targetLanguage,
                                    text = content.text.trim(),
                                    canonicalKey = targetKey,
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
