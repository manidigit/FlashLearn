package com.flashlearn.domain.usecase

import com.flashlearn.domain.model.*
import com.flashlearn.domain.repository.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.time.Instant
import java.util.UUID

class RefreshDataUseCaseTest {
    @Test
    fun contentVersionMigrationRepairsCanonicalKeysAndReachesCurrentVersion() = runBlocking {
        val id = UUID.randomUUID(); val conceptId = UUID.randomUUID()
        val content = Content(id, conceptId, "es", "  Hola   Mundo ", "stale", null, null, null)
        val versions = FakeVersions(concept = 0, content = 0)
        val concepts = emptyConceptRepository()
        val repo = fakeContentRepository(content)
        val useCase = useCase(versions, concepts, repo)
        val result = useCase()
        assertEquals(2, result.conceptTo)
        assertEquals(3, result.contentTo)
        assertEquals("hola mundo", repo.value.single().canonicalKey)
        assertEquals(2, versions.concept)
        assertEquals(3, versions.content)
    }

    @Test
    fun refreshIsIdempotentAfterAllVersionsAreCurrent() = runBlocking {
        val versions = FakeVersions(concept = 0, content = 0)
        val repo = fakeContentRepository(Content(UUID.randomUUID(), UUID.randomUUID(), "es", "Hola", "stale"))
        val useCase = useCase(versions, emptyConceptRepository(), repo)
        val first = useCase()
        val second = useCase()
        assertEquals(2, first.conceptTo)
        assertEquals(3, first.contentTo)
        assertEquals(0, second.changedContentRows)
        assertEquals(0, second.changedConceptRows)
        assertEquals(2, second.conceptFrom)
        assertEquals(3, second.contentFrom)
    }

    @Test
    fun conceptMigrationRejectsInvalidTimelineWithoutTouchingContent() = runBlocking {
        val now = Instant.now()
        val bad = Concept(UUID.randomUUID(), EntryType.WORD, null, false, true, now, now.minusSeconds(1))
        val versions = FakeVersions(concept = 1, content = 3)
        val repo = fakeContentRepository(Content(UUID.randomUUID(), UUID.randomUUID(), "es", "hola", "hola"))
        val concepts = object : ConceptRepository by emptyConceptRepository() {
            override suspend fun getAllActive() = listOf(bad)
        }
        assertThrows(IllegalArgumentException::class.java) { runBlocking { useCase(versions, concepts, repo)() } }
        assertEquals(1, versions.concept)
        assertEquals(3, versions.content)
        assertEquals("hola", repo.value.single().text)
    }

    @Test
    fun mergedTranslationsAreSplitIntoSeparateRows() = runBlocking {
        val conceptId = UUID.randomUUID()
        val first = Content(UUID.randomUUID(), conceptId, "fa", "کشیش / درمان", "کشیش / درمان")
        val versions = FakeVersions(concept = 2, content = 2)
        val repo = fakeContentRepository(first, mutable = true)
        useCase(versions, emptyConceptRepository(), repo)()
        assertEquals(listOf("کشیش", "درمان"), repo.value.sortedBy { it.translationIndex }.map { it.text })
    }

    private fun useCase(v: FakeVersions, c: ConceptRepository, r: ContentRepository) =
        RefreshDataUseCase(v, c, r, object : FlashLearnDatabase {
            override suspend fun <T> withTransaction(block: suspend () -> T): T = block()
        })

    private fun emptyConceptRepository() = object : ConceptRepository {
        override suspend fun insert(concept: Concept) = concept.id
        override suspend fun get(conceptId: UUID): Concept? = null
        override suspend fun getAllActive() = emptyList<Concept>()
        override suspend fun searchActive(query: String) = emptyList<Concept>()
        override suspend fun update(concept: Concept) {}
        override suspend fun softDelete(conceptId: UUID, now: Instant) {}
    }

    private data class FakeContentRepository(var value: MutableList<Content>) : ContentRepository {
        override suspend fun findByUuid(uuid: UUID) = value.firstOrNull { it.id == uuid }
        override suspend fun find(conceptId: UUID, languageCode: String) = value.firstOrNull { it.conceptId == conceptId && it.languageCode == languageCode }
        override suspend fun upsert(content: Content) { value.removeAll { it.id == content.id }; value += content }
        override suspend fun insertTranslation(content: Content) { value += content }
        override suspend fun getAll() = value.toList()
    }

    private fun fakeContentRepository(content: Content, mutable: Boolean = false) =
        FakeContentRepository(mutableListOf(content))

    private data class FakeVersions(var concept: Int, var content: Int) : DataVersionRepository {
        override suspend fun getConceptDataVersion() = concept
        override suspend fun getContentDataVersion() = content
        override suspend fun setConceptDataVersion(version: Int) { concept = version }
        override suspend fun setContentDataVersion(version: Int) { content = version }
    }
}
