package com.flashlearn.app.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.flashlearn.app.navigation.AppRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor() : ViewModel() {
    private val _state = mutableStateOf(AppUiState())
    val state: State<AppUiState> get() = _state

    fun setAppearance(mode: AppearanceMode) {
        _state.value = _state.value.copy(appearance = mode)
    }

    fun openLibraryDetail(conceptId: UUID) {
        _state.value = _state.value.copy(selectedRoute = AppRoutes.LIBRARY_DETAIL, selectedConceptId = conceptId)
    }

    fun goBack() {
        when (_state.value.selectedRoute) {
            AppRoutes.LIBRARY_DETAIL -> _state.value = _state.value.copy(selectedRoute = AppRoutes.LIBRARY, selectedConceptId = null)
            AppRoutes.BACKUP -> _state.value = _state.value.copy(selectedRoute = AppRoutes.SETTINGS, selectedConceptId = null)
            AppRoutes.HOME -> Unit
            else -> _state.value = _state.value.copy(selectedRoute = AppRoutes.HOME, selectedConceptId = null)
        }
    }

    fun navigate(route: String) {
        require(route in AppRoutes.all()) { "Unknown application route: $route" }
        _state.value = _state.value.copy(selectedRoute = route, selectedConceptId = null)
    }
}
