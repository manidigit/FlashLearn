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
    val reviewButtonContent: Color,
    // Semantic status roles. Screens must use these instead of owning palettes.
    val info: Color,
    val critical: Color,
    // Visual personality (driven by the active theme)
    val cardBorderAlpha: Float = 0.45f,
    val cardBorderStrongAlpha: Float = 0.7f,
    val accentSurfaceAlpha: Float = 0.12f,
    val hierarchyBoost: Float = 1f,
    val preferFilledButtons: Boolean = false
) {
    fun dp(value: Float): Dp = value.dp

    // Semantic layout tokens. Screens should consume these instead of repeating
    // raw dp values that represent the design system.
    val screenPadding get() = dp(20f * densityScale)
    val screenVerticalPadding get() = dp(12f * densityScale)
    val contentPadding get() = dp(16f * densityScale)
    val cardPadding get() = dp(16f * densityScale)
    val compactPadding get() = dp(8f * densityScale)
    val tinyGap get() = dp(4f * densityScale)
    val microGap get() = dp(6f * densityScale)
    val contentGap get() = dp(12f * densityScale)
    val compactGap get() = dp(8f * densityScale)
    val sectionGap get() = dp(16f * densityScale)
    val itemGap get() = dp(8f * densityScale)
    val headerHeight get() = dp(58f * densityScale)
    val headerPadding get() = dp(10f * densityScale)
    val controlHeight get() = dp(52f * densityScale)
    val buttonHeight get() = dp(52f * densityScale)
    val fieldHeight get() = dp(52f * densityScale)
    val cardMinHeight get() = dp(84f * densityScale)
    val statCardHeight get() = dp(132f * densityScale)
    val largeChoiceHeight get() = dp(96f * densityScale)
    val mediumChoiceHeight get() = dp(72f * densityScale)
    val chartHeight get() = dp(210f * densityScale)
    val progressTrackHeight get() = dp(9f)
    val borderThin get() = dp(1f)
    val borderStrong get() = dp(2f)
    val borderEmphasis get() = dp(3f)
    val iconTileSize get() = dp(48f * densityScale)
    val choiceIconSize get() = dp(30f * densityScale)
    val iconSmall get() = dp(20f)
    val iconMedium get() = dp(24f)
    val iconLarge get() = dp(28f)
    val cardElevation get() = dp(4f * elevationScale)
    val cardElevationStrong get() = dp(8f * elevationScale)
    val navHeight get() = dp(76f * densityScale)
    val libraryHeaderHeight get() = dp(58f)
    val librarySearchHeight get() = dp(58f)
    val libraryStatHeight get() = statCardHeight
    val libraryStatIconSize get() = dp(42f)
    val libraryCardCorner get() = largeCorner
    val librarySearchCorner get() = mediumCorner
    val libraryCategoryCorner get() = largeCorner
    val libraryIconTileSize get() = dp(48f)
    val libraryIconTileCorner get() = smallCorner
    val libraryWordIconSize get() = dp(25f)
    val libraryFavoriteIconSize get() = dp(31f)
    val libraryDifficultyHorizontalPadding get() = dp(16f)
    val libraryDifficultyVerticalPadding get() = dp(7f)
    val smallCorner get() = cornerSmall
    val mediumCorner get() = cornerMedium
    val largeCorner get() = cornerLarge

    val reviewHeaderHeight get() = dp(92f)
    val reviewHeaderGap get() = dp(8f)
    val reviewBackButtonSize get() = dp(50f)
    val reviewBackIcon get() = dp(28f)
    val reviewBackElevation get() = dp(2f)
    val reviewOrnamentLine get() = dp(36f)
    val reviewOrnamentHeight get() = dp(2f)
    val reviewOrnamentIcon get() = dp(12f)
    val reviewTinyGap get() = dp(3f)
    val reviewSectionGap get() = dp(7f)
    val reviewItemGap get() = dp(7f)
    val reviewChoiceHeight get() = dp(58f)
    val reviewCategoryHeight get() = dp(56f)
    val reviewDifficultyHeight get() = dp(56f)
    val reviewQuizHeight get() = dp(56f)
    val reviewFullChoiceHeight get() = dp(48f)
    val reviewCountHeight get() = dp(44f)
    val reviewCardPadding get() = dp(12f)
    val reviewCompactPadding get() = dp(8f)
    val reviewContentPadding get() = dp(14f)
    val reviewIconLarge get() = dp(26f)
    val reviewIconMedium get() = dp(24f)
    val reviewCategoryIconTile get() = dp(38f)
    val reviewSelectedBadgeInset get() = dp(4f)
    val reviewSelectedBadgePadding get() = dp(2f)
    val reviewSelectedBadgeIcon get() = dp(12f)
    val reviewCardElevation get() = dp(1.5f)
    val reviewButtonHeight get() = dp(50f)
    val reviewPlayCircle get() = dp(30f)
    val reviewPlayIcon get() = dp(19f)
    val reviewNavHeight get() = dp(70f)
}

enum class IconStyle { OUTLINED, FILLED }

val LocalFlashLearnThemeTokens = staticCompositionLocalOf {
    FlashLearnThemeTokens(
        background = Color(0xFFF8FAFC),
        surface = Color.White,
        surfaceVariant = Color(0xFFF1F5F9),
        cardColor = Color.White,
        elevatedCardColor = Color.White,
        primary = Color(0xFF2563EB),
        secondary = Color(0xFF475569),
        tertiary = Color(0xFF475569),
        onBackground = Color(0xFF111827),
        onPrimary = Color.White,
        onSurface = Color(0xFF111827),
        onSurfaceVariant = Color(0xFF64748B),
        outlineColor = Color(0xFFD8E0EA),
        dividerColor = Color(0xFFD8E0EA),
        success = Color(0xFF138A5B),
        warning = Color(0xFFF59E0B),
        error = Color(0xFFD92D48),
        gradientStart = Color(0xFF2563EB),
        gradientEnd = Color(0xFF475569),
        iconStyle = IconStyle.OUTLINED,
        elevationScale = 1f,
        densityScale = 1f,
        typographyScale = 1f,
        cornerSmall = 12.dp,
        cornerMedium = 16.dp,
        cornerLarge = 24.dp,
        reviewBackground = Color(0xFFF8FAFC),
        reviewSurface = Color.White,
        reviewSurfaceSelected = Color(0xFFEFF6FF),
        reviewAccent = Color(0xFF2563EB),
        reviewText = Color(0xFF111827),
        reviewMutedText = Color(0xFF64748B),
        reviewBorder = Color(0xFFD8E0EA),
        reviewNav = Color(0xFFF1F5F9),
        reviewButton = Color(0xFF2563EB),
        reviewButtonContent = Color.White,
        info = Color(0xFF536DFE),
        critical = Color(0xFFDC2626),
        cardBorderAlpha = 0.45f,
        cardBorderStrongAlpha = 0.7f,
        accentSurfaceAlpha = 0.12f,
        hierarchyBoost = 1f,
        preferFilledButtons = false
    )
}
