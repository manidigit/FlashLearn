package com.flashlearn.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.flashlearn.app.ui.AccentColor
import com.flashlearn.app.ui.AppearanceMode

@Composable
fun FlashLearnTheme(
    appearance: AppearanceMode = AppearanceMode.SYSTEM,
    themeId: String = "modern_purple",
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
    val dark = when (appearance) {
        AppearanceMode.SYSTEM -> isSystemInDarkTheme()
        AppearanceMode.LIGHT -> false
        AppearanceMode.DARK -> true
    }

    val colors = if (dark) {
        darkColorScheme(
            primary = accentDark,
            onPrimary = if (spec.id == "luxury") Color(0xFF211707) else Color.White,
            secondary = Color(spec.darkSecondary),
            background = Color(spec.darkBackground),
            surface = Color(spec.darkSurface),
            surfaceVariant = Color(spec.darkSurfaceVariant),
            onSurface = Color(spec.darkOnSurface),
            onSurfaceVariant = Color(spec.darkOnSurfaceVariant),
            outline = Color(spec.darkOutline)
        )
    } else {
        lightColorScheme(
            primary = accentLight,
            onPrimary = if (spec.id == "luxury") Color.White else Color.White,
            secondary = Color(spec.lightSecondary),
            background = Color(spec.lightBackground),
            surface = Color(spec.lightSurface),
            surfaceVariant = Color(spec.lightSurfaceVariant),
            onSurface = Color(spec.lightOnSurface),
            onSurfaceVariant = Color(spec.lightOnSurfaceVariant),
            outline = Color(spec.lightOutline)
        )
    }

    val base = Typography()
    val scale = spec.typographyScale
    val typography = Typography(
        displayLarge = base.displayLarge.copy(fontSize = base.displayLarge.fontSize * scale, fontWeight = FontWeight.Bold),
        displayMedium = base.displayMedium.copy(fontSize = base.displayMedium.fontSize * scale, fontWeight = FontWeight.Bold),
        displaySmall = base.displaySmall.copy(fontSize = base.displaySmall.fontSize * scale, fontWeight = FontWeight.Bold),
        headlineLarge = base.headlineLarge.copy(fontSize = base.headlineLarge.fontSize * scale, fontWeight = FontWeight.Bold),
        headlineMedium = base.headlineMedium.copy(fontSize = base.headlineMedium.fontSize * scale, fontWeight = FontWeight.Bold),
        headlineSmall = base.headlineSmall.copy(fontSize = base.headlineSmall.fontSize * scale, fontWeight = FontWeight.SemiBold),
        titleLarge = base.titleLarge.copy(fontSize = base.titleLarge.fontSize * scale, fontWeight = FontWeight.Bold),
        titleMedium = base.titleMedium.copy(fontSize = base.titleMedium.fontSize * scale, fontWeight = FontWeight.SemiBold),
        titleSmall = base.titleSmall.copy(fontSize = base.titleSmall.fontSize * scale, fontWeight = FontWeight.Medium),
        bodyLarge = base.bodyLarge.copy(fontSize = base.bodyLarge.fontSize * scale),
        bodyMedium = base.bodyMedium.copy(fontSize = base.bodyMedium.fontSize * scale),
        bodySmall = base.bodySmall.copy(fontSize = base.bodySmall.fontSize * scale),
        labelLarge = base.labelLarge.copy(fontSize = base.labelLarge.fontSize * scale, fontWeight = FontWeight.SemiBold),
        labelMedium = base.labelMedium.copy(fontSize = base.labelMedium.fontSize * scale),
        labelSmall = base.labelSmall.copy(fontSize = base.labelSmall.fontSize * scale)
    )

    val tokens = FlashLearnThemeTokens(
        cardColor = if (dark) Color(spec.darkCard) else Color(spec.lightCard),
        outlineColor = if (dark) Color(spec.darkOutline) else Color(spec.lightOutline),
        gradientStart = if (dark) Color(spec.darkPrimary) else Color(spec.gradientStart),
        gradientEnd = if (dark) Color(spec.darkSecondary) else Color(spec.gradientEnd),
        iconStyle = if (spec.iconStyle.equals("filled", true)) IconStyle.FILLED else IconStyle.OUTLINED,
        elevationScale = spec.elevationScale,
        densityScale = spec.densityScale
    )

    CompositionLocalProvider(LocalFlashLearnThemeTokens provides tokens) {
        MaterialTheme(
            colorScheme = colors,
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
