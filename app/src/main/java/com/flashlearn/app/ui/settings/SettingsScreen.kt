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
import com.flashlearn.app.ui.LanguagePair
import com.flashlearn.app.ui.LearningLanguage

@Composable
fun SettingsScreen(
    appearance: AppearanceMode,
    onAppearanceChange: (AppearanceMode) -> Unit,
    accentColor: AccentColor = AccentColor.PURPLE,
    onAccentColorChange: (AccentColor) -> Unit = {},
    layoutDirection: AppLayoutDirection = AppLayoutDirection.RTL,
    onLayoutDirectionChange: (AppLayoutDirection) -> Unit = {},
    languagePair: LanguagePair = LanguagePair(),
    onLanguagePairChange: (LanguagePair) -> Unit = {},
    onBackup: () -> Unit = {},
    onImportExport: () -> Unit = onBackup,
    onBack: () -> Unit = {}
) {
    var sourceMenu by remember { mutableStateOf(false) }
    var targetMenu by remember { mutableStateOf(false) }
    var aboutDialog by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("تنظیمات", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
            TextButton(onClick = onBack) { Text("بازگشت") }
        }
        Spacer(Modifier.height(10.dp))

        Section("ظاهر برنامه")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            AppearanceChoice("سیستمی", AppearanceMode.SYSTEM, appearance, onAppearanceChange, Modifier.weight(1f))
            AppearanceChoice("تاریک", AppearanceMode.DARK, appearance, onAppearanceChange, Modifier.weight(1f))
            AppearanceChoice("روشن", AppearanceMode.LIGHT, appearance, onAppearanceChange, Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        Section("رنگ برنامه")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            AccentChoice("بنفش", AccentColor.PURPLE, accentColor, onAccentColorChange, Color(0xFF7C3AED), Modifier.weight(1f))
            AccentChoice("آبی", AccentColor.BLUE, accentColor, onAccentColorChange, Color(0xFF2563EB), Modifier.weight(1f))
            AccentChoice("سبز", AccentColor.GREEN, accentColor, onAccentColorChange, Color(0xFF16A34A), Modifier.weight(1f))
            AccentChoice("نارنجی", AccentColor.ORANGE, accentColor, onAccentColorChange, Color(0xFFEA580C), Modifier.weight(1f))
            AccentChoice("صورتی", AccentColor.PINK, accentColor, onAccentColorChange, Color(0xFFDB2777), Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))

        Section("زبان برنامه")
        SettingsRow(Icons.Outlined.Language, "زبان رابط کاربری", "فارسی", null)
        Spacer(Modifier.height(8.dp))
        Section("زبان پیش‌فرض یادگیری")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LanguageChoice(languagePair.source, "مبدأ", { sourceMenu = true }, Modifier.weight(1f))
            LanguageChoice(languagePair.target, "مقصد", { targetMenu = true }, Modifier.weight(1f))
        }
        TextButton(onClick = { onLanguagePairChange(languagePair.reversed()) }, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("↔ جابه‌جایی زبان‌ها") }

        DropdownMenu(sourceMenu, { sourceMenu = false }) { LearningLanguage.entries.forEach { lang -> DropdownMenuItem(text = { Text(lang.labelFa) }, onClick = { onLanguagePairChange(languagePair.copy(source = lang)); sourceMenu = false }) } }
        DropdownMenu(targetMenu, { targetMenu = false }) { LearningLanguage.entries.forEach { lang -> DropdownMenuItem(text = { Text(lang.labelFa) }, onClick = { onLanguagePairChange(languagePair.copy(target = lang)); targetMenu = false }) } }

        Spacer(Modifier.height(4.dp))
        SettingsRow(Icons.Outlined.Translate, "جهت نمایش زبان", if (layoutDirection == AppLayoutDirection.RTL) "راست‌به‌چپ" else "چپ‌به‌راست") { onLayoutDirectionChange(if (layoutDirection == AppLayoutDirection.RTL) AppLayoutDirection.LTR else AppLayoutDirection.RTL) }

        Spacer(Modifier.height(10.dp))
        Section("داده‌ها")
        SettingsRow(Icons.Outlined.Storage, "پشتیبان‌گیری و بازیابی", "نسخه کامل واژگان و پیشرفت") { onBackup() }
        SettingsRow(Icons.Outlined.Storage, "صادرات / واردات کلمه", "ورود و خروج واژه‌ها") { onImportExport() }
        Spacer(Modifier.height(10.dp))
        Section("درباره")
        SettingsRow(Icons.Outlined.Language, "درباره برنامه", "FlashLearn • نسخه 5.58") { aboutDialog = true }
    }

    if (aboutDialog) AlertDialog(onDismissRequest = { aboutDialog = false }, title = { Text("درباره FlashLearn") }, text = { Text("FlashLearn\nنسخه 5.58\n\nبرنامه یادگیری و مرور واژگان با پیگیری پیشرفت.") }, confirmButton = { TextButton(onClick = { aboutDialog = false }) { Text("باشه") } })
}

@Composable private fun Section(text: String) { Text(text, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 6.dp)) }

@Composable private fun LanguageChoice(language: LearningLanguage, label: String, onClick: () -> Unit, modifier: Modifier) {
    OutlinedCard(modifier.clickable(onClick = onClick)) { Column(Modifier.fillMaxWidth().padding(9.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(label, style = MaterialTheme.typography.labelSmall); Text(language.labelFa) } }
}

@Composable private fun AppearanceChoice(label: String, mode: AppearanceMode, selected: AppearanceMode, onSelect: (AppearanceMode) -> Unit, modifier: Modifier) {
    OutlinedCard(onClick = { onSelect(mode) }, modifier = modifier, colors = CardDefaults.outlinedCardColors(containerColor = if (selected == mode) MaterialTheme.colorScheme.primary.copy(alpha = .08f) else MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, if (selected == mode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)) { Column(Modifier.fillMaxWidth().padding(vertical = 9.dp), horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Outlined.Palette, null, tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.height(2.dp)); Text(label, style = MaterialTheme.typography.labelSmall) } }
}

@Composable private fun AccentChoice(label: String, color: AccentColor, selected: AccentColor, onSelect: (AccentColor) -> Unit, tint: Color, modifier: Modifier) {
    OutlinedCard(onClick = { onSelect(color) }, modifier = modifier, border = BorderStroke(2.dp, if (selected == color) tint else MaterialTheme.colorScheme.outlineVariant)) { Column(Modifier.fillMaxWidth().padding(vertical = 7.dp), horizontalAlignment = Alignment.CenterHorizontally) { Surface(shape = MaterialTheme.shapes.small, color = tint, modifier = Modifier.size(20.dp)) {}; Spacer(Modifier.height(2.dp)); Text(label, style = MaterialTheme.typography.labelSmall) } }
}

@Composable private fun SettingsRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, value: String, onClick: (() -> Unit)? = null) {
    Card(modifier = Modifier.fillMaxWidth().padding(top = 5.dp).then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier), shape = MaterialTheme.shapes.medium) { Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(9.dp)); Column(Modifier.weight(1f)) { Text(title, style = MaterialTheme.typography.titleSmall); Text(value, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall) }; if (onClick != null) Text("‹", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
}
