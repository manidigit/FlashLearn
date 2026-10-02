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
    fun hintUsesCategoryBeforeTranslationInitial() {
        assertEquals("دسته‌بندی: سفر", useCase.hintFor("viaje", "سفر", "سفر"))
        assertEquals("حرف اول معنی: س", useCase.hintFor("viaje", "سفر", null))
    }

    @Test
    fun hintFallsBackWhenNoTranslationIsAvailable() {
        assertTrue(useCase.hintFor("casa", null, null).isNotBlank())
    }

    @Test
    fun hintIsPureAndIndependentOfReviewState() {
        val first = useCase.hintFor("casa", "خانه", null)
        val second = useCase.hintFor("casa", "خانه", null)
        assertEquals(first, second)
    }

    @Test
    fun noteIsOnlyReturnedWhenPresent() {
        assertNull(useCase.noteFor(null))
        assertNull(useCase.noteFor("   "))
        assertEquals("یادداشت", useCase.noteFor("  یادداشت  "))
    }
}
