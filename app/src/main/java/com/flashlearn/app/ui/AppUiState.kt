package com.flashlearn.app.ui

import com.flashlearn.app.navigation.AppRoutes

data class AppUiState(
    val selectedRoute: String = AppRoutes.HOME,
    val selectedConceptId: java.util.UUID? = null
) {
    init {
        require(selectedRoute in AppRoutes.all()) {
            "Unknown application route: $selectedRoute"
        }
    }
}
