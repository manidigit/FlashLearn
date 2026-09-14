package com.flashlearn.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.flashlearn.app.ui.AppearanceMode

private val Purple = Color(0xFF7C3AED)
private val DarkBackground = Color(0xFF0B0D12)
private val DarkSurface = Color(0xFF151820)
private val LightBackground = Color(0xFFF8F7FC)

private val LightColors = lightColorScheme(
    primary = Purple,
    onPrimary = Color.White,
    secondary = Color(0xFF536DFE),
    background = LightBackground,
    surface = Color.White,
    surfaceVariant = Color(0xFFF0EDF6),
    onSurface = Color(0xFF17141C),
    onSurfaceVariant = Color(0xFF68636F)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8B5CF6),
    onPrimary = Color.White,
    secondary = Color(0xFF8191FF),
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = Color(0xFF20232D),
    onSurface = Color(0xFFF5F2F8),
    onSurfaceVariant = Color(0xFFB7B2BE)
)

@Composable
fun FlashLearnTheme(
    appearance: AppearanceMode = AppearanceMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val dark = when (appearance) {
        AppearanceMode.SYSTEM -> isSystemInDarkTheme()
        AppearanceMode.LIGHT -> false
        AppearanceMode.DARK -> true
    }
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
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
