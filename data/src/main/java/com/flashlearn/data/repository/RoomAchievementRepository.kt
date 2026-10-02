package com.flashlearn.data.repository

import com.flashlearn.database.AchievementDao
import com.flashlearn.database.AchievementEntity
import com.flashlearn.domain.gamification.AchievementState
import com.flashlearn.domain.repository.AchievementRepository
import javax.inject.Inject

class RoomAchievementRepository @Inject constructor(private val dao: AchievementDao) : AchievementRepository {
    override suspend fun getAll(): List<AchievementState> = dao.getAll().map { AchievementState(it.achievementId, it.unlocked) }
    override suspend fun upsertAll(states: List<AchievementState>) {
        if (states.isNotEmpty()) dao.upsertAll(states.map { AchievementEntity(it.achievementId, it.unlocked) })
    }
}
