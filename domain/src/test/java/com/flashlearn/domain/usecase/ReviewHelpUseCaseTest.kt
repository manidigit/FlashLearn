package com.flashlearn.domain.usecase

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ReviewHelpUseCaseTest {
    private val useCase = ReviewHelpUseCase()

    @Test
    fun hintDoesNotRevealSourceOrAnswer() {
        val hint = useCase.hintFor("hola")
        assertTrue(hint.isNotBlank())
        assertNotEquals("hola", hint, ignoreCase = true)
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
