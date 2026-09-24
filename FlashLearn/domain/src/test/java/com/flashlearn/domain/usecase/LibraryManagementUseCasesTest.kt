package com.flashlearn.domain.usecase

import com.flashlearn.domain.model.*
import com.flashlearn.domain.repository.*
import kotlinx.coroutines.runBlocking
import java.time.Instant
import java.util.UUID
import org.junit.Test
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Assert.fail

class LibraryManagementUseCasesTest {
    private class MemDb : FlashLearnDatabase { override suspend fun <T> withTransaction(block: suspend () -> T): T = block() }
    private class Concepts : ConceptRepository {
        val data = linkedMapOf<UUID, Concept>()
        override suspend fun insert(concept: Concept)=concept.id.also { data[it]=concept }
        override suspend fun get(conceptId: UUID)=data[conceptId]?.takeIf { it.active }
        override suspend fun getAllActive()=data.values.filter { it.active }
        override suspend fun searchActive(query: String)=getAllActive().filter { it.id.toString().contains(query) }
        override suspend fun update(concept: Concept) { data[concept.id]=concept }
        override suspend fun softDelete(conceptId: UUID, now: Instant) { data[conceptId]=data.getValue(conceptId).copy(active=false, updatedAt=now) }
    }
    private class Contents : ContentRepository {
        val data=mutableListOf<Content>()
        override suspend fun findByUuid(uuid: UUID)=data.find { it.id==uuid }
        override suspend fun find(conceptId: UUID, languageCode: String)=data.find { it.conceptId==conceptId && it.languageCode==languageCode }
        override suspend fun upsert(content: Content) { data.removeAll { it.conceptId==content.conceptId && it.languageCode==content.languageCode }; data.add(content) }
        override suspend fun getAll()=data.toList()
    }
    @Test fun favoriteEditDelete_workTogether() = runBlocking {
        val c=Concepts(); val content=Contents(); val id=UUID.randomUUID(); val now=Instant.now()
        c.insert(Concept(id, EntryType.WORD, null, false, true, now, now))
        content.upsert(Content(UUID.randomUUID(),id,"es","hola","hola")); content.upsert(Content(UUID.randomUUID(),id,"fa","سلام","سلام"))
        val db=MemDb()
        assertTrue(ToggleFavoriteUseCase(c,db)(id)); assertTrue(c.data.getValue(id).favorite)
        UpdateConceptUseCase(c,content,db)(UpdateConceptCommand(id,"adiós","خداحافظ","note"))
        assertEquals("adiós", content.find(id,"es")!!.text); assertEquals("note", content.find(id,"es")!!.notes)
        DeleteConceptUseCase(c,db)(id); assertFalse(c.data.getValue(id).active); assertEquals(null,c.get(id))
    }

    @Test fun updateRejectsDuplicateActiveSourceTargetPair() = runBlocking {
        val c=Concepts(); val content=Contents(); val db=MemDb(); val now=Instant.now()
        val first=UUID.randomUUID(); val second=UUID.randomUUID()
        c.insert(Concept(first, EntryType.WORD, null, false, true, now, now))
        c.insert(Concept(second, EntryType.WORD, null, false, true, now, now))
        content.upsert(Content(UUID.randomUUID(),first,"es","hola","hola")); content.upsert(Content(UUID.randomUUID(),first,"fa","سلام","سلام"))
        content.upsert(Content(UUID.randomUUID(),second,"es","adios","adios")); content.upsert(Content(UUID.randomUUID(),second,"fa","خداحافظ","خداحافظ"))
        try {
            UpdateConceptUseCase(c,content,db)(UpdateConceptCommand(second,"hola","سلام"))
            fail("Expected DuplicateConceptException")
        } catch (_: DuplicateConceptException) {
            assertEquals("adios", content.find(second,"es")!!.text)
        }
    }

}
