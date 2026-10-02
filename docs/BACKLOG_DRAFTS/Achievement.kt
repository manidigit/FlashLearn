package com.flashlearn.domain.gamification

data class AchievementDefinition(
    val id: String,
    val title: String,
    val description: String
)

data class AchievementState(
    val achievementId: String,
    val unlocked: Boolean
)

data class AchievementContext(
    val totalReviews: Int,
    val totalCorrect: Int,
    val totalWrong: Int,
    val currentStreakDays: Int,
    val longestStreakDays: Int,
    val learnedConcepts: Int
)

data class AchievementEvaluationResult(
    val states: List<AchievementState>,
    val newlyUnlocked: List<String>
)

interface AchievementRule {
    fun isSatisfied(
        definition: AchievementDefinition,
        context: AchievementContext
    ): Boolean
}

class EvaluateAchievementsUseCase(
    private val rules: List<AchievementRule>
) {
    fun evaluate(
        definitions: List<AchievementDefinition>,
        existing: List<AchievementState>,
        context: AchievementContext
    ): AchievementEvaluationResult {
        val old = existing.associateBy { it.achievementId }
        val states = definitions.map { definition ->
            val wasUnlocked = old[definition.id]?.unlocked == true
            AchievementState(
                achievementId = definition.id,
                unlocked = wasUnlocked ||
                    rules.any { it.isSatisfied(definition, context) }
            )
        }
        return AchievementEvaluationResult(
            states = states,
            newlyUnlocked = states
                .filter { it.unlocked && old[it.achievementId]?.unlocked != true }
                .map { it.achievementId }
        )
    }
}
