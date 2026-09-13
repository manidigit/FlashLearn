package com.flashlearn.domain.usecase

import com.flashlearn.domain.gamification.AchievementContext
import com.flashlearn.domain.gamification.AchievementEvaluationResult
import com.flashlearn.domain.gamification.DefaultAchievements
import com.flashlearn.domain.gamification.EvaluateAchievementsUseCase
import com.flashlearn.domain.repository.AchievementRepository

class EvaluateAndPersistAchievementsUseCase(
    private val repository: AchievementRepository,
    private val evaluator: EvaluateAchievementsUseCase = EvaluateAchievementsUseCase()
) {
    suspend fun execute(context: AchievementContext): AchievementEvaluationResult {
        val existing = repository.getAll()
        val result = evaluator.evaluate(DefaultAchievements.definitions, existing, context)
        repository.upsertAll(result.states)
        return result
    }
}
