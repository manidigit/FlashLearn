package com.flashlearn.domain.gamification

import org.junit.Assert.assertEquals
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
    fun thresholds_unlock_expected_achievements_and_preserve_existing_state() {
        val context = AchievementContext(
            totalReviews = 100,
            totalCorrect = 50,
            totalWrong = 10,
            currentStreakDays = 30,
            longestStreakDays = 30,
            learnedConcepts = 100
        )

        val result = useCase.evaluate(
            DefaultAchievements.definitions,
            listOf(AchievementState(AchievementIds.MEMORY_BUILDER, true)),
            context
        )

        assertEquals(7, result.states.count { it.unlocked })
        assertEquals(6, result.newlyUnlocked.size)
        assertTrue(AchievementIds.LONG_TERM_MEMORY in result.newlyUnlocked)
        assertTrue(AchievementIds.MEMORY_BUILDER !in result.newlyUnlocked)
    }

    @Test
    fun seven_day_achievement_uses_longest_streak_when_current_streak_reset() {
        val context = AchievementContext(0, 0, 0, 0, 7, 0)
        val result = useCase.evaluate(DefaultAchievements.definitions, emptyList(), context)

        assertTrue(result.states.first { it.achievementId == AchievementIds.SEVEN_DAY_STREAK }.unlocked)
        assertTrue(result.states.none { it.achievementId == AchievementIds.THIRTY_DAY_STREAK && it.unlocked })
    }
}
