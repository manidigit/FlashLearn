package com.flashlearn.app.ui.localization

import java.util.Locale

object FlashLearnLocales {
    const val PERSIAN = "fa"
    const val ENGLISH = "en"

    val supported: Set<String> = setOf(PERSIAN, ENGLISH)

    fun normalize(languageTag: String?): String =
        languageTag?.lowercase(Locale.ROOT)?.substringBefore('-')?.substringBefore('_')
            ?.takeIf { it in supported } ?: PERSIAN
}
