package com.flashlearn.domain.settings

/** Stable persisted setting keys shared by domain, data and UI layers. */
object SettingsKeys {
    const val MAXIMUM_REVIEW_CARDS = "maximum_review_cards"
    const val THEME = "theme"
    const val COLOR_SCHEME = "color_scheme"
    const val THRESHOLD_DIFFICULTY = "threshold_difficulty"

    const val DEFAULT_MAXIMUM_REVIEW_CARDS = 30
    const val MINIMUM_REVIEW_CARDS = 1
    const val MAXIMUM_REVIEW_CARDS_LIMIT = 100
    const val DEFAULT_THRESHOLD_DIFFICULTY = 3
}
