package com.flashlearn.domain.gamification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
}
