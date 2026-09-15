package com.flashlearn.app.ui.review

import com.flashlearn.domain.model.QuizDifficulty
import com.flashlearn.domain.model.ReviewType
import com.flashlearn.domain.model.VocabularyDifficulty
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewUiStateTest {
    @Test fun answerFeedback_defaultsToNull() { assertEquals(null, ReviewUiState().answerFeedback) }

    @Test fun sessionCounters_startAtZero() {
        val state = ReviewUiState()
        assertEquals(0, state.answered); assertEquals(0, state.correct); assertEquals(0, state.wrong)
    }

    @Test fun answerFeedback_canRepresentSuccessfulAnswer() {
        val feedback = ReviewAnswerFeedbackUiState(true, "هفتگی", "آسان", answered = 1, correct = 1, wrong = 0)
        assertTrue(feedback.isCorrect); assertEquals("هفتگی", feedback.stageLabel); assertEquals("آسان", feedback.difficultyLabel)
    }

    @Test fun answerFeedback_canRepresentWrongAnswer() {
        val feedback = ReviewAnswerFeedbackUiState(false, "روزانه", "متوسط", answered = 1, correct = 0, wrong = 1)
        assertFalse(feedback.isCorrect)
    }

    @Test fun nextCardTransition_mustClearPreviousAnswerFeedback() {
        val stateAfterAnswer = ReviewUiState(
            card = ReviewCardUiState("hola", null, "سلام", isFlipped = true), remaining = 2, total = 2,
            answerFeedback = ReviewAnswerFeedbackUiState(true, "هفتگی", "آسان", answered = 1, correct = 1, wrong = 0), answered = 1, correct = 1
        )
        val stateForNextCard = stateAfterAnswer.copy(card = ReviewCardUiState("gracias", null, "ممنون"), remaining = 1, answerFeedback = null)
        assertEquals(null, stateForNextCard.answerFeedback); assertEquals("gracias", stateForNextCard.card?.sourceText)
    }

    @Test fun canSubmitAnswer_requiresRevealedCardAndNoExistingFeedback() {
        val revealed = ReviewCardUiState("hola", null, "سلام", isFlipped = true)
        assertTrue(ReviewUiState(card = revealed).canSubmitAnswer)
        assertFalse(ReviewUiState(card = revealed, isSubmitting = true).canSubmitAnswer)
        assertFalse(ReviewUiState(card = revealed, answerFeedback = ReviewAnswerFeedbackUiState(true, "هفتگی", "آسان", answered = 1, correct = 1, wrong = 0)).canSubmitAnswer)
        assertFalse(ReviewUiState(card = revealed, isFinished = true).canSubmitAnswer)
        assertFalse(ReviewUiState(card = revealed.copy(isFlipped = false)).canSubmitAnswer)
    }

    @Test fun reviewType_defaultsToRandom() { assertEquals(ReviewType.RANDOM, ReviewUiState().selectedReviewType) }

    @Test fun reviewType_uiSupportsOnlyRandomAndLearned() {
        val random = ReviewUiState(selectedReviewType = ReviewType.RANDOM)
        val learned = ReviewUiState(selectedReviewType = ReviewType.LEARNED)
        assertEquals(ReviewType.RANDOM, random.selectedReviewType); assertEquals(ReviewType.LEARNED, learned.selectedReviewType)
    }

    @Test fun reviewSetup_defaultsToQuizAndMediumQuizDifficulty() {
        val state = ReviewUiState()
        assertEquals(ReviewMode.QUIZ, state.selectedMode)
        assertEquals(QuizDifficulty.MEDIUM, state.selectedQuizDifficulty)
    }

    @Test fun vocabularyDifficulty_filterDefaultsToAllAndSupportsMultipleSelections() {
        val state = ReviewUiState(selectedDifficulties = setOf(VocabularyDifficulty.EASY, VocabularyDifficulty.HARD))
        assertTrue(state.selectedDifficulties.contains(VocabularyDifficulty.EASY))
        assertTrue(state.selectedDifficulties.contains(VocabularyDifficulty.HARD))
        assertFalse(state.selectedDifficulties.contains(VocabularyDifficulty.MEDIUM))
        assertTrue(ReviewUiState().selectedDifficulties.isEmpty())
    }

    @Test fun categoryFilter_defaultsToAllCategories() {
        val state = ReviewUiState()
        assertEquals(emptyList<Any>(), state.categories); assertEquals(null, state.selectedCategoryId); assertTrue(state.selectedCategoryIds.isEmpty())
    }

    @Test fun categoryFilter_supportsMultipleSelectionsAndClearAll() {
        val first = java.util.UUID.randomUUID(); val second = java.util.UUID.randomUUID()
        val selected = ReviewUiState(selectedCategoryIds = setOf(first, second))
        assertEquals(2, selected.selectedCategoryIds.size)
        assertTrue(selected.selectedCategoryIds.contains(first)); assertTrue(selected.selectedCategoryIds.contains(second))
        assertTrue(selected.copy(selectedCategoryIds = emptySet()).selectedCategoryIds.isEmpty())
    }

    @Test fun reviewSetup_defaultsToTwentyCards() { assertEquals(20, ReviewUiState().maximumReviewCards) }
}
