package com.flashlearn.app.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flashlearn.domain.progress.CalculateProgressUseCase
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

data class DailyReviewStat(val dayLabel: String, val total: Int, val correct: Int) {
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
    val error: String? = null
)

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val calculateProgress: CalculateProgressUseCase,
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

    fun refresh(now: Instant = Instant.now(), zoneId: ZoneId = ZoneId.systemDefault()) {
        val generation = ++refreshGeneration
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            runCatching {
                val stats = calculateStatistics()
                val progress = calculateProgress(now)
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
                val dayNames = listOf("دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه", "شنبه", "یکشنبه")
                val daily = (6 downTo 0).map { offset ->
                    val date = today.minusDays(offset.toLong())
                    val entries = history.filter { it.reviewedAt.atZone(zoneId).toLocalDate() == date }
                    DailyReviewStat(dayNames[date.dayOfWeek.value - 1], entries.size, entries.count { it.isCorrect })
                }
                ProgressPayload(progress, summary, stats, streak, achievements, daily)
            }.onSuccess { payload ->
                if (generation == refreshGeneration) {
                    _state.value = ProgressUiState(false, payload.progress, payload.summary, payload.stats, payload.streak, payload.achievements, payload.dailyReviews, null)
                }
            }.onFailure {
                if (generation == refreshGeneration) _state.value = _state.value.copy(loading = false, error = it.message ?: "خطا در محاسبه آمار")
            }
        }
    }
}

private data class ProgressPayload(
    val progress: com.flashlearn.domain.progress.ProgressSnapshot,
    val summary: ProgressSummary,
    val stats: StatisticsSnapshot,
    val streak: StreakSnapshot,
    val achievements: List<Pair<AchievementDefinition, AchievementState>>,
    val dailyReviews: List<DailyReviewStat>
)
