package com.flashlearn.domain.usecase

import com.flashlearn.domain.model.*
import com.flashlearn.domain.repository.*
import com.flashlearn.domain.settings.SettingsKeys
import java.time.Instant
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject

data class ReviewSelectionFilters(
    val reviewType: ReviewType,
    val difficulty: VocabularyDifficulty? = null,
    val categoryId: UUID? = null,
    val difficulties: Set<VocabularyDifficulty> = emptySet(),
    val categoryIds: Set<UUID> = emptySet(),
    val sourceLanguage: String? = null,
    val targetLanguage: String? = null,
    val tagId: UUID? = null,
    val now: Instant,
    val zoneId: ZoneId = ZoneId.systemDefault(),
    val maxCards: Int = SettingsKeys.DEFAULT_MAXIMUM_REVIEW_CARDS
)

data class ReviewCandidate(
    val concept: Concept,
    val learningState: LearningState,
    val difficulty: DifficultyState,
    val tagIds: List<UUID>
)

/**
 * A concept is unavailable for every review surface after its first review
 * on the same local calendar day. Shared by queue, counts, summaries and
 * answer submission so Quiz and Flashcard cannot diverge.
 */
internal fun wasReviewedToday(lastReviewedAt: Instant?, now: Instant, zoneId: ZoneId = ZoneId.systemDefault()): Boolean {
    if (lastReviewedAt == null) return false
    return lastReviewedAt.atZone(zoneId).toLocalDate() == now.atZone(zoneId).toLocalDate()
}

private object EmptyContentRepository : ContentRepository { override suspend fun findByUuid(uuid: UUID): Content? = null; override suspend fun find(conceptId: UUID, languageCode: String): Content? = null; override suspend fun upsert(content: Content) = Unit; override suspend fun getAll(): List<Content> = emptyList() }

private object EmptyReviewHistoryRepository : ReviewHistoryRepository {
    override suspend fun insert(entry: ReviewHistory) = Unit
    override suspend fun existsByAttemptId(sessionId: UUID, reviewAttemptId: UUID) = false
    override suspend fun getAll(): List<ReviewHistory> = emptyList()
}

class SelectReviewQueueUseCase @Inject constructor(
    private val conceptRepository: ConceptRepository,
    private val learningStateRepository: LearningStateRepository,
    private val difficultyStateRepository: DifficultyStateRepository,
    private val conceptTagRepository: ConceptTagRepository,
    private val reviewHistoryRepository: ReviewHistoryRepository,
    private val contentRepository: ContentRepository
) {
    constructor(
        conceptRepository: ConceptRepository,
        learningStateRepository: LearningStateRepository,
        difficultyStateRepository: DifficultyStateRepository,
        conceptTagRepository: ConceptTagRepository
    ) : this(
        conceptRepository,
        learningStateRepository,
        difficultyStateRepository,
        conceptTagRepository,
        EmptyReviewHistoryRepository,
        EmptyContentRepository
    )

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

        val conceptsById = conceptRepository.getAllActive().associateBy { it.id }
        val difficultiesById = difficultyStateRepository.getAll().associateBy { it.conceptId }
        val tagsByConcept = conceptTagRepository.getAll()
            .groupBy(ConceptTag::conceptId)
            .mapValues { (_, tags) -> tags.map(ConceptTag::tagId) }
        val contentsByConcept = contentRepository.getAll().groupBy { it.conceptId }
        val candidates = ArrayList<ReviewCandidate>(states.size)

        for (learning in states) {
            if (wasReviewedToday(learning.lastReviewedAt, filters.now, filters.zoneId)) continue
            val concept = conceptsById[learning.conceptId] ?: continue
            val difficulty = difficultiesById[learning.conceptId] ?: continue
            val tags = tagsByConcept[learning.conceptId].orEmpty()
            val contents = contentsByConcept[learning.conceptId].orEmpty()
            if (filters.sourceLanguage != null && contents.none { it.languageCode.equals(filters.sourceLanguage, true) && it.text.isNotBlank() }) continue
            if (filters.targetLanguage != null && contents.none { it.languageCode.equals(filters.targetLanguage, true) && it.text.isNotBlank() }) continue
            if (filters.difficulties.isNotEmpty() && difficulty.current !in filters.difficulties) continue
            else if (filters.difficulty != null && difficulty.current != filters.difficulty) continue
            if (filters.categoryIds.isNotEmpty() && concept.categoryId !in filters.categoryIds) continue
            else if (filters.categoryId != null && concept.categoryId != filters.categoryId) continue
                        if (filters.tagId != null && filters.tagId !in tags) continue
            candidates += ReviewCandidate(concept, learning, difficulty, tags)
        }

        val unique = candidates.distinctBy { it.concept.id }
        val safeMaxCards = filters.maxCards.coerceIn(
            SettingsKeys.MINIMUM_REVIEW_CARDS,
            SettingsKeys.MAXIMUM_REVIEW_CARDS_LIMIT
        )
        val ordered = when (filters.reviewType) {
            ReviewType.DAILY, ReviewType.WEEKLY, ReviewType.MONTHLY ->
                unique.sortedWith(
                    compareBy<ReviewCandidate> { it.learningState.nextReviewAt }
                        .thenBy { it.concept.id.toString() }
                )
            ReviewType.LEARNED, ReviewType.RANDOM -> unique.shuffled()
        }
        return ordered.take(safeMaxCards)
    }
}

class StartReviewSessionUseCase @Inject constructor(
    private val repository: ReviewSessionRepository
) {
    suspend operator fun invoke(
        reviewType: ReviewType,
        startedAt: Instant = Instant.now()
    ): UUID {
        val id = UUID.randomUUID()
        repository.insert(ReviewSession(id, startedAt, null, reviewType))
        return id
    }
}

class EndReviewSessionUseCase @Inject constructor(
    private val repository: ReviewSessionRepository
) {
    suspend operator fun invoke(
        sessionId: UUID,
        endedAt: Instant = Instant.now()
    ) {
        val session = repository.get(sessionId)
            ?: error("REVIEW_SESSION_NOT_FOUND: $sessionId")
        if (session.endedAt != null) return
        require(!endedAt.isBefore(session.startedAt)) { "endedAt cannot be before startedAt" }
        repository.update(session.copy(endedAt = endedAt))
    }
}
