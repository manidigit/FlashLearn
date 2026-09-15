package com.flashlearn.domain.gamification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementRulesTest {
    @Test
    fun exactFrozenThresholdsAreApplied() {
        val context = AchievementContext(
            totalReviews = 100,
            totalCorrect = 50,
            totalWrong = 50,
            currentStreakDays = 7,
            longestStreakDays = 7,
            learnedConcepts = 100,
            practicedWords = 10,
            totalActiveWords = 500,
            veryHardLearnedConcepts = 25,
            monthlyCorrectConcepts = 50
        )
        val result = EvaluateAchievementsUseCase().evaluate(DefaultAchievements.definitions, emptyList(), context)
        assertEquals(DefaultAchievements.definitions.map { it.id }.toSet(), result.newlyUnlocked.toSet())
    }

    @Test
    fun achievementDoesNotUnlockBeforeItsThreshold() {
        val context = AchievementContext(99, 49, 50, 6, 6, 99, 9, 499, 24, 49)
        val result = EvaluateAchievementsUseCase().evaluate(DefaultAchievements.definitions, emptyList(), context)
        assertFalse(result.newlyUnlocked.contains(AchievementIds.FIRST_TEN_WORDS))
        assertFalse(result.newlyUnlocked.contains(AchievementIds.SEVEN_DAY_STREAK))
        assertFalse(result.newlyUnlocked.contains(AchievementIds.MEMORY_BUILDER))
        assertFalse(result.newlyUnlocked.contains(AchievementIds.VOCABULARY_BUILDER))
        assertFalse(result.newlyUnlocked.contains(AchievementIds.HARD_MODE_MASTER))
        assertFalse(result.newlyUnlocked.contains(AchievementIds.LONG_TERM_MEMORY))
        assertTrue(result.states.isNotEmpty())
    }

    @Test
    fun alreadyUnlockedAchievementIsNeverReturnedAsNew() {
        val existing = listOf(AchievementState(AchievementIds.SEVEN_DAY_STREAK, true))
        val context = AchievementContext(1, 1, 0, 7, 7, 1, 10)
        val result = EvaluateAchievementsUseCase().evaluate(DefaultAchievements.definitions, existing, context)
        assertFalse(result.newlyUnlocked.contains(AchievementIds.SEVEN_DAY_STREAK))
        assertTrue(result.states.first { it.achievementId == AchievementIds.SEVEN_DAY_STREAK }.unlocked)
    }
}
