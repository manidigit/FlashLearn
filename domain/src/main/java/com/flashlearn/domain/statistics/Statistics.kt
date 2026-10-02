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

data class ReviewStatisticsAggregate(val totalReviews:Int,val totalCorrect:Int,val totalWrong:Int,val reviewedConceptCount:Int)

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
        val learningStates = learningStateRepository.getAll()
            .filter { it.conceptId in activeIds }

        val learnedIds = learningStates
            .filter { it.stage == com.flashlearn.domain.model.Stage.LEARNED }
            .map { it.conceptId }
            .toSet()

        val practicedIds = learningStates
            .filter {
                it.stage != com.flashlearn.domain.model.Stage.LEARNED &&
                    it.lastReviewedAt != null
            }
            .map { it.conceptId }
            .toSet()

        val practiced = practicedIds.size
        val unpracticed = maxOf(0, active.size - practiced - learnedIds.size)

        return BasicStatistics(
            totalActiveWords = active.size,
            practicedWords = practiced,
            unpracticedWords = unpracticed,
            learnedWords = learnedIds.size
        )
    }
}

class CalculateStatisticsUseCase @Inject constructor(
    private val repository: ReviewHistoryRepository,
    private val conceptRepository: ConceptRepository
) {
    suspend operator fun invoke(): StatisticsSnapshot {
        val activeIds = conceptRepository.getAllActive().asSequence().map { it.id }.toSet()
        return invoke(repository.getStatisticsAggregate(activeIds))
    }
    suspend operator fun invoke(aggregate: ReviewStatisticsAggregate): StatisticsSnapshot =
        StatisticsSnapshot(aggregate.totalReviews,aggregate.totalCorrect,aggregate.totalWrong,
            if (aggregate.totalReviews == 0) 0 else aggregate.totalCorrect * 100 / aggregate.totalReviews,
            aggregate.reviewedConceptCount)
}

class CalculateStreakUseCase @Inject constructor() {
    fun calculate(records: List<ReviewHistory>, now: Instant, zoneId: ZoneId): StreakSnapshot = calculateDates(records.asSequence().map { it.reviewedAt }, now, zoneId)

    fun calculateDates(timestamps: Sequence<Instant>, now: Instant, zoneId: ZoneId): StreakSnapshot {
        val dates = timestamps.map { it.atZone(zoneId).toLocalDate() }.toSet()
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
