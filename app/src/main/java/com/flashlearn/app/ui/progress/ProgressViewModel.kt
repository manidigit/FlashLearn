package com.flashlearn.app.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flashlearn.domain.progress.CalculateProgressUseCase
import com.flashlearn.domain.statistics.CalculateStatisticsUseCase
import com.flashlearn.domain.statistics.CalculateStreakUseCase
import com.flashlearn.domain.statistics.StatisticsSnapshot
import com.flashlearn.domain.statistics.StreakSnapshot
import com.flashlearn.domain.model.ProgressSummary
import com.flashlearn.domain.usecase.GetProgressSummaryUseCase
import com.flashlearn.domain.repository.ReviewHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ProgressUiState(
    val loading: Boolean = true,
    val progress: com.flashlearn.domain.progress.ProgressSnapshot? = null,
    val summary: ProgressSummary? = null,
    val statistics: StatisticsSnapshot? = null,
    val streak: StreakSnapshot? = null,
    val error: String? = null
)

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val calculateProgress: CalculateProgressUseCase,
    private val calculateStatistics: CalculateStatisticsUseCase,
    private val calculateStreak: CalculateStreakUseCase,
    private val getProgressSummary: GetProgressSummaryUseCase,
    private val historyRepository: ReviewHistoryRepository
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
                val streak = calculateStreak.calculate(historyRepository.getAll(), now, zoneId)
                Quadruple(progress, summary, stats, streak)
            }.onSuccess { (progress, summary, stats, streak) ->
                if (generation == refreshGeneration) {
                    _state.value = ProgressUiState(false, progress, summary, stats, streak, null)
                }
            }.onFailure {
                if (generation == refreshGeneration) {
                    _state.value = _state.value.copy(loading = false, error = it.message ?: "Unknown error")
                }
            }
        }
    }
}


private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
