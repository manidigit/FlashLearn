package com.flashlearn.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flashlearn.domain.model.ProgressSummary
import com.flashlearn.app.ui.LanguagePair
import com.flashlearn.domain.usecase.CountReviewQueueUseCase
import com.flashlearn.domain.usecase.ReviewSelectionFilters
import com.flashlearn.domain.model.ReviewType
import com.flashlearn.domain.repository.ReviewHistoryRepository
import com.flashlearn.domain.statistics.BasicStatistics
import com.flashlearn.domain.statistics.CalculateStreakUseCase
import com.flashlearn.domain.statistics.GetBasicStatistics
import com.flashlearn.domain.statistics.StreakSnapshot
import com.flashlearn.domain.progress.CalculateProgressPercentage
import com.flashlearn.domain.progress.CalculateProgressUseCase
import com.flashlearn.domain.usecase.EnsureStarterDataUseCase
import com.flashlearn.domain.usecase.GetProgressSummaryUseCase
import java.time.Instant
import java.time.ZoneId
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class HomeUiState(
    val isLoading: Boolean = true,
    val summary: ProgressSummary? = null,
    val basicStats: BasicStatistics? = null,
    val streak: StreakSnapshot? = null,
    val progressPercentage: Double = 0.0,
    val dailyTotal: Int = 0,
    val weeklyTotal: Int = 0,
    val monthlyTotal: Int = 0,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getProgressSummary: GetProgressSummaryUseCase,
    private val getBasicStatistics: GetBasicStatistics,
    private val ensureStarterData: EnsureStarterDataUseCase,
    private val ensureStatesUseCase: com.flashlearn.domain.usecase.EnsureStatesUseCase,
    private val calculateStreak: CalculateStreakUseCase,
    private val historyRepository: ReviewHistoryRepository,
    private val calculateProgressPercentage: CalculateProgressPercentage,
    private val calculateProgress: CalculateProgressUseCase,
    private val countReviewQueue: CountReviewQueueUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()
    private var refreshGeneration = 0L
    private var refreshJob: Job? = null

    init { refresh(repairStates = true) }

    fun refresh(languagePair: LanguagePair = LanguagePair(), repairStates: Boolean = false) {
        refreshJob?.cancel()
        val generation = ++refreshGeneration
        refreshJob = viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            runCatching {
                ensureStarterData()
                if (repairStates) ensureStatesUseCase(Instant.now())
                val now = Instant.now()
                val summary = getProgressSummary(now)
                val basicStats = getBasicStatistics()
                val history = historyRepository.getAll()
                val streak = withContext(Dispatchers.Default) {
                    calculateStreak.calculate(history, now, ZoneId.systemDefault())
                }
                val progressPercentage = calculateProgressPercentage(history)
                // Ready counts come from the same due/eligibility summary used by review.
                // Denominators are total words currently assigned to each learning stage,
                // matching the Statistics screen's Learning Stages card.
                val progress = calculateProgress(now)
                val zone = ZoneId.systemDefault()
                val dailyReady = countReviewQueue(ReviewSelectionFilters(reviewType = ReviewType.DAILY, sourceLanguage = languagePair.source.code, targetLanguage = languagePair.target.code, now = now, zoneId = zone))
                val weeklyReady = countReviewQueue(ReviewSelectionFilters(reviewType = ReviewType.WEEKLY, sourceLanguage = languagePair.source.code, targetLanguage = languagePair.target.code, now = now, zoneId = zone))
                val monthlyReady = countReviewQueue(ReviewSelectionFilters(reviewType = ReviewType.MONTHLY, sourceLanguage = languagePair.source.code, targetLanguage = languagePair.target.code, now = now, zoneId = zone))
                val dailyTotal = progress.dailyConcepts
                val weeklyTotal = progress.weeklyConcepts
                val monthlyTotal = progress.monthlyConcepts
                HomeSnapshot(summary.copy(dueConceptCount = dailyReady + weeklyReady + monthlyReady, dailyDueConceptCount = dailyReady, weeklyDueConceptCount = weeklyReady, monthlyDueConceptCount = monthlyReady), basicStats, streak, progressPercentage, dailyTotal, weeklyTotal, monthlyTotal)
            }.onSuccess { snapshot ->
                if (generation == refreshGeneration) {
                    _state.value = HomeUiState(
                        isLoading = false,
                        summary = snapshot.summary,
                        basicStats = snapshot.basicStats,
                        streak = snapshot.streak,
                        progressPercentage = snapshot.progressPercentage,
                        dailyTotal = snapshot.dailyTotal,
                        weeklyTotal = snapshot.weeklyTotal,
                        monthlyTotal = snapshot.monthlyTotal
                    )
                }
            }.onFailure {
                if (generation == refreshGeneration) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = it.message ?: "خطا در بارگذاری خانه"
                    )
                }
            }
        }
    }

    private data class HomeSnapshot(
        val summary: ProgressSummary,
        val basicStats: BasicStatistics,
        val streak: StreakSnapshot,
        val progressPercentage: Double,
        val dailyTotal: Int,
        val weeklyTotal: Int,
        val monthlyTotal: Int
    )
}
