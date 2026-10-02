package com.flashlearn.domain.usecase

import com.flashlearn.domain.model.*
import com.flashlearn.domain.repository.*
import java.time.Instant
import java.time.ZoneId
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

class EnsureStatesUseCase @Inject constructor(
    private val conceptRepository: ConceptRepository,
    private val learningStateRepository: LearningStateRepository,
    private val difficultyStateRepository: DifficultyStateRepository
) {
    suspend operator fun invoke(now: Instant = Instant.now()) {
        val concepts = conceptRepository.getAllActive()
        val learningByConcept = learningStateRepository.getAll().associateBy { it.conceptId }
        val difficultyByConcept = difficultyStateRepository.getAll().associateBy { it.conceptId }

        concepts.forEach { concept ->
            if (concept.id !in learningByConcept) {
                learningStateRepository.upsert(
                    LearningState(UUID.randomUUID(), concept.id, Stage.DAILY, now, 0, false, 0, 0, null)
                )
            }
            if (concept.id !in difficultyByConcept) {
                difficultyStateRepository.upsert(
                    DifficultyState(UUID.randomUUID(), concept.id, VocabularyDifficulty.EASY, 0, 0, false)
                )
            }
        }
    }
}

class GetProgressSummaryUseCase @Inject constructor(
    private val conceptRepository: ConceptRepository,
    private val learningStateRepository: LearningStateRepository
) {
    suspend operator fun invoke(now: Instant): ProgressSummary {
        val concepts = conceptRepository.getAllActive()
        val statesById = learningStateRepository.getAll().associateBy { it.conceptId }

        var learned = 0
        var due = 0
        var dailyDue = 0
        var weeklyDue = 0
        var monthlyDue = 0
        var correct = 0
        var wrong = 0

        concepts.forEach { concept ->
            val state = statesById[concept.id] ?: return@forEach

            if (state.stage == Stage.LEARNED) learned++

            val reviewedToday = wasReviewedToday(state.lastReviewedAt, now, ZoneId.systemDefault())
            if (!reviewedToday && state.stage != Stage.LEARNED && (state.nextReviewAt == null || state.nextReviewAt <= now)) {
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

