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
    
    // =========== Primary Color Selection ===========
    // The explicit accent setting is applied to Material primary so the user-visible
    // accent control has a deterministic visual effect across all themes.
    val accent = when (accentColor) {
        AccentColor.PURPLE -> if (isDark) Color(0xFFB39DDB) else Color(0xFF6750A4)
        AccentColor.BLUE -> if (isDark) Color(0xFF90CAF9) else Color(0xFF1565C0)
        AccentColor.GREEN -> if (isDark) Color(0xFFA5D6A7) else Color(0xFF2E7D32)
        AccentColor.ORANGE -> if (isDark) Color(0xFFFFCC80) else Color(0xFFEF6C00)
        AccentColor.PINK -> if (isDark) Color(0xFFF48FB1) else Color(0xFFC2185B)
    }
    val primary = accent
    val onPrimaryColor = if (isDark) Color(0xFF161218) else Color.White
    
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
        dividerColor = colorScheme.outline.copy(alpha = spec.design.metric("dividerAlpha")),
        success = if (isDark) Color(spec.design.darkSuccess) else Color(spec.design.lightSuccess),
        warning = if (isDark) Color(spec.design.darkWarning) else Color(spec.design.lightWarning),
        error = colorScheme.error,
        
        // --- Gradients ---
        gradientStart = accent,
        gradientEnd = if (isDark) Color(spec.darkSecondary) else Color(spec.gradientEnd),
        
        // --- Layout & Appearance ---
        iconStyle = spec.design.iconStyle,
        activeIconStyle = spec.design.activeIconStyle,
        iconSizeScale = spec.design.iconSizeScale,
        activeIconSizeScale = spec.design.activeIconSizeScale,
        navIndicatorAlpha = spec.design.navIndicatorAlpha,
        elevationScale = spec.elevationScale,
        densityScale = spec.densityScale,
        typographyScale = spec.typographyScale,
        spacingScale = spec.spacingScale,
        
        // --- Shapes (Corners) ---
        cornerSmall = spec.cornerSmall.dp,
        cornerMedium = spec.cornerMedium.dp,
        cornerLarge = spec.cornerLarge.dp,
        
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


