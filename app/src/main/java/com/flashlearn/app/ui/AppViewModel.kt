package com.flashlearn.app.ui

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flashlearn.app.navigation.AppRoutes
import com.flashlearn.data.repository.RoomSettingsRepository
import com.flashlearn.domain.model.VocabularyDifficulty
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class AppViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val settingsRepository: RoomSettingsRepository
) : ViewModel() {
    companion object {
        private const val PREFS = "flashlearn_ui_settings"
        private const val KEY_APPEARANCE = "appearance"
        private const val KEY_ACCENT = "accent"
        private const val KEY_LAYOUT = "layout"
        private const val KEY_SOURCE = "language_source"
        private const val KEY_TARGET = "language_target"
        private const val KEY_PERSONAL_DIFFICULTY = "personal_difficulty"
        private const val KEY_QUIZ_CHALLENGE = "quiz_challenge"
        private const val KEY_THRESHOLD = "threshold_difficulty"
        private const val DEFAULT_THRESHOLD = 3
    }

    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val _state = mutableStateOf(loadPersistedState())
    val state: State<AppUiState> get() = _state

    init {
        viewModelScope.launch {
            val threshold = settingsRepository.getInt(KEY_THRESHOLD, DEFAULT_THRESHOLD).coerceIn(1, 20)
            _state.value = _state.value.copy(difficultyThreshold = threshold)
        }
    }

    private fun loadPersistedState(): AppUiState {
        val languages = LearningLanguage.entries
        val defaults = AppUiState()
        val sourceIndex = prefs.getInt(KEY_SOURCE, defaults.languagePair.source.ordinal).coerceIn(languages.indices)
        var targetIndex = prefs.getInt(KEY_TARGET, defaults.languagePair.target.ordinal).coerceIn(languages.indices)
        if (sourceIndex == targetIndex) targetIndex = (targetIndex + 1) % languages.size
        return defaults.copy(
            appearance = AppearanceMode.entries.getOrElse(prefs.getInt(KEY_APPEARANCE, AppearanceMode.SYSTEM.ordinal)) { AppearanceMode.SYSTEM },
            accentColor = AccentColor.entries.getOrElse(prefs.getInt(KEY_ACCENT, AccentColor.PURPLE.ordinal)) { AccentColor.PURPLE },
            layoutDirection = AppLayoutDirection.entries.getOrElse(prefs.getInt(KEY_LAYOUT, AppLayoutDirection.RTL.ordinal)) { AppLayoutDirection.RTL },
            languagePair = LanguagePair(languages[sourceIndex], languages[targetIndex]),
            personalWordDifficulty = VocabularyDifficulty.entries.getOrNull(prefs.getInt(KEY_PERSONAL_DIFFICULTY, -1)),
            quizChallenge = QuizChallenge.entries.getOrElse(prefs.getInt(KEY_QUIZ_CHALLENGE, QuizChallenge.B.ordinal)) { QuizChallenge.B },
            difficultyThreshold = DEFAULT_THRESHOLD
        )
    }

    fun setAppearance(mode: AppearanceMode) {
        _state.value = _state.value.copy(appearance = mode)
        prefs.edit().putInt(KEY_APPEARANCE, mode.ordinal).commit()
    }

    fun setAccentColor(color: AccentColor) {
        _state.value = _state.value.copy(accentColor = color)
        prefs.edit().putInt(KEY_ACCENT, color.ordinal).commit()
    }

    fun setLayoutDirection(direction: AppLayoutDirection) {
        _state.value = _state.value.copy(layoutDirection = direction)
        prefs.edit().putInt(KEY_LAYOUT, direction.ordinal).commit()
    }

    fun setLanguagePair(pair: LanguagePair) {
        if (pair.source == pair.target) return
        _state.value = _state.value.copy(languagePair = pair)
        prefs.edit().putInt(KEY_SOURCE, pair.source.ordinal).putInt(KEY_TARGET, pair.target.ordinal).commit()
    }

    fun reverseLanguagePair() = setLanguagePair(_state.value.languagePair.reversed())

    fun setPersonalWordDifficulty(value: VocabularyDifficulty?) {
        _state.value = _state.value.copy(personalWordDifficulty = value)
        prefs.edit().putInt(KEY_PERSONAL_DIFFICULTY, value?.ordinal ?: -1).commit()
    }

    fun setQuizChallenge(value: QuizChallenge) {
        _state.value = _state.value.copy(quizChallenge = value)
        prefs.edit().putInt(KEY_QUIZ_CHALLENGE, value.ordinal).commit()
    }

    fun setDifficultyThreshold(value: Int) {
        val safe = value.coerceIn(1, 20)
        _state.value = _state.value.copy(difficultyThreshold = safe)
        viewModelScope.launch { settingsRepository.setInt(KEY_THRESHOLD, safe) }
    }

    fun openLibraryDetail(conceptId: UUID) {
        _state.value = _state.value.copy(selectedRoute = AppRoutes.LIBRARY_DETAIL, selectedConceptId = conceptId)
    }

    fun goBack() {
        when (_state.value.selectedRoute) {
            AppRoutes.LIBRARY_DETAIL -> _state.value = _state.value.copy(selectedRoute = AppRoutes.LIBRARY, selectedConceptId = null)
            AppRoutes.BACKUP -> _state.value = _state.value.copy(selectedRoute = AppRoutes.SETTINGS, selectedConceptId = null)
            AppRoutes.HOME -> Unit
            else -> _state.value = _state.value.copy(selectedRoute = AppRoutes.HOME, selectedConceptId = null)
        }
    }

    fun navigate(route: String) {
        require(route in AppRoutes.all()) { "Unknown application route: $route" }
        _state.value = _state.value.copy(selectedRoute = route, selectedConceptId = null)
    }
}
