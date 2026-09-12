package com.flashlearn.domain.statistics

import com.flashlearn.domain.model.ReviewHistory
import com.flashlearn.domain.repository.ReviewHistoryRepository
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

data class StatisticsSnapshot(
    val totalReviews: Int,
    val totalCorrect: Int,
    val totalWrong: Int,
    val accuracyPercent: Int,
    val reviewedConceptCount: Int
)

data class StreakSnapshot(
    val currentStreakDays: Int,
    val longestStreakDays: Int,
    val activeDates: Set<java.time.LocalDate>
)

class CalculateStatisticsUseCase @Inject constructor(
    private val repository: ReviewHistoryRepository
) {
    suspend operator fun invoke(): StatisticsSnapshot {
        val records = repository.getAll()
        val total = records.size
        val correct = records.count { it.isCorrect }
        return StatisticsSnapshot(
            totalReviews = total,
            totalCorrect = correct,
            totalWrong = total - correct,
            accuracyPercent = if (total == 0) 0 else correct * 100 / total,
            reviewedConceptCount = records.map { it.conceptId }.distinct().size
        )
    }
}

class CalculateStreakUseCase @Inject constructor() {
    fun calculate(records: List<ReviewHistory>, now: Instant, zoneId: ZoneId): StreakSnapshot {
        val dates = records.map { it.reviewedAt.atZone(zoneId).toLocalDate() }.toSet()
        if (dates.isEmpty()) return StreakSnapshot(0, 0, emptySet())
        val sorted = dates.sorted()
        var longest = 1
        var run = 1
        for (i in 1 until sorted.size) {
            run = if (sorted[i] == sorted[i - 1].plusDays(1)) run + 1 else 1
            if (run > longest) longest = run
        }
        val today = now.atZone(zoneId).toLocalDate()
        var current = 0
        var cursor = today
        while (cursor in dates) {
            current++
            cursor = cursor.minusDays(1)
        }
        return StreakSnapshot(current, longest, dates)
    }
}
