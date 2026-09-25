package com.flashlearn.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.flashlearn.app.ui.AccentColor
import com.flashlearn.app.ui.AppearanceMode

/**
 * FlashLearnTheme - Universal Theme System
 * 
 * یک‌بار صدا می‌شود (root level)
 * تمام theme معلومات را provide می‌کند:
 * 1. MaterialTheme.colorScheme (برای تمام رنگ‌ها)
 * 2. MaterialTheme.shapes (برای تمام corners)
 * 3. MaterialTheme.typography (برای تمام typography)
 * 4. LocalFlashLearnThemeTokens (برای تمام spacing)
 * 
 * صفحات از این معلومات استفاده می‌کنند، نه hardcoded values
 */
@Composable
fun FlashLearnTheme(
    appearance: AppearanceMode = AppearanceMode.SYSTEM,
    themeId: String = "grok",
    accentColor: AccentColor = AccentColor.PURPLE,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    
    // =========== Theme Loading ===========
    val spec = FlashLearnThemeSpec.BUILT_IN.firstOrNull { it.id == themeId }
        ?: FlashLearnThemeSpec.loadCustom(context).firstOrNull { it.id == themeId }
        ?: FlashLearnThemeSpec.GROK
    
    // =========== Dark Mode Detection ===========
    val isDark = when (appearance) {
        AppearanceMode.SYSTEM -> isSystemInDarkTheme()
        AppearanceMode.LIGHT -> false
        AppearanceMode.DARK -> true
    }
    
    // =========== Primary Color Selection ===========
    // GROK theme: always use its gold palette
    // Others: respect accent override
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
    
    val primary = when {
        spec.id == "grok" && isDark -> Color(spec.darkPrimary)
        spec.id == "grok" -> Color(spec.lightPrimary)
        isDark -> accentDark
        else -> accentLight
    }
    
    val onPrimaryColor = when (spec.id) {
        "grok" -> Color(0xFF0F1419)  // dark on gold
        "claud" -> Color.White
        else -> Color.White
    }
    
    // =========== Material Color Scheme ===========
    // This is what screens use for all colors
    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = primary,
            onPrimary = onPrimaryColor,
            secondary = Color(spec.darkSecondary),
            tertiary = Color(spec.darkSecondary),
            background = Color(spec.darkBackground),
            surface = Color(spec.darkSurface),
            surfaceVariant = Color(spec.darkSurfaceVariant),
            onBackground = Color(spec.darkOnSurface),
            onSurface = Color(spec.darkOnSurface),
            onSurfaceVariant = Color(spec.darkOnSurfaceVariant),
            outline = Color(spec.darkOutline),
            error = Color(0xFFFF8A9A)
        )
    } else {
        lightColorScheme(
            primary = primary,
            onPrimary = onPrimaryColor,
            secondary = Color(spec.lightSecondary),
            tertiary = Color(spec.lightSecondary),
            background = Color(spec.lightBackground),
            surface = Color(spec.lightSurface),
            surfaceVariant = Color(spec.lightSurfaceVariant),
            onBackground = Color(spec.lightOnSurface),
            onSurface = Color(spec.lightOnSurface),
            onSurfaceVariant = Color(spec.lightOnSurfaceVariant),
            outline = Color(spec.lightOutline),
            error = Color(0xFFD92D48)
        )
    }
    
    // =========== Material Typography ===========
    // This is what screens use for all text styles
    val baseTypography = Typography()
    val scale = spec.typographyScale
    fun androidx.compose.ui.text.TextStyle.scaled(weight: FontWeight? = null) =
        copy(fontSize = fontSize * scale, fontWeight = weight ?: fontWeight)
    
    val typography = Typography(
        displayLarge = baseTypography.displayLarge.scaled(FontWeight.Bold),
        displayMedium = baseTypography.displayMedium.scaled(FontWeight.Bold),
        displaySmall = baseTypography.displaySmall.scaled(FontWeight.Bold),
        headlineLarge = baseTypography.headlineLarge.scaled(FontWeight.Bold),
        headlineMedium = baseTypography.headlineMedium.scaled(FontWeight.Bold),
        headlineSmall = baseTypography.headlineSmall.scaled(FontWeight.SemiBold),
        titleLarge = baseTypography.titleLarge.scaled(FontWeight.Bold),
        titleMedium = baseTypography.titleMedium.scaled(FontWeight.SemiBold),
        titleSmall = baseTypography.titleSmall.scaled(FontWeight.Medium),
        bodyLarge = baseTypography.bodyLarge.scaled(),
        bodyMedium = baseTypography.bodyMedium.scaled(),
        bodySmall = baseTypography.bodySmall.scaled(),
        labelLarge = baseTypography.labelLarge.scaled(FontWeight.SemiBold),
        labelMedium = baseTypography.labelMedium.scaled(),
        labelSmall = baseTypography.labelSmall.scaled()
    )
    
    // =========== Flash Learn Tokens ===========
    // Additional tokens for screens (spacing, layout, etc)
    val tokens = FlashLearnThemeTokens(
        // --- Colors (from colorScheme) ---
        background = colorScheme.background,
        surface = colorScheme.surface,
        surfaceVariant = colorScheme.surfaceVariant,
        cardColor = if (isDark) Color(spec.darkCard) else Color(spec.lightCard),
        elevatedCardColor = (if (isDark) Color(spec.darkCard) else Color(spec.lightCard))
            .compositeOver(Color.White),
        primary = colorScheme.primary,
        secondary = colorScheme.secondary,
        tertiary = colorScheme.tertiary,
        onBackground = colorScheme.onBackground,
        onPrimary = colorScheme.onPrimary,
        onSurface = colorScheme.onSurface,
        onSurfaceVariant = colorScheme.onSurfaceVariant,
        outlineColor = colorScheme.outline,
        dividerColor = colorScheme.outline.copy(alpha = 0.65f),
        success = if (isDark) Color(0xFF52D49A) else Color(0xFF138A5B),
        warning = if (isDark) Color(0xFFFBBF24) else Color(0xFFF59E0B),
        error = colorScheme.error,
        
        // --- Gradients ---
        gradientStart = if (isDark) Color(spec.darkPrimary) else Color(spec.gradientStart),
        gradientEnd = if (isDark) Color(spec.darkSecondary) else Color(spec.gradientEnd),
        
        // --- Layout & Appearance ---
        iconStyle = if (spec.iconStyle.equals("filled", true)) IconStyle.FILLED else IconStyle.OUTLINED,
        icons = FlashLearnIconSet.forStyle(if (spec.iconStyle.equals("filled", true)) IconStyle.FILLED else IconStyle.OUTLINED),
        elevationScale = spec.elevationScale,
        densityScale = spec.densityScale,
        typographyScale = spec.typographyScale,
        
        // --- Shapes (Corners) ---
        cornerSmall = spec.cornerSmall.dp,
        cornerMedium = spec.cornerMedium.dp,
        cornerLarge = spec.cornerLarge.dp,
        
        // --- Review Screen Colors ---
        reviewBackground = colorScheme.background,
        reviewSurface = colorScheme.surface,
        reviewSurfaceSelected = colorScheme.primary.copy(alpha = 0.10f).compositeOver(colorScheme.surface),
        reviewAccent = colorScheme.primary,
        reviewText = colorScheme.onSurface,
        reviewMutedText = colorScheme.onSurfaceVariant,
        reviewBorder = colorScheme.outline,
        reviewNav = colorScheme.surfaceVariant,
        reviewButton = colorScheme.primary,
        reviewButtonContent = colorScheme.onPrimary,
        
        // --- Semantic Colors ---
        info = if (isDark) Color(spec.darkSecondary) else Color(spec.lightSecondary),
        critical = colorScheme.error,
        
        // --- Visual Personality ---
        cardBorderAlpha = if (spec.id == "grok") 0.55f else 0.40f,
        cardBorderStrongAlpha = if (spec.id == "grok") 0.85f else 0.65f,
        accentSurfaceAlpha = if (spec.id == "grok") 0.16f else 0.10f,
        hierarchyBoost = if (spec.id == "grok") 1.08f else 1f,
        preferFilledButtons = spec.id == "grok" || spec.iconStyle.equals("filled", true)
    )
    
    // =========== Density Adjustment ===========
    val baseDensity = LocalDensity.current
    val themedDensity = Density(
        density = baseDensity.density * spec.densityScale,
        fontScale = baseDensity.fontScale
    )
    
    // =========== Provide All Theme Data ===========
    CompositionLocalProvider(
        LocalFlashLearnThemeTokens provides tokens,
        LocalDensity provides themedDensity
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            shapes = Shapes(
                extraSmall = RoundedCornerShape(spec.cornerSmall.dp),
                small = RoundedCornerShape(spec.cornerSmall.dp),
                medium = RoundedCornerShape(spec.cornerMedium.dp),
                large = RoundedCornerShape(spec.cornerLarge.dp)
            ),
            content = content
        )
    }
}

// ============================================================================
// Helper Functions
// ============================================================================

private fun Color.compositeOver(background: Color): Color = this.copy(alpha = 1f)

