package com.flashlearn.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

enum class FlashLearnWidthClass { COMPACT, MEDIUM, EXPANDED }

@Composable
fun rememberFlashLearnWidthClass(): FlashLearnWidthClass {
    val width = LocalConfiguration.current.screenWidthDp.dp
    return when {
        width >= 840.dp -> FlashLearnWidthClass.EXPANDED
        width >= 600.dp -> FlashLearnWidthClass.MEDIUM
        else -> FlashLearnWidthClass.COMPACT
    }
}
