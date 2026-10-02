package com.flashlearn.app.navigation

sealed interface AppDestination {
    val route: String
    data object Home : AppDestination { override val route = AppRoutes.HOME }
    data object Review : AppDestination { override val route = AppRoutes.REVIEW }
    data object NeedsReview : AppDestination { override val route = AppRoutes.NEEDS_REVIEW }
    data object Progress : AppDestination { override val route = AppRoutes.PROGRESS }
    data object Settings : AppDestination { override val route = AppRoutes.SETTINGS }
    data object Help : AppDestination { override val route = AppRoutes.HELP }
    data object About : AppDestination { override val route = AppRoutes.ABOUT }
    data object AddWord : AppDestination { override val route = AppRoutes.ADD_WORD }
    data object AddWordForm : AppDestination { override val route = AppRoutes.ADD_WORD_FORM }
    data object BulkImport : AppDestination { override val route = AppRoutes.BULK_IMPORT }
    data object Backup : AppDestination { override val route = AppRoutes.BACKUP }
    data object Library : AppDestination { override val route = AppRoutes.LIBRARY }
    data object LibraryDetail : AppDestination { override val route = AppRoutes.LIBRARY_DETAIL }
    data object CategorySelection : AppDestination { override val route = AppRoutes.CATEGORY_SELECTION }
}
object AppRoutes {
    const val HOME="home"; const val REVIEW="review"; const val NEEDS_REVIEW="needs_review"; const val PROGRESS="progress"; const val SETTINGS="settings"; const val HELP="help"; const val ABOUT="about"; const val ADD_WORD="add_word"; const val ADD_WORD_FORM="add_word_form"; const val BULK_IMPORT="bulk_import"; const val BACKUP="backup"; const val LIBRARY="library"; const val LIBRARY_DETAIL="library_detail"; const val CATEGORY_SELECTION="category_selection"
    fun all(): List<String> = listOf(HOME,REVIEW,NEEDS_REVIEW,PROGRESS,SETTINGS,HELP,ABOUT,ADD_WORD,ADD_WORD_FORM,BULK_IMPORT,BACKUP,LIBRARY,LIBRARY_DETAIL,CATEGORY_SELECTION)
}
