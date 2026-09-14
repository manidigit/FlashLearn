package com.flashlearn.app.ui

import com.flashlearn.app.navigation.AppRoutes
import com.flashlearn.domain.model.VocabularyDifficulty

enum class AppearanceMode { SYSTEM, LIGHT, DARK }
enum class AppLayoutDirection { RTL, LTR }
enum class AccentColor { PURPLE, BLUE, GREEN, ORANGE, PINK }
enum class LearningLanguage(val code: String, val labelFa: String) {
    PERSIAN("fa", "فارسی"), SPANISH("es", "اسپانیایی"), ENGLISH("en", "انگلیسی")
}

enum class QuizChallenge(val label: String) { A("A"), B("B"), C("C") }

data class LanguagePair(
    val source: LearningLanguage = LearningLanguage.SPANISH,
    val target: LearningLanguage = LearningLanguage.PERSIAN
) {
    init { require(source != target) { "Learning language pair must contain two different languages" } }
    fun reversed() = LanguagePair(target, source)
}

data class AppUiState(
    val selectedRoute: String = AppRoutes.HOME,
    val selectedConceptId: java.util.UUID? = null,
    val appearance: AppearanceMode = AppearanceMode.SYSTEM,
    val accentColor: AccentColor = AccentColor.PURPLE,
    val layoutDirection: AppLayoutDirection = AppLayoutDirection.RTL,
    val languagePair: LanguagePair = LanguagePair(),
    val personalWordDifficulty: VocabularyDifficulty? = null,
    val quizChallenge: QuizChallenge = QuizChallenge.B,
    val difficultyThreshold: Int = 3
) {
    init {
        require(selectedRoute in AppRoutes.all()) { "Unknown application route: $selectedRoute" }
        require(difficultyThreshold in 1..20) { "difficultyThreshold must be between 1 and 20" }
    }
}
