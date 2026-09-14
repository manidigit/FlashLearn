package com.flashlearn.app.ui.review

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewUiStateTest {
    @Test
    fun answerFeedback_defaultsToNull() {
        assertEquals(null, ReviewUiState().answerFeedback)
    }

    @Test
    fun sessionCounters_startAtZero() {
        val state = ReviewUiState()
        assertEquals(0, state.answered)
        assertEquals(0, state.correct)
        assertEquals(0, state.wrong)
    }

    @Test
    fun answerFeedback_canRepresentSuccessfulAnswer() {
        val feedback = ReviewAnswerFeedbackUiState(
            isCorrect = true,
            stageLabel = "هفتگی",
            difficultyLabel = "آسان",
            answered = 1,
            correct = 1,
            wrong = 0
        )
        assertTrue(feedback.isCorrect)
        assertEquals("هفتگی", feedback.stageLabel)
        assertEquals("آسان", feedback.difficultyLabel)
    }

    @Test
    fun answerFeedback_canRepresentWrongAnswer() {
        val feedback = ReviewAnswerFeedbackUiState(
            isCorrect = false,
            stageLabel = "روزانه",
            difficultyLabel = "متوسط",
            answered = 1,
            correct = 0,
            wrong = 1
        )
        assertFalse(feedback.isCorrect)
    }
    @Test
    fun nextCardTransition_mustClearPreviousAnswerFeedback() {
        val stateAfterAnswer = ReviewUiState(
            card = ReviewCardUiState(
                sourceText = "hola",
                sourceNotes = null,
                targetText = "سلام",
                isFlipped = true
            ),
            remaining = 2,
            total = 2,
            answerFeedback = ReviewAnswerFeedbackUiState(
                isCorrect = true,
                stageLabel = "هفتگی",
                difficultyLabel = "آسان",
                answered = 1,
                correct = 1,
                wrong = 0
            ),
            answered = 1,
            correct = 1
        )

        val stateForNextCard = stateAfterAnswer.copy(
            card = ReviewCardUiState(
                sourceText = "gracias",
                sourceNotes = null,
                targetText = "ممنون"
            ),
            remaining = 1,
            answerFeedback = null
        )

        assertEquals(null, stateForNextCard.answerFeedback)
        assertEquals("gracias", stateForNextCard.card?.sourceText)
    }

    @Test
    fun canSubmitAnswer_requiresRevealedCardAndNoExistingFeedback() {
        val revealed = ReviewCardUiState(
            sourceText = "hola",
            sourceNotes = null,
            targetText = "سلام",
            isFlipped = true
        )
        assertTrue(ReviewUiState(card = revealed).canSubmitAnswer)
        assertFalse(ReviewUiState(card = revealed, isSubmitting = true).canSubmitAnswer)
        assertFalse(ReviewUiState(card = revealed, answerFeedback = ReviewAnswerFeedbackUiState(
            isCorrect = true, stageLabel = "هفتگی", difficultyLabel = "آسان", answered = 1, correct = 1, wrong = 0
        )).canSubmitAnswer)
        assertFalse(ReviewUiState(card = revealed, isFinished = true).canSubmitAnswer)
        assertFalse(ReviewUiState(card = revealed.copy(isFlipped = false)).canSubmitAnswer)
    }

    @Test
    fun reviewType_defaultsToDaily() {
        assertEquals(com.flashlearn.domain.model.ReviewType.DAILY, ReviewUiState().selectedReviewType)
    }

    @Test
    fun reviewType_canRepresentEverySupportedMode() {
        com.flashlearn.domain.model.ReviewType.entries.forEach { type ->
            assertEquals(type, ReviewUiState(selectedReviewType = type).selectedReviewType)
        }
    }

    @Test
    fun categoryFilter_defaultsToAllCategories() {
        val state = ReviewUiState()
        assertEquals(emptyList<Any>(), state.categories)
        assertEquals(null, state.selectedCategoryId)
    }

    @Test
    fun categoryFilter_canSelectAndClearCategory() {
        val categoryId = java.util.UUID.randomUUID()
        val selected = ReviewUiState(selectedCategoryId = categoryId)
        assertEquals(categoryId, selected.selectedCategoryId)
        val cleared = selected.copy(selectedCategoryId = null)
        assertEquals(null, cleared.selectedCategoryId)
    }

}
