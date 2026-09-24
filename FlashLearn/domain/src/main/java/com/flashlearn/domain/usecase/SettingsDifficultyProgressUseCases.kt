package com.flashlearn.domain.usecase

import com.flashlearn.domain.model.*
import com.flashlearn.domain.repository.*
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

object FlashLearnSettingsKeys {
    const val THRESHOLD_DIFFICULTY = "threshold_difficulty"
    const val DEFAULT_THRESHOLD_DIFFICULTY = 3
}

class GetDifficultyStateUseCase @Inject constructor(
    private val repository: DifficultyStateRepository
) {
    suspend operator fun invoke(conceptId: UUID): DifficultyState =
        repository.get(conceptId)
            ?: error("DATA_INTEGRITY_ERROR: DifficultyState not found for concept $conceptId")
}

class GetThresholdDifficultyUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(): Int =
        repository.getInt(
            FlashLearnSettingsKeys.THRESHOLD_DIFFICULTY,
            FlashLearnSettingsKeys.DEFAULT_THRESHOLD_DIFFICULTY
        )
}

class GetProgressSummaryUseCase @Inject constructor(
    private val conceptRepository: ConceptRepository,
    private val learningStateRepository: LearningStateRepository
) {
    suspend operator fun invoke(now: Instant): ProgressSummary {
        val concepts = conceptRepository.getAllActive()
        val statesById = learningStateRepository.getAll().associateBy { it.conceptId }

        // Summary screens used to query LearningState once per concept. That is an N+1
        // pattern and becomes visibly slow after a large backup restore. Join in memory.
        var learned = 0
        var due = 0
        var dailyDue = 0
        var weeklyDue = 0
        var monthlyDue = 0
        var correct = 0
        var wrong = 0

        concepts.forEach { concept ->
            val state = statesById[concept.id]
                ?: error("DATA_INTEGRITY_ERROR: LearningState not found for concept ${concept.id}")

            if (state.stage == Stage.LEARNED) learned++
            if (state.stage != Stage.LEARNED && (state.nextReviewAt == null || state.nextReviewAt <= now)) {
                due++
                when (state.stage) {
                    Stage.DAILY -> dailyDue++
                    Stage.WEEKLY -> weeklyDue++
                    Stage.MONTHLY -> monthlyDue++
                    Stage.LEARNED -> Unit
                }
            }
            correct += state.totalCorrect
            wrong += state.totalWrong
        }

        val total = correct + wrong
        val accuracy = if (total == 0) 0 else ((correct.toDouble() / total) * 100.0).toInt()

        return ProgressSummary(
            activeConceptCount = concepts.size,
            learnedConceptCount = learned,
            dueConceptCount = due,
            dailyDueConceptCount = dailyDue,
            weeklyDueConceptCount = weeklyDue,
            monthlyDueConceptCount = monthlyDue,
            totalCorrect = correct,
            totalWrong = wrong,
            accuracyPercent = accuracy
        )
    }
}
