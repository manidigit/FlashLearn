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
    val cornerLarge: Dp,
    val reviewBackground: Color,
    val reviewSurface: Color,
    val reviewSurfaceSelected: Color,
    val reviewAccent: Color,
    val reviewText: Color,
    val reviewMutedText: Color,
    val reviewBorder: Color,
    val reviewNav: Color,
    val reviewButton: Color,
    val reviewButtonContent: Color
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

    val reviewHeaderHeight get() = dp(118f)
    val reviewHeaderGap get() = dp(12f)
    val reviewBackButtonSize get() = dp(62f)
    val reviewBackIcon get() = dp(34f)
    val reviewBackElevation get() = dp(3f)
    val reviewOrnamentLine get() = dp(46f)
    val reviewOrnamentHeight get() = dp(2f)
    val reviewOrnamentIcon get() = dp(14f)
    val reviewTinyGap get() = dp(3f)
    val reviewSectionGap get() = dp(12f)
    val reviewItemGap get() = dp(10f)
    val reviewChoiceHeight get() = dp(82f)
    val reviewCategoryHeight get() = dp(78f)
    val reviewDifficultyHeight get() = dp(82f)
    val reviewQuizHeight get() = dp(82f)
    val reviewFullChoiceHeight get() = dp(54f)
    val reviewCountHeight get() = dp(52f)
    val reviewCardPadding get() = dp(18f)
    val reviewCompactPadding get() = dp(10f)
    val reviewContentPadding get() = dp(18f)
    val reviewIconLarge get() = dp(34f)
    val reviewIconMedium get() = dp(24f)
    val reviewCategoryIconTile get() = dp(48f)
    val reviewSelectedBadgeInset get() = dp(7f)
    val reviewSelectedBadgePadding get() = dp(3f)
    val reviewSelectedBadgeIcon get() = dp(16f)
    val reviewCardElevation get() = dp(2f)
    val reviewButtonHeight get() = dp(58f)
    val reviewPlayCircle get() = dp(34f)
    val reviewPlayIcon get() = dp(22f)
    val reviewNavHeight get() = dp(82f)
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
        cornerLarge = 24.dp,
        reviewBackground = Color(0xFFFFFBF2),
        reviewSurface = Color(0xFFFFFDF8),
        reviewSurfaceSelected = Color(0xFFFFF3D7),
        reviewAccent = Color(0xFF9B6A1D),
        reviewText = Color(0xFF3B2515),
        reviewMutedText = Color(0xFF7A6B5A),
        reviewBorder = Color(0xFFE4D7C1),
        reviewNav = Color(0xFFFFF8EA),
        reviewButton = Color(0xFFA56D12),
        reviewButtonContent = Color.White
    )
}
