package com.flashlearn.domain.usecase

import com.flashlearn.domain.model.*
import com.flashlearn.domain.repository.ConceptRepository
import com.flashlearn.domain.repository.ConceptTagRepository
import com.flashlearn.domain.repository.DifficultyStateRepository
import com.flashlearn.domain.repository.LearningStateRepository
import com.flashlearn.domain.repository.ReviewHistoryRepository
import com.flashlearn.domain.repository.ContentRepository
import javax.inject.Inject

/**
 * Counts exactly the same review-eligible population used by SelectReviewQueueUseCase.
 * Review setup and Home must never advertise a card that the review engine rejects.
 */
class CountReviewQueueUseCase @Inject constructor(
    private val conceptRepository: ConceptRepository,
    private val learningStateRepository: LearningStateRepository,
    private val difficultyStateRepository: DifficultyStateRepository,
    private val conceptTagRepository: ConceptTagRepository,
    private val reviewHistoryRepository: ReviewHistoryRepository,
    private val contentRepository: ContentRepository
) {
    constructor(conceptRepository: ConceptRepository, learningStateRepository: LearningStateRepository, difficultyStateRepository: DifficultyStateRepository, conceptTagRepository: ConceptTagRepository, reviewHistoryRepository: ReviewHistoryRepository) : this(conceptRepository, learningStateRepository, difficultyStateRepository, conceptTagRepository, reviewHistoryRepository, object : ContentRepository { override suspend fun findByUuid(uuid: java.util.UUID): Content? = null; override suspend fun find(conceptId: java.util.UUID, languageCode: String): Content? = null; override suspend fun upsert(content: Content) = Unit; override suspend fun getAll(): List<Content> = emptyList() })
    suspend operator fun invoke(filters: ReviewSelectionFilters): Int {
        val states = when (filters.reviewType) {
            ReviewType.RANDOM -> learningStateRepository.getDueNonLearned(filters.now)
            ReviewType.DAILY -> learningStateRepository.getAllByStage(Stage.DAILY)
                .filter { it.nextReviewAt != null && it.nextReviewAt <= filters.now }
            ReviewType.WEEKLY -> learningStateRepository.getAllByStage(Stage.WEEKLY)
                .filter { it.nextReviewAt != null && it.nextReviewAt <= filters.now }
            ReviewType.MONTHLY -> learningStateRepository.getAllByStage(Stage.MONTHLY)
                .filter { it.nextReviewAt != null && it.nextReviewAt <= filters.now }
            ReviewType.LEARNED -> learningStateRepository.getAllByStage(Stage.LEARNED)
        }

        val conceptsById = conceptRepository.getAllActive().associateBy { it.id }
        val difficultiesById = difficultyStateRepository.getAll().associateBy { it.conceptId }
        val tagsByConcept = conceptTagRepository.getAll()
            .groupBy(ConceptTag::conceptId)
            .mapValues { (_, tags) -> tags.map(ConceptTag::tagId) }
        val contentsByConcept = contentRepository.getAll().groupBy { it.conceptId }

        return states.asSequence()
            .filterNot { wasReviewedToday(it.lastReviewedAt, filters.now, filters.zoneId) }
            .mapNotNull { learning ->
                val concept = conceptsById[learning.conceptId] ?: return@mapNotNull null
                val difficulty = difficultiesById[learning.conceptId] ?: return@mapNotNull null
                val tags = tagsByConcept[learning.conceptId].orEmpty()
                val contents = contentsByConcept[learning.conceptId].orEmpty()
                if (filters.sourceLanguage != null && contents.none { it.languageCode.equals(filters.sourceLanguage, true) && it.text.isNotBlank() }) return@mapNotNull null
                if (filters.targetLanguage != null && contents.none { it.languageCode.equals(filters.targetLanguage, true) && it.text.isNotBlank() }) return@mapNotNull null
                if (filters.difficulties.isNotEmpty() && difficulty.current !in filters.difficulties) return@mapNotNull null
                else if (filters.difficulty != null && difficulty.current != filters.difficulty) return@mapNotNull null
                if (filters.categoryIds.isNotEmpty() && concept.categoryId !in filters.categoryIds) return@mapNotNull null
                else if (filters.categoryId != null && concept.categoryId != filters.categoryId) return@mapNotNull null
                if (filters.tagId != null && filters.tagId !in tags) return@mapNotNull null
                concept.id
            }
            .distinct()
            .count()
    }
}
