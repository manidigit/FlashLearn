package com.flashlearn.domain.statistics

data class ReviewRecord(
    val conceptId: String,
    val reviewedAtEpochMillis: Long,
    val isCorrect: Boolean
)

data class StatisticsSnapshot(
    val totalReviews: Int,
    val totalCorrect: Int,
    val totalWrong: Int,
    val accuracyPercent: Int,
    val reviewedConceptCount: Int
)

class CalculateStatisticsUseCase {
    fun calculate(records: List<ReviewRecord>): StatisticsSnapshot {
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
