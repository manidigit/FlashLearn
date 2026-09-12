package com.flashlearn.domain.usecase

import com.flashlearn.domain.model.*
import com.flashlearn.domain.repository.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.time.Instant
import java.util.UUID

class CreateConceptDuplicateTest {
    @Test fun duplicateSourceAndTargetIsRejected() = runBlocking {
        val concepts = FakeConceptRepo()
        val contents = FakeContentRepo()
        val learning = FakeLearningRepo()
        val difficulty = FakeDifficultyRepo()
        val tags = FakeTagRepo()
        val useCase = CreateConceptUseCase(concepts, contents, learning, difficulty, tags, FakeDb())
        useCase(CreateConceptCommand(" Hola ", "سلام"))

        val error = assertThrows(DuplicateConceptException::class.java) {
            runBlocking { useCase(CreateConceptCommand("hola", "سلام")) }
        }
        assertEquals("این واژه با همین ترجمه قبلاً در کتابخانه وجود دارد", error.message)
    }

    @Test fun sameSourceAndTargetCanBeReaddedAfterSoftDelete() = runBlocking {
        val concepts = FakeConceptRepo(); val contents = FakeContentRepo()
        val useCase = CreateConceptUseCase(concepts, contents, FakeLearningRepo(), FakeDifficultyRepo(), FakeTagRepo(), FakeDb())
        val firstId = useCase(CreateConceptCommand("hola", "سلام"))
        concepts.softDelete(firstId, Instant.parse("2026-09-12T10:00:00Z"))

        val secondId = useCase(CreateConceptCommand(" hola ", " سلام "))
        assertEquals(2, concepts.items.size)
        assertEquals(false, concepts.items.first { it.id == firstId }.active)
        assertEquals(true, concepts.items.first { it.id == secondId }.active)
    }

    @Test fun sameSourceWithDifferentTargetIsAllowed() = runBlocking {
        val concepts = FakeConceptRepo(); val contents = FakeContentRepo()
        val useCase = CreateConceptUseCase(concepts, contents, FakeLearningRepo(), FakeDifficultyRepo(), FakeTagRepo(), FakeDb())
        useCase(CreateConceptCommand("hola", "سلام"))
        useCase(CreateConceptCommand("hola", "درود"))
        assertEquals(2, concepts.items.size)
    }

    private class FakeDb : FlashLearnDatabase { override suspend fun <T> withTransaction(block: suspend () -> T): T = block() }
    private class FakeConceptRepo : ConceptRepository {
        val items = mutableListOf<Concept>()
        override suspend fun insert(concept: Concept): UUID { items += concept; return concept.id }
        override suspend fun get(conceptId: UUID) = items.find { it.id == conceptId }
        override suspend fun getAllActive() = items.filter { it.active }
        override suspend fun searchActive(query: String) = getAllActive()
        override suspend fun update(concept: Concept) { items[items.indexOfFirst { it.id == concept.id }] = concept }
        override suspend fun softDelete(conceptId: UUID, now: Instant) {
            val index = items.indexOfFirst { it.id == conceptId }
            if (index >= 0) items[index] = items[index].copy(active = false, updatedAt = now)
        }
    }
    private class FakeContentRepo : ContentRepository {
        val items = mutableListOf<Content>()
        override suspend fun findByUuid(uuid: UUID) = items.find { it.id == uuid }
        override suspend fun find(conceptId: UUID, languageCode: String) = items.find { it.conceptId == conceptId && it.languageCode == languageCode }
        override suspend fun upsert(content: Content) { items.removeIf { it.conceptId == content.conceptId && it.languageCode == content.languageCode }; items += content }
        override suspend fun getAll() = items.toList()
    }
    private class FakeLearningRepo : LearningStateRepository {
        override suspend fun get(conceptId: UUID) = null
        override suspend fun upsert(state: LearningState) {}
        override suspend fun getAllByStage(stage: Stage) = emptyList<LearningState>()
        override suspend fun getDueNonLearned(now: Instant) = emptyList<LearningState>()
        override suspend fun getAll() = emptyList<LearningState>()
    }
    private class FakeDifficultyRepo : DifficultyStateRepository {
        override suspend fun get(conceptId: UUID) = null
        override suspend fun upsert(state: DifficultyState) {}
        override suspend fun delete(conceptId: UUID) {}
        override suspend fun getAll() = emptyList<DifficultyState>()
    }
    private class FakeTagRepo : ConceptTagRepository {
        override suspend fun insert(conceptTag: ConceptTag) {}
        override suspend fun getTagsForConcept(conceptId: UUID): List<UUID> = emptyList()
        override suspend fun getConceptsForTag(tagId: UUID) = emptyList<UUID>()
    }
}
