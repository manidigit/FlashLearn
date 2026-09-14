package com.flashlearn.domain.progress

import com.flashlearn.domain.model.*
import com.flashlearn.domain.repository.ConceptRepository
import com.flashlearn.domain.repository.DifficultyStateRepository
import com.flashlearn.domain.repository.LearningStateRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.util.UUID

class ProgressUseCaseTest {
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
        override suspend fun getDueNonLearned(now: Instant) = values
        override suspend fun getAll() = values
    }
    private class Difficulty(private val values: List<DifficultyState>) : DifficultyStateRepository {
        override suspend fun get(conceptId: UUID) = values.find { it.conceptId == conceptId }
        override suspend fun upsert(state: DifficultyState) {}
        override suspend fun delete(conceptId: UUID) {}
        override suspend fun getAll() = values
    }
    private fun concept(id: UUID) = Concept(id, EntryType.WORD, null, false, true, now, now)
    private fun state(id: UUID, stage: Stage, failed: Boolean = false) = LearningState(UUID.randomUUID(), id, stage, null, 0, failed, 0, 0, null)
    private fun difficulty(id: UUID, veryHard: Boolean) = DifficultyState(UUID.randomUUID(), id, VocabularyDifficulty.EASY, 0, 0, veryHard)

    @Test
    fun progress_counts_each_stage_and_flags_from_active_concepts_only() = runBlocking {
        val daily = UUID.randomUUID(); val weekly = UUID.randomUUID(); val monthly = UUID.randomUUID(); val learned = UUID.randomUUID()
        val concepts = Concepts(listOf(concept(daily), concept(weekly), concept(monthly), concept(learned)))
        val learning = Learning(listOf(state(daily, Stage.DAILY, true), state(weekly, Stage.WEEKLY), state(monthly, Stage.MONTHLY), state(learned, Stage.LEARNED, true)))
        val difficulty = Difficulty(listOf(difficulty(daily, false), difficulty(weekly, true), difficulty(monthly, false), difficulty(learned, true)))
        val result = CalculateProgressUseCase(concepts, learning, difficulty)(now)
        assertEquals(4, result.totalConcepts)
        assertEquals(1, result.dailyConcepts)
        assertEquals(1, result.weeklyConcepts)
        assertEquals(1, result.monthlyConcepts)
        assertEquals(1, result.learnedConcepts)
        assertEquals(2, result.pathFailureConcepts)
        assertEquals(2, result.veryHardConcepts)
    }
}
