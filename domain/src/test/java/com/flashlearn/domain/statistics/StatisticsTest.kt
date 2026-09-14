package com.flashlearn.domain.statistics

import com.flashlearn.domain.model.*
import org.junit.Assert.assertEquals
import org.junit.Test
import com.flashlearn.domain.repository.ReviewHistoryRepository
import kotlinx.coroutines.runBlocking
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

class StatisticsTest {
    private fun r(day: String, ok: Boolean) = ReviewHistory(
        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
        Instant.parse("${day}T10:00:00Z"), ok, ReviewType.DAILY
    )

    @Test fun streak_counts_calendar_days_not_review_count() {
        val records = listOf(r("2026-09-08", true), r("2026-09-09", false), r("2026-09-09", true))
        val result = CalculateStreakUseCase().calculate(
            records, Instant.parse("2026-09-09T23:00:00Z"), ZoneOffset.UTC
        )
        assertEquals(2, result.currentStreakDays)
        assertEquals(2, result.longestStreakDays)
    }

    @Test fun streak_break_resets_current_but_preserves_longest() {
        val records = listOf(r("2026-09-05", true), r("2026-09-06", true), r("2026-09-08", true))
        val result = CalculateStreakUseCase().calculate(
            records, Instant.parse("2026-09-08T23:00:00Z"), ZoneOffset.UTC
        )
        assertEquals(1, result.currentStreakDays)
        assertEquals(2, result.longestStreakDays)
    }

    @Test
    fun statistics_emptyHistory_reportsZeroAccuracyAndZeroReviewedConcepts() = runBlocking {
        val repository = object : ReviewHistoryRepository {
            override suspend fun insert(entry: ReviewHistory) {}
            override suspend fun existsByAttemptId(sessionId: UUID, reviewAttemptId: UUID) = false
            override suspend fun getAll() = emptyList<ReviewHistory>()
        }
        val result = CalculateStatisticsUseCase(repository)()
        assertEquals(0, result.totalReviews)
        assertEquals(0, result.totalCorrect)
        assertEquals(0, result.totalWrong)
        assertEquals(0, result.accuracyPercent)
        assertEquals(0, result.reviewedConceptCount)
    }

    @Test
    fun statistics_counts_distinct_reviewed_concepts_not_reviewAttempts() = runBlocking {
        val conceptA = UUID.randomUUID()
        val conceptB = UUID.randomUUID()
        val records = listOf(
            ReviewHistory(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), conceptA, Instant.parse("2026-09-12T10:00:00Z"), true, ReviewType.DAILY),
            ReviewHistory(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), conceptA, Instant.parse("2026-09-12T11:00:00Z"), false, ReviewType.DAILY),
            ReviewHistory(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), conceptB, Instant.parse("2026-09-12T12:00:00Z"), true, ReviewType.WEEKLY)
        )
        val repository = object : ReviewHistoryRepository {
            override suspend fun insert(entry: ReviewHistory) {}
            override suspend fun existsByAttemptId(sessionId: UUID, reviewAttemptId: UUID) = false
            override suspend fun getAll() = records
        }
        val result = CalculateStatisticsUseCase(repository)()
        assertEquals(3, result.totalReviews)
        assertEquals(2, result.totalCorrect)
        assertEquals(1, result.totalWrong)
        assertEquals(66, result.accuracyPercent)
        assertEquals(2, result.reviewedConceptCount)
    }
}
