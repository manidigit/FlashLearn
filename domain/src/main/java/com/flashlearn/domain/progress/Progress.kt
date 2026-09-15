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
    val veryHardConcepts: Int
)

class CalculateProgressUseCase @Inject constructor(
    private val conceptRepository: ConceptRepository,
    private val learningRepository: LearningStateRepository,
    private val difficultyRepository: DifficultyStateRepository
) {
    @Suppress("UNUSED_PARAMETER")
    suspend operator fun invoke(now: Instant): ProgressSnapshot {
        val concepts = conceptRepository.getAllActive()
        val learningById = learningRepository.getAll().associateBy { it.conceptId }
        val difficultyById = difficultyRepository.getAll().associateBy { it.conceptId }
        return ProgressSnapshot(
            totalConcepts = concepts.size,
            dailyConcepts = concepts.count { learningById[it.id]?.stage == Stage.DAILY },
            weeklyConcepts = concepts.count { learningById[it.id]?.stage == Stage.WEEKLY },
            monthlyConcepts = concepts.count { learningById[it.id]?.stage == Stage.MONTHLY },
            learnedConcepts = concepts.count { learningById[it.id]?.stage == Stage.LEARNED },
            pathFailureConcepts = concepts.count { learningById[it.id]?.hasPathFailure == true },
            veryHardConcepts = concepts.count { difficultyById[it.id]?.hasReachedVeryHard == true }
        )
    }
}

object ProgressScoring {
    fun score(stage: Stage?, hasReviewHistory: Boolean): Int = when (stage) {
        Stage.LEARNED -> 100
        Stage.MONTHLY -> 80
        Stage.WEEKLY -> 60
        Stage.DAILY -> 35
        null -> if (hasReviewHistory) 15 else 0
    }
}

data class ProgressPercentageResult(val percentage: Double)

class CalculateProgressPercentage @Inject constructor(
    private val conceptRepository: ConceptRepository,
    private val learningRepository: LearningStateRepository,
    private val reviewHistoryRepository: ReviewHistoryRepository
) {
    suspend operator fun invoke(): Double {
        val concepts = conceptRepository.getAllActive()
        if (concepts.isEmpty()) return 0.0
        val states = learningRepository.getAll().associateBy { it.conceptId }
        val reviewed = reviewHistoryRepository.getAll().groupBy { it.conceptId }
        val totalScore = concepts.sumOf { concept ->
            ProgressScoring.score(states[concept.id]?.stage, !reviewed[concept.id].isNullOrEmpty())
        }
        return totalScore.toDouble() / concepts.size.toDouble()
    }
}
