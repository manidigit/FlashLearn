package com.flashlearn.domain.gamification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import com.flashlearn.domain.model.*
import com.flashlearn.domain.repository.*
import java.time.Instant
import java.time.ZoneId
import java.util.UUID
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementTest {
    private val useCase = EvaluateAchievementsUseCase()

    @Test
    fun default_definitions_cover_all_product_achievements() {
        assertEquals(
            listOf(
                AchievementIds.FIRST_TEN_WORDS,
                AchievementIds.SEVEN_DAY_STREAK,
                AchievementIds.THIRTY_DAY_STREAK,
                AchievementIds.MEMORY_BUILDER,
                AchievementIds.VOCABULARY_BUILDER,
                AchievementIds.HARD_MODE_MASTER,
                AchievementIds.LONG_TERM_MEMORY
            ),
            DefaultAchievements.definitions.map { it.id }
        )
    }

    @Test
    fun thresholds_unlock_only_when_each_frozen_condition_is_met() {
        val context = AchievementContext(
            totalReviews = 100,
            totalCorrect = 50,
            totalWrong = 10,
            currentStreakDays = 30,
            longestStreakDays = 30,
            learnedConcepts = 100,
            practicedWords = 10,
            totalActiveWords = 500,
            veryHardLearnedConcepts = 25,
            monthlyCorrectConcepts = 50
        )

        val result = useCase.evaluate(
            DefaultAchievements.definitions,
            listOf(AchievementState(AchievementIds.MEMORY_BUILDER, true)),
            context
        )

        assertEquals(7, result.states.count { it.unlocked })
        assertEquals(6, result.newlyUnlocked.size)
        assertTrue(AchievementIds.FIRST_TEN_WORDS in result.newlyUnlocked)
        assertTrue(AchievementIds.SEVEN_DAY_STREAK in result.newlyUnlocked)
        assertTrue(AchievementIds.THIRTY_DAY_STREAK in result.newlyUnlocked)
        assertTrue(AchievementIds.VOCABULARY_BUILDER in result.newlyUnlocked)
        assertTrue(AchievementIds.HARD_MODE_MASTER in result.newlyUnlocked)
        assertTrue(AchievementIds.LONG_TERM_MEMORY in result.newlyUnlocked)
        assertFalse(AchievementIds.MEMORY_BUILDER in result.newlyUnlocked)
    }

    @Test
    fun seven_day_achievement_requires_current_streak() {
        val context = AchievementContext(0, 0, 0, 0, 7, 0)
        val result = useCase.evaluate(DefaultAchievements.definitions, emptyList(), context)

        assertFalse(result.states.first { it.achievementId == AchievementIds.SEVEN_DAY_STREAK }.unlocked)
        assertFalse(result.states.first { it.achievementId == AchievementIds.THIRTY_DAY_STREAK }.unlocked)
    }
    @Test
    fun check_and_unlock_achievements_uses_real_practice_context() = kotlinx.coroutines.runBlocking {
        val now = Instant.parse("2026-10-02T12:00:00Z")
        val concepts = (0 until 10).map {
            Concept(UUID.randomUUID(), EntryType.WORD, null, false, true, now, now)
        }
        val history = concepts.map {
            ReviewHistory(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), it.id, now, true, ReviewType.DAILY)
        }
        val achievementRepository = TestAchievementRepository()

        val newlyUnlocked = CheckAndUnlockAchievements(
            TestConceptRepository(concepts),
            TestReviewHistoryRepository(history),
            TestLearningStateRepository(),
            TestDifficultyStateRepository(),
            achievementRepository,
            com.flashlearn.domain.statistics.CalculateStreakUseCase(),
            EvaluateAchievementsUseCase()
        )(now, ZoneId.of("UTC"))

        assertTrue(newlyUnlocked.any { it.achievementId == AchievementIds.FIRST_TEN_WORDS })
        assertTrue(achievementRepository.states.any { it.achievementId == AchievementIds.FIRST_TEN_WORDS && it.unlocked })
    }

    private class TestConceptRepository(private val values: List<Concept>) : ConceptRepository {
        override suspend fun insert(concept: Concept): UUID = concept.id
        override suspend fun get(conceptId: UUID): Concept? = values.firstOrNull { it.id == conceptId }
        override suspend fun getAllActive(): List<Concept> = values
        override suspend fun searchActive(query: String): List<Concept> = values
        override suspend fun update(concept: Concept) = Unit
        override suspend fun softDelete(conceptId: UUID, now: Instant) = Unit
    }

    private class TestReviewHistoryRepository(private val values: List<ReviewHistory>) : ReviewHistoryRepository {
        override suspend fun insert(entry: ReviewHistory) = Unit
        override suspend fun existsByAttemptId(sessionId: UUID, reviewAttemptId: UUID): Boolean = false
        override suspend fun getAll(): List<ReviewHistory> = values
    }

    private class TestLearningStateRepository : LearningStateRepository {
        override suspend fun get(conceptId: UUID): LearningState? = null
        override suspend fun upsert(state: LearningState) = Unit
        override suspend fun getAllByStage(stage: Stage): List<LearningState> = emptyList()
        override suspend fun getDueNonLearned(now: Instant): List<LearningState> = emptyList()
        override suspend fun getAll(): List<LearningState> = emptyList()
    }

    private class TestDifficultyStateRepository : DifficultyStateRepository {
        override suspend fun get(conceptId: UUID): DifficultyState? = null
        override suspend fun upsert(state: DifficultyState) = Unit
        override suspend fun delete(conceptId: UUID) = Unit
        override suspend fun getAll(): List<DifficultyState> = emptyList()
    }

    private class TestAchievementRepository : AchievementRepository {
        val states = mutableListOf<AchievementState>()
        override suspend fun getAll(): List<AchievementState> = states.toList()
        override suspend fun upsertAll(states: List<AchievementState>) {
            states.forEach { state ->
                val index = this.states.indexOfFirst { it.achievementId == state.achievementId }
                if (index >= 0) this.states[index] = state else this.states += state
            }
        }
    }

}
