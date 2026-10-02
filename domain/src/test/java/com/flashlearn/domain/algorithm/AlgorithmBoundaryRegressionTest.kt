package com.flashlearn.domain.algorithm

import com.flashlearn.domain.model.*
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

class AlgorithmBoundaryRegressionTest {
    private val now = Instant.parse("2026-09-10T10:00:00Z")

    private fun state(stage: Stage, nextReviewAt: Instant? = now) =
        LearningState(UUID.randomUUID(), UUID.randomUUID(), stage, nextReviewAt, 0, false, 0, 0, null)

    private fun difficulty(level: VocabularyDifficulty = VocabularyDifficulty.EASY) =
        DifficultyState(UUID.randomUUID(), UUID.randomUUID(), level, 0, 0, false)

    @Test fun dailyWrongSchedulesNextLocalDay() {
        val r = calculateLearningTransition(state(Stage.DAILY), false, now, ZoneOffset.UTC)
        assertEquals(Stage.DAILY, r.newStage)
        assertEquals(Instant.parse("2026-09-11T00:00:00Z"), r.nextReviewAt)
    }

    @Test fun weeklyCorrectMovesToMonthly() {
        val r = calculateLearningTransition(state(Stage.WEEKLY), true, now, ZoneOffset.UTC)
        assertEquals(Stage.MONTHLY, r.newStage)
        assertEquals(Instant.parse("2026-10-10T00:00:00Z"), r.nextReviewAt)
    }

    @Test fun learnedStateIsStableOnBothAnswerOutcomes() {
        val correct = calculateLearningTransition(state(Stage.LEARNED, null), true, now, ZoneOffset.UTC)
        val wrong = calculateLearningTransition(state(Stage.LEARNED, null), false, now, ZoneOffset.UTC)
        assertEquals(Stage.LEARNED, correct.newStage)
        assertEquals(Stage.LEARNED, wrong.newStage)
        assertNull(correct.nextReviewAt)
        assertNull(wrong.nextReviewAt)
    }

    @Test fun veryHardRemainsCappedAfterFurtherWrongAnswers() {
        val s = difficulty(VocabularyDifficulty.VERY_HARD)
        val r = calculateDifficulty(s, false)
        assertEquals(VocabularyDifficulty.VERY_HARD, r.current)
        assertTrue(r.hasReachedVeryHard)
    }

    @Test fun difficultyUsesOnlyConsecutiveAnswersAcrossAllReviewStages() {
        var state = difficulty(VocabularyDifficulty.EASY)
        repeat(2) { state = calculateDifficulty(state, false) }
        assertEquals(VocabularyDifficulty.EASY, state.current)

        state = calculateDifficulty(state, false)
        assertEquals(VocabularyDifficulty.MEDIUM, state.current)

        state = calculateDifficulty(state, true)
        assertEquals(VocabularyDifficulty.MEDIUM, state.current)

        state = calculateDifficulty(state, true)
        state = calculateDifficulty(state, true)
        assertEquals(VocabularyDifficulty.EASY, state.current)
    }

    @Test fun weeklyAndMonthlyWrongDoNotForceDifficultyChanges() {
        val weekly = calculateDifficulty(difficulty(VocabularyDifficulty.EASY), false)
        val monthly = calculateDifficulty(difficulty(VocabularyDifficulty.EASY), false)
        assertEquals(VocabularyDifficulty.EASY, weekly.current)
        assertEquals(VocabularyDifficulty.EASY, monthly.current)
    }
}
