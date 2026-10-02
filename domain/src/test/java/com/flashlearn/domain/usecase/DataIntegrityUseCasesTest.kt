package com.flashlearn.domain.usecase

import com.flashlearn.domain.model.*
import com.flashlearn.domain.repository.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.util.UUID

class DataIntegrityUseCasesTest {
    private class CRepo(private val values: List<Concept>): ConceptRepository {
        override suspend fun insert(concept: Concept)=concept.id
        override suspend fun get(conceptId: UUID)=values.find { it.id == conceptId }
        override suspend fun getAllActive()=values
        override suspend fun searchActive(query: String)=values.filter { it.id.toString().contains(query) }
        override suspend fun update(concept: Concept) {}
        override suspend fun softDelete(conceptId: UUID, now: Instant) {}
    }
    private class LRepo(private val values: List<LearningState>): LearningStateRepository {
        override suspend fun get(conceptId: UUID)=values.find { it.conceptId == conceptId }
        override suspend fun upsert(state: LearningState) {}
        override suspend fun getAllByStage(stage: Stage)=values.filter { it.stage == stage }
        override suspend fun getDueNonLearned(now: Instant)=emptyList<LearningState>()
        override suspend fun getAll()=values
    }
    private class DRepo(private val values: List<DifficultyState>): DifficultyStateRepository {
        override suspend fun get(conceptId: UUID)=values.find { it.conceptId == conceptId }
        override suspend fun upsert(state: DifficultyState) {}
        override suspend fun delete(conceptId: UUID) {}
        override suspend fun getAll()=values
    }
    private class HRepo(private val values: List<ReviewHistory>): ReviewHistoryRepository {
        override suspend fun insert(entry: ReviewHistory) {}
        override suspend fun existsByAttemptId(sessionId: UUID, reviewAttemptId: UUID)=false
        override suspend fun getAll()=values
    }

    @Test fun validConcept_hasValidReport() = runBlocking {
        val id=UUID.randomUUID(); val now=Instant.now()
        val report=ValidateDataIntegrityUseCase(
            CRepo(listOf(Concept(id,EntryType.WORD,null,false,true,now,now))),
            LRepo(listOf(LearningState(UUID.randomUUID(),id,Stage.DAILY,null,0,false,0,0,null))),
            DRepo(listOf(DifficultyState(UUID.randomUUID(),id,VocabularyDifficulty.EASY,0,0,false))),
            HRepo(emptyList())
        )()
        assertTrue(report.valid)
    }
}
