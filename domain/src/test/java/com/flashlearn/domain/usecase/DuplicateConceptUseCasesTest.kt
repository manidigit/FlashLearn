package com.flashlearn.domain.usecase

import com.flashlearn.domain.model.*
import com.flashlearn.domain.repository.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.time.Instant
import java.util.UUID

class DuplicateConceptUseCasesTest {
    private val now = Instant.parse("2026-09-12T10:00:00Z")

    private class Concepts(initial: List<Concept>) : ConceptRepository {
        val values = initial.associateBy { it.id }.toMutableMap()
        override suspend fun insert(concept: Concept): UUID { values[concept.id] = concept; return concept.id }
        override suspend fun get(conceptId: UUID) = values[conceptId]
        override suspend fun getAllActive() = values.values.filter { it.active }
        override suspend fun searchActive(query: String) = getAllActive()
        override suspend fun update(concept: Concept) { values[concept.id] = concept }
        override suspend fun softDelete(conceptId: UUID, now: Instant) { values[conceptId]?.let { values[conceptId] = it.copy(active = false, updatedAt = now) } }
    }

    private class Contents(initial: List<Content>) : ContentRepository {
        val values = initial.toMutableList()
        override suspend fun findByUuid(uuid: UUID) = values.find { it.id == uuid }
        override suspend fun find(conceptId: UUID, languageCode: String) = values.filter { it.conceptId == conceptId && it.languageCode == languageCode }.minByOrNull { it.translationIndex }
        override suspend fun findAll(conceptId: UUID, languageCode: String) = values.filter { it.conceptId == conceptId && it.languageCode == languageCode }.sortedBy { it.translationIndex }
        override suspend fun upsert(content: Content) { values.removeAll { it.id == content.id }; values += content }
        override suspend fun insertTranslation(content: Content) { values += content }
        override suspend fun getAll() = values.toList()
    }

    private class Db : FlashLearnDatabase { override suspend fun <T> withTransaction(block: suspend () -> T): T = block() }

    private fun concept(id: UUID, createdAt: Instant) = Concept(id, EntryType.WORD, null, false, true, createdAt, createdAt)
    private fun content(conceptId: UUID, language: String, text: String, index: Int = 0, key: String = computeCanonicalKey(text)) = Content(UUID.randomUUID(), conceptId, language, text, key, translationIndex = index)

    @Test
    fun exactDuplicateConcepts_keepOne_andPreserveAdditionalMeaning() = runBlocking {
        val first = UUID.randomUUID(); val duplicate = UUID.randomUUID()
        val concepts = Concepts(listOf(concept(first, now.minusSeconds(60)), concept(duplicate, now)))
        val contents = Contents(listOf(
            content(first, "es", "cura"), content(first, "fa", "کشیش"),
            content(duplicate, "es", "cura"), content(duplicate, "fa", "کشیش"), content(duplicate, "fa", "درمان", 1)
        ))
        val removed = RemoveExactDuplicateConceptsUseCase(concepts, contents, Db())("es", "fa")
        assertEquals(1, removed); assertEquals(true, concepts.values[first]?.active); assertFalse(concepts.values[duplicate]?.active ?: true)
        assertEquals(listOf("کشیش", "درمان"), contents.findAll(first, "fa").map { it.text })
    }

    @Test
    fun sameSourceDifferentMeaning_consolidatesIntoOneConcept() = runBlocking {
        val first = UUID.randomUUID(); val second = UUID.randomUUID()
        val concepts = Concepts(listOf(concept(first, now.minusSeconds(60)), concept(second, now)))
        val contents = Contents(listOf(content(first, "es", "cura"), content(first, "fa", "کشیش"), content(second, "es", "cura"), content(second, "fa", "درمان")))
        val removed = RemoveExactDuplicateConceptsUseCase(concepts, contents, Db())("es", "fa")
        assertEquals(1, removed); assertEquals(1, concepts.values.values.count { it.active })
        assertEquals(listOf("کشیش", "درمان"), contents.findAll(first, "fa").map { it.text })
    }

    @Test
    fun staleCanonicalKeys_areIgnored_duringCleanup() = runBlocking {
        val first = UUID.randomUUID(); val second = UUID.randomUUID()
        val concepts = Concepts(listOf(concept(first, now.minusSeconds(60)), concept(second, now)))
        val contents = Contents(listOf(
            content(first, "es", "ellos", key = "old-key"), content(first, "fa", "آن‌ها", key = "old-target"),
            content(second, "es", "  ELLOS  ", key = "another-old-key"), content(second, "fa", " آن‌ها ", key = "another-old-target")
        ))
        val removed = RemoveExactDuplicateConceptsUseCase(concepts, contents, Db())("es", "fa")
        assertEquals(1, removed)
        assertEquals(1, concepts.values.values.count { it.active })
        assertEquals(listOf("آن‌ها"), contents.findAll(first, "fa").map { it.text })
    }
}
