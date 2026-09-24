package com.flashlearn.domain.statistics

import com.flashlearn.domain.model.ReviewHistory
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.ZoneId
import java.util.UUID

class StatisticsAndProgressTest {
    private val zone = ZoneId.of("UTC")
    private val base = Instant.parse("2026-09-15T12:00:00Z")

    @Test
    fun streakAllowsTodayOrYesterdayAndCountsCalendarDaysOnce() {
        val concept = UUID.randomUUID()
        val records = listOf(
            ReviewHistory(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), concept, base.minusSeconds(3600), true, com.flashlearn.domain.model.ReviewType.DAILY),
            ReviewHistory(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), concept, base.minusSeconds(7200), false, com.flashlearn.domain.model.ReviewType.DAILY),
            ReviewHistory(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), concept, base.minusSeconds(86400), true, com.flashlearn.domain.model.ReviewType.DAILY),
            ReviewHistory(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), concept, base.minusSeconds(2 * 86400L), true, com.flashlearn.domain.model.ReviewType.DAILY)
        )
        val result = CalculateStreakUseCase().calculate(records, base, zone)
        assertEquals(3, result.currentStreakDays)
        assertEquals(3, result.longestStreakDays)
    }

    @Test
    fun streakReturnsZeroWhenLatestReviewIsOlderThanYesterday() {
        val r = ReviewHistory(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), base.minusSeconds(3 * 86400L), true, com.flashlearn.domain.model.ReviewType.DAILY)
        assertEquals(0, CalculateStreakUseCase().calculate(listOf(r), base, zone).currentStreakDays)
    }
}
