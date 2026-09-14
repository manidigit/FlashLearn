package com.flashlearn.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.flashlearn.app.navigation.AppRoutes
import com.flashlearn.app.ui.AppLayoutDirection
import com.flashlearn.app.ui.AppViewModel
import com.flashlearn.app.ui.addword.AddWordScreen
import com.flashlearn.app.ui.addword.AddWordViewModel
import com.flashlearn.app.ui.addword.BulkImportScreen
import com.flashlearn.app.ui.addword.BulkImportViewModel
import com.flashlearn.app.ui.backup.BackupScreen
import com.flashlearn.app.ui.backup.BackupViewModel
import com.flashlearn.app.ui.components.FlashLearnShell
import com.flashlearn.app.ui.home.HomeScreen
import com.flashlearn.app.ui.home.HomeViewModel
import com.flashlearn.app.ui.library.LibraryDetailScreen
import com.flashlearn.app.ui.library.LibraryDetailViewModel
import com.flashlearn.app.ui.library.LibraryScreen
import com.flashlearn.app.ui.library.LibraryViewModel
import com.flashlearn.app.ui.progress.ProgressScreen
import com.flashlearn.app.ui.progress.ProgressViewModel
import com.flashlearn.app.ui.review.ReviewScreen
import com.flashlearn.app.ui.review.ReviewViewModel
import com.flashlearn.app.ui.settings.SettingsScreen
import com.flashlearn.app.ui.theme.FlashLearnTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val appViewModel: AppViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()
    private val reviewViewModel: ReviewViewModel by viewModels()
    private val addWordViewModel: AddWordViewModel by viewModels()
    private val progressViewModel: ProgressViewModel by viewModels()
    private val bulkImportViewModel: BulkImportViewModel by viewModels()
    private val backupViewModel: BackupViewModel by viewModels()
    private val libraryViewModel: LibraryViewModel by viewModels()
    private val libraryDetailViewModel: LibraryDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val appState by appViewModel.state
            FlashLearnTheme(appearance = appState.appearance, accentColor = appState.accentColor) {
                CompositionLocalProvider(LocalLayoutDirection provides if (appState.layoutDirection == AppLayoutDirection.RTL) LayoutDirection.Rtl else LayoutDirection.Ltr) {
                    Surface(Modifier.fillMaxSize()) {
                        BackHandler { if (appViewModel.state.value.selectedRoute == AppRoutes.HOME) finish() else appViewModel.goBack() }
                        AppRootScreen()
                    }
                }
            }
        }
    }

    @Composable private fun AppRootScreen() {
        val uiState by appViewModel.state
        val topLevel = uiState.selectedRoute in setOf(AppRoutes.HOME, AppRoutes.REVIEW, AppRoutes.LIBRARY, AppRoutes.PROGRESS, AppRoutes.SETTINGS)
        if (topLevel) {
            FlashLearnShell(selectedRoute = uiState.selectedRoute, onNavigate = { route ->
                appViewModel.navigate(route)
                when (route) { AppRoutes.HOME -> homeViewModel.refresh(); AppRoutes.LIBRARY -> libraryViewModel.refresh(); AppRoutes.PROGRESS -> progressViewModel.refresh() }
            }) { TopLevelContent(uiState.selectedRoute) }
        } else when (uiState.selectedRoute) {
            AppRoutes.LIBRARY_DETAIL -> uiState.selectedConceptId?.let { id -> LibraryDetailScreen(libraryDetailViewModel, id, onBack = { libraryViewModel.refresh(); appViewModel.navigate(AppRoutes.LIBRARY) }, onDeleted = { libraryViewModel.refresh(); homeViewModel.refresh(); appViewModel.navigate(AppRoutes.LIBRARY) }) }
            AppRoutes.ADD_WORD -> AddWordScreen(addWordViewModel, languagePair = uiState.languagePair) { appViewModel.navigate(AppRoutes.LIBRARY); libraryViewModel.refresh(); homeViewModel.refresh() }
            AppRoutes.BULK_IMPORT -> BulkImportScreen(bulkImportViewModel) { appViewModel.navigate(AppRoutes.LIBRARY); libraryViewModel.refresh(); homeViewModel.refresh() }
            AppRoutes.BACKUP -> BackupScreen(backupViewModel, onBack = { appViewModel.navigate(AppRoutes.SETTINGS) }, onRestored = { homeViewModel.refresh(); libraryViewModel.refresh(); progressViewModel.refresh() })
        }
    }

    @Composable private fun TopLevelContent(route: String) {
        val uiState by appViewModel.state
        when (route) {
            AppRoutes.HOME -> HomeScreen(homeViewModel, onStartReview = { type -> reviewViewModel.prepareReviewType(type); appViewModel.navigate(AppRoutes.REVIEW) }, onAddWord = { appViewModel.navigate(AppRoutes.ADD_WORD) }, onBulkImport = { appViewModel.navigate(AppRoutes.BULK_IMPORT) }, onLibrary = { appViewModel.navigate(AppRoutes.LIBRARY) }, onProgress = { progressViewModel.refresh(); appViewModel.navigate(AppRoutes.PROGRESS) }, onSettings = { appViewModel.navigate(AppRoutes.SETTINGS) })
            AppRoutes.REVIEW -> ReviewScreen(reviewViewModel, personalDifficulty = uiState.personalWordDifficulty, quizChallenge = uiState.quizChallenge) { homeViewModel.refresh(); progressViewModel.refresh(); appViewModel.navigate(AppRoutes.HOME) }
            AppRoutes.LIBRARY -> LibraryScreen(libraryViewModel, onBack = { appViewModel.navigate(AppRoutes.HOME) }, onOpen = appViewModel::openLibraryDetail, onAddWord = { appViewModel.navigate(AppRoutes.ADD_WORD) })
            AppRoutes.PROGRESS -> ProgressScreen(progressViewModel) { appViewModel.navigate(AppRoutes.HOME) }
            AppRoutes.SETTINGS -> SettingsScreen(appearance = uiState.appearance, accentColor = uiState.accentColor, onAppearanceChange = appViewModel::setAppearance, onAccentColorChange = appViewModel::setAccentColor, layoutDirection = uiState.layoutDirection, onLayoutDirectionChange = appViewModel::setLayoutDirection, languagePair = uiState.languagePair, onLanguagePairChange = appViewModel::setLanguagePair, personalWordDifficulty = uiState.personalWordDifficulty, onPersonalWordDifficultyChange = appViewModel::setPersonalWordDifficulty, quizChallenge = uiState.quizChallenge, onQuizChallengeChange = appViewModel::setQuizChallenge, onBackup = { appViewModel.navigate(AppRoutes.BACKUP) }, onImportExport = { appViewModel.navigate(AppRoutes.BACKUP) }, onBack = { appViewModel.navigate(AppRoutes.HOME) })
        }
    }
}