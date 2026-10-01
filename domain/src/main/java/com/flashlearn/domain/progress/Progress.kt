package com.flashlearn.domain.progress

import com.flashlearn.domain.model.*
import com.flashlearn.domain.repository.ConceptRepository
import com.flashlearn.domain.repository.DifficultyStateRepository
import com.flashlearn.domain.repository.LearningStateRepository
import com.flashlearn.domain.repository.ReviewHistoryRepository
import java.time.Instant
import javax.inject.Inject

data class ProgressSnapshot(
    val totalConcepts: Int,
    val dailyConcepts: Int,
    val weeklyConcepts: Int,
    val monthlyConcepts: Int,
    val learnedConcepts: Int,
    val pathFailureConcepts: Int,
    val easyConcepts: Int,
    val mediumConcepts: Int,
    val hardConcepts: Int,
    val veryHardConcepts: Int
)

class CalculateProgressUseCase @Inject constructor(
    private val conceptRepository: ConceptRepository,
    private val learningRepository: LearningStateRepository,
    private val difficultyRepository: DifficultyStateRepository
) {
    suspend operator fun invoke(now: Instant): ProgressSnapshot {
        val concepts = conceptRepository.getAllActive()
        val stageCounts = learningRepository.countByStage()
        val difficultyCounts = difficultyRepository.countByDifficulty()
        val learningStates = learningRepository.getAll()
        return ProgressSnapshot(
            totalConcepts = concepts.size,
            dailyConcepts = stageCounts[Stage.DAILY] ?: 0,
            weeklyConcepts = stageCounts[Stage.WEEKLY] ?: 0,
            monthlyConcepts = stageCounts[Stage.MONTHLY] ?: 0,
            learnedConcepts = stageCounts[Stage.LEARNED] ?: 0,
            pathFailureConcepts = learningStates.count { it.hasPathFailure && it.conceptId in concepts.map { concept -> concept.id }.toSet() },
            easyConcepts = difficultyCounts[VocabularyDifficulty.EASY] ?: 0,
            mediumConcepts = difficultyCounts[VocabularyDifficulty.MEDIUM] ?: 0,
            hardConcepts = difficultyCounts[VocabularyDifficulty.HARD] ?: 0,
            veryHardConcepts = difficultyCounts[VocabularyDifficulty.VERY_HARD] ?: 0
        )
    }
}

object ProgressScoring {
    fun score(stage: Stage?, hasReviewHistory: Boolean): Int {
        // A newly imported word may already have an initial DAILY learning state,
        // but it must not receive learning progress before the first real review.
        if (!hasReviewHistory) return 0
        return when (stage) {
            Stage.LEARNED -> 100
            Stage.MONTHLY -> 80
            Stage.WEEKLY -> 60
            Stage.DAILY -> 35
            null -> 15
        }
    }
}

data class ProgressPercentageResult(val percentage: Double)

class CalculateProgressPercentage @Inject constructor(
    private val conceptRepository: ConceptRepository,
    private val learningRepository: LearningStateRepository,
    private val reviewHistoryRepository: ReviewHistoryRepository
) {
    suspend operator fun invoke(): Double = invoke(reviewHistoryRepository.getAll())

    suspend operator fun invoke(history: List<ReviewHistory>): Double {
        val concepts = conceptRepository.getAllActive()
        if (concepts.isEmpty()) return 0.0
        val states = learningRepository.getAll().associateBy { it.conceptId }
        val reviewedIds = history.asSequence().map { it.conceptId }.toSet()
        val totalScore: Int = concepts.sumOf { concept ->
            ProgressScoring.score(states[concept.id]?.stage, concept.id in reviewedIds)
        }
        return totalScore.toDouble() / concepts.size.toDouble()
    }
}
