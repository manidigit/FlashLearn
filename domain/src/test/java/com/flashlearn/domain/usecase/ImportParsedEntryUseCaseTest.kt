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
    private class Db : FlashLearnDatabase { override suspend fun <T> withTransaction(block: suspend () -> T): T = block() }
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
        override suspend fun find(conceptId: UUID, languageCode: String) = values.find { it.conceptId == conceptId && it.languageCode == languageCode }
        override suspend fun findAll(conceptId: UUID, languageCode: String) = values.filter { it.conceptId == conceptId && it.languageCode == languageCode }
        override suspend fun upsert(content: Content) { values.removeAll { it.id == content.id }; values += content }
        override suspend fun insertTranslation(content: Content) { values.removeAll { it.id == content.id }; values += content }
        override suspend fun getAll() = values.toList()
    }
    private class Learning : LearningStateRepository {
        override suspend fun get(conceptId: UUID) = null
        override suspend fun upsert(state: LearningState) {}
        override suspend fun getAllByStage(stage: Stage) = emptyList<LearningState>()
        override suspend fun getDueNonLearned(now: Instant) = emptyList<LearningState>()
        override suspend fun getAll() = emptyList<LearningState>()
    }
    private class Difficulty : DifficultyStateRepository {
        override suspend fun get(conceptId: UUID) = null
        override suspend fun upsert(state: DifficultyState) {}
        override suspend fun delete(conceptId: UUID) {}
        override suspend fun getAll() = emptyList<DifficultyState>()
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
    private class Relations : VocabularyRelationRepository {
        val values = mutableListOf<VocabularyRelation>()
        override suspend fun insert(value: VocabularyRelation) { values += value }
        override suspend fun getForConcept(conceptId: UUID) = values.filter { it.sourceConceptId == conceptId }
        override suspend fun getAll() = values.toList()
    }
    private class Variants : VocabularyVariantRepository {
        val values = mutableListOf<VocabularyVariant>()
        override suspend fun insert(value: VocabularyVariant) { values += value }
        override suspend fun getForConcept(conceptId: UUID) = values.filter { it.conceptId == conceptId }
        override suspend fun getAll() = values.toList()
    }
    private class ReviewQueue : ReviewQueueRepository {
        val values = mutableListOf<ReviewQueueItem>()
        override suspend fun upsert(value: ReviewQueueItem) { values += value }
        override suspend fun getPending() = values.filter { it.status == ReviewQueueStatus.PENDING }
        override suspend fun getAll() = values.toList()
        override suspend fun update(value: ReviewQueueItem) { values.removeAll { it.id == value.id }; values += value }
    }

    private fun useCase(concepts: Concepts, contents: Contents, metadata: Metadata): ImportParsedEntryUseCase {
        val create = CreateConceptUseCase(concepts, contents, Learning(), Difficulty(), Tags(), Db())
        return ImportParsedEntryUseCase(create, concepts, contents, metadata, Relations(), Variants(), ReviewQueue(), Db())
    }

    private fun entry(source: String, translation: String) = ParsedEntry(
        sourceText = source, translationText = translation, notes = null, language = DetectedLanguage.MIXED,
        entryType = EntryKind.PHRASE, rawLines = listOf("$source → $translation"), confidence = 0.91
    )

    @Test
    fun import_persists_parser_metadata_with_same_concept() = runBlocking {
        val concepts = Concepts(); val contents = Contents(); val metadata = Metadata(); val import = useCase(concepts, contents, metadata)
        val parsed = entry("estar listo", "آماده بودن").copy(
            breakdown = listOf(BreakdownPart("estar + listo")),
            relationships = listOf(ParsedRelationship("related", "preparado")), variants = listOf(ParsedVariant("estar preparado"))
        )
        val id = import(parsed)
        assertTrue(concepts.values.containsKey(id))
        assertEquals(listOf("estar + listo"), metadata.values[id]?.breakdown)
        assertEquals(listOf("related: preparado"), metadata.values[id]?.relationships)
        assertEquals(listOf("estar preparado"), metadata.values[id]?.variants)
        assertEquals(0.91, metadata.values[id]?.confidence ?: 0.0, 0.0001)
    }

    @Test
    fun sameSourceDifferentTranslation_createsSeparateContentRows() = runBlocking {
        val concepts = Concepts(); val contents = Contents(); val import = useCase(concepts, contents, Metadata())
        val firstId = import(entry("cura", "کشیش")); val secondId = import(entry("cura", "درمان"))
        assertEquals(firstId, secondId)
        assertEquals(1, concepts.values.size)
        assertEquals(listOf("کشیش", "درمان"), contents.findAll(firstId, "fa").map { it.text })
    }

    @Test
    fun exactDuplicateStillRejected() = runBlocking {
        val concepts = Concepts(); val contents = Contents(); val import = useCase(concepts, contents, Metadata())
        import(entry("hola", "سلام"))
        val error = runCatching { import(entry(" Hola ", " سلام")) }.exceptionOrNull()
        assertTrue(error is DuplicateConceptException)
    }
}
