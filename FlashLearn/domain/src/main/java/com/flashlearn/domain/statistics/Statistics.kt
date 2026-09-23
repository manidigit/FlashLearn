package com.flashlearn.domain.statistics

import com.flashlearn.domain.model.ReviewHistory
import com.flashlearn.domain.repository.ConceptRepository
import com.flashlearn.domain.repository.LearningStateRepository
import com.flashlearn.domain.repository.ReviewHistoryRepository
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

data class BasicStatistics(
    val totalActiveWords: Int,
    val practicedWords: Int,
    val unpracticedWords: Int,
    val learnedWords: Int
)

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

class GetBasicStatistics @Inject constructor(
    private val conceptRepository: ConceptRepository,
    private val reviewHistoryRepository: ReviewHistoryRepository,
    private val learningStateRepository: LearningStateRepository
) {
    suspend operator fun invoke(): BasicStatistics {
        val active = conceptRepository.getAllActive()
        val activeIds = active.map { it.id }.toSet()
        val history = reviewHistoryRepository.getAll().filter { it.conceptId in activeIds }
        val learnedIds = learningStateRepository.getAll()
            .filter { it.conceptId in activeIds && it.stage == com.flashlearn.domain.model.Stage.LEARNED }
            .map { it.conceptId }
            .toSet()
        val practiced = history.map { it.conceptId }.toSet().size
        return BasicStatistics(
            totalActiveWords = active.size,
            practicedWords = practiced,
            unpracticedWords = maxOf(0, active.size - practiced),
            learnedWords = learnedIds.size
        )
    }
}

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
        val sorted = dates.sortedDescending()
        val today = now.atZone(zoneId).toLocalDate()
        val mostRecent = sorted.first()
        if (mostRecent != today && mostRecent != today.minusDays(1)) {
            return StreakSnapshot(0, longestRun(sorted), dates)
        }
        var current = 1
        for (i in 1 until sorted.size) {
            if (sorted[i] == sorted[i - 1].minusDays(1)) current++ else break
        }
        return StreakSnapshot(current, longestRun(sorted), dates)
    }

    private fun longestRun(sortedDescending: List<java.time.LocalDate>): Int {
        if (sortedDescending.isEmpty()) return 0
        var longest = 1
        var run = 1
        for (i in 1 until sortedDescending.size) {
            run = if (sortedDescending[i] == sortedDescending[i - 1].minusDays(1)) run + 1 else 1
            longest = maxOf(longest, run)
        }
        return longest
    }
}
