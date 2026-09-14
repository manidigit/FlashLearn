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
        val learningById = learningRepository.getAll().associateBy { it.conceptId }
        val difficultyById = difficultyRepository.getAll().associateBy { it.conceptId }

        // Do not issue one Room query per concept. A restored 100k-word library must be
        // summarized with three bulk reads rather than 200k sequential database calls.
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
