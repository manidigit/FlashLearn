package com.flashlearn.domain.usecase

import com.flashlearn.domain.model.*
import com.flashlearn.domain.repository.ConceptRepository
import com.flashlearn.domain.repository.LearningStateRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.util.UUID

class ProgressSummaryUseCaseTest {
    private val now = Instant.parse("2026-09-12T10:00:00Z")

    private class Concepts(private val values: List<Concept>) : ConceptRepository {
        override suspend fun insert(concept: Concept) = concept.id
        override suspend fun get(conceptId: UUID) = values.find { it.id == conceptId }
        override suspend fun getAllActive() = values.filter { it.active }
        override suspend fun searchActive(query: String) = getAllActive()
        override suspend fun update(concept: Concept) {}
        override suspend fun softDelete(conceptId: UUID, now: Instant) {}
    }

    private class Learning(private val values: List<LearningState>) : LearningStateRepository {
        override suspend fun get(conceptId: UUID) = values.find { it.conceptId == conceptId }
        override suspend fun upsert(state: LearningState) {}
        override suspend fun getAllByStage(stage: Stage) = values.filter { it.stage == stage }
        override suspend fun getDueNonLearned(now: Instant) = values.filter {
            it.stage != Stage.LEARNED && (it.nextReviewAt?.let { next -> next <= now } ?: true)
        }
        override suspend fun getAll() = values
    }

    private fun concept(id: UUID, active: Boolean = true) =
        Concept(id, EntryType.WORD, null, false, active, now, now)

    private fun state(
        conceptId: UUID,
        stage: Stage,
        nextReviewAt: Instant?,
        correct: Int = 0,
        wrong: Int = 0
    ) = LearningState(UUID.randomUUID(), conceptId, stage, nextReviewAt, 0, false, correct, wrong, null)

    @Test
    fun summary_counts_due_by_stage_and_ignores_inactive_concepts() = runBlocking {
        val dailyDue = UUID.randomUUID()
        val weeklyFuture = UUID.randomUUID()
        val monthlyDue = UUID.randomUUID()
        val learned = UUID.randomUUID()
        val inactive = UUID.randomUUID()
        val concepts = Concepts(listOf(
            concept(dailyDue), concept(weeklyFuture), concept(monthlyDue),
            concept(learned), concept(inactive, active = false)
        ))
        val learning = Learning(listOf(
            state(dailyDue, Stage.DAILY, null, correct = 2, wrong = 1),
            state(weeklyFuture, Stage.WEEKLY, now.plusSeconds(60), correct = 1),
            state(monthlyDue, Stage.MONTHLY, now.minusSeconds(60), wrong = 2),
            state(learned, Stage.LEARNED, now.minusSeconds(60), correct = 4),
            state(inactive, Stage.DAILY, now.minusSeconds(60), wrong = 9)
        ))

        val result = GetProgressSummaryUseCase(concepts, learning)(now)

        assertEquals(4, result.activeConceptCount)
        assertEquals(1, result.learnedConceptCount)
        assertEquals(2, result.dueConceptCount)
        assertEquals(1, result.dailyDueConceptCount)
        assertEquals(0, result.weeklyDueConceptCount)
        assertEquals(1, result.monthlyDueConceptCount)
        assertEquals(7, result.totalCorrect)
        assertEquals(3, result.totalWrong)
        assertEquals(70, result.accuracyPercent)
    }

    @Test
    fun summary_with_no_reviews_reports_zero_accuracy() = runBlocking {
        val id = UUID.randomUUID()
        val result = GetProgressSummaryUseCase(
            Concepts(listOf(concept(id))),
            Learning(listOf(state(id, Stage.DAILY, null)))
        )(now)

        assertEquals(1, result.activeConceptCount)
        assertEquals(1, result.dueConceptCount)
        assertEquals(0, result.totalCorrect)
        assertEquals(0, result.totalWrong)
        assertEquals(0, result.accuracyPercent)
    }
}
