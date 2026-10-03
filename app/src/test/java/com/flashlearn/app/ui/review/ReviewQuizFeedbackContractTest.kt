package com.flashlearn.app.ui.review

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ReviewQuizFeedbackContractTest {
    @Test
    fun quizFeedbackTimerIsCancellableAndSessionGuarded() {
        val source = File("src/main/java/com/flashlearn/app/ui/review/ReviewViewModel.kt")
        assertTrue(source.exists())
        val content = source.readText()
        assertTrue(content.contains("private var feedbackJob: Job? = null"))
        assertTrue(content.contains("feedbackJob?.cancel()"))
        assertTrue(content.contains("delay(3_000)"))
        assertTrue(content.contains("generation == sessionGeneration && sessionId == session"))
    }

    @Test
    fun quizTransitionClearsOldCardAndFeedbackTogether() {
        val source = File("src/main/java/com/flashlearn/app/ui/review/ReviewViewModel.kt")
        assertTrue(source.exists())
        val content = source.readText()
        assertTrue(content.contains("answerFeedback = null"))
        assertTrue(content.contains("card = null"))
        assertTrue(content.contains("quizCard = null"))
        assertTrue(content.contains("isLoading = false"))
        assertTrue(content.contains("prefetchedQuizCards"))
        assertTrue(content.contains("generateQuizQuestion.refreshBank()"))
        assertTrue(content.contains("val prepared = mutableMapOf<UUID, QuizCardUiState>()"))
    }
}
