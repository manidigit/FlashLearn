package com.flashlearn.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.flashlearn.app.navigation.AppRoutes
import com.flashlearn.app.ui.AppViewModel
import com.flashlearn.app.ui.addword.AddWordScreen
import com.flashlearn.app.ui.addword.AddWordViewModel
import com.flashlearn.app.ui.addword.BulkImportScreen
import com.flashlearn.app.ui.addword.BulkImportViewModel
import com.flashlearn.app.ui.home.HomeScreen
import com.flashlearn.app.ui.home.HomeViewModel
import com.flashlearn.app.ui.review.ReviewScreen
import com.flashlearn.app.ui.review.ReviewViewModel
import com.flashlearn.app.ui.progress.ProgressScreen
import com.flashlearn.app.ui.progress.ProgressViewModel
import com.flashlearn.app.ui.settings.SettingsScreen
import com.flashlearn.app.ui.backup.BackupScreen
import com.flashlearn.app.ui.backup.BackupViewModel
import com.flashlearn.app.ui.library.LibraryScreen
import com.flashlearn.app.ui.library.LibraryViewModel
import com.flashlearn.app.ui.library.LibraryDetailScreen
import com.flashlearn.app.ui.library.LibraryDetailViewModel
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
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppRootScreen(
                        appViewModel = appViewModel,
                        homeViewModel = homeViewModel,
                        reviewViewModel = reviewViewModel,
                        addWordViewModel = addWordViewModel,
                        progressViewModel = progressViewModel,
                        bulkImportViewModel = bulkImportViewModel,
                        backupViewModel = backupViewModel,
                        libraryViewModel = libraryViewModel,
                        libraryDetailViewModel = libraryDetailViewModel
                    )
                }
            }
        }
    }
}

@Composable
private fun AppRootScreen(
    appViewModel: AppViewModel,
    homeViewModel: HomeViewModel,
    reviewViewModel: ReviewViewModel,
    addWordViewModel: AddWordViewModel,
    progressViewModel: ProgressViewModel,
    bulkImportViewModel: BulkImportViewModel,
    backupViewModel: BackupViewModel,
    libraryViewModel: LibraryViewModel,
    libraryDetailViewModel: LibraryDetailViewModel
) {
    val uiState by appViewModel.state

    when (uiState.selectedRoute) {
        AppRoutes.LIBRARY -> LibraryScreen(
            libraryViewModel,
            onBack = {
                appViewModel.navigate(AppRoutes.HOME)
                homeViewModel.refresh()
            },
            onOpen = { id -> appViewModel.openLibraryDetail(id) }
        )
        AppRoutes.LIBRARY_DETAIL -> uiState.selectedConceptId?.let {
            LibraryDetailScreen(
                libraryDetailViewModel,
                it,
                onBack = {
                    libraryViewModel.refresh()
                    appViewModel.navigate(AppRoutes.LIBRARY)
                },
                onDeleted = {
                    libraryViewModel.refresh()
                    appViewModel.navigate(AppRoutes.LIBRARY)
                    homeViewModel.refresh()
                }
            )
        }
        AppRoutes.PROGRESS -> ProgressScreen(
            viewModel = progressViewModel,
            onBack = {
                appViewModel.navigate(AppRoutes.HOME)
                homeViewModel.refresh()
            }
        )
        AppRoutes.SETTINGS -> SettingsScreen(
            onBackup = { appViewModel.navigate(AppRoutes.BACKUP) },
            onBack = { appViewModel.navigate(AppRoutes.HOME) }
        )
        AppRoutes.BACKUP -> BackupScreen(
            viewModel = backupViewModel,
            onBack = { appViewModel.navigate(AppRoutes.SETTINGS) },
            onRestored = {
                homeViewModel.refresh()
                libraryViewModel.refresh()
                progressViewModel.refresh()
            }
        )
        AppRoutes.REVIEW -> ReviewScreen(
            viewModel = reviewViewModel,
            onFinished = {
                appViewModel.navigate(AppRoutes.HOME)
                homeViewModel.refresh()
            }
        )
        AppRoutes.BULK_IMPORT -> BulkImportScreen(
            viewModel = bulkImportViewModel,
            onBack = {
                appViewModel.navigate(AppRoutes.HOME)
                homeViewModel.refresh()
            }
        )
        AppRoutes.ADD_WORD -> AddWordScreen(
            viewModel = addWordViewModel,
            onBack = {
                appViewModel.navigate(AppRoutes.HOME)
                homeViewModel.refresh()
            }
        )
        else -> HomeScreen(
            viewModel = homeViewModel,
            onStartReview = { reviewType ->
                reviewViewModel.prepareReviewType(reviewType)
                appViewModel.navigate(AppRoutes.REVIEW)
            },
            onAddWord = { appViewModel.navigate(AppRoutes.ADD_WORD) },
            onBulkImport = { appViewModel.navigate(AppRoutes.BULK_IMPORT) },
            onLibrary = { appViewModel.navigate(AppRoutes.LIBRARY) },
            onProgress = {
                progressViewModel.refresh()
                appViewModel.navigate(AppRoutes.PROGRESS)
            },
            onSettings = { appViewModel.navigate(AppRoutes.SETTINGS) }
        )
    }
}
