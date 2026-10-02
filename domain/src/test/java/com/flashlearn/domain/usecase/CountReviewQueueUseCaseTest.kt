package com.flashlearn.domain.usecase

import com.flashlearn.domain.model.*
import com.flashlearn.domain.repository.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.util.UUID

class CountReviewQueueUseCaseTest {
    private val now = Instant.parse("2026-10-02T10:00:00Z")
    private val past = now.minusSeconds(3600)

    private class Env(val concepts: List<Concept>, val learning: List<LearningState>, val contents: List<Content>) {
        var conceptLoads = 0
        var contentLoads = 0
        var tagLoads = 0
        var difficultyLoads = 0

        val conceptRepo = object : ConceptRepository {
            override suspend fun insert(concept: Concept) = concept.id
            override suspend fun get(conceptId: UUID) = concepts.firstOrNull { it.id == conceptId }
            override suspend fun getAllActive(): List<Concept> { conceptLoads++; return concepts }
            override suspend fun searchActive(query: String) = emptyList<Concept>()
            override suspend fun update(concept: Concept) {}
            override suspend fun softDelete(conceptId: UUID, now: Instant) {}
        }
        val learningRepo = object : LearningStateRepository {
            override suspend fun get(conceptId: UUID) = learning.firstOrNull { it.conceptId == conceptId }
            override suspend fun upsert(state: LearningState) {}
            override suspend fun getAllByStage(stage: Stage) = learning.filter { it.stage == stage }
            override suspend fun getDueNonLearned(now: Instant) = learning.filter { it.stage != Stage.LEARNED }
            override suspend fun getAll() = learning
        }
        val difficultyRepo = object : DifficultyStateRepository {
            override suspend fun get(conceptId: UUID) = null
            override suspend fun upsert(state: DifficultyState) {}
            override suspend fun delete(conceptId: UUID) {}
            override suspend fun getAll(): List<DifficultyState> {
                difficultyLoads++
                return concepts.map { DifficultyState(UUID.randomUUID(), it.id, VocabularyDifficulty.EASY, 0, 0, false) }
            }
        }
        val tagRepo = object : ConceptTagRepository {
            override suspend fun insert(conceptTag: ConceptTag) {}
            override suspend fun getTagsForConcept(conceptId: UUID) = emptyList<UUID>()
            override suspend fun getConceptsForTag(tagId: UUID) = emptyList<UUID>()
            override suspend fun getAll(): List<ConceptTag> { tagLoads++; return emptyList() }
        }
        val historyRepo = object : ReviewHistoryRepository {
            override suspend fun insert(entry: ReviewHistory) {}
            override suspend fun existsByAttemptId(sessionId: UUID, reviewAttemptId: UUID) = false
            override suspend fun getAll() = emptyList<ReviewHistory>()
        }
        val contentRepo = object : ContentRepository {
            override suspend fun findByUuid(uuid: UUID) = null
            override suspend fun find(conceptId: UUID, languageCode: String) = null
            override suspend fun upsert(content: Content) {}
            override suspend fun getAll(): List<Content> { contentLoads++; return contents }
        }
        val useCase = CountReviewQueueUseCase(conceptRepo, learningRepo, difficultyRepo, tagRepo, historyRepo, contentRepo)
    }

    private fun env(): Env {
        val concepts = (1..6).map { Concept(UUID.randomUUID(), EntryType.WORD, null, false, true, past, past) }
        val stages = listOf(Stage.DAILY, Stage.DAILY, Stage.WEEKLY, Stage.MONTHLY, Stage.MONTHLY, Stage.LEARNED)
        val learning = concepts.mapIndexed { i, c ->
            // concept 1 was already reviewed today, so it must not be counted.
            val reviewedToday = if (i == 1) now.minusSeconds(600) else null
            LearningState(UUID.randomUUID(), c.id, stages[i], past, 0, false, 0, 0, reviewedToday)
        }
        // concept 4 has no Persian content, so it is not eligible when languages are filtered.
        val contents = concepts.flatMapIndexed { i, c ->
            buildList {
                add(Content(UUID.randomUUID(), c.id, "es", "palabra$i", "palabra$i"))
                if (i != 4) add(Content(UUID.randomUUID(), c.id, "fa", "کلمه$i", "کلمه$i"))
            }
        }
        return Env(concepts, learning, contents)
    }

    private fun filters(type: ReviewType, languages: Boolean = true) = ReviewSelectionFilters(
        reviewType = type,
        sourceLanguage = if (languages) "es" else null,
        targetLanguage = if (languages) "fa" else null,
        now = now,
        zoneId = java.time.ZoneOffset.UTC
    )

    @Test
    fun countByType_matchesIndividualInvocations() = runBlocking {
        val e = env()
        val together = e.useCase.countByType(filters(ReviewType.DAILY), listOf(ReviewType.DAILY, ReviewType.WEEKLY, ReviewType.MONTHLY))
        assertEquals(e.useCase(filters(ReviewType.DAILY)), together.getValue(ReviewType.DAILY))
        assertEquals(e.useCase(filters(ReviewType.WEEKLY)), together.getValue(ReviewType.WEEKLY))
        assertEquals(e.useCase(filters(ReviewType.MONTHLY)), together.getValue(ReviewType.MONTHLY))
        // DAILY: 2 states, one reviewed today -> 1. WEEKLY: 1. MONTHLY: 2 states, one has no Persian -> 1.
        assertEquals(1, together.getValue(ReviewType.DAILY))
        assertEquals(1, together.getValue(ReviewType.WEEKLY))
        assertEquals(1, together.getValue(ReviewType.MONTHLY))
    }

    @Test
    fun countByType_loadsSharedTablesOnlyOnce() = runBlocking {
        val e = env()
        e.useCase.countByType(filters(ReviewType.DAILY), listOf(ReviewType.DAILY, ReviewType.WEEKLY, ReviewType.MONTHLY))
        assertEquals(1, e.conceptLoads)
        assertEquals(1, e.difficultyLoads)
        assertEquals(1, e.contentLoads)
        assertEquals(0, e.tagLoads)
    }

    @Test
    fun withoutLanguageOrTagFilters_contentsAndTagsAreNotLoaded() = runBlocking {
        val e = env()
        val count = e.useCase(filters(ReviewType.MONTHLY, languages = false))
        assertEquals(2, count)
        assertEquals(0, e.contentLoads)
        assertEquals(0, e.tagLoads)
    }
}
