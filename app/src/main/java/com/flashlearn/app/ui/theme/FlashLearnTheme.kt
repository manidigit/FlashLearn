package com.flashlearn.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.flashlearn.app.ui.AccentColor
import com.flashlearn.app.ui.AppearanceMode

@Composable
fun FlashLearnTheme(
    appearance: AppearanceMode = AppearanceMode.SYSTEM,
    themeId: String = "grok",
    accentColor: AccentColor = AccentColor.PURPLE,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val spec = FlashLearnThemeSpec.BUILT_IN.firstOrNull { it.id == themeId }
        ?: FlashLearnThemeSpec.loadCustom(context).firstOrNull { it.id == themeId }
        ?: FlashLearnThemeSpec.MODERN_PURPLE
    val accentLight = when (accentColor) {
        AccentColor.PURPLE -> Color(spec.lightPrimary)
        AccentColor.BLUE -> Color(0xFF2563EB)
        AccentColor.GREEN -> Color(0xFF16A34A)
        AccentColor.ORANGE -> Color(0xFFEA580C)
        AccentColor.PINK -> Color(0xFFDB2777)
    }
    val accentDark = when (accentColor) {
        AccentColor.PURPLE -> Color(spec.darkPrimary)
        AccentColor.BLUE -> Color(0xFF60A5FA)
        AccentColor.GREEN -> Color(0xFF4ADE80)
        AccentColor.ORANGE -> Color(0xFFFB923C)
        AccentColor.PINK -> Color(0xFFF472B6)
    }
    val dark = when (appearance) { AppearanceMode.SYSTEM -> isSystemInDarkTheme(); AppearanceMode.LIGHT -> false; AppearanceMode.DARK -> true }
    // Grok owns its gold palette; other themes still respect accent override
    val primary = when {
        spec.id == "grok" && dark -> Color(spec.darkPrimary)
        spec.id == "grok" -> Color(spec.lightPrimary)
        dark -> accentDark
        else -> accentLight
    }
    val onPrimaryColor = if (spec.id == "grok") Color(0xFF0F1419) else Color.White
    val colors = if (dark) darkColorScheme(
        primary = primary,
        onPrimary = onPrimaryColor,
        secondary = Color(spec.darkSecondary),
        tertiary = Color(spec.darkSecondary),
        onBackground = Color(spec.darkOnSurface),
        background = Color(spec.darkBackground),
        surface = Color(spec.darkSurface),
        surfaceVariant = Color(spec.darkSurfaceVariant),
        onSurface = Color(spec.darkOnSurface),
        onSurfaceVariant = Color(spec.darkOnSurfaceVariant),
        outline = Color(spec.darkOutline),
        error = Color(0xFFFF8A9A)
    ) else lightColorScheme(
        primary = primary,
        onPrimary = if (spec.id == "grok") Color(0xFF0F1419) else Color.White,
        secondary = Color(spec.lightSecondary),
        tertiary = Color(spec.lightSecondary),
        onBackground = Color(spec.lightOnSurface),
        background = Color(spec.lightBackground),
        surface = Color(spec.lightSurface),
        surfaceVariant = Color(spec.lightSurfaceVariant),
        onSurface = Color(spec.lightOnSurface),
        onSurfaceVariant = Color(spec.lightOnSurfaceVariant),
        outline = Color(spec.lightOutline),
        error = Color(0xFFD92D48)
    )
    val base=Typography(); val scale=spec.typographyScale
    fun androidx.compose.ui.text.TextStyle.scaled(weight:FontWeight?=null)=copy(fontSize=fontSize*scale,fontWeight=weight?:fontWeight)
    val typography=Typography(displayLarge=base.displayLarge.scaled(FontWeight.Bold),displayMedium=base.displayMedium.scaled(FontWeight.Bold),displaySmall=base.displaySmall.scaled(FontWeight.Bold),headlineLarge=base.headlineLarge.scaled(FontWeight.Bold),headlineMedium=base.headlineMedium.scaled(FontWeight.Bold),headlineSmall=base.headlineSmall.scaled(FontWeight.SemiBold),titleLarge=base.titleLarge.scaled(FontWeight.Bold),titleMedium=base.titleMedium.scaled(FontWeight.SemiBold),titleSmall=base.titleSmall.scaled(FontWeight.Medium),bodyLarge=base.bodyLarge.scaled(),bodyMedium=base.bodyMedium.scaled(),bodySmall=base.bodySmall.scaled(),labelLarge=base.labelLarge.scaled(FontWeight.SemiBold),labelMedium=base.labelMedium.scaled(),labelSmall=base.labelSmall.scaled())
    val tokens=FlashLearnThemeTokens(
        background=if(dark)Color(spec.darkBackground) else Color(spec.lightBackground),surface=if(dark)Color(spec.darkSurface) else Color(spec.lightSurface),surfaceVariant=if(dark)Color(spec.darkSurfaceVariant) else Color(spec.lightSurfaceVariant),
        cardColor=if(dark)Color(spec.darkCard) else Color(spec.lightCard),elevatedCardColor=if(dark)Color(spec.darkCard).compositeOver(Color.White) else Color(spec.lightCard),
        primary=primary,secondary=if(dark)Color(spec.darkSecondary) else Color(spec.lightSecondary),tertiary=if(dark)Color(spec.darkSecondary) else Color(spec.lightSecondary),
        onBackground=if(dark)Color(spec.darkOnSurface) else Color(spec.lightOnSurface),
        onPrimary=onPrimaryColor,
        onSurface=if(dark)Color(spec.darkOnSurface) else Color(spec.lightOnSurface),
        onSurfaceVariant=if(dark)Color(spec.darkOnSurfaceVariant) else Color(spec.lightOnSurfaceVariant),
        outlineColor=if(dark)Color(spec.darkOutline) else Color(spec.lightOutline),dividerColor=(if(dark)Color(spec.darkOutline) else Color(spec.lightOutline)).copy(alpha=.65f),
        success=if(dark)Color(0xFF52D49A) else Color(0xFF138A5B),warning=if(dark)Color(0xFFFBBF24) else Color(0xFFF59E0B),error=colors.error,
        gradientStart=if(dark)Color(spec.darkPrimary) else Color(spec.gradientStart),gradientEnd=if(dark)Color(spec.darkSecondary) else Color(spec.gradientEnd),
        iconStyle=if(spec.iconStyle.equals("filled",true))IconStyle.FILLED else IconStyle.OUTLINED,elevationScale=spec.elevationScale,densityScale=spec.densityScale,typographyScale=spec.typographyScale,
        cornerSmall=spec.cornerSmall.dp,cornerMedium=spec.cornerMedium.dp,cornerLarge=spec.cornerLarge.dp,
        reviewBackground=colors.background,reviewSurface=colors.surface,reviewSurfaceSelected=primary.copy(alpha=.10f).compositeOver(colors.surface),reviewAccent=colors.primary,reviewText=colors.onSurface,reviewMutedText=colors.onSurfaceVariant,reviewBorder=colors.outline,reviewNav=colors.surfaceVariant,reviewButton=colors.primary,reviewButtonContent=colors.onPrimary,
        info=if(dark)Color(spec.darkSecondary) else Color(spec.lightSecondary),critical=colors.error,
        // Visual personality – Luxury gets stronger presence
        cardBorderAlpha = if (spec.id == "grok") 0.55f else 0.40f,
        cardBorderStrongAlpha = if (spec.id == "grok") 0.85f else 0.65f,
        accentSurfaceAlpha = if (spec.id == "grok") 0.16f else 0.10f,
        hierarchyBoost = if (spec.id == "grok") 1.08f else 1f,
        preferFilledButtons = spec.id == "grok" || spec.iconStyle.equals("filled", true)
    )
    val baseDensity=LocalDensity.current
    val themedDensity=Density(density=baseDensity.density*spec.densityScale,fontScale=baseDensity.fontScale)
    CompositionLocalProvider(LocalFlashLearnThemeTokens provides tokens,LocalDensity provides themedDensity){
        MaterialTheme(colorScheme=colors,typography=typography,shapes=Shapes(extraSmall=RoundedCornerShape(spec.cornerSmall.dp),small=RoundedCornerShape(spec.cornerSmall.dp),medium=RoundedCornerShape(spec.cornerMedium.dp),large=RoundedCornerShape(spec.cornerLarge.dp)),content=content)
    }
}
private fun Color.compositeOver(background:Color):Color=this.copy(alpha=1f)
