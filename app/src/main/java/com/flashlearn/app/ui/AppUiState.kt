package com.flashlearn.app.ui

import com.flashlearn.app.navigation.AppRoutes

enum class AppearanceMode { SYSTEM, LIGHT, DARK }

data class AppUiState(
    val selectedRoute: String = AppRoutes.HOME,
    val selectedConceptId: java.util.UUID? = null,
    val appearance: AppearanceMode = AppearanceMode.SYSTEM
) {
    init {
        require(selectedRoute in AppRoutes.all()) { "Unknown application route: $selectedRoute" }
    }
}
