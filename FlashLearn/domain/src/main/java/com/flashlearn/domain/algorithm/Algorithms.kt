package com.flashlearn.domain.algorithm
import com.flashlearn.domain.model.*
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

fun calculateLearningTransition(
    learningState: LearningState, isCorrect: Boolean, reviewedAt: Instant,
    zoneId: ZoneId = ZoneId.systemDefault()
): TransitionResult {
    val current = learningState.stage
    val wrong = learningState.monthlyWrongCount
    val failure = learningState.hasPathFailure
    if (current == Stage.LEARNED) return TransitionResult(Stage.LEARNED, null, failure, wrong)
    val nextDay = reviewedAt.atZone(zoneId).toLocalDate().plusDays(1)
        .atStartOfDay(zoneId).toInstant()
    return if (isCorrect) when (current) {
        Stage.DAILY -> TransitionResult(Stage.WEEKLY, reviewedAt.plus(7, ChronoUnit.DAYS), failure, wrong)
        Stage.WEEKLY -> TransitionResult(Stage.MONTHLY, reviewedAt.plus(30, ChronoUnit.DAYS), failure, wrong)
        Stage.MONTHLY -> TransitionResult(Stage.LEARNED, null, failure, wrong)
        Stage.LEARNED -> TransitionResult(Stage.LEARNED, null, failure, wrong)
    } else when (current) {
        Stage.DAILY -> TransitionResult(Stage.DAILY, nextDay, failure, wrong)
        Stage.WEEKLY -> TransitionResult(Stage.DAILY, nextDay, true, wrong)
        Stage.MONTHLY -> TransitionResult(Stage.DAILY, nextDay, true, wrong + 1)
        Stage.LEARNED -> TransitionResult(Stage.LEARNED, null, failure, wrong)
    }
}

fun calculateDifficulty(
    state: DifficultyState, isCorrect: Boolean, reviewType: ReviewType,
    monthlyWrongCountBefore: Int, threshold: Int = 3
): DifficultyState {
    // LEARNED review is an explicit optional flow. It records the answer, but it
    // must not re-enter the scheduler or mutate difficulty state.
    if (reviewType == ReviewType.LEARNED) return state

    var level = state.current
    var cc = state.consecutiveCorrect
    var cw = state.consecutiveWrong
    var reached = state.hasReachedVeryHard
    when {
        reviewType == ReviewType.WEEKLY && !isCorrect -> {
            level = if (level.ordinal < VocabularyDifficulty.MEDIUM.ordinal) VocabularyDifficulty.MEDIUM else level
            cc = 0; cw = 0
        }
        reviewType == ReviewType.MONTHLY && !isCorrect -> {
            level = if (monthlyWrongCountBefore == 0) VocabularyDifficulty.HARD else VocabularyDifficulty.VERY_HARD
            cc = 0; cw = 0
        }
        else -> if (isCorrect) {
            cw = 0; val n = cc + 1
            if (n >= threshold) { level = easier(level); cc = 0; cw = 0 } else cc = n
        } else {
            cc = 0; val n = cw + 1
            if (n >= threshold) { level = harder(level); cc = 0; cw = 0 } else cw = n
        }
    }
    if (level == VocabularyDifficulty.VERY_HARD) reached = true
    return state.copy(current = level, consecutiveCorrect = cc, consecutiveWrong = cw, hasReachedVeryHard = reached)
}
private fun easier(d: VocabularyDifficulty) = when (d) {
    VocabularyDifficulty.VERY_HARD -> VocabularyDifficulty.HARD
    VocabularyDifficulty.HARD -> VocabularyDifficulty.MEDIUM
    VocabularyDifficulty.MEDIUM -> VocabularyDifficulty.EASY
    VocabularyDifficulty.EASY -> VocabularyDifficulty.EASY
}
private fun harder(d: VocabularyDifficulty) = when (d) {
    VocabularyDifficulty.EASY -> VocabularyDifficulty.MEDIUM
    VocabularyDifficulty.MEDIUM -> VocabularyDifficulty.HARD
    VocabularyDifficulty.HARD -> VocabularyDifficulty.VERY_HARD
    VocabularyDifficulty.VERY_HARD -> VocabularyDifficulty.VERY_HARD
}
