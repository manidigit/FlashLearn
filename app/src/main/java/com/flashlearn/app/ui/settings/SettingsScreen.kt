package com.flashlearn.app.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flashlearn.app.ui.AppearanceMode
import com.flashlearn.app.ui.components.SectionTitle

@Composable
fun SettingsScreen(
    appearance: AppearanceMode,
    onAppearanceChange: (AppearanceMode) -> Unit,
    onBackup: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 14.dp)) {
        Text("تنظیمات", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(18.dp))
        SectionTitle("ظاهر برنامه")
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppearanceChoice("سیستمی", AppearanceMode.SYSTEM, appearance, onAppearanceChange, Modifier.weight(1f))
            AppearanceChoice("تم تاریک", AppearanceMode.DARK, appearance, onAppearanceChange, Modifier.weight(1f))
            AppearanceChoice("تم روشن", AppearanceMode.LIGHT, appearance, onAppearanceChange, Modifier.weight(1f))
        }
        Spacer(Modifier.height(20.dp))
        SectionTitle("زبان برنامه")
        SettingsRow(Icons.Outlined.Language, "زبان برنامه", "فارسی")
        Spacer(Modifier.height(10.dp))
        SectionTitle("زبان پیش‌فرض یادگیری")
        SettingsRow(Icons.Outlined.Translate, "زبان مبدأ و مقصد", "اسپانیایی  →  انگلیسی")
        Spacer(Modifier.height(20.dp))
        SectionTitle("دیگر")
        SettingsRow(Icons.Outlined.Storage, "پشتیبان‌گیری و بازیابی", "صدور / ورود داده‌ها", onClick = onBackup)
        SettingsRow(Icons.Outlined.Storage, "صادرات / واردات داده‌ها", "مدیریت فایل‌های داده")
        SettingsRow(Icons.Outlined.Language, "درباره برنامه", "FlashLearn")
    }
}

@Composable
private fun AppearanceChoice(label: String, mode: AppearanceMode, selected: AppearanceMode, onSelect: (AppearanceMode) -> Unit, modifier: Modifier) {
    OutlinedCard(
        onClick = { onSelect(mode) },
        modifier = modifier,
        colors = CardDefaults.outlinedCardColors(containerColor = if (selected == mode) MaterialTheme.colorScheme.primary.copy(alpha = .08f) else MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, if (selected == mode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.fillMaxWidth().padding(vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Outlined.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(6.dp))
            Text(label)
        }
    }
}

@Composable
private fun SettingsRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, value: String, onClick: (() -> Unit)? = null) {
    Card(onClick = onClick ?: {}, enabled = onClick != null, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), shape = MaterialTheme.shapes.medium) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(value, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
            Text("‹", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
