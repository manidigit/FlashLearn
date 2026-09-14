package com.flashlearn.domain.usecase

import com.flashlearn.domain.model.*
import com.flashlearn.domain.repository.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.util.UUID

class ReviewEngineUseCasesTest {
    private val now = Instant.parse("2026-09-12T10:00:00Z")

    private class CRepo(private val values: List<Concept>) : ConceptRepository {
        override suspend fun insert(concept: Concept) = concept.id
        override suspend fun get(conceptId: UUID) = values.find { it.id == conceptId }
        override suspend fun getAllActive() = values.filter { it.active }
        override suspend fun searchActive(query: String) = values.filter { it.active && it.id.toString().contains(query) }
        override suspend fun update(concept: Concept) {}
        override suspend fun softDelete(conceptId: UUID, now: Instant) {}
    }
    private class LRepo(private val values: List<LearningState>) : LearningStateRepository {
        override suspend fun get(conceptId: UUID) = values.find { it.conceptId == conceptId }
        override suspend fun upsert(state: LearningState) {}
        override suspend fun getAllByStage(stage: Stage) = values.filter { it.stage == stage }
        override suspend fun getDueNonLearned(now: Instant) = values.filter { it.stage != Stage.LEARNED && (it.nextReviewAt?.let { next -> next <= now } ?: true) }
        override suspend fun getAll() = values
    }
    private class DRepo(private val values: List<DifficultyState>) : DifficultyStateRepository {
        override suspend fun get(conceptId: UUID) = values.find { it.conceptId == conceptId }
        override suspend fun upsert(state: DifficultyState) {}
        override suspend fun delete(conceptId: UUID) {}
        override suspend fun getAll() = values
    }
    private class TRepo(private val values: Map<UUID, List<UUID>>) : ConceptTagRepository {
        override suspend fun insert(conceptTag: ConceptTag) {}
        override suspend fun getTagsForConcept(conceptId: UUID) = values[conceptId].orEmpty()
        override suspend fun getConceptsForTag(tagId: UUID) = values.filterValues { tagId in it }.keys.toList()
        override suspend fun getAll() = values.flatMap { (conceptId, tagIds) -> tagIds.map { ConceptTag(conceptId, it) } }
    }

    private fun concept(id: UUID, categoryId: UUID? = null, active: Boolean = true) = Concept(id, EntryType.WORD, categoryId, false, active, now, now)
    private fun learning(id: UUID, stage: Stage, due: Instant? = now.minusSeconds(1)) = LearningState(UUID.randomUUID(), id, stage, due, 0, false, 0, 0, null)
    private fun difficulty(id: UUID, level: VocabularyDifficulty) = DifficultyState(UUID.randomUUID(), id, level, 0, 0, false)

    @Test fun dailyMode_returnsOnlyDueDailyCandidates() = runBlocking {
        val a = UUID.randomUUID(); val b = UUID.randomUUID()
        val repo = SelectReviewQueueUseCase(CRepo(listOf(concept(a), concept(b))), LRepo(listOf(learning(a, Stage.DAILY), learning(b, Stage.DAILY, now.plusSeconds(60)))), DRepo(listOf(difficulty(a, VocabularyDifficulty.EASY), difficulty(b, VocabularyDifficulty.EASY))), TRepo(emptyMap()))
        val result = repo(ReviewSelectionFilters(ReviewType.DAILY, now = now))
        assertEquals(listOf(a), result.map { it.concept.id })
    }

    @Test fun scheduledModes_returnDueCandidatesInRandomBatch() = runBlocking {
        val oldest = UUID.randomUUID(); val newer = UUID.randomUUID(); val future = UUID.randomUUID()
        val repo = SelectReviewQueueUseCase(CRepo(listOf(concept(newer), concept(oldest), concept(future))), LRepo(listOf(learning(newer, Stage.WEEKLY, now.minusSeconds(60)), learning(oldest, Stage.WEEKLY, now.minusSeconds(120)), learning(future, Stage.WEEKLY, now.plusSeconds(60)))), DRepo(listOf(difficulty(newer, VocabularyDifficulty.EASY), difficulty(oldest, VocabularyDifficulty.EASY), difficulty(future, VocabularyDifficulty.EASY))), TRepo(emptyMap()))
        val result = repo(ReviewSelectionFilters(ReviewType.WEEKLY, now = now))
        assertEquals(setOf(oldest, newer), result.map { it.concept.id }.toSet())
    }

    @Test fun randomMode_appliesDifficultyFilter() = runBlocking {
        val easy = UUID.randomUUID(); val hard = UUID.randomUUID()
        val repo = SelectReviewQueueUseCase(CRepo(listOf(concept(easy), concept(hard))), LRepo(listOf(learning(easy, Stage.DAILY), learning(hard, Stage.WEEKLY))), DRepo(listOf(difficulty(easy, VocabularyDifficulty.EASY), difficulty(hard, VocabularyDifficulty.HARD))), TRepo(emptyMap()))
        val result = repo(ReviewSelectionFilters(reviewType = ReviewType.RANDOM, difficulty = VocabularyDifficulty.HARD, now = now))
        assertEquals(1, result.size); assertEquals(hard, result.single().concept.id); assertTrue(result.single().difficulty.current == VocabularyDifficulty.HARD)
    }

    @Test fun randomMode_returnsAllDueNonLearnedCandidatesWhenUnderBatchLimit() = runBlocking {
        val ids = (1..20).map { UUID.randomUUID() }; val concepts = ids.map { concept(it) }; val states = ids.map { learning(it, Stage.DAILY) }; val difficulties = ids.map { difficulty(it, VocabularyDifficulty.EASY) }
        val repo = SelectReviewQueueUseCase(CRepo(concepts), LRepo(states), DRepo(difficulties), TRepo(emptyMap()))
        val result = repo(ReviewSelectionFilters(ReviewType.RANDOM, now = now))
        assertEquals(ids.toSet(), result.map { it.concept.id }.toSet()); assertEquals(ids.size, result.size)
    }

    @Test fun randomMode_appliesCategoryAndTagFilters() = runBlocking {
        val categoryA = UUID.randomUUID(); val categoryB = UUID.randomUUID(); val tagKeep = UUID.randomUUID(); val a = UUID.randomUUID(); val b = UUID.randomUUID(); val c = UUID.randomUUID()
        val repo = SelectReviewQueueUseCase(CRepo(listOf(concept(a, categoryA), concept(b, categoryA), concept(c, categoryB))), LRepo(listOf(learning(a, Stage.DAILY), learning(b, Stage.DAILY), learning(c, Stage.DAILY))), DRepo(listOf(difficulty(a, VocabularyDifficulty.EASY), difficulty(b, VocabularyDifficulty.EASY), difficulty(c, VocabularyDifficulty.EASY))), TRepo(mapOf(a to listOf(tagKeep), b to emptyList(), c to listOf(tagKeep))))
        val result = repo(ReviewSelectionFilters(reviewType = ReviewType.RANDOM, categoryId = categoryA, tagId = tagKeep, now = now))
        assertEquals(listOf(a), result.map { it.concept.id })
    }

    @Test fun learnedMode_doesNotApplyDueDateGate() = runBlocking {
        val id = UUID.randomUUID(); val repo = SelectReviewQueueUseCase(CRepo(listOf(concept(id))), LRepo(listOf(learning(id, Stage.LEARNED, now.plusSeconds(3600)))), DRepo(listOf(difficulty(id, VocabularyDifficulty.EASY))), TRepo(emptyMap()))
        val result = repo(ReviewSelectionFilters(ReviewType.LEARNED, now = now)); assertEquals(1, result.size)
    }

    @Test fun dailyMode_excludesCardsWithoutScheduledReviewTime() = runBlocking {
        val scheduled = UUID.randomUUID(); val unscheduled = UUID.randomUUID(); val repo = SelectReviewQueueUseCase(CRepo(listOf(concept(scheduled), concept(unscheduled))), LRepo(listOf(learning(scheduled, Stage.DAILY), learning(unscheduled, Stage.DAILY, null))), DRepo(listOf(difficulty(scheduled, VocabularyDifficulty.EASY), difficulty(unscheduled, VocabularyDifficulty.EASY))), TRepo(emptyMap()))
        val result = repo(ReviewSelectionFilters(ReviewType.DAILY, now = now)); assertEquals(listOf(scheduled), result.map { it.concept.id })
    }

    @Test fun ordinaryModes_excludeInactiveConcepts() = runBlocking {
        val activeId = UUID.randomUUID(); val deletedId = UUID.randomUUID(); val repo = SelectReviewQueueUseCase(CRepo(listOf(concept(activeId), concept(deletedId, active = false))), LRepo(listOf(learning(activeId, Stage.DAILY), learning(deletedId, Stage.DAILY))), DRepo(listOf(difficulty(activeId, VocabularyDifficulty.EASY), difficulty(deletedId, VocabularyDifficulty.EASY))), TRepo(emptyMap()))
        val result = repo(ReviewSelectionFilters(ReviewType.DAILY, now = now)); assertEquals(listOf(activeId), result.map { it.concept.id })
    }

    @Test fun randomMode_neverIncludesLearnedConcepts() = runBlocking {
        val dailyId = UUID.randomUUID(); val weeklyId = UUID.randomUUID(); val monthlyId = UUID.randomUUID(); val learnedId = UUID.randomUUID()
        val repo = SelectReviewQueueUseCase(CRepo(listOf(concept(dailyId), concept(weeklyId), concept(monthlyId), concept(learnedId))), LRepo(listOf(learning(dailyId, Stage.DAILY), learning(weeklyId, Stage.WEEKLY), learning(monthlyId, Stage.MONTHLY), learning(learnedId, Stage.LEARNED, null))), DRepo(listOf(difficulty(dailyId, VocabularyDifficulty.EASY), difficulty(weeklyId, VocabularyDifficulty.MEDIUM), difficulty(monthlyId, VocabularyDifficulty.HARD), difficulty(learnedId, VocabularyDifficulty.EASY))), TRepo(emptyMap()))
        val result = repo(ReviewSelectionFilters(ReviewType.RANDOM, now = now)); assertEquals(setOf(dailyId, weeklyId, monthlyId), result.map { it.concept.id }.toSet()); assertEquals(3, result.size)
    }

    @Test fun learnedMode_includesOnlyLearnedAndDoesNotApplyDueDateGate() = runBlocking {
        val learnedId = UUID.randomUUID(); val futureLearnedId = UUID.randomUUID(); val dailyId = UUID.randomUUID()
        val repo = SelectReviewQueueUseCase(CRepo(listOf(concept(learnedId), concept(futureLearnedId), concept(dailyId))), LRepo(listOf(learning(learnedId, Stage.LEARNED, now.plusSeconds(3600)), learning(futureLearnedId, Stage.LEARNED, now.plusSeconds(7200)), learning(dailyId, Stage.DAILY))), DRepo(listOf(difficulty(learnedId, VocabularyDifficulty.EASY), difficulty(futureLearnedId, VocabularyDifficulty.EASY), difficulty(dailyId, VocabularyDifficulty.EASY))), TRepo(emptyMap()))
        val result = repo(ReviewSelectionFilters(ReviewType.LEARNED, now = now)); assertEquals(setOf(learnedId, futureLearnedId), result.map { it.concept.id }.toSet()); assertEquals(2, result.size)
    }
}
