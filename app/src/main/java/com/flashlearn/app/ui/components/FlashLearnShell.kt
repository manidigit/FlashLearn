package com.flashlearn.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.flashlearn.app.navigation.AppRoutes
import com.flashlearn.app.ui.theme.IconStyle
import com.flashlearn.app.ui.theme.LocalFlashLearnThemeTokens

@Composable
fun FlashLearnShell(selectedRoute: String, onNavigate: (String) -> Unit, content: @Composable ColumnScope.() -> Unit) {
    val tokens = LocalFlashLearnThemeTokens.current
    val reviewRoute = selectedRoute == AppRoutes.REVIEW
    val shellBackground = if (reviewRoute) tokens.reviewBackground else tokens.background
    val navigationBackground = if (reviewRoute) tokens.reviewNav else tokens.cardColor
    Column(Modifier.fillMaxSize().background(shellBackground)) {
        Column(Modifier.weight(1f).fillMaxWidth()) { content() }
        NavigationBar(
            modifier = Modifier.fillMaxWidth().height(if (reviewRoute) tokens.reviewNavHeight else tokens.navHeight),
            containerColor = navigationBackground,
            tonalElevation = tokens.cardElevation
        ) {
            NavItem(AppRoutes.HOME, "خانه", Icons.Outlined.Home, Icons.Filled.Home, selectedRoute, onNavigate)
            NavItem(AppRoutes.REVIEW, "مرور", Icons.Outlined.History, Icons.Filled.History, selectedRoute, onNavigate)
            NavItem(AppRoutes.LIBRARY, "واژگان", Icons.Outlined.MenuBook, Icons.Filled.MenuBook, selectedRoute, onNavigate)
            NavItem(AppRoutes.PROGRESS, "آمار", Icons.Outlined.BarChart, Icons.Filled.BarChart, selectedRoute, onNavigate)
            NavItem(AppRoutes.SETTINGS, "تنظیمات", Icons.Outlined.Settings, Icons.Filled.Settings, selectedRoute, onNavigate)
        }
    }
}

@Composable
private fun RowScope.NavItem(route: String, label: String, outlinedIcon: ImageVector, filledIcon: ImageVector, selectedRoute: String, onNavigate: (String) -> Unit) {
    val tokens = LocalFlashLearnThemeTokens.current
    val selected = selectedRoute == route
    val reviewRoute = selectedRoute == AppRoutes.REVIEW
    val selectedColor = if (reviewRoute) tokens.reviewAccent else tokens.primary
    val unselectedColor = if (reviewRoute) tokens.reviewMutedText else tokens.onSurfaceVariant
    NavigationBarItem(
        selected = selected,
        onClick = { onNavigate(route) },
        icon = {
            Box(
                Modifier
                    .then(if (selected) Modifier.background(selectedColor.copy(alpha = .12f), MaterialTheme.shapes.medium) else Modifier)
                    .padding(horizontal = tokens.dp(13f), vertical = tokens.dp(6f))
            ) {
                Icon(
                    imageVector = if (selected && tokens.iconStyle == IconStyle.FILLED) filledIcon else outlinedIcon,
                    contentDescription = label,
                    modifier = Modifier.size(tokens.iconMedium)
                )
            }
        },
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = selectedColor,
            selectedTextColor = selectedColor,
            indicatorColor = Color.Transparent,
            unselectedIconColor = unselectedColor,
            unselectedTextColor = unselectedColor
        )
    )
}

@Composable
fun ScreenHeader(title: String, onBack: (() -> Unit)? = null, trailing: @Composable (() -> Unit)? = null) {
    FlashLearnScreenHeader(title = title, onBack = onBack, trailing = trailing)
}

@Composable
fun PurpleHeroCard(title: String, value: String, subtitle: String) {
    val tokens = LocalFlashLearnThemeTokens.current
    Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, colors = CardDefaults.cardColors(tokens.cardColor), elevation = CardDefaults.cardElevation(tokens.cardElevation)) {
        Box(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(tokens.gradientStart, tokens.gradientEnd)))) {
            Row(Modifier.fillMaxWidth().padding(horizontal = tokens.dp(22f), vertical = tokens.dp(18f)), verticalAlignment = Alignment.CenterVertically) {
                Text("🔥", style = MaterialTheme.typography.displaySmall)
                Spacer(Modifier.width(tokens.contentGap))
                Column(Modifier.weight(1f)) {
                    Text(title, color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.titleMedium)
                    Text(value, color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.displaySmall)
                    Text(subtitle, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = .9f), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    val tokens = LocalFlashLearnThemeTokens.current
    Text(text, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = tokens.dp(4f), vertical = tokens.dp(2f)))
}
