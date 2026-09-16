package com.flashlearn.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class FlashLearnThemeTokens(
    val cardColor: Color,
    val outlineColor: Color,
    val gradientStart: Color,
    val gradientEnd: Color,
    val iconStyle: IconStyle,
    val elevationScale: Float,
    val densityScale: Float
) {
    val screenPadding get() = (20f * densityScale).dp
    val contentGap get() = (12f * densityScale).dp
    val compactGap get() = (8f * densityScale).dp
}

enum class IconStyle { OUTLINED, FILLED }

val LocalFlashLearnThemeTokens = staticCompositionLocalOf {
    FlashLearnThemeTokens(
        cardColor = Color.White,
        outlineColor = Color(0xFFE1DDE7),
        gradientStart = Color(0xFF7C3AED),
        gradientEnd = Color(0xFF536DFE),
        iconStyle = IconStyle.OUTLINED,
        elevationScale = 1f,
        densityScale = 1f
    )
}
