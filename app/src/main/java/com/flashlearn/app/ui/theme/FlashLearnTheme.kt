package com.flashlearn.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
    val spec = remember(themeId) { FlashLearnThemeSpec.BUILT_IN.firstOrNull { it.id == themeId }
        ?: FlashLearnThemeSpec.loadCustom(context).firstOrNull { it.id == themeId }
        ?: FlashLearnThemeSpec.GROK }
    
    // =========== Dark Mode Detection ===========
    val isDark = when (appearance) {
        AppearanceMode.SYSTEM -> isSystemInDarkTheme()
        AppearanceMode.LIGHT -> false
        AppearanceMode.DARK -> true
    }
    
    // ThemeDesign/ThemeSpec owns the visual identity. The legacy accent preference
    // is retained for settings/JSON compatibility but cannot override a selected theme.
    val primary = if (isDark) Color(spec.darkPrimary) else Color(spec.lightPrimary)
    val onPrimaryColor = Color(if (isDark) spec.design.darkOnPrimary else spec.design.lightOnPrimary)
    val accent = when (accentColor) {
        AccentColor.PURPLE -> if (isDark) Color(0xFFB794F6) else Color(0xFF6D28D9)
        AccentColor.BLUE -> if (isDark) Color(0xFF93C5FD) else Color(0xFF2563EB)
        AccentColor.GREEN -> if (isDark) Color(0xFF86EFAC) else Color(0xFF16A34A)
        AccentColor.ORANGE -> if (isDark) Color(0xFFFDBA74) else Color(0xFFEA580C)
        AccentColor.PINK -> if (isDark) Color(0xFFF9A8D4) else Color(0xFFDB2777)
    }
    
    // =========== Material Color Scheme ===========
    // This is what screens use for all colors
    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = primary,
            onPrimary = onPrimaryColor,
            secondary = Color(spec.darkSecondary),
            tertiary = accent,
            background = Color(spec.darkBackground),
            surface = Color(spec.darkSurface),
            surfaceVariant = Color(spec.darkSurfaceVariant),
            onBackground = Color(spec.darkOnSurface),
            onSurface = Color(spec.darkOnSurface),
            onSurfaceVariant = Color(spec.darkOnSurfaceVariant),
            outline = Color(spec.darkOutline),
            error = Color(spec.design.darkError)
        )
    } else {
        lightColorScheme(
            primary = primary,
            onPrimary = onPrimaryColor,
            secondary = Color(spec.lightSecondary),
            tertiary = accent,
            background = Color(spec.lightBackground),
            surface = Color(spec.lightSurface),
            surfaceVariant = Color(spec.lightSurfaceVariant),
            onBackground = Color(spec.lightOnSurface),
            onSurface = Color(spec.lightOnSurface),
            onSurfaceVariant = Color(spec.lightOnSurfaceVariant),
            outline = Color(spec.lightOutline),
            error = Color(spec.design.lightError)
        )
    }
    
    // =========== Material Typography ===========
    // This is what screens use for all text styles
    val baseTypography = Typography()
    val scale = spec.design.metric("typographyScale")
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
        dividerColor = colorScheme.outline.copy(alpha = spec.design.metric("dividerAlpha")),
        success = if (isDark) Color(spec.design.darkSuccess) else Color(spec.design.lightSuccess),
        warning = if (isDark) Color(spec.design.darkWarning) else Color(spec.design.lightWarning),
        error = colorScheme.error,
        
        // --- Gradients ---
        gradientStart = if (isDark) Color(spec.darkPrimary) else Color(spec.gradientStart),
        gradientEnd = if (isDark) Color(spec.darkSecondary) else Color(spec.gradientEnd),
        
        // --- Layout & Appearance ---
        iconStyle = spec.design.iconStyle,
        activeIconStyle = spec.design.activeIconStyle,
        iconSizeScale = spec.design.iconSizeScale,
        activeIconSizeScale = spec.design.activeIconSizeScale,
        navIndicatorAlpha = spec.design.navIndicatorAlpha,
        elevationScale = spec.design.metric("elevationScale"),
        densityScale = spec.design.metric("densityScale"),
        typographyScale = spec.design.metric("typographyScale"),
        spacingScale = spec.design.metric("spacingScale"),
        
        // --- Shapes (Corners) ---
        cornerSmall = spec.design.metric("cornerSmall").dp,
        cornerMedium = spec.design.metric("cornerMedium").dp,
        cornerLarge = spec.design.metric("cornerLarge").dp,
        
        // --- Review Screen Colors ---
        reviewBackground = colorScheme.background,
        reviewSurface = colorScheme.surface,
        reviewSurfaceSelected = colorScheme.primary.copy(alpha = spec.design.metric("reviewSelectedAlpha")).compositeOver(colorScheme.surface),
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
        design = spec.design
    )
    
    // =========== Provide All Theme Data ===========
    // Density must remain system-owned; theme scaling is handled by spacing/typography tokens.
    CompositionLocalProvider(
        LocalFlashLearnThemeTokens provides tokens
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


