package com.flashlearn.domain.statistics

import com.flashlearn.domain.model.*
import com.flashlearn.domain.repository.ConceptRepository
import com.flashlearn.domain.repository.LearningStateRepository
import com.flashlearn.domain.repository.ReviewHistoryRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

class StatisticsTest {
    private fun r(day: String, ok: Boolean, conceptId: UUID = UUID.randomUUID()) = ReviewHistory(
        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), conceptId,
        Instant.parse("${day}T10:00:00Z"), ok, ReviewType.DAILY
    )

    private class Concepts(private val activeIds: Set<UUID>) : ConceptRepository {
        override suspend fun insert(concept: Concept) = concept.id
        override suspend fun get(conceptId: UUID) = null
        override suspend fun getAllActive() = activeIds.map {
            Concept(it, EntryType.WORD, null, false, true, Instant.EPOCH, Instant.EPOCH)
        }
        override suspend fun searchActive(query: String) = getAllActive()
        override suspend fun update(concept: Concept) = Unit
        override suspend fun softDelete(conceptId: UUID, now: Instant) = Unit
    }

    private class Learning(private val states: List<LearningState>) : LearningStateRepository {
        override suspend fun get(conceptId: UUID) = states.firstOrNull { it.conceptId == conceptId }
        override suspend fun upsert(state: LearningState) = Unit
        override suspend fun getAllByStage(stage: Stage) = states.filter { it.stage == stage }
        override suspend fun getDueNonLearned(now: Instant) = states.filter { it.stage != Stage.LEARNED }
        override suspend fun getAll() = states
    }

    private class EmptyHistory : ReviewHistoryRepository {
        override suspend fun insert(entry: ReviewHistory) = Unit
        override suspend fun existsByAttemptId(sessionId: UUID, reviewAttemptId: UUID) = false
        override suspend fun getAll() = emptyList<ReviewHistory>()
    }

    @Test
    fun basicStatistics_reconcilesActiveTotalWithPracticedUnpracticedAndLearned() = runBlocking {
        val activeIds = (1..4).map { UUID.randomUUID() }
        val inactiveId = UUID.randomUUID()
        val now = Instant.parse("2026-09-30T10:00:00Z")
        fun state(id: UUID, stage: Stage, reviewed: Boolean) = LearningState(
            UUID.randomUUID(), id, stage, null, 0, false, 0, 0,
            if (reviewed) now else null
        )

        val states = listOf(
            state(activeIds[0], Stage.LEARNED, true),
            state(activeIds[1], Stage.WEEKLY, true),
            state(activeIds[2], Stage.DAILY, false),
            state(activeIds[3], Stage.DAILY, false),
            state(inactiveId, Stage.LEARNED, true)
        )

        val result = GetBasicStatistics(
            Concepts(activeIds.toSet()),
            EmptyHistory(),
            Learning(states)
        )()

        assertEquals(4, result.totalActiveWords)
        assertEquals(1, result.practicedWords)
        assertEquals(2, result.unpracticedWords)
        assertEquals(1, result.learnedWords)
        assertEquals(
            result.totalActiveWords,
            result.practicedWords + result.unpracticedWords + result.learnedWords
        )
    }

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
        val result = CalculateStatisticsUseCase(repository, Concepts(emptySet()))()
        assertEquals(0, result.totalReviews)
        assertEquals(0, result.totalCorrect)
        assertEquals(0, result.totalWrong)
        assertEquals(0, result.accuracyPercent)
        assertEquals(0, result.reviewedConceptCount)
    }

    @Test
    fun statistics_counts_only_active_distinct_reviewed_concepts() = runBlocking {
        val active = UUID.randomUUID()
        val inactive = UUID.randomUUID()
        val records = listOf(
            r("2026-09-12", true, active),
            r("2026-09-12", false, active),
            r("2026-09-12", true, inactive)
        )
        val repository = object : ReviewHistoryRepository {
            override suspend fun insert(entry: ReviewHistory) {}
            override suspend fun existsByAttemptId(sessionId: UUID, reviewAttemptId: UUID) = false
            override suspend fun getAll() = records
        }
        val result = CalculateStatisticsUseCase(repository, Concepts(setOf(active)))()
        assertEquals(3, result.totalReviews)
        assertEquals(2, result.totalCorrect)
        assertEquals(1, result.totalWrong)
        assertEquals(66, result.accuracyPercent)
        assertEquals(1, result.reviewedConceptCount)
    }

    @Test
    fun statistics_counts_multiple_active_concepts_not_reviewAttempts() = runBlocking {
        val conceptA = UUID.randomUUID()
        val conceptB = UUID.randomUUID()
        val records = listOf(
            r("2026-09-12", true, conceptA),
            r("2026-09-12", false, conceptA),
            r("2026-09-12", true, conceptB)
        )
        val repository = object : ReviewHistoryRepository {
            override suspend fun insert(entry: ReviewHistory) {}
            override suspend fun existsByAttemptId(sessionId: UUID, reviewAttemptId: UUID) = false
            override suspend fun getAll() = records
        }
        val result = CalculateStatisticsUseCase(repository, Concepts(setOf(conceptA, conceptB)))()
        assertEquals(3, result.totalReviews)
        assertEquals(2, result.totalCorrect)
        assertEquals(1, result.totalWrong)
        assertEquals(66, result.accuracyPercent)
        assertEquals(2, result.reviewedConceptCount)
    }
}
