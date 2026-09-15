package com.flashlearn.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewHelpUseCaseTest {
    private val useCase = ReviewHelpUseCase()

    @Test
    fun hintDoesNotRevealSourceOrAnswer() {
        val hint = useCase.hintFor("hola")
        assertTrue(hint.isNotBlank())
        assertTrue("hola" !in hint.lowercase())
    }

    @Test
    fun hintIsPureAndIndependentOfReviewState() {
        val first = useCase.hintFor("casa")
        val second = useCase.hintFor("casa")
        assertEquals(first, second)
    }

    @Test
    fun noteIsOnlyReturnedWhenPresent() {
        assertNull(useCase.noteFor(null))
        assertNull(useCase.noteFor("   "))
        assertEquals("یادداشت", useCase.noteFor("  یادداشت  "))
    }
}
