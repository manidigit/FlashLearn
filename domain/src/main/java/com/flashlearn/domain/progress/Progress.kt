package com.flashlearn.domain.progress

import com.flashlearn.domain.model.*
import com.flashlearn.domain.repository.ConceptRepository
import com.flashlearn.domain.repository.DifficultyStateRepository
import com.flashlearn.domain.repository.LearningStateRepository
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
        val learning = concepts.map { concept ->
            learningRepository.get(concept.id)
                ?: error("DATA_INTEGRITY_ERROR: LearningState not found for concept ${concept.id}")
        }
        val difficulty = concepts.map { concept ->
            difficultyRepository.get(concept.id)
                ?: error("DATA_INTEGRITY_ERROR: DifficultyState not found for concept ${concept.id}")
        }
        return ProgressSnapshot(
            totalConcepts = concepts.size,
            dailyConcepts = learning.count { it.stage == Stage.DAILY },
            weeklyConcepts = learning.count { it.stage == Stage.WEEKLY },
            monthlyConcepts = learning.count { it.stage == Stage.MONTHLY },
            learnedConcepts = learning.count { it.stage == Stage.LEARNED },
            pathFailureConcepts = learning.count { it.hasPathFailure },
            veryHardConcepts = difficulty.count { it.hasReachedVeryHard }
        )
    }
}
