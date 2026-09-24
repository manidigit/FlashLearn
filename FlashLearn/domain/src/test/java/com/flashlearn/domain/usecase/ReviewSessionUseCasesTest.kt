package com.flashlearn.domain.usecase

import com.flashlearn.domain.model.ReviewSession
import com.flashlearn.domain.model.ReviewType
import com.flashlearn.domain.repository.ReviewSessionRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.time.Instant
import java.util.UUID

class ReviewSessionUseCasesTest {
    private class Repo : ReviewSessionRepository {
        val values = linkedMapOf<UUID, ReviewSession>()
        var updateCount = 0
        override suspend fun insert(session: ReviewSession) { values[session.id] = session }
        override suspend fun get(sessionId: UUID) = values[sessionId]
        override suspend fun update(session: ReviewSession) { updateCount++; values[session.id] = session }
    }

    @Test
    fun endSession_isIdempotent() = runBlocking {
        val repo = Repo()
        val start = Instant.parse("2026-09-12T10:00:00Z")
        val end = Instant.parse("2026-09-12T10:05:00Z")
        val sessionId = StartReviewSessionUseCase(repo)(ReviewType.DAILY, start)

        EndReviewSessionUseCase(repo)(sessionId, end)
        EndReviewSessionUseCase(repo)(sessionId, end.plusSeconds(30))

        assertEquals(1, repo.updateCount)
        assertNotNull(repo.values[sessionId]?.endedAt)
        assertEquals(end, repo.values[sessionId]?.endedAt)
    }

    @Test(expected = IllegalArgumentException::class)
    fun endSession_rejectsEndBeforeStart() = runBlocking {
        val repo = Repo()
        val start = Instant.parse("2026-09-12T10:00:00Z")
        val sessionId = StartReviewSessionUseCase(repo)(ReviewType.DAILY, start)
        EndReviewSessionUseCase(repo)(sessionId, start.minusSeconds(1))
    }
}
