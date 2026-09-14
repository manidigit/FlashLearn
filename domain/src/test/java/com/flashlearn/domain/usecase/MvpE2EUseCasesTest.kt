package com.flashlearn.domain.usecase

import com.flashlearn.domain.model.*
import com.flashlearn.domain.repository.*
import com.flashlearn.domain.progress.CalculateProgressUseCase
import com.flashlearn.domain.statistics.CalculateStatisticsUseCase
import com.flashlearn.domain.statistics.CalculateStreakUseCase
import java.time.ZoneOffset
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.util.UUID

/** Domain-level MVP acceptance path: AddWord -> Review -> stage/progress/statistics. */
class MvpE2EUseCasesTest {
    private class Db : FlashLearnDatabase {
        override suspend fun <T> withTransaction(block: suspend () -> T): T = block()
    }
    private class Concepts : ConceptRepository {
        val values = linkedMapOf<UUID, Concept>()
        override suspend fun insert(concept: Concept): UUID { values[concept.id] = concept; return concept.id }
        override suspend fun get(conceptId: UUID) = values[conceptId]
        override suspend fun getAllActive() = values.values.filter { it.active }
        override suspend fun searchActive(query: String) = getAllActive().filter { it.id.toString().contains(query) }
        override suspend fun update(concept: Concept) { values[concept.id] = concept }
        override suspend fun softDelete(conceptId: UUID, now: Instant) { values[conceptId]?.let { values[conceptId] = it.copy(active = false, updatedAt = now) } }
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
        override suspend fun insert(conceptTag: ConceptTag) {}
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
    fun addWordThenReviewThenProgress_isConsistent() = runBlocking {
        val concepts = Concepts(); val learning = Learning(); val difficulty = Difficulty(); val contents = Contents(); val history = History(); val sessions = Sessions()
        val create = CreateConceptUseCase(concepts, contents, learning, difficulty, Tags(), Db())
        val ids = (1..10).map { index -> create(CreateConceptCommand("hola$index", "سلام$index")) }

        val evaluationNow = Instant.now()
        val reviewNow = evaluationNow.plusMillis(1)
        val home = GetProgressSummaryUseCase(concepts, learning)(evaluationNow)
        assertEquals(10, home.activeConceptCount)
        assertEquals(10, home.dueConceptCount)
        assertEquals(10, home.dailyDueConceptCount)
        assertEquals(0, home.weeklyDueConceptCount)
        assertEquals(0, home.monthlyDueConceptCount)

        val start = StartReviewSessionUseCase(sessions)
        val sessionId = start(ReviewType.DAILY, reviewNow)
        assertNotNull(sessions.values[sessionId])
        assertEquals(null, sessions.values[sessionId]?.endedAt)

        val submit = SubmitReviewAnswerUseCase(learning, difficulty, history, Settings(), Db())
        val result = submit(SubmitReviewAnswerRequest(ids.first(), sessionId, UUID.randomUUID(), ReviewType.DAILY, true, reviewNow))

        val nextReviewAt = result.learningState.nextReviewAt
        assertNotNull(nextReviewAt)
        assertTrue(result.learningState.stage != Stage.DAILY)
        assertEquals(1, history.values.size)
        assertEquals(1, result.learningState.totalCorrect)

        val refreshedHome = GetProgressSummaryUseCase(concepts, learning)(Instant.now())
        assertEquals(10, refreshedHome.activeConceptCount)
        assertEquals(9, refreshedHome.dailyDueConceptCount)
        assertEquals(0, refreshedHome.weeklyDueConceptCount)
        assertEquals(0, refreshedHome.monthlyDueConceptCount)
        assertEquals(9, refreshedHome.dueConceptCount)

        EndReviewSessionUseCase(sessions)(sessionId, reviewNow.plusSeconds(60))
        assertNotNull(sessions.values[sessionId]?.endedAt)

        val progress = CalculateProgressUseCase(concepts, learning, difficulty)(Instant.now())
        assertEquals(10, progress.totalConcepts)
        assertEquals(10, progress.weeklyConcepts + progress.monthlyConcepts + progress.learnedConcepts + progress.dailyConcepts)

        val stats = CalculateStatisticsUseCase(history)()
        assertEquals(1, stats.totalReviews)
        assertEquals(1, stats.totalCorrect)
        assertEquals(100, stats.accuracyPercent)

        val weeklyAt = nextReviewAt!!
        val failed = submit(
            SubmitReviewAnswerRequest(
                ids.first(), sessionId, UUID.randomUUID(), ReviewType.WEEKLY, false, weeklyAt
            )
        )
        assertEquals(Stage.DAILY, failed.learningState.stage)
        assertTrue(failed.learningState.hasPathFailure)
        assertEquals(1, failed.learningState.totalWrong)
        assertEquals(0, failed.learningState.monthlyWrongCount)
        assertEquals(VocabularyDifficulty.MEDIUM, failed.difficultyState.current)

        val retryAt = failed.learningState.nextReviewAt!!
        val recovered = submit(
            SubmitReviewAnswerRequest(
                ids.first(), sessionId, UUID.randomUUID(), ReviewType.DAILY, true, retryAt
            )
        )
        assertEquals(Stage.WEEKLY, recovered.learningState.stage)
        assertEquals(2, recovered.learningState.totalCorrect)
        assertEquals(1, recovered.learningState.totalWrong)

        val statsAfterRecovery = CalculateStatisticsUseCase(history)()
        assertEquals(3, statsAfterRecovery.totalReviews)
        assertEquals(2, statsAfterRecovery.totalCorrect)
        assertEquals(1, statsAfterRecovery.totalWrong)
        assertEquals(66, statsAfterRecovery.accuracyPercent)

        DeleteConceptUseCase(concepts, Db())(ids.first())
        assertEquals(false, concepts.get(ids.first())?.active)

        val afterDelete = GetProgressSummaryUseCase(concepts, learning)(Instant.now())
        assertEquals(9, afterDelete.activeConceptCount)
        assertEquals(9, afterDelete.dueConceptCount)
        assertEquals(9, afterDelete.dailyDueConceptCount)
        assertEquals(0, afterDelete.weeklyDueConceptCount)
        assertEquals(0, afterDelete.monthlyDueConceptCount)

        val queue = SelectReviewQueueUseCase(concepts, learning, difficulty, Tags())(
            ReviewSelectionFilters(ReviewType.RANDOM, now = Instant.now())
        )
        assertEquals(9, queue.size)
        assertTrue(queue.none { it.concept.id == ids.first() })
        assertEquals(3, history.values.size)

        val statistics = CalculateStatisticsUseCase(history)()
        assertEquals(3, statistics.totalReviews)
        assertEquals(2, statistics.totalCorrect)
        assertEquals(1, statistics.totalWrong)
        assertEquals(66, statistics.accuracyPercent)
        assertEquals(1, statistics.reviewedConceptCount)

        val streak = CalculateStreakUseCase().calculate(
            history.values, retryAt.plusSeconds(60), ZoneOffset.UTC
        )
        assertEquals(2, streak.currentStreakDays)
        assertEquals(2, streak.longestStreakDays)
    }
}
