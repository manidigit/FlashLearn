package com.flashlearn.app.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flashlearn.domain.progress.CalculateProgressUseCase
import com.flashlearn.domain.progress.CalculateProgressPercentage
import com.flashlearn.domain.statistics.CalculateStatisticsUseCase
import com.flashlearn.domain.statistics.CalculateStreakUseCase
import com.flashlearn.domain.statistics.StatisticsSnapshot
import com.flashlearn.domain.statistics.StreakSnapshot
import com.flashlearn.domain.model.ProgressSummary
import com.flashlearn.domain.repository.ReviewHistoryRepository
import com.flashlearn.domain.repository.AchievementRepository
import com.flashlearn.domain.gamification.AchievementContext
import com.flashlearn.domain.gamification.AchievementDefinition
import com.flashlearn.domain.gamification.AchievementState
import com.flashlearn.domain.gamification.DefaultAchievements
import com.flashlearn.domain.gamification.EvaluateAchievementsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class ActivityRange(val label: String, val days: Int?) {
    WEEK("هفتگی", 7),
    MONTH("ماهانه", 30),
    THREE_MONTHS("سه‌ماهه", 90),
    ALL("همه", null)
}

data class ActivityReviewStat(val label: String, val total: Int, val correct: Int)

data class DailyReviewStat(val dayLabel: String, val total: Int, val correct: Int) {
    val accuracyPercent: Int get() = if (total == 0) 0 else (correct * 100) / total
}

data class ReviewPeriodStat(val total: Int, val correct: Int) {
    val accuracyPercent: Int get() = if (total == 0) 0 else (correct * 100) / total
}

data class ProgressUiState(
    val loading: Boolean = true,
    val progress: com.flashlearn.domain.progress.ProgressSnapshot? = null,
    val summary: ProgressSummary? = null,
    val statistics: StatisticsSnapshot? = null,
    val streak: StreakSnapshot? = null,
    val achievements: List<Pair<AchievementDefinition, AchievementState>> = emptyList(),
    val dailyReviews: List<DailyReviewStat> = emptyList(),
    val activityRange: ActivityRange = ActivityRange.WEEK,
    val activityReviews: List<ActivityReviewStat> = emptyList(),
    val todayReviews: ReviewPeriodStat = ReviewPeriodStat(0, 0),
    val weekReviews: ReviewPeriodStat = ReviewPeriodStat(0, 0),
    val monthReviews: ReviewPeriodStat = ReviewPeriodStat(0, 0),
    val progressPercentage: Double = 0.0,
    val error: String? = null
)

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val calculateProgress: CalculateProgressUseCase,
    private val calculateProgressPercentage: CalculateProgressPercentage,
    private val calculateStatistics: CalculateStatisticsUseCase,
    private val calculateStreak: CalculateStreakUseCase,
    private val getProgressSummary: com.flashlearn.domain.usecase.GetProgressSummaryUseCase,
    private val historyRepository: ReviewHistoryRepository,
    private val achievementRepository: AchievementRepository,
    private val evaluateAchievements: EvaluateAchievementsUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(ProgressUiState())
    val state: StateFlow<ProgressUiState> = _state
    private var refreshGeneration = 0L

    init { refresh() }

    fun setActivityRange(range: ActivityRange) {
        _state.value = _state.value.copy(activityRange = range)
        refresh()
    }

    fun refresh(now: Instant = Instant.now(), zoneId: ZoneId = ZoneId.systemDefault()) {
        val generation = ++refreshGeneration
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            runCatching {
                val stats = calculateStatistics()
                val progress = calculateProgress(now)
                val progressPercentage = calculateProgressPercentage()
                val summary = getProgressSummary(now)
                val history = historyRepository.getAll()
                val streak = calculateStreak.calculate(history, now, zoneId)
                val existingAchievements = achievementRepository.getAll()
                val achievementResult = evaluateAchievements.evaluate(
                    DefaultAchievements.definitions,
                    existingAchievements,
                    AchievementContext(
                        totalReviews = stats.totalReviews,
                        totalCorrect = stats.totalCorrect,
                        totalWrong = stats.totalWrong,
                        currentStreakDays = streak.currentStreakDays,
                        longestStreakDays = streak.longestStreakDays,
                        learnedConcepts = summary.learnedConceptCount
                    )
                )
                achievementRepository.upsertAll(achievementResult.states)
                val achievements = DefaultAchievements.definitions.mapNotNull { definition ->
                    achievementResult.states.find { it.achievementId == definition.id }?.let { definition to it }
                }
                val today = now.atZone(zoneId).toLocalDate()
                val weekStart = today.minusDays(6)
                val monthStart = today.minusDays(29)
                val todayEntries = history.filter { it.reviewedAt.atZone(zoneId).toLocalDate() == today }
                val weekEntries = history.filter {
                    val date = it.reviewedAt.atZone(zoneId).toLocalDate()
                    !date.isBefore(weekStart) && !date.isAfter(today)
                }
                val monthEntries = history.filter {
                    val date = it.reviewedAt.atZone(zoneId).toLocalDate()
                    !date.isBefore(monthStart) && !date.isAfter(today)
                }
                val dayNames = listOf("دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه", "شنبه", "یکشنبه")
                val daily = (6 downTo 0).map { offset ->
                    val date = today.minusDays(offset.toLong())
                    val entries = history.filter { it.reviewedAt.atZone(zoneId).toLocalDate() == date }
                    DailyReviewStat(dayNames[date.dayOfWeek.value - 1], entries.size, entries.count { it.isCorrect })
                }
                val initialActivity = buildActivityData(history, today, zoneId, _state.value.activityRange)
                ProgressPayload(
                    progress, progressPercentage, summary, stats, streak, achievements, daily, initialActivity,
                    ReviewPeriodStat(todayEntries.size, todayEntries.count { it.isCorrect }),
                    ReviewPeriodStat(weekEntries.size, weekEntries.count { it.isCorrect }),
                    ReviewPeriodStat(monthEntries.size, monthEntries.count { it.isCorrect })
                )
            }.onSuccess { payload ->
                if (generation == refreshGeneration) {
                    _state.value = ProgressUiState(
                        loading = false,
                        progress = payload.progress,
                        progressPercentage = payload.progressPercentage,
                        summary = payload.summary,
                        statistics = payload.stats,
                        streak = payload.streak,
                        achievements = payload.achievements,
                        dailyReviews = payload.dailyReviews,
                        activityRange = _state.value.activityRange,
                        activityReviews = payload.activityReviews,
                        todayReviews = payload.todayReviews,
                        weekReviews = payload.weekReviews,
                        monthReviews = payload.monthReviews,
                        error = null
                    )
                }
            }.onFailure {
                if (generation == refreshGeneration) _state.value = _state.value.copy(loading = false, error = it.message ?: "خطا در محاسبه آمار")
            }
        }
    }
}

private data class ProgressPayload(
    val progress: com.flashlearn.domain.progress.ProgressSnapshot,
    val progressPercentage: Double,
    val summary: ProgressSummary,
    val stats: StatisticsSnapshot,
    val streak: StreakSnapshot,
    val achievements: List<Pair<AchievementDefinition, AchievementState>>,
    val dailyReviews: List<DailyReviewStat>,
    val activityReviews: List<ActivityReviewStat>,
    val todayReviews: ReviewPeriodStat,
    val weekReviews: ReviewPeriodStat,
    val monthReviews: ReviewPeriodStat
)

private fun buildActivityData(
    history: List<com.flashlearn.domain.model.ReviewHistory>,
    today: java.time.LocalDate,
    zoneId: ZoneId,
    range: ActivityRange
): List<ActivityReviewStat> {
    val start = range.days?.let { today.minusDays((it - 1).toLong()) }
    val filtered = history.filter {
        val date = it.reviewedAt.atZone(zoneId).toLocalDate()
        start == null || (!date.isBefore(start) && !date.isAfter(today))
    }
    if (filtered.isEmpty()) return emptyList()
    if (range == ActivityRange.ALL) {
        val grouped = filtered.groupBy { it.reviewedAt.atZone(zoneId).toLocalDate().with(java.time.DayOfWeek.MONDAY) }
        return grouped.toSortedMap().map { (date, entries) ->
            ActivityReviewStat(date.monthValue.toString() + "/" + date.dayOfMonth, entries.size, entries.count { it.isCorrect })
        }
    }
    val days = range.days ?: 7
    return (days - 1 downTo 0).map { offset ->
        val date = today.minusDays(offset.toLong())
        val entries = filtered.filter { it.reviewedAt.atZone(zoneId).toLocalDate() == date }
        val label = when (range) {
            ActivityRange.WEEK -> listOf("دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه", "شنبه", "یکشنبه")[date.dayOfWeek.value - 1]
            else -> date.monthValue.toString() + "/" + date.dayOfMonth
        }
        ActivityReviewStat(label, entries.size, entries.count { it.isCorrect })
    }
}
