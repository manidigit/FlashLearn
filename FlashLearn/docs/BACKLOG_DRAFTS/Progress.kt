package com.flashlearn.domain.progress

enum class Stage { DAILY, WEEKLY, MONTHLY, LEARNED }
enum class VocabularyDifficulty { EASY, MEDIUM, HARD, VERY_HARD }

data class LearningStateView(
    val conceptId: String,
    val stage: Stage,
    val hasPathFailure: Boolean
)

data class DifficultyStateView(
    val conceptId: String,
    val hasReachedVeryHard: Boolean
)

data class ProgressSnapshot(
    val totalConcepts: Int,
    val dailyConcepts: Int,
    val weeklyConcepts: Int,
    val monthlyConcepts: Int,
    val learnedConcepts: Int,
    val pathFailureConcepts: Int,
    val veryHardConcepts: Int
)

class CalculateProgressUseCase {
    fun calculate(
        learning: List<LearningStateView>,
        difficulty: List<DifficultyStateView>
    ): ProgressSnapshot {
        val counts = learning.groupingBy { it.stage }.eachCount()
        return ProgressSnapshot(
            totalConcepts = learning.size,
            dailyConcepts = counts[Stage.DAILY] ?: 0,
            weeklyConcepts = counts[Stage.WEEKLY] ?: 0,
            monthlyConcepts = counts[Stage.MONTHLY] ?: 0,
            learnedConcepts = counts[Stage.LEARNED] ?: 0,
            pathFailureConcepts = learning.count { it.hasPathFailure },
            veryHardConcepts = difficulty.count { it.hasReachedVeryHard }
        )
    }
}
