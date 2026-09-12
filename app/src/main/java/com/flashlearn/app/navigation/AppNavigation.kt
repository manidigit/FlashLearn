package com.flashlearn.app.navigation

sealed interface AppDestination {
    val route: String

    data object Home : AppDestination { override val route = "home" }
    data object Review : AppDestination { override val route = "review" }
    data object Progress : AppDestination { override val route = "progress" }
    data object Settings : AppDestination { override val route = "settings" }
}

object AppRoutes {
    const val HOME = "home"
    const val REVIEW = "review"
    const val PROGRESS = "progress"
    const val SETTINGS = "settings"
    const val ADD_WORD = "add_word"
    const val BULK_IMPORT = "bulk_import"
    const val BACKUP = "backup"
    const val LIBRARY = "library"
    const val LIBRARY_DETAIL = "library_detail"

    fun all(): List<String> = listOf(HOME, REVIEW, PROGRESS, SETTINGS, ADD_WORD, BULK_IMPORT, BACKUP, LIBRARY, LIBRARY_DETAIL)
}
