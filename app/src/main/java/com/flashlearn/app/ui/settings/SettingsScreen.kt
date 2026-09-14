package com.flashlearn.app.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.flashlearn.app.ui.AccentColor
import com.flashlearn.app.ui.AppLayoutDirection
import com.flashlearn.app.ui.AppearanceMode
import com.flashlearn.app.ui.components.SectionTitle

@Composable
fun SettingsScreen(
    appearance: AppearanceMode,
    onAppearanceChange: (AppearanceMode) -> Unit,
    accentColor: AccentColor = AccentColor.PURPLE,
    onAccentColorChange: (AccentColor) -> Unit = {},
    layoutDirection: AppLayoutDirection = AppLayoutDirection.RTL,
    onLayoutDirectionChange: (AppLayoutDirection) -> Unit = {},
    onBackup: () -> Unit = {},
    onImportExport: () -> Unit = onBackup,
    onBack: () -> Unit = {}
) {
    var languageDialog by remember { mutableStateOf(false) }
    var aboutDialog by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 14.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("تنظیمات", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
            TextButton(onClick = onBack) { Text("بازگشت") }
        }
        Spacer(Modifier.height(14.dp))

        SectionTitle("ظاهر برنامه")
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppearanceChoice("سیستمی", AppearanceMode.SYSTEM, appearance, onAppearanceChange, Modifier.weight(1f))
            AppearanceChoice("تاریک", AppearanceMode.DARK, appearance, onAppearanceChange, Modifier.weight(1f))
            AppearanceChoice("روشن", AppearanceMode.LIGHT, appearance, onAppearanceChange, Modifier.weight(1f))
        }

        Spacer(Modifier.height(16.dp))
        SectionTitle("رنگ برنامه")
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AccentChoice("بنفش", AccentColor.PURPLE, accentColor, onAccentColorChange, Color(0xFF7C3AED), Modifier.weight(1f))
            AccentChoice("آبی", AccentColor.BLUE, accentColor, onAccentColorChange, Color(0xFF2563EB), Modifier.weight(1f))
            AccentChoice("سبز", AccentColor.GREEN, accentColor, onAccentColorChange, Color(0xFF16A34A), Modifier.weight(1f))
            AccentChoice("نارنجی", AccentColor.ORANGE, accentColor, onAccentColorChange, Color(0xFFEA580C), Modifier.weight(1f))
            AccentChoice("صورتی", AccentColor.PINK, accentColor, onAccentColorChange, Color(0xFFDB2777), Modifier.weight(1f))
        }

        Spacer(Modifier.height(16.dp))
        SectionTitle("زبان برنامه")
        SettingsRow(Icons.Outlined.Language, "زبان برنامه", "فارسی", onClick = { languageDialog = true })

        Spacer(Modifier.height(12.dp))
        SectionTitle("زبان پیش‌فرض یادگیری")
        SettingsRow(
            Icons.Outlined.Translate,
            "جهت نمایش زبان پیش‌فرض",
            if (layoutDirection == AppLayoutDirection.RTL) "راست‌به‌چپ" else "چپ‌به‌راست",
            onClick = {
                onLayoutDirectionChange(if (layoutDirection == AppLayoutDirection.RTL) AppLayoutDirection.LTR else AppLayoutDirection.RTL)
            }
        )

        Spacer(Modifier.height(12.dp))
        SectionTitle("داده‌ها")
        SettingsRow(Icons.Outlined.Storage, "پشتیبان‌گیری و بازیابی", "ساخت و بازیابی نسخه پشتیبان", onClick = onBackup)
        SettingsRow(Icons.Outlined.Storage, "صادرات / واردات کلمه", "باز کردن ابزار ورود و خروج داده", onClick = onImportExport)

        Spacer(Modifier.height(12.dp))
        SectionTitle("درباره")
        SettingsRow(Icons.Outlined.Language, "درباره برنامه", "FlashLearn", onClick = { aboutDialog = true })
    }

    if (languageDialog) {
        AlertDialog(
            onDismissRequest = { languageDialog = false },
            title = { Text("زبان برنامه") },
            text = { Text("زبان فعلی رابط کاربری: فارسی\n\nدر این نسخه رابط کاربری فارسی فعال است.") },
            confirmButton = { TextButton(onClick = { languageDialog = false }) { Text("باشه") } }
        )
    }

    if (aboutDialog) {
        AlertDialog(
            onDismissRequest = { aboutDialog = false },
            title = { Text("درباره FlashLearn") },
            text = { Text("FlashLearn\nنسخه 5.56\n\nبرنامه یادگیری و مرور واژگان با سیستم مرور و پیگیری پیشرفت.") },
            confirmButton = { TextButton(onClick = { aboutDialog = false }) { Text("باشه") } }
        )
    }
}

@Composable
private fun AppearanceChoice(label: String, mode: AppearanceMode, selected: AppearanceMode, onSelect: (AppearanceMode) -> Unit, modifier: Modifier) {
    OutlinedCard(
        onClick = { onSelect(mode) }, modifier = modifier,
        colors = CardDefaults.outlinedCardColors(containerColor = if (selected == mode) MaterialTheme.colorScheme.primary.copy(alpha = .08f) else MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, if (selected == mode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Outlined.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(4.dp)); Text(label)
        }
    }
}

@Composable
private fun AccentChoice(label: String, color: AccentColor, selected: AccentColor, onSelect: (AccentColor) -> Unit, tint: Color, modifier: Modifier) {
    OutlinedCard(
        onClick = { onSelect(color) }, modifier = modifier,
        border = BorderStroke(2.dp, if (selected == color) tint else MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.fillMaxWidth().padding(vertical = 9.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(shape = MaterialTheme.shapes.small, color = tint, modifier = Modifier.size(24.dp)) {}
            Spacer(Modifier.height(4.dp)); Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun SettingsRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, value: String, onClick: (() -> Unit)? = null) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp).then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(value, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
            if (onClick != null) Text("‹", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
