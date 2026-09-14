package com.flashlearn.domain.usecase

import com.flashlearn.domain.model.Content
import com.flashlearn.domain.repository.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.UUID

class RefreshDataUseCaseTest {
    @Test fun contentVersionMigrationRepairsCanonicalKeys() = runBlocking {
        val id = UUID.randomUUID(); val conceptId = UUID.randomUUID()
        val content = Content(id, conceptId, "es", "  Hola   Mundo ", "stale", null, null, null)
        val versions = FakeVersions()
        val repo = object : ContentRepository {
            var value = listOf(content)
            override suspend fun findByUuid(uuid: UUID) = value.firstOrNull { it.id == uuid }
            override suspend fun find(conceptId: UUID, languageCode: String) = value.firstOrNull { it.conceptId == conceptId && it.languageCode == languageCode }
            override suspend fun upsert(content: Content) { value = value.map { if (it.id == content.id) content else it } }
            override suspend fun getAll() = value
        }
        val useCase = RefreshDataUseCase(versions, repo, object : FlashLearnDatabase {
            override suspend fun <T> withTransaction(block: suspend () -> T): T = block()
        })
        val result = useCase()
        assertEquals(1, result.contentTo)
        assertEquals("hola mundo", repo.value.single().canonicalKey)
        assertEquals(1, versions.content)
    }
    private class FakeVersions : DataVersionRepository {
        var concept = 0; var content = 0
        override suspend fun getConceptDataVersion() = concept
        override suspend fun getContentDataVersion() = content
        override suspend fun setConceptDataVersion(version: Int) { concept = version }
        override suspend fun setContentDataVersion(version: Int) { content = version }
    }
}
