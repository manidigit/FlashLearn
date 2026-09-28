package com.flashlearn.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

enum class FlashLearnWidthClass { COMPACT, MEDIUM, EXPANDED }

@Composable
fun rememberFlashLearnWidthClass(): FlashLearnWidthClass {
    val width = LocalConfiguration.current.screenWidthDp.dp
    val tokens = LocalFlashLearnThemeTokens.current
    return when {
        width >= tokens.dp(tokens.design.metric("adaptiveExpandedBreakpoint")) -> FlashLearnWidthClass.EXPANDED
        width >= tokens.dp(tokens.design.metric("adaptiveMediumBreakpoint")) -> FlashLearnWidthClass.MEDIUM
        else -> FlashLearnWidthClass.COMPACT
    }
}
