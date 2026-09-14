package com.flashlearn.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.flashlearn.app.navigation.AppRoutes

@Composable
fun FlashLearnShell(
    selectedRoute: String,
    onNavigate: (String) -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(Modifier.weight(1f).fillMaxWidth()) { content() }
        NavigationBar(
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            NavItem(AppRoutes.HOME, "خانه", Icons.Outlined.Home, selectedRoute, onNavigate)
            NavItem(AppRoutes.REVIEW, "مرور", Icons.Outlined.History, selectedRoute, onNavigate)
            NavItem(AppRoutes.LIBRARY, "واژگان", Icons.Outlined.Book, selectedRoute, onNavigate)
            NavItem(AppRoutes.PROGRESS, "آمار", Icons.Outlined.BarChart, selectedRoute, onNavigate)
            NavItem(AppRoutes.SETTINGS, "تنظیمات", Icons.Outlined.Settings, selectedRoute, onNavigate)
        }
    }
}

@Composable
private fun RowScope.NavItem(
    route: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selectedRoute: String,
    onNavigate: (String) -> Unit
) {
    NavigationBarItem(
        selected = selectedRoute == route,
        onClick = { onNavigate(route) },
        icon = { Icon(icon, contentDescription = label) },
        label = { Text(label) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

@Composable
fun ScreenHeader(
    title: String,
    onBack: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Text("←", style = MaterialTheme.typography.titleLarge)
            }
        } else {
            Spacer(Modifier.width(48.dp))
        }
        Text(title, Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
        trailing?.invoke() ?: Spacer(Modifier.width(48.dp))
    }
}

@Composable
fun PurpleHeroCard(title: String, value: String, subtitle: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        shape = MaterialTheme.shapes.large
    ) {
        Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = Color.White, style = MaterialTheme.typography.titleMedium)
            Text(value, color = Color.White, style = MaterialTheme.typography.displaySmall)
            Text(subtitle, color = Color.White.copy(alpha = .9f), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
}
