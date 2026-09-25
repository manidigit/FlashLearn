package com.flashlearn.app.ui

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flashlearn.app.navigation.AppRoutes
import com.flashlearn.app.ui.theme.FlashLearnThemeSpec
import com.flashlearn.data.repository.RoomSettingsRepository
import com.flashlearn.domain.model.QuizDifficulty
import com.flashlearn.domain.model.VocabularyDifficulty
import com.flashlearn.domain.settings.SettingsKeys
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class AppViewModel @Inject constructor(@ApplicationContext context: Context, private val settingsRepository: RoomSettingsRepository) : ViewModel() {
    companion object {
        private const val PREFS = "flashlearn_ui_settings"
        private const val KEY_APPEARANCE = "appearance"
        private const val KEY_ACCENT = "accent"
        private const val KEY_THEME = "theme"
        private const val KEY_THEME_MIGRATION_621 = "theme_migration_621"
        private const val KEY_THEME_MIGRATION_GROK = "theme_migration_grok_623"
        private const val KEY_LAYOUT = "layout"
        private const val KEY_SOURCE = "language_source"
        private const val KEY_TARGET = "language_target"
        private const val KEY_PERSONAL_DIFFICULTY = "personal_difficulty"
        private const val KEY_QUIZ_DIFFICULTY = "quiz_difficulty"
    }
    private val appContext = context.applicationContext
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val _state = mutableStateOf(loadPersistedState())
    val state: State<AppUiState> get() = _state
    init { viewModelScope.launch { val threshold = settingsRepository.getInt(SettingsKeys.THRESHOLD_DIFFICULTY, SettingsKeys.DEFAULT_THRESHOLD_DIFFICULTY).coerceIn(1,20); val maxCards = settingsRepository.getInt(SettingsKeys.MAXIMUM_REVIEW_CARDS, SettingsKeys.DEFAULT_MAXIMUM_REVIEW_CARDS).coerceIn(SettingsKeys.MINIMUM_REVIEW_CARDS, SettingsKeys.MAXIMUM_REVIEW_CARDS_LIMIT); val q = runCatching { QuizDifficulty.valueOf(settingsRepository.getString(KEY_QUIZ_DIFFICULTY, QuizDifficulty.MEDIUM.name)) }.getOrDefault(QuizDifficulty.MEDIUM); _state.value = _state.value.copy(difficultyThreshold=threshold, maximumReviewCards=maxCards, quizDifficulty=q) } }
    private fun loadPersistedState(): AppUiState { val languages=LearningLanguage.entries; val d=AppUiState(); val s=prefs.getInt(KEY_SOURCE,d.languagePair.source.ordinal).coerceIn(languages.indices); var t=prefs.getInt(KEY_TARGET,d.languagePair.target.ordinal).coerceIn(languages.indices); if(s==t)t=(t+1)%languages.size; val storedTheme = prefs.getString(KEY_THEME, null)
        var migratedTheme = if (!prefs.getBoolean(KEY_THEME_MIGRATION_621, false) && storedTheme == "modern_purple") "modern_minimal" else (storedTheme ?: "grok")
        if (!prefs.getBoolean(KEY_THEME_MIGRATION_621, false)) prefs.edit().putString(KEY_THEME, migratedTheme).putBoolean(KEY_THEME_MIGRATION_621, true).apply()
        // One-time: surface Grok theme for users still on modern_minimal after 6.23
        if (!prefs.getBoolean(KEY_THEME_MIGRATION_GROK, false)) {
            if (migratedTheme == "modern_minimal" || migratedTheme == "luxury") migratedTheme = "grok"
            prefs.edit().putString(KEY_THEME, migratedTheme).putBoolean(KEY_THEME_MIGRATION_GROK, true).apply()
        }
        return d.copy(appearance=AppearanceMode.entries.getOrElse(prefs.getInt(KEY_APPEARANCE,0)){AppearanceMode.SYSTEM},accentColor=AccentColor.entries.getOrElse(prefs.getInt(KEY_ACCENT,0)){AccentColor.PURPLE},themeId=migratedTheme,layoutDirection=AppLayoutDirection.entries.getOrElse(prefs.getInt(KEY_LAYOUT,0)){AppLayoutDirection.RTL},languagePair=LanguagePair(languages[s],languages[t]),personalWordDifficulty=VocabularyDifficulty.entries.getOrNull(prefs.getInt(KEY_PERSONAL_DIFFICULTY,-1))) }
    fun setAppearance(v:AppearanceMode){_state.value=_state.value.copy(appearance=v);prefs.edit().putInt(KEY_APPEARANCE,v.ordinal).apply()}
    fun setAccentColor(v:AccentColor){_state.value=_state.value.copy(accentColor=v);prefs.edit().putInt(KEY_ACCENT,v.ordinal).apply()}
    fun setTheme(id:String){_state.value=_state.value.copy(themeId=id);prefs.edit().putString(KEY_THEME,id).apply()}
    fun importTheme(uri:Uri):Boolean=runCatching{val raw=appContext.contentResolver.openInputStream(uri)?.bufferedReader()?.use{it.readText()}?:return false;val spec=FlashLearnThemeSpec.fromJson(raw);FlashLearnThemeSpec.saveCustom(appContext,spec);setTheme(spec.id);true}.getOrDefault(false)
    fun availableThemes():List<FlashLearnThemeSpec> =FlashLearnThemeSpec.BUILT_IN+FlashLearnThemeSpec.loadCustom(appContext)
    fun setLayoutDirection(v:AppLayoutDirection){_state.value=_state.value.copy(layoutDirection=v);prefs.edit().putInt(KEY_LAYOUT,v.ordinal).apply()}
    fun setLanguagePair(v:LanguagePair){if(v.source==v.target)return;_state.value=_state.value.copy(languagePair=v);prefs.edit().putInt(KEY_SOURCE,v.source.ordinal).putInt(KEY_TARGET,v.target.ordinal).apply()}
    fun reverseLanguagePair()=setLanguagePair(_state.value.languagePair.reversed())
    fun setPersonalWordDifficulty(v:VocabularyDifficulty?){_state.value=_state.value.copy(personalWordDifficulty=v);prefs.edit().putInt(KEY_PERSONAL_DIFFICULTY,v?.ordinal?:-1).apply()}
    fun setQuizDifficulty(v:QuizDifficulty){_state.value=_state.value.copy(quizDifficulty=v);viewModelScope.launch{settingsRepository.setString(KEY_QUIZ_DIFFICULTY,v.name)}}
    fun setDifficultyThreshold(v:Int){val x=v.coerceIn(1,20);_state.value=_state.value.copy(difficultyThreshold=x);viewModelScope.launch{settingsRepository.setInt(SettingsKeys.THRESHOLD_DIFFICULTY,x)}}
    fun setMaximumReviewCards(v:Int){val x=v.coerceIn(SettingsKeys.MINIMUM_REVIEW_CARDS,SettingsKeys.MAXIMUM_REVIEW_CARDS_LIMIT);_state.value=_state.value.copy(maximumReviewCards=x);viewModelScope.launch{settingsRepository.setInt(SettingsKeys.MAXIMUM_REVIEW_CARDS,x)}}
    fun openLibraryDetail(id:UUID){_state.value=_state.value.copy(selectedRoute=AppRoutes.LIBRARY_DETAIL,selectedConceptId=id)}
    fun goBack(){when(_state.value.selectedRoute){AppRoutes.LIBRARY_DETAIL->_state.value=_state.value.copy(selectedRoute=AppRoutes.LIBRARY,selectedConceptId=null);AppRoutes.BACKUP->_state.value=_state.value.copy(selectedRoute=AppRoutes.SETTINGS,selectedConceptId=null);AppRoutes.HOME->Unit;else->_state.value=_state.value.copy(selectedRoute=AppRoutes.HOME,selectedConceptId=null)}}
    fun navigate(route:String){require(route in AppRoutes.all());_state.value=_state.value.copy(selectedRoute=route,selectedConceptId=null)}
}
