package com.flashlearn.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.flashlearn.app.navigation.AppRoutes
import com.flashlearn.app.ui.theme.IconStyle
import com.flashlearn.app.ui.theme.LocalFlashLearnThemeTokens

@Composable
fun FlashLearnShell(
    selectedRoute: String,
    onNavigate: (String) -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = LocalFlashLearnThemeTokens.current
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(Modifier.weight(1f).fillMaxWidth()) { content() }
        NavigationBar(
            modifier = Modifier.fillMaxWidth().height((76f * tokens.densityScale).dp),
            containerColor = tokens.cardColor,
            tonalElevation = (5f * tokens.elevationScale).dp
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
private fun RowScope.NavItem(
    route: String,
    label: String,
    outlinedIcon: ImageVector,
    filledIcon: ImageVector,
    selectedRoute: String,
    onNavigate: (String) -> Unit
) {
    val tokens = LocalFlashLearnThemeTokens.current
    val selected = selectedRoute == route
    NavigationBarItem(
        selected = selected,
        onClick = { onNavigate(route) },
        icon = {
            Box(
                Modifier
                    .clip(RoundedCornerShape(MaterialTheme.shapes.medium.topStart))
                    .then(if (selected) Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = .12f)) else Modifier)
                    .padding(horizontal = (13f * tokens.densityScale).dp, vertical = (6f * tokens.densityScale).dp)
            ) {
                Icon(
                    imageVector = if (selected && tokens.iconStyle == IconStyle.FILLED) filledIcon else outlinedIcon,
                    contentDescription = label,
                    modifier = Modifier.size((24f * tokens.densityScale).dp)
                )
            }
        },
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            indicatorColor = Color.Transparent,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

@Composable
fun ScreenHeader(title: String, onBack: (() -> Unit)? = null, trailing: @Composable (() -> Unit)? = null) {
    val tokens = LocalFlashLearnThemeTokens.current
    Row(Modifier.fillMaxWidth().padding(horizontal = tokens.screenPadding, vertical = (14f * tokens.densityScale).dp), verticalAlignment = Alignment.CenterVertically) {
        if (onBack != null) IconButton(onClick = onBack) { Text("←", style = MaterialTheme.typography.titleLarge) }
        else Spacer(Modifier.width((48f * tokens.densityScale).dp))
        Text(title, Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
        trailing?.invoke() ?: Spacer(Modifier.width((48f * tokens.densityScale).dp))
    }
}

@Composable
fun PurpleHeroCard(title: String, value: String, subtitle: String) {
    val tokens = LocalFlashLearnThemeTokens.current
    Card(
        Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = tokens.cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = (4f * tokens.elevationScale).dp)
    ) {
        Box(
            Modifier.fillMaxWidth().background(
                Brush.linearGradient(listOf(tokens.gradientStart, tokens.gradientEnd))
            )
        ) {
            Row(Modifier.fillMaxWidth().padding(horizontal = (22f * tokens.densityScale).dp, vertical = (18f * tokens.densityScale).dp), verticalAlignment = Alignment.CenterVertically) {
                Text("🔥", style = MaterialTheme.typography.displaySmall)
                Spacer(Modifier.width(tokens.contentGap))
                Column(Modifier.weight(1f)) {
                    Text(title, color = Color.White, style = MaterialTheme.typography.titleMedium)
                    Text(value, color = Color.White, style = MaterialTheme.typography.displaySmall)
                    Text(subtitle, color = Color.White.copy(alpha = .9f), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    val tokens = LocalFlashLearnThemeTokens.current
    Text(text, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = (4f * tokens.densityScale).dp, vertical = (2f * tokens.densityScale).dp))
}
