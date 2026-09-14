package com.flashlearn.app.ui

import com.flashlearn.app.navigation.AppRoutes

enum class AppearanceMode { SYSTEM, LIGHT, DARK }
enum class AppLayoutDirection { RTL, LTR }
enum class AccentColor { PURPLE, BLUE, GREEN, ORANGE, PINK }
enum class LearningLanguage(val code: String, val labelFa: String) {
    PERSIAN("fa", "فارسی"),
    SPANISH("es", "اسپانیایی"),
    ENGLISH("en", "انگلیسی")
}

data class LanguagePair(
    val source: LearningLanguage = LearningLanguage.SPANISH,
    val target: LearningLanguage = LearningLanguage.PERSIAN
) {
    fun reversed() = LanguagePair(target, source)
}

data class AppUiState(
    val selectedRoute: String = AppRoutes.HOME,
    val selectedConceptId: java.util.UUID? = null,
    val appearance: AppearanceMode = AppearanceMode.SYSTEM,
    val accentColor: AccentColor = AccentColor.PURPLE,
    val layoutDirection: AppLayoutDirection = AppLayoutDirection.RTL,
    val languagePair: LanguagePair = LanguagePair()
) {
    init { require(selectedRoute in AppRoutes.all()) { "Unknown application route: $selectedRoute" } }
}
