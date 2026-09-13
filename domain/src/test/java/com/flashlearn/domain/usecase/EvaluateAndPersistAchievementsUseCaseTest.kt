package com.flashlearn.domain.usecase

import com.flashlearn.domain.gamification.AchievementContext
import com.flashlearn.domain.gamification.AchievementState
import com.flashlearn.domain.repository.AchievementRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EvaluateAndPersistAchievementsUseCaseTest {
    private class FakeAchievementRepository(private var states: List<AchievementState> = emptyList()) : AchievementRepository {
        var writes = 0
        override suspend fun getAll(): List<AchievementState> = states
        override suspend fun upsertAll(states: List<AchievementState>) { this.states = states; writes++ }
    }

    @Test fun evaluatesAgainstStoredStateAndPersistsNewUnlocks() = runBlocking {
        val repository = FakeAchievementRepository(listOf(AchievementState("FIRST_TEN_WORDS", true)))
        val result = EvaluateAndPersistAchievementsUseCase(repository).execute(
            AchievementContext(100, 50, 0, 30, 30, 10)
        )
        assertEquals(1, repository.writes)
        assertTrue(result.newlyUnlocked.isNotEmpty())
        assertTrue(result.states.all { it.unlocked })
    }

    @Test fun persistsLockedCatalogWhenNoThresholdIsMet() = runBlocking {
        val repository = FakeAchievementRepository()
        val result = EvaluateAndPersistAchievementsUseCase(repository).execute(
            AchievementContext(0, 0, 0, 0, 0, 0)
        )
        assertEquals(1, repository.writes)
        assertEquals(7, result.states.size)
        assertTrue(result.states.none { it.unlocked })
        assertTrue(result.newlyUnlocked.isEmpty())
    }
}
