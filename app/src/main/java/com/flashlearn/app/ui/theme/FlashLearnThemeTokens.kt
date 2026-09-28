package com.flashlearn.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class FlashLearnThemeTokens(
    val background: Color,val surface: Color,val surfaceVariant: Color,val cardColor: Color,val elevatedCardColor: Color,
    val primary: Color,val secondary: Color,val tertiary: Color,val onBackground: Color,val onPrimary: Color,
    val onSurface: Color,val onSurfaceVariant: Color,val outlineColor: Color,val dividerColor: Color,
    val success: Color,val warning: Color,val error: Color,val gradientStart: Color,val gradientEnd: Color,
    val iconStyle: IconStyle,val activeIconStyle: IconStyle,val iconSizeScale: Float,val activeIconSizeScale: Float,val navIndicatorAlpha: Float,
    val elevationScale: Float,val densityScale: Float,val typographyScale: Float,val spacingScale: Float,
    val cornerSmall: Dp,val cornerMedium: Dp,val cornerLarge: Dp,
    val reviewBackground: Color,val reviewSurface: Color,val reviewSurfaceSelected: Color,val reviewAccent: Color,
    val reviewText: Color,val reviewMutedText: Color,val reviewBorder: Color,val reviewNav: Color,
    val reviewButton: Color,val reviewButtonContent: Color,val info: Color,val critical: Color,
    val design: ThemeDesign
) {
    fun dp(value: Float): Dp = (value * spacingScale).dp
    private fun m(name:String)=design.metric(name)
    val screenPadding get()=dp(m("screenPadding")); val screenVerticalPadding get()=dp(m("screenVerticalPadding"))
    val contentPadding get()=dp(m("contentPadding")); val cardPadding get()=dp(m("cardPadding")); val compactPadding get()=dp(m("compactPadding"))
    val tinyGap get()=dp(m("tinyGap")); val microGap get()=dp(m("microGap")); val contentGap get()=dp(m("contentGap"))
    val compactGap get()=dp(m("compactGap")); val sectionGap get()=dp(m("sectionGap")); val itemGap get()=dp(m("itemGap"))
    val headerHeight get()=dp(m("headerHeight")); val headerPadding get()=dp(m("headerPadding")); val controlHeight get()=dp(m("controlHeight"))
    val buttonHeight get()=dp(m("buttonHeight")); val fieldHeight get()=dp(m("fieldHeight")); val cardMinHeight get()=dp(m("cardMinHeight"))
    val statCardHeight get()=dp(m("statCardHeight")); val largeChoiceHeight get()=dp(m("largeChoiceHeight")); val mediumChoiceHeight get()=dp(m("mediumChoiceHeight"))
    val chartHeight get()=dp(m("chartHeight")); val progressTrackHeight get()=dp(m("progressTrackHeight")); val borderThin get()=dp(m("borderThin"))
    val borderStrong get()=dp(m("borderStrong")); val borderEmphasis get()=dp(m("borderEmphasis")); val iconTileSize get()=dp(m("iconTileSize"))
    val choiceIconSize get()=dp(m("choiceIconSize")); val iconSmall get()=dp(m("iconSmall")); val iconMedium get()=dp(m("iconMedium")); val iconLarge get()=dp(m("iconLarge"))
    val cardElevation get()=dp(m("cardElevationBase")*elevationScale); val navHeight get()=dp(m("navHeight"))
    val libraryHeaderHeight get()=dp(m("libraryHeaderHeight")); val librarySearchHeight get()=dp(m("librarySearchHeight")); val libraryStatHeight get()=statCardHeight
    val libraryStatIconSize get()=dp(m("libraryStatIconSize")); val libraryCardCorner get()=largeCorner; val librarySearchCorner get()=mediumCorner
    val libraryCategoryCorner get()=largeCorner; val libraryIconTileSize get()=iconTileSize; val libraryIconTileCorner get()=smallCorner
    val libraryWordIconSize get()=dp(m("libraryWordIconSize")); val libraryFavoriteIconSize get()=dp(m("libraryFavoriteIconSize"))
    val libraryDifficultyHorizontalPadding get()=dp(m("libraryDifficultyHorizontalPadding")); val libraryDifficultyVerticalPadding get()=dp(m("libraryDifficultyVerticalPadding"))
    val smallCorner get()=cornerSmall; val mediumCorner get()=cornerMedium; val largeCorner get()=cornerLarge
    val reviewHeaderHeight get()=dp(m("reviewHeaderHeight")); val reviewHeaderGap get()=dp(m("reviewHeaderGap")); val reviewBackButtonSize get()=dp(m("reviewBackButtonSize"))
    val reviewBackIcon get()=dp(m("reviewBackIcon")); val reviewBackElevation get()=dp(m("reviewBackElevation")); val reviewOrnamentLine get()=dp(m("reviewOrnamentLine"))
    val reviewOrnamentHeight get()=dp(m("reviewOrnamentHeight")); val reviewOrnamentIcon get()=dp(m("reviewOrnamentIcon")); val reviewTinyGap get()=dp(m("reviewTinyGap"))
    val reviewSectionGap get()=dp(m("reviewSectionGap")); val reviewItemGap get()=dp(m("reviewItemGap")); val reviewChoiceHeight get()=dp(m("reviewChoiceHeight"))
    val reviewCategoryHeight get()=dp(m("reviewCategoryHeight")); val reviewDifficultyHeight get()=dp(m("reviewDifficultyHeight")); val reviewQuizHeight get()=dp(m("reviewQuizHeight"))
    val reviewFullChoiceHeight get()=dp(m("reviewFullChoiceHeight")); val reviewCountHeight get()=dp(m("reviewCountHeight")); val reviewCardPadding get()=dp(m("reviewCardPadding"))
    val reviewCompactPadding get()=dp(m("reviewCompactPadding")); val reviewContentPadding get()=dp(m("reviewContentPadding")); val reviewIconLarge get()=dp(m("reviewIconLarge"))
    val reviewIconMedium get()=dp(m("reviewIconMedium")); val reviewCategoryIconTile get()=dp(m("reviewCategoryIconTile")); val reviewSelectedBadgeInset get()=dp(m("reviewSelectedBadgeInset"))
    val reviewSelectedBadgePadding get()=dp(m("reviewSelectedBadgePadding")); val reviewSelectedBadgeIcon get()=dp(m("reviewSelectedBadgeIcon"))
    val reviewCardElevation get()=dp(m("reviewCardElevation")); val reviewButtonHeight get()=dp(m("reviewButtonHeight")); val reviewPlayCircle get()=dp(m("reviewPlayCircle"))
    val reviewPlayIcon get()=dp(m("reviewPlayIcon")); val reviewNavHeight get()=dp(m("reviewNavHeight"))
    val statsLayout get()=design.statsLayout; val reviewsLayout get()=design.reviewsLayout; val libraryLayout get()=design.libraryLayout
    val reviewPresentation get()=design.reviewPresentation; val buttonStyle get()=design.buttonStyle; val navStyle get()=design.navStyle
    val homeHeroHeight get()=dp(m("homeHeroHeight")); val homeReviewHeight get()=dp(m("homeReviewHeight")); val homeCtaHeight get()=dp(m("homeCtaHeight")); val homeBottomGap get()=dp(m("homeBottomGap"))\n    val cardBorderAlpha get()=m("cardBorderAlpha"); val cardBorderStrongAlpha get()=m("cardBorderStrongAlpha")
    val accentSurfaceAlpha get()=m("accentSurfaceAlpha"); val hierarchyBoost get()=m("hierarchyBoost"); val preferFilledButtons get()=design.buttonStyle==ButtonStyle.FILLED
}

enum class IconStyle { OUTLINED, FILLED }

val LocalFlashLearnThemeTokens = staticCompositionLocalOf {
    val s=FlashLearnThemeSpec.MODERN_MINIMAL
    FlashLearnThemeTokens(
        background=Color(s.lightBackground),surface=Color(s.lightSurface),surfaceVariant=Color(s.lightSurfaceVariant),cardColor=Color(s.lightCard),elevatedCardColor=Color(s.lightCard),
        primary=Color(s.lightPrimary),secondary=Color(s.lightSecondary),tertiary=Color(s.lightSecondary),onBackground=Color(s.lightOnSurface),onPrimary=Color.White,
        onSurface=Color(s.lightOnSurface),onSurfaceVariant=Color(s.lightOnSurfaceVariant),outlineColor=Color(s.lightOutline),dividerColor=Color(s.lightOutline),
        success=Color(s.design.lightSuccess),warning=Color(s.design.lightWarning),error=Color(s.design.lightError),gradientStart=Color(s.gradientStart),gradientEnd=Color(s.gradientEnd),
        iconStyle=s.design.iconStyle,activeIconStyle=s.design.activeIconStyle,iconSizeScale=s.design.iconSizeScale,activeIconSizeScale=s.design.activeIconSizeScale,navIndicatorAlpha=s.design.navIndicatorAlpha,elevationScale=s.elevationScale,densityScale=s.densityScale,typographyScale=s.typographyScale,spacingScale=s.spacingScale,
        cornerSmall=s.cornerSmall.dp,cornerMedium=s.cornerMedium.dp,cornerLarge=s.cornerLarge.dp,
        reviewBackground=Color(s.lightBackground),reviewSurface=Color(s.lightSurface),reviewSurfaceSelected=Color(s.lightPrimary).copy(alpha=s.design.metric("reviewSelectedAlpha")),
        reviewAccent=Color(s.lightPrimary),reviewText=Color(s.lightOnSurface),reviewMutedText=Color(s.lightOnSurfaceVariant),reviewBorder=Color(s.lightOutline),
        reviewNav=Color(s.lightSurfaceVariant),reviewButton=Color(s.lightPrimary),reviewButtonContent=Color.White,info=Color(s.design.lightInfo),critical=Color(s.design.lightError),design=s.design
    )
}
