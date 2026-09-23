package com.flashlearn.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class FlashLearnThemeTokens(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val cardColor: Color,
    val elevatedCardColor: Color,
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val onBackground: Color,
    val onPrimary: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val outlineColor: Color,
    val dividerColor: Color,
    val success: Color,
    val warning: Color,
    val error: Color,
    val gradientStart: Color,
    val gradientEnd: Color,
    val iconStyle: IconStyle,
    val elevationScale: Float,
    val densityScale: Float,
    val typographyScale: Float,
    val cornerSmall: Dp,
    val cornerMedium: Dp,
    val cornerLarge: Dp
) {
    fun dp(value: Float): Dp = value.dp

    // Semantic layout tokens. Screens should consume these instead of repeating
    // raw dp values that represent the design system.
    val screenPadding get() = dp(20f)
    val screenVerticalPadding get() = dp(12f)
    val contentPadding get() = dp(16f)
    val cardPadding get() = dp(16f)
    val compactPadding get() = dp(8f)
    val tinyGap get() = dp(4f)
    val microGap get() = dp(6f)
    val contentGap get() = dp(12f)
    val compactGap get() = dp(8f)
    val sectionGap get() = dp(16f)
    val itemGap get() = dp(10f)
    val headerHeight get() = dp(58f)
    val headerPadding get() = dp(10f)
    val controlHeight get() = dp(52f)
    val buttonHeight get() = dp(52f)
    val fieldHeight get() = dp(52f)
    val cardMinHeight get() = dp(84f)
    val statCardHeight get() = dp(132f)
    val largeChoiceHeight get() = dp(96f)
    val mediumChoiceHeight get() = dp(72f)
    val chartHeight get() = dp(210f)
    val progressTrackHeight get() = dp(9f)
    val borderThin get() = dp(1f)
    val borderStrong get() = dp(2f)
    val borderEmphasis get() = dp(3f)
    val iconTileSize get() = dp(48f)
    val choiceIconSize get() = dp(30f)
    val iconSmall get() = dp(20f)
    val iconMedium get() = dp(24f)
    val iconLarge get() = dp(28f)
    val cardElevation get() = dp(4f * elevationScale)
    val navHeight get() = dp(76f)
    val smallCorner get() = cornerSmall
    val mediumCorner get() = cornerMedium
    val largeCorner get() = cornerLarge
}

enum class IconStyle { OUTLINED, FILLED }

val LocalFlashLearnThemeTokens = staticCompositionLocalOf {
    FlashLearnThemeTokens(
        background = Color(0xFFF8F7FC),
        surface = Color.White,
        surfaceVariant = Color(0xFFF0EDF6),
        cardColor = Color.White,
        elevatedCardColor = Color.White,
        primary = Color(0xFF7C3AED),
        secondary = Color(0xFF536DFE),
        tertiary = Color(0xFF536DFE),
        onBackground = Color(0xFF17141C),
        onPrimary = Color.White,
        onSurface = Color(0xFF17141C),
        onSurfaceVariant = Color(0xFF68636F),
        outlineColor = Color(0xFFE1DDE7),
        dividerColor = Color(0xFFE8E4ED),
        success = Color(0xFF138A5B),
        warning = Color(0xFFF59E0B),
        error = Color(0xFFD92D48),
        gradientStart = Color(0xFF7C3AED),
        gradientEnd = Color(0xFF536DFE),
        iconStyle = IconStyle.OUTLINED,
        elevationScale = 1f,
        densityScale = 1f,
        typographyScale = 1f,
        cornerSmall = 14.dp,
        cornerMedium = 18.dp,
        cornerLarge = 24.dp
    )
}
