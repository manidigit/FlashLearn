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

fun interface AchievementRule {
    fun isSatisfied(definition: AchievementDefinition, context: AchievementContext): Boolean
}

object AchievementIds {
    const val FIRST_TEN_WORDS = "FIRST_TEN_WORDS"
    const val SEVEN_DAY_STREAK = "SEVEN_DAY_STREAK"
    const val THIRTY_DAY_STREAK = "THIRTY_DAY_STREAK"
    const val MEMORY_BUILDER = "MEMORY_BUILDER"
    const val VOCABULARY_BUILDER = "VOCABULARY_BUILDER"
    const val HARD_MODE_MASTER = "HARD_MODE_MASTER"
    const val LONG_TERM_MEMORY = "LONG_TERM_MEMORY"
}

object DefaultAchievements {
    val definitions = listOf(
        AchievementDefinition(AchievementIds.FIRST_TEN_WORDS, "اولین ۱۰ واژه", "یادگیری ۱۰ واژه"),
        AchievementDefinition(AchievementIds.SEVEN_DAY_STREAK, "هفت روز پیوسته", "رسیدن به زنجیره ۷ روزه"),
        AchievementDefinition(AchievementIds.THIRTY_DAY_STREAK, "سی روز پیوسته", "رسیدن به زنجیره ۳۰ روزه"),
        AchievementDefinition(AchievementIds.MEMORY_BUILDER, "سازنده حافظه", "انجام ۱۰۰ مرور"),
        AchievementDefinition(AchievementIds.VOCABULARY_BUILDER, "سازنده واژگان", "یادگیری ۱۰۰ واژه"),
        AchievementDefinition(AchievementIds.HARD_MODE_MASTER, "استاد سخت", "ثبت ۵۰ پاسخ صحیح"),
        AchievementDefinition(AchievementIds.LONG_TERM_MEMORY, "حافظه بلندمدت", "رسیدن به ۳۰ روز زنجیره و ۱۰۰ مرور")
    )
}

object DefaultAchievementRules : AchievementRule {
    override fun isSatisfied(definition: AchievementDefinition, context: AchievementContext): Boolean = when (definition.id) {
        AchievementIds.FIRST_TEN_WORDS -> context.learnedConcepts >= 10
        AchievementIds.SEVEN_DAY_STREAK -> context.currentStreakDays >= 7 || context.longestStreakDays >= 7
        AchievementIds.THIRTY_DAY_STREAK -> context.currentStreakDays >= 30 || context.longestStreakDays >= 30
        AchievementIds.MEMORY_BUILDER -> context.totalReviews >= 100
        AchievementIds.VOCABULARY_BUILDER -> context.learnedConcepts >= 100
        AchievementIds.HARD_MODE_MASTER -> context.totalCorrect >= 50
        AchievementIds.LONG_TERM_MEMORY ->
            (context.currentStreakDays >= 30 || context.longestStreakDays >= 30) && context.totalReviews >= 100
        else -> false
    }
}

class EvaluateAchievementsUseCase(
    private val rules: List<AchievementRule> = listOf(DefaultAchievementRules)
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
                definition.id,
                wasUnlocked || rules.any { it.isSatisfied(definition, context) }
            )
        }
        return AchievementEvaluationResult(
            states = states,
            newlyUnlocked = states.filter {
                it.unlocked && old[it.achievementId]?.unlocked != true
            }.map { it.achievementId }
        )
    }
}
