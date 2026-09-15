package com.flashlearn.domain.usecase

import com.flashlearn.domain.model.*
import com.flashlearn.domain.repository.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.util.UUID

class RefreshDataUseCaseTest {
    @Test
    fun contentVersionMigrationRepairsCanonicalKeysAndReachesCurrentVersion() = runBlocking {
        val id = UUID.randomUUID(); val conceptId = UUID.randomUUID()
        val content = Content(id, conceptId, "es", "  Hola   Mundo ", "stale", null, null, null)
        val versions = FakeVersions(concept = 0, content = 0)
        val concepts = object : ConceptRepository {
            override suspend fun insert(concept: Concept) = concept.id
            override suspend fun get(conceptId: UUID): Concept? = null
            override suspend fun getAllActive() = emptyList<Concept>()
            override suspend fun searchActive(query: String) = emptyList<Concept>()
            override suspend fun update(concept: Concept) {}
            override suspend fun softDelete(conceptId: UUID, now: Instant) {}
        }
        val repo = object : ContentRepository {
            var value = listOf(content)
            override suspend fun findByUuid(uuid: UUID) = value.firstOrNull { it.id == uuid }
            override suspend fun find(conceptId: UUID, languageCode: String) = value.firstOrNull { it.conceptId == conceptId && it.languageCode == languageCode }
            override suspend fun upsert(content: Content) { value = value.map { if (it.id == content.id) content else it } }
            override suspend fun insertTranslation(content: Content) { value = value + content }
            override suspend fun getAll() = value
        }
        val useCase = RefreshDataUseCase(versions, concepts, repo, object : FlashLearnDatabase {
            override suspend fun <T> withTransaction(block: suspend () -> T): T = block()
        })
        val result = useCase()
        assertEquals(2, result.conceptTo)
        assertEquals(3, result.contentTo)
        assertEquals("hola mundo", repo.value.single().canonicalKey)
        assertEquals(2, versions.concept)
        assertEquals(3, versions.content)
    }

    @Test
    fun mergedTranslationsAreSplitIntoSeparateRows() = runBlocking {
        val conceptId = UUID.randomUUID()
        val first = Content(UUID.randomUUID(), conceptId, "fa", "کشیش / درمان", "کشیش / درمان")
        val versions = FakeVersions(concept = 2, content = 2)
        val concepts = object : ConceptRepository {
            override suspend fun insert(concept: Concept) = concept.id
            override suspend fun get(conceptId: UUID): Concept? = null
            override suspend fun getAllActive() = emptyList<Concept>()
            override suspend fun searchActive(query: String) = emptyList<Concept>()
            override suspend fun update(concept: Concept) {}
            override suspend fun softDelete(conceptId: UUID, now: Instant) {}
        }
        val repo = object : ContentRepository {
            val values = mutableListOf(first)
            override suspend fun findByUuid(uuid: UUID) = values.firstOrNull { it.id == uuid }
            override suspend fun find(conceptId: UUID, languageCode: String) = values.firstOrNull { it.conceptId == conceptId && it.languageCode == languageCode }
            override suspend fun upsert(content: Content) { values.removeAll { it.id == content.id }; values += content }
            override suspend fun insertTranslation(content: Content) { values += content }
            override suspend fun getAll() = values.toList()
        }
        RefreshDataUseCase(versions, concepts, repo, object : FlashLearnDatabase {
            override suspend fun <T> withTransaction(block: suspend () -> T): T = block()
        })()
        assertEquals(listOf("کشیش", "درمان"), repo.values.sortedBy { it.translationIndex }.map { it.text })
    }

    private data class FakeVersions(var concept: Int, var content: Int) : DataVersionRepository {
        override suspend fun getConceptDataVersion() = concept
        override suspend fun getContentDataVersion() = content
        override suspend fun setConceptDataVersion(version: Int) { concept = version }
        override suspend fun setContentDataVersion(version: Int) { content = version }
    }
}
