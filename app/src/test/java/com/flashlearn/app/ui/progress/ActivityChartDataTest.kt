package com.flashlearn.app.ui.progress

import com.flashlearn.domain.model.ReviewHistory
import com.flashlearn.domain.model.ReviewType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.UUID
import org.junit.Assert.assertEquals
import org.junit.Test

class ActivityChartDataTest {

    @Test
    fun weeklyActivity_isChronologicalFromOldestDayToToday() {
        val zone = ZoneOffset.UTC
        val today = LocalDate.of(2026, 10, 2)
        val history = (0..6).map { offset ->
            ReviewHistory(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                today.minusDays(offset.toLong()).atStartOfDay().toInstant(zone),
                true,
                ReviewType.DAILY
            )
        }

        val result = buildActivityData(history, today, zone, ActivityRange.WEEK)

        assertEquals(
            listOf("شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه"),
            result.map { it.label }
        )
        assertEquals(List(7) { 1 }, result.map { it.total })
    }
}
