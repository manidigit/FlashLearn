package com.flashlearn.app.ui.localization

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import java.util.Locale

class FlashLearnLocaleContext(base: Context) : ContextWrapper(base) {
    companion object {
        const val PREFS_NAME = "flashlearn_locale"
        const val KEY_LANGUAGE = "language"

        fun wrap(context: Context, language: String): Context {
            val normalized = FlashLearnLocales.normalize(language)
            val locale = Locale(normalized)
            Locale.setDefault(locale)
            val configuration = Configuration(context.resources.configuration)
            configuration.setLocale(locale)
            configuration.setLocales(android.os.LocaleList(locale))
            return FlashLearnLocaleContext(context.createConfigurationContext(configuration))
        }
    }
}
