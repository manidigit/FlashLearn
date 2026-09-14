package com.flashlearn.domain.usecase

import com.flashlearn.domain.model.*
import com.flashlearn.domain.parser.*
import com.flashlearn.domain.repository.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.util.UUID

class ImportParsedEntryUseCaseTest {
    private class Db : FlashLearnDatabase {
        override suspend fun <T> withTransaction(block: suspend () -> T): T = block()
    }
    private class Concepts : ConceptRepository {
        val values = linkedMapOf<UUID, Concept>()
        override suspend fun insert(concept: Concept) = concept.id.also { values[it] = concept }
        override suspend fun get(conceptId: UUID) = values[conceptId]?.takeIf { it.active }
        override suspend fun getAllActive() = values.values.filter { it.active }
        override suspend fun searchActive(query: String) = getAllActive()
        override suspend fun update(concept: Concept) { values[concept.id] = concept }
        override suspend fun softDelete(conceptId: UUID, now: Instant) {}
    }
    private class Contents : ContentRepository {
        val values = mutableListOf<Content>()
        override suspend fun findByUuid(uuid: UUID) = values.find { it.id == uuid }
        override suspend fun find(conceptId: UUID, languageCode: String) =
            values.find { it.conceptId == conceptId && it.languageCode == languageCode }
        override suspend fun upsert(content: Content) {
            values.removeAll { it.conceptId == content.conceptId && it.languageCode == content.languageCode }
            values += content
        }
        override suspend fun getAll() = values.toList()
    }
    private class Learning : LearningStateRepository {
        val values = mutableListOf<LearningState>()
        override suspend fun get(conceptId: UUID) = values.find { it.conceptId == conceptId }
        override suspend fun upsert(state: LearningState) { values += state }
        override suspend fun getAllByStage(stage: Stage) = values.filter { it.stage == stage }
        override suspend fun getDueNonLearned(now: Instant) = values
        override suspend fun getAll() = values
    }
    private class Difficulty : DifficultyStateRepository {
        val values = mutableListOf<DifficultyState>()
        override suspend fun get(conceptId: UUID) = values.find { it.conceptId == conceptId }
        override suspend fun upsert(state: DifficultyState) { values += state }
        override suspend fun delete(conceptId: UUID) {}
        override suspend fun getAll() = values
    }
    private class Tags : ConceptTagRepository {
        override suspend fun insert(conceptTag: ConceptTag) {}
        override suspend fun getTagsForConcept(conceptId: UUID) = emptyList<UUID>()
        override suspend fun getConceptsForTag(tagId: UUID) = emptyList<UUID>()
    }
    private class Metadata : ParserMetadataRepository {
        val values = linkedMapOf<UUID, ParserMetadata>()
        override suspend fun get(conceptId: UUID) = values[conceptId]
        override suspend fun upsert(conceptId: UUID, metadata: ParserMetadata) { values[conceptId] = metadata }
        override suspend fun getAll() = values.entries.map { it.key to it.value }
    }

    @Test
    fun import_persists_parser_metadata_with_same_concept() = runBlocking {
        val concepts = Concepts()
        val metadata = Metadata()
        val create = CreateConceptUseCase(concepts, Contents(), Learning(), Difficulty(), Tags(), Db())
        val import = ImportParsedEntryUseCase(create, metadata, Db())
        val entry = ParsedEntry(
            sourceText = "estar listo",
            translationText = "آماده بودن",
            notes = "usage",
            language = DetectedLanguage.MIXED,
            entryType = EntryKind.PHRASE,
            rawLines = listOf("estar listo → آماده بودن"),
            breakdown = listOf(BreakdownPart("estar + listo")),
            relationships = listOf(ParsedRelationship("related", "preparado")),
            variants = listOf(ParsedVariant("estar preparado")),
            confidence = 0.91
        )

        val id = import(entry)

        assertTrue(concepts.values.containsKey(id))
        assertEquals(listOf("estar + listo"), metadata.values[id]?.breakdown)
        assertEquals(listOf("related: preparado"), metadata.values[id]?.relationships)
        assertEquals(listOf("estar preparado"), metadata.values[id]?.variants)
        assertEquals(0.91, metadata.values[id]?.confidence ?: 0.0, 0.0001)
    }
}
