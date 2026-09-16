package com.flashlearn.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flashlearn.domain.model.ProgressSummary
import com.flashlearn.domain.repository.ReviewHistoryRepository
import com.flashlearn.domain.statistics.BasicStatistics
import com.flashlearn.domain.statistics.CalculateStreakUseCase
import com.flashlearn.domain.statistics.GetBasicStatistics
import com.flashlearn.domain.statistics.StreakSnapshot
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

data class HomeUiState(
    val isLoading: Boolean = true,
    val summary: ProgressSummary? = null,
    val basicStats: BasicStatistics? = null,
    val streak: StreakSnapshot? = null,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getProgressSummary: GetProgressSummaryUseCase,
    private val getBasicStatistics: GetBasicStatistics,
    private val ensureStarterData: EnsureStarterDataUseCase,
    private val calculateStreak: CalculateStreakUseCase,
    private val historyRepository: ReviewHistoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()
    private var refreshGeneration = 0L

    init { refresh() }

    fun refresh() {
        val generation = ++refreshGeneration
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            runCatching {
                ensureStarterData()
                val now = Instant.now()
                val summary = getProgressSummary(now)
                val basicStats = getBasicStatistics()
                val streak = calculateStreak.calculate(
                    historyRepository.getAll(), now, ZoneId.systemDefault()
                )
                Triple(summary, basicStats, streak)
            }.onSuccess { (summary, basicStats, streak) ->
                if (generation == refreshGeneration) {
                    _state.value = HomeUiState(
                        isLoading = false,
                        summary = summary,
                        basicStats = basicStats,
                        streak = streak
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
}
