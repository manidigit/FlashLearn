package com.flashlearn.domain.usecase

import com.flashlearn.domain.model.*
import com.flashlearn.domain.repository.*
import com.flashlearn.domain.progress.CalculateProgressUseCase
import com.flashlearn.domain.statistics.CalculateStatisticsUseCase
import com.flashlearn.domain.statistics.CalculateStreakUseCase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

/**
 * v5.52 end-to-end contract: vocabulary -> review -> persisted history ->
 * progress/statistics/streak projections. This deliberately uses the same
 * domain use cases as the application instead of duplicating their logic.
 */
class ProgressStatisticsE2EContractTest {
    private class Db : FlashLearnDatabase {
        override suspend fun <T> withTransaction(block: suspend () -> T): T = block()
    }
    private class Concepts : ConceptRepository {
        val values = linkedMapOf<UUID, Concept>()
        override suspend fun insert(concept: Concept) = concept.id.also { values[it] = concept }
        override suspend fun get(conceptId: UUID) = values[conceptId]
        override suspend fun getAllActive() = values.values.filter { it.active }
        override suspend fun searchActive(query: String) = getAllActive().filter { it.id.toString().contains(query) }
        override suspend fun update(concept: Concept) { values[concept.id] = concept }
        override suspend fun softDelete(conceptId: UUID, now: Instant) { values[conceptId] = values.getValue(conceptId).copy(active = false, updatedAt = now) }
    }

    private class Learning : LearningStateRepository {
        val values = linkedMapOf<UUID, LearningState>()
        override suspend fun get(conceptId: UUID) = values.values.find { it.conceptId == conceptId }
        override suspend fun upsert(state: LearningState) { values[state.id] = state }
        override suspend fun getAllByStage(stage: Stage) = values.values.filter { it.stage == stage }
        override suspend fun getDueNonLearned(now: Instant) = values.values.filter { it.stage != Stage.LEARNED && (it.nextReviewAt?.let { next -> next <= now } ?: true) }
        override suspend fun getAll() = values.values.toList()
    }

    private class Difficulty : DifficultyStateRepository {
        val values = linkedMapOf<UUID, DifficultyState>()
        override suspend fun get(conceptId: UUID) = values.values.find { it.conceptId == conceptId }
        override suspend fun upsert(state: DifficultyState) { values[state.id] = state }
        override suspend fun delete(conceptId: UUID) { values.values.removeIf { it.conceptId == conceptId } }
        override suspend fun getAll() = values.values.toList()
    }

    private class Contents : ContentRepository {
        val values = linkedMapOf<UUID, Content>()
        override suspend fun findByUuid(uuid: UUID) = values[uuid]
        override suspend fun find(conceptId: UUID, languageCode: String) = values.values.find { it.conceptId == conceptId && it.languageCode == languageCode }
        override suspend fun upsert(content: Content) { values[content.id] = content }
        override suspend fun getAll() = values.values.toList()
    }

    private class Tags : ConceptTagRepository {
        override suspend fun insert(conceptTag: ConceptTag) = Unit
        override suspend fun getTagsForConcept(conceptId: UUID) = emptyList<UUID>()
        override suspend fun getConceptsForTag(tagId: UUID) = emptyList<UUID>()
    }

    private class History : ReviewHistoryRepository {
        val values = mutableListOf<ReviewHistory>()
        override suspend fun insert(entry: ReviewHistory) { values += entry }
        override suspend fun existsByAttemptId(sessionId: UUID, reviewAttemptId: UUID) = values.any { it.sessionId == sessionId && it.reviewAttemptId == reviewAttemptId }
        override suspend fun getAll() = values.toList()
    }

    private class Sessions : ReviewSessionRepository {
        val values = linkedMapOf<UUID, ReviewSession>()
        override suspend fun insert(session: ReviewSession) { values[session.id] = session }
        override suspend fun get(sessionId: UUID) = values[sessionId]
        override suspend fun update(session: ReviewSession) { values[session.id] = session }
    }

    private class Settings : SettingsRepository {
        override suspend fun getInt(key: String, default: Int) = default
    }

    @Test
    fun addWordReviewAndDelete_projectConsistentlyIntoProgressStatisticsAndStreak() = runBlocking {
        val now = Instant.now().plusSeconds(1)
        val concepts = Concepts()
        val learning = Learning()
        val difficulty = Difficulty()
        val contents = Contents()
        val history = History()
        val sessions = Sessions()
        val create = CreateConceptUseCase(concepts, contents, learning, difficulty, Tags(), Db())

        val first = create(CreateConceptCommand("hola", "سلام"))
        val second = create(CreateConceptCommand("adios", "خداحافظ"))
        assertEquals(2, concepts.getAllActive().size)

        val sessionId = StartReviewSessionUseCase(sessions)(ReviewType.DAILY, now)
        val submit = SubmitReviewAnswerUseCase(learning, difficulty, history, Settings(), Db())
        submit(SubmitReviewAnswerRequest(first, sessionId, UUID.randomUUID(), ReviewType.DAILY, true, now))
        submit(SubmitReviewAnswerRequest(second, sessionId, UUID.randomUUID(), ReviewType.DAILY, false, now.plusSeconds(1)))
        EndReviewSessionUseCase(sessions)(sessionId, now.plusSeconds(60))

        val progress = CalculateProgressUseCase(concepts, learning, difficulty)(now)
        assertEquals(2, progress.totalConcepts)
        assertEquals(1, progress.weeklyConcepts)
        assertEquals(1, progress.dailyConcepts)
        assertEquals(0, progress.learnedConcepts)

        val stats = CalculateStatisticsUseCase(history)()
        assertEquals(2, stats.totalReviews)
        assertEquals(1, stats.totalCorrect)
        assertEquals(1, stats.totalWrong)
        assertEquals(50, stats.accuracyPercent)
        assertEquals(2, stats.reviewedConceptCount)

        val streak = CalculateStreakUseCase().calculate(history.getAll(), now.plusSeconds(120), ZoneOffset.UTC)
        assertEquals(1, streak.currentStreakDays)
        assertEquals(1, streak.longestStreakDays)
        assertTrue(streak.activeDates.contains(now.atZone(ZoneOffset.UTC).toLocalDate()))

        concepts.softDelete(first, now.plusSeconds(120))
        val afterDelete = CalculateProgressUseCase(concepts, learning, difficulty)(now.plusSeconds(120))
        assertEquals(1, afterDelete.totalConcepts)
        assertEquals(1, afterDelete.dailyConcepts)
        assertEquals(0, afterDelete.weeklyConcepts)
        // Historical review statistics remain immutable after soft deletion.
        val statsAfterDelete = CalculateStatisticsUseCase(history)()
        assertEquals(stats, statsAfterDelete)
    }
}
