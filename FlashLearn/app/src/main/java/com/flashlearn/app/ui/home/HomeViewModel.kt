package com.flashlearn.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flashlearn.domain.model.ProgressSummary
import com.flashlearn.domain.model.Stage
import com.flashlearn.domain.repository.ConceptRepository
import com.flashlearn.domain.repository.LearningStateRepository
import com.flashlearn.domain.repository.ReviewHistoryRepository
import com.flashlearn.domain.statistics.BasicStatistics
import com.flashlearn.domain.statistics.CalculateStreakUseCase
import com.flashlearn.domain.statistics.GetBasicStatistics
import com.flashlearn.domain.statistics.StreakSnapshot
import com.flashlearn.domain.progress.CalculateProgressPercentage
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
    private val calculateStreak: CalculateStreakUseCase,
    private val historyRepository: ReviewHistoryRepository,
    private val learningStateRepository: LearningStateRepository,
    private val conceptRepository: ConceptRepository,
    private val calculateProgressPercentage: CalculateProgressPercentage
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
                val progressPercentage = calculateProgressPercentage()
                val activeConceptIds = conceptRepository.getAllActive().map { it.id }.toSet()
                val dailyTotal = learningStateRepository.getAllByStage(Stage.DAILY).count { it.conceptId in activeConceptIds }
                val weeklyTotal = learningStateRepository.getAllByStage(Stage.WEEKLY).count { it.conceptId in activeConceptIds }
                val monthlyTotal = learningStateRepository.getAllByStage(Stage.MONTHLY).count { it.conceptId in activeConceptIds }
                HomeSnapshot(summary, basicStats, streak, progressPercentage, dailyTotal, weeklyTotal, monthlyTotal)
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
