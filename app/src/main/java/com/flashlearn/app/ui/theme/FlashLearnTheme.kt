package com.flashlearn.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.flashlearn.app.ui.AccentColor
import com.flashlearn.app.ui.AppearanceMode

private val DarkBackground = Color(0xFF0B0D12)
private val DarkSurface = Color(0xFF151820)
private val LightBackground = Color(0xFFF8F7FC)

private data class AccentPalette(val light: Color, val dark: Color, val secondaryLight: Color, val secondaryDark: Color)

private fun accentPalette(accent: AccentColor) = when (accent) {
    AccentColor.PURPLE -> AccentPalette(Color(0xFF7C3AED), Color(0xFF9B6CFF), Color(0xFF536DFE), Color(0xFF8191FF))
    AccentColor.BLUE -> AccentPalette(Color(0xFF2563EB), Color(0xFF60A5FA), Color(0xFF0EA5E9), Color(0xFF38BDF8))
    AccentColor.GREEN -> AccentPalette(Color(0xFF16A34A), Color(0xFF4ADE80), Color(0xFF0D9488), Color(0xFF2DD4BF))
    AccentColor.ORANGE -> AccentPalette(Color(0xFFEA580C), Color(0xFFFB923C), Color(0xFFF59E0B), Color(0xFFFBBF24))
    AccentColor.PINK -> AccentPalette(Color(0xFFDB2777), Color(0xFFF472B6), Color(0xFFE11D48), Color(0xFFFB7185))
}

@Composable
fun FlashLearnTheme(
    appearance: AppearanceMode = AppearanceMode.SYSTEM,
    accentColor: AccentColor = AccentColor.PURPLE,
    content: @Composable () -> Unit
) {
    val dark = when (appearance) {
        AppearanceMode.SYSTEM -> isSystemInDarkTheme()
        AppearanceMode.LIGHT -> false
        AppearanceMode.DARK -> true
    }
    val accent = accentPalette(accentColor)
    val lightColors = lightColorScheme(
        primary = accent.light,
        onPrimary = Color.White,
        secondary = accent.secondaryLight,
        background = LightBackground,
        surface = Color.White,
        surfaceVariant = Color(0xFFF0EDF6),
        onSurface = Color(0xFF17141C),
        onSurfaceVariant = Color(0xFF68636F)
    )
    val darkColors = darkColorScheme(
        primary = accent.dark,
        onPrimary = Color.White,
        secondary = accent.secondaryDark,
        background = DarkBackground,
        surface = DarkSurface,
        surfaceVariant = Color(0xFF20232D),
        onSurface = Color(0xFFF5F2F8),
        onSurfaceVariant = Color(0xFFB7B2BE)
    )
    MaterialTheme(
        colorScheme = if (dark) darkColors else lightColors,
        typography = Typography(
            headlineLarge = Typography().headlineLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
            headlineMedium = Typography().headlineMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
            titleLarge = Typography().titleLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        ),
        shapes = Shapes(
            extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(10),
            small = androidx.compose.foundation.shape.RoundedCornerShape(14),
            medium = androidx.compose.foundation.shape.RoundedCornerShape(18),
            large = androidx.compose.foundation.shape.RoundedCornerShape(24)
        ),
        content = content
    )
}
