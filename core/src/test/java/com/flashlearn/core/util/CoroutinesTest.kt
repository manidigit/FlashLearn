package com.flashlearn.core.util

import kotlin.coroutines.cancellation.CancellationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class CoroutinesTest {
    @Test
    fun exceptionsBecomeFailure() {
        val result = runCatchingCancellable<Int> { error("boom") }
        assertEquals("boom", result.exceptionOrNull()?.message)
    }

    @Test
    fun cancellationIsRethrown() {
        assertThrows(CancellationException::class.java) {
            runCatchingCancellable<Int> { throw CancellationException("cancelled") }
        }
    }
}
