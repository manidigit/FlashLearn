package com.flashlearn.app.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.flashlearn.app.navigation.AppRoutes

@HiltViewModel
class AppViewModel @Inject constructor() : ViewModel() {
    private val _state = mutableStateOf(AppUiState())
    val state: State<AppUiState> get() = _state

    fun openLibraryDetail(conceptId: java.util.UUID) { _state.value = _state.value.copy(selectedRoute = AppRoutes.LIBRARY_DETAIL, selectedConceptId = conceptId) }

    fun navigate(route: String) {
        require(route in AppRoutes.all()) { "Unknown application route: $route" }
        _state.value = _state.value.copy(selectedRoute = route, selectedConceptId = null)
    }
}
