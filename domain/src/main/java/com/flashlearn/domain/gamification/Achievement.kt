package com.flashlearn.domain.gamification

import com.flashlearn.domain.model.Stage
import com.flashlearn.domain.model.ReviewType
import com.flashlearn.domain.repository.AchievementRepository
import com.flashlearn.domain.repository.ConceptRepository
import com.flashlearn.domain.repository.DifficultyStateRepository
import com.flashlearn.domain.repository.LearningStateRepository
import com.flashlearn.domain.repository.ReviewHistoryRepository
import java.time.Instant
import javax.inject.Inject

data class AchievementDefinition(val id: String, val title: String, val description: String)
data class AchievementState(val achievementId: String, val unlocked: Boolean)
data class AchievementContext(
    val totalReviews: Int,
    val totalCorrect: Int,
    val totalWrong: Int,
    val currentStreakDays: Int,
    val longestStreakDays: Int,
    val learnedConcepts: Int,
    val practicedWords: Int = 0,
    val totalActiveWords: Int = 0,
    val veryHardLearnedConcepts: Int = 0,
    val monthlyCorrectConcepts: Int = 0
)
data class AchievementEvaluationResult(val states: List<AchievementState>, val newlyUnlocked: List<String>)
fun interface AchievementRule { fun isSatisfied(definition: AchievementDefinition, context: AchievementContext): Boolean }

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
        AchievementDefinition(AchievementIds.FIRST_TEN_WORDS, "اولین ۱۰ واژه", "یادگیری ۱۰ واژه اول"),
        AchievementDefinition(AchievementIds.SEVEN_DAY_STREAK, "هفت روز پیوسته", "۷ روز پیوسته تمرین"),
        AchievementDefinition(AchievementIds.THIRTY_DAY_STREAK, "سی روز پیوسته", "۳۰ روز پیوسته تمرین"),
        AchievementDefinition(AchievementIds.MEMORY_BUILDER, "سازنده حافظه", "۱۰۰ کلمه Learned"),
        AchievementDefinition(AchievementIds.VOCABULARY_BUILDER, "سازنده واژگان", "۵۰۰ Concept فعال"),
        AchievementDefinition(AchievementIds.HARD_MODE_MASTER, "استاد سخت", "تسلط بر ۲۵ کلمه VERY_HARD و Learned"),
        AchievementDefinition(AchievementIds.LONG_TERM_MEMORY, "حافظه بلندمدت", "۵۰ Concept با پاسخ صحیح در مرور Monthly")
    )
}

object DefaultAchievementRules : AchievementRule {
    override fun isSatisfied(definition: AchievementDefinition, context: AchievementContext): Boolean = when (definition.id) {
        AchievementIds.FIRST_TEN_WORDS -> context.practicedWords >= 10
        AchievementIds.SEVEN_DAY_STREAK -> context.currentStreakDays >= 7
        AchievementIds.THIRTY_DAY_STREAK -> context.currentStreakDays >= 30
        AchievementIds.MEMORY_BUILDER -> context.learnedConcepts >= 100
        AchievementIds.VOCABULARY_BUILDER -> context.totalActiveWords >= 500
        AchievementIds.HARD_MODE_MASTER -> context.veryHardLearnedConcepts >= 25
        AchievementIds.LONG_TERM_MEMORY -> context.monthlyCorrectConcepts >= 50
        else -> false
    }
}

class EvaluateAchievementsUseCase @Inject constructor() {
    private val rules: List<AchievementRule> = listOf(DefaultAchievementRules)
    fun evaluate(definitions: List<AchievementDefinition>, existing: List<AchievementState>, context: AchievementContext): AchievementEvaluationResult {
        val old = existing.associateBy { it.achievementId }
        val states = definitions.map { definition ->
            val wasUnlocked = old[definition.id]?.unlocked == true
            AchievementState(definition.id, wasUnlocked || rules.any { it.isSatisfied(definition, context) })
        }
        return AchievementEvaluationResult(states, states.filter { it.unlocked && old[it.achievementId]?.unlocked != true }.map { it.achievementId })
    }
}

class CheckAndUnlockAchievements @Inject constructor(
    private val conceptRepository: ConceptRepository,
    private val reviewHistoryRepository: ReviewHistoryRepository,
    private val learningStateRepository: LearningStateRepository,
    private val difficultyStateRepository: DifficultyStateRepository,
    private val achievementRepository: AchievementRepository,
    private val streakCalculator: com.flashlearn.domain.statistics.CalculateStreakUseCase
) {
    suspend operator fun invoke(now: Instant, zoneId: java.time.ZoneId): List<AchievementState> {
        val activeConcepts = conceptRepository.getAllActive()
        val activeIds = activeConcepts.map { it.id }.toSet()
        val history = reviewHistoryRepository.getAll().filter { it.conceptId in activeIds }
        val learning = learningStateRepository.getAll().filter { it.conceptId in activeIds }
        val difficulty = difficultyStateRepository.getAll().filter { it.conceptId in activeIds }
        val streak = streakCalculator.calculate(history, now, zoneId)
        val context = AchievementContext(
            totalReviews = history.size,
            totalCorrect = history.count { it.isCorrect },
            totalWrong = history.count { !it.isCorrect },
            currentStreakDays = streak.currentStreakDays,
            longestStreakDays = streak.longestStreakDays,
            practicedWords = history.map { it.conceptId }.distinct().size,
            learnedConcepts = learning.count { it.stage == Stage.LEARNED },
            totalActiveWords = activeConcepts.size,
            veryHardLearnedConcepts = activeConcepts.count { concept ->
                learning.any { it.conceptId == concept.id && it.stage == Stage.LEARNED } &&
                    difficulty.any { it.conceptId == concept.id && it.hasReachedVeryHard }
            },
            monthlyCorrectConcepts = history.asSequence()
                .filter { it.reviewType == ReviewType.MONTHLY && it.isCorrect }
                .map { it.conceptId }
                .distinct()
                .count()
        )
        val existing = achievementRepository.getAll()
        val result = EvaluateAchievementsUseCase().evaluate(DefaultAchievements.definitions, existing, context)
        val oldUnlocked = existing.filter { it.unlocked }.associateBy { it.achievementId }
        val newlyUnlocked = result.states.filter { it.unlocked && oldUnlocked[it.achievementId] == null }
        if (newlyUnlocked.isNotEmpty()) achievementRepository.upsertAll(newlyUnlocked)
        return newlyUnlocked
    }
}
