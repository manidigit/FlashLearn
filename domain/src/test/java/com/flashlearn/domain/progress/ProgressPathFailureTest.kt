package com.flashlearn.domain.progress

import com.flashlearn.domain.model.*
import com.flashlearn.domain.repository.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.util.UUID

class ProgressPathFailureTest {
    private val t = Instant.parse("2026-10-02T10:00:00Z")

    @Test
    fun pathFailureCount_onlyCountsActiveConcepts() = runBlocking {
        val active = (1..3).map { Concept(UUID.randomUUID(), EntryType.WORD, null, false, true, t, t) }
        val inactiveId = UUID.randomUUID()
        val states = listOf(
            LearningState(UUID.randomUUID(), active[0].id, Stage.DAILY, t, 0, true, 0, 0, null),
            LearningState(UUID.randomUUID(), active[1].id, Stage.DAILY, t, 0, false, 0, 0, null),
            LearningState(UUID.randomUUID(), active[2].id, Stage.WEEKLY, t, 0, true, 0, 0, null),
            LearningState(UUID.randomUUID(), inactiveId, Stage.DAILY, t, 0, true, 0, 0, null)
        )
        val concepts = object : ConceptRepository {
            override suspend fun insert(concept: Concept) = concept.id
            override suspend fun get(conceptId: UUID) = null
            override suspend fun getAllActive() = active
            override suspend fun searchActive(query: String) = emptyList<Concept>()
            override suspend fun update(concept: Concept) {}
            override suspend fun softDelete(conceptId: UUID, now: Instant) {}
        }
        val learning = object : LearningStateRepository {
            override suspend fun get(conceptId: UUID) = null
            override suspend fun upsert(state: LearningState) {}
            override suspend fun getAllByStage(stage: Stage) = states.filter { it.stage == stage }
            override suspend fun getDueNonLearned(now: Instant) = states
            override suspend fun getAll() = states
        }
        val difficulty = object : DifficultyStateRepository {
            override suspend fun get(conceptId: UUID) = null
            override suspend fun upsert(state: DifficultyState) {}
            override suspend fun delete(conceptId: UUID) {}
            override suspend fun getAll() = emptyList<DifficultyState>()
        }
        val snapshot = CalculateProgressUseCase(concepts, learning, difficulty)(t)
        assertEquals(2, snapshot.pathFailureConcepts)
        assertEquals(3, snapshot.totalConcepts)
    }
}
