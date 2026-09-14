package com.flashlearn.domain.usecase

import com.flashlearn.domain.model.*
import com.flashlearn.domain.repository.*
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

data class ReviewSelectionFilters(
    val reviewType: ReviewType,
    val difficulty: VocabularyDifficulty? = null,
    val categoryId: UUID? = null,
    val tagId: UUID? = null,
    val now: Instant
)

data class ReviewCandidate(
    val concept: Concept,
    val learningState: LearningState,
    val difficulty: DifficultyState,
    val tagIds: List<UUID>
)

class SelectReviewQueueUseCase @Inject constructor(
    private val conceptRepository: ConceptRepository,
    private val learningStateRepository: LearningStateRepository,
    private val difficultyStateRepository: DifficultyStateRepository,
    private val conceptTagRepository: ConceptTagRepository
) {
    suspend operator fun invoke(filters: ReviewSelectionFilters): List<ReviewCandidate> {
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

        // Large restored libraries used to cause 3 database queries per learning state
        // (concept + difficulty + tags). With 8k words that becomes tens of thousands of
        // sequential Room calls; at 100k it becomes unusable. Load each table once and
        // do the joins/filters in memory instead.
        val conceptsById = conceptRepository.getAllActive().associateBy { it.id }
        val difficultiesById = difficultyStateRepository.getAll().associateBy { it.conceptId }
        val tagsByConcept = conceptTagRepository.getAll().groupBy(ConceptTag::conceptId)
            .mapValues { (_, tags) -> tags.map(ConceptTag::tagId) }

        val candidates = ArrayList<ReviewCandidate>(states.size)
        for (learning in states) {
            val concept = conceptsById[learning.conceptId] ?: continue
            val difficulty = difficultiesById[learning.conceptId] ?: continue
            val tags = tagsByConcept[learning.conceptId].orEmpty()
            if (filters.difficulty != null && difficulty.current != filters.difficulty) continue
            if (filters.categoryId != null && concept.categoryId != filters.categoryId) continue
            if (filters.tagId != null && filters.tagId !in tags) continue
            candidates += ReviewCandidate(concept, learning, difficulty, tags)
        }

        val uniqueCandidates = candidates.distinctBy { it.concept.id }
        return when (filters.reviewType) {
            ReviewType.RANDOM, ReviewType.LEARNED -> uniqueCandidates.shuffled()
            ReviewType.DAILY, ReviewType.WEEKLY, ReviewType.MONTHLY ->
                uniqueCandidates.sortedWith(
                    compareBy<ReviewCandidate> { it.learningState.nextReviewAt ?: Instant.MIN }
                        .thenBy { it.concept.id.toString() }
                )
        }
    }
}

class StartReviewSessionUseCase @Inject constructor(
    private val repository: ReviewSessionRepository
) {
    suspend operator fun invoke(reviewType: ReviewType, startedAt: Instant = Instant.now()): UUID {
        val id = UUID.randomUUID()
        repository.insert(ReviewSession(id, startedAt, null, reviewType))
        return id
    }
}

class EndReviewSessionUseCase @Inject constructor(
    private val repository: ReviewSessionRepository
) {
    suspend operator fun invoke(sessionId: UUID, endedAt: Instant = Instant.now()) {
        val session = repository.get(sessionId)
            ?: error("REVIEW_SESSION_NOT_FOUND: $sessionId")
        if (session.endedAt != null) return
        require(!endedAt.isBefore(session.startedAt)) { "endedAt cannot be before startedAt" }
        repository.update(session.copy(endedAt = endedAt))
    }
}
