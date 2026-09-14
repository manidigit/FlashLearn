package com.flashlearn.app.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import com.flashlearn.app.ui.*
import com.flashlearn.domain.model.VocabularyDifficulty

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
    personalWordDifficulty: VocabularyDifficulty? = null,
    onPersonalWordDifficultyChange: (VocabularyDifficulty?) -> Unit = {},
    quizChallenge: QuizChallenge = QuizChallenge.B,
    onQuizChallengeChange: (QuizChallenge) -> Unit = {},
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
        Spacer(Modifier.height(12.dp))
        Section("زبان برنامه")
        SettingsRow(Icons.Outlined.Language, "زبان رابط کاربری", "فارسی", null)
        Spacer(Modifier.height(8.dp))
        Section("زبان پیش‌فرض یادگیری")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LanguageChoice(languagePair.source, "مبدأ", { sourceMenu = true }, Modifier.weight(1f))
            LanguageChoice(languagePair.target, "مقصد", { targetMenu = true }, Modifier.weight(1f))
        }
        TextButton(onClick = { onLanguagePairChange(languagePair.reversed()) }, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("↔ جابه‌جایی زبان‌ها") }
        DropdownMenu(expanded = sourceMenu, onDismissRequest = { sourceMenu = false }) {
            LearningLanguage.entries.forEach { lang -> DropdownMenuItem(text = { Text(lang.labelFa) }, onClick = { onLanguagePairChange(languagePair.copy(source = lang)); sourceMenu = false }) }
        }
        DropdownMenu(expanded = targetMenu, onDismissRequest = { targetMenu = false }) {
            LearningLanguage.entries.forEach { lang -> DropdownMenuItem(text = { Text(lang.labelFa) }, onClick = { onLanguagePairChange(languagePair.copy(target = lang)); targetMenu = false }) }
        }
        SettingsRow(Icons.Outlined.Translate, "جهت نمایش زبان", if (layoutDirection == AppLayoutDirection.RTL) "راست‌به‌چپ" else "چپ‌به‌راست") { onLayoutDirectionChange(if (layoutDirection == AppLayoutDirection.RTL) AppLayoutDirection.LTR else AppLayoutDirection.RTL) }
        Spacer(Modifier.height(12.dp))
        Section("سختی واژه‌ها")
        Text("چقدر این کلمه برای من سخت است", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(6.dp))
        val wordDifficultyOptions = listOf("همه" to (personalWordDifficulty == null)) + VocabularyDifficulty.entries.map { difficultyLabel(it) to (it == personalWordDifficulty) }
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            wordDifficultyOptions.forEachIndexed { index, (label, selected) -> FilterChip(selected = selected, onClick = { onPersonalWordDifficultyChange(if (index == 0) null else VocabularyDifficulty.entries[index - 1]) }, label = { Text(label) }) }
        }
        Spacer(Modifier.height(12.dp))
        Section("چالش آزمون")
        Text("درجه شباهت گزینه‌های غلط", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuizChallenge.entries.forEach { challenge -> FilterChip(selected = challenge == quizChallenge, onClick = { onQuizChallengeChange(challenge) }, label = { Text(challenge.label) }, modifier = Modifier.weight(1f)) }
        }
        Spacer(Modifier.height(12.dp))
        Section("داده‌ها")
        SettingsRow(Icons.Outlined.Storage, "پشتیبان‌گیری و بازیابی", "نسخه کامل واژگان و پیشرفت") { onBackup() }
        SettingsRow(Icons.Outlined.Storage, "صادرات / واردات کلمه", "ورود و خروج واژه‌ها") { onImportExport() }
        Spacer(Modifier.height(10.dp))
        Section("درباره")
        SettingsRow(Icons.Outlined.Language, "درباره برنامه", "FlashLearn • نسخه 5.62") { aboutDialog = true }
    }
    if (aboutDialog) {
        AlertDialog(onDismissRequest = { aboutDialog = false }, title = { Text("درباره FlashLearn") }, text = { Text("FlashLearn\nنسخه 5.62\n\nبرنامه یادگیری و مرور واژگان با پیگیری پیشرفت.") }, confirmButton = { TextButton(onClick = { aboutDialog = false }) { Text("باشه") } })
    }
}

private fun difficultyLabel(difficulty: VocabularyDifficulty) = when (difficulty) {
    VocabularyDifficulty.EASY -> "آسان"; VocabularyDifficulty.MEDIUM -> "متوسط"; VocabularyDifficulty.HARD -> "سخت"; VocabularyDifficulty.VERY_HARD -> "خیلی سخت"
}

@Composable private fun Section(text: String) { Text(text, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 6.dp)) }
@Composable private fun LanguageChoice(language: LearningLanguage, label: String, onClick: () -> Unit, modifier: Modifier) { OutlinedCard(modifier.clickable(onClick = onClick)) { Column(Modifier.fillMaxWidth().padding(9.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(label, style = MaterialTheme.typography.labelSmall); Text(language.labelFa) } } }
@Composable private fun AppearanceChoice(label: String, mode: AppearanceMode, selected: AppearanceMode, onSelect: (AppearanceMode) -> Unit, modifier: Modifier) { OutlinedCard(onClick = { onSelect(mode) }, modifier = modifier, colors = CardDefaults.outlinedCardColors(containerColor = if (selected == mode) MaterialTheme.colorScheme.primary.copy(alpha = .08f) else MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, if (selected == mode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)) { Column(Modifier.fillMaxWidth().padding(vertical = 9.dp), horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Outlined.Palette, null, tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.height(2.dp)); Text(label, style = MaterialTheme.typography.labelSmall) } } }
@Composable private fun AccentChoice(label: String, color: AccentColor, selected: AccentColor, onSelect: (AccentColor) -> Unit, tint: Color, modifier: Modifier) { OutlinedCard(onClick = { onSelect(color) }, modifier = modifier, border = BorderStroke(2.dp, if (selected == color) tint else MaterialTheme.colorScheme.outlineVariant)) { Column(Modifier.fillMaxWidth().padding(vertical = 7.dp), horizontalAlignment = Alignment.CenterHorizontally) { Surface(shape = MaterialTheme.shapes.small, color = tint, modifier = Modifier.size(20.dp)) {}; Spacer(Modifier.height(2.dp)); Text(label, style = MaterialTheme.typography.labelSmall) } } }
@Composable private fun SettingsRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, value: String, onClick: (() -> Unit)? = null) { Card(modifier = Modifier.fillMaxWidth().padding(top = 5.dp).then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier), shape = MaterialTheme.shapes.medium) { Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(9.dp)); Column(Modifier.weight(1f)) { Text(title, style = MaterialTheme.typography.titleSmall); Text(value, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall) }; if (onClick != null) Text("‹", color = MaterialTheme.colorScheme.onSurfaceVariant) } } }
