package com.flashlearn.app.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Brightness4
import androidx.compose.material.icons.outlined.Brightness7
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.flashlearn.app.ui.*
import com.flashlearn.domain.model.QuizDifficulty
import com.flashlearn.domain.model.VocabularyDifficulty
import com.flashlearn.domain.settings.SettingsKeys

@Composable
fun SettingsScreen(appearance: AppearanceMode, onAppearanceChange: (AppearanceMode) -> Unit, accentColor: AccentColor = AccentColor.PURPLE, onAccentColorChange: (AccentColor) -> Unit = {}, layoutDirection: AppLayoutDirection = AppLayoutDirection.RTL, onLayoutDirectionChange: (AppLayoutDirection) -> Unit = {}, languagePair: LanguagePair = LanguagePair(), onLanguagePairChange: (LanguagePair) -> Unit = {}, personalWordDifficulty: VocabularyDifficulty? = null, onPersonalWordDifficultyChange: (VocabularyDifficulty?) -> Unit = {}, quizDifficulty: QuizDifficulty = QuizDifficulty.MEDIUM, onQuizDifficultyChange: (QuizDifficulty) -> Unit = {}, difficultyThreshold: Int = SettingsKeys.DEFAULT_THRESHOLD_DIFFICULTY, onDifficultyThresholdChange: (Int) -> Unit = {}, maximumReviewCards: Int = SettingsKeys.DEFAULT_MAXIMUM_REVIEW_CARDS, onMaximumReviewCardsChange: (Int) -> Unit = {}, onBackup: () -> Unit = {}, onAbout: () -> Unit = {}, onBack: () -> Unit = {}) {
    var sourceMenu by remember { mutableStateOf(false) }; var targetMenu by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text("تنظیمات", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f)); TextButton(onClick = onBack) { Text("←") } }
        Spacer(Modifier.height(10.dp)); Section("ظاهر برنامه")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) { AppearanceChoice("سیستمی", AppearanceMode.SYSTEM, Icons.Outlined.Computer, appearance, onAppearanceChange, Modifier.weight(1f)); AppearanceChoice("تم تاریک", AppearanceMode.DARK, Icons.Outlined.Brightness4, appearance, onAppearanceChange, Modifier.weight(1f)); AppearanceChoice("تم روشن", AppearanceMode.LIGHT, Icons.Outlined.Brightness7, appearance, onAppearanceChange, Modifier.weight(1f)) }
        Spacer(Modifier.height(12.dp)); Section("رنگ برنامه")
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) { AccentChoice("بنفش", AccentColor.PURPLE, accentColor, onAccentColorChange, Color(0xFF7C3AED)); AccentChoice("آبی", AccentColor.BLUE, accentColor, onAccentColorChange, Color(0xFF2563EB)); AccentChoice("سبز", AccentColor.GREEN, accentColor, onAccentColorChange, Color(0xFF16A34A)); AccentChoice("نارنجی", AccentColor.ORANGE, accentColor, onAccentColorChange, Color(0xFFEA580C)); AccentChoice("صورتی", AccentColor.PINK, accentColor, onAccentColorChange, Color(0xFFDB2777)) }
        Spacer(Modifier.height(12.dp)); Section("زبان برنامه")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SettingsRow(Icons.Outlined.Language, "زبان رابط کاربری", "فارسی", modifier = Modifier.weight(1f))
            SettingsRow(Icons.Outlined.Translate, "جهت نمایش زبان", if (layoutDirection == AppLayoutDirection.RTL) "راست‌به‌چپ" else "چپ‌به‌راست", { onLayoutDirectionChange(if (layoutDirection == AppLayoutDirection.RTL) AppLayoutDirection.LTR else AppLayoutDirection.RTL) }, Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp)); Section("زبان پیش‌فرض یادگیری")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.weight(1f)) { LanguageChoice(languagePair.source, "مبدأ", { targetMenu = false; sourceMenu = true }, Modifier.fillMaxWidth()); DropdownMenu(expanded = sourceMenu, onDismissRequest = { sourceMenu = false }) { LearningLanguage.entries.filter { it != languagePair.target }.forEach { lang -> DropdownMenuItem(text = { LanguageLabel(lang) }, onClick = { onLanguagePairChange(LanguagePair(lang, languagePair.target)); sourceMenu = false }) } } }
            Box(Modifier.weight(1f)) { LanguageChoice(languagePair.target, "مقصد", { sourceMenu = false; targetMenu = true }, Modifier.fillMaxWidth()); DropdownMenu(expanded = targetMenu, onDismissRequest = { targetMenu = false }) { LearningLanguage.entries.filter { it != languagePair.source }.forEach { lang -> DropdownMenuItem(text = { LanguageLabel(lang) }, onClick = { onLanguagePairChange(LanguagePair(languagePair.source, lang)); targetMenu = false }) } } }
        }
        TextButton(onClick = { onLanguagePairChange(languagePair.reversed()) }, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("↔ جابه‌جایی زبان‌ها") }
        Spacer(Modifier.height(12.dp)); Section("تعداد پاسخ برای تغییر سطح")
        Text("تعداد پاسخ صحیح یا غلط پیاپی برای جابه‌جایی بین آسان، متوسط، سخت و خیلی سخت", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall); Spacer(Modifier.height(6.dp))
        Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) { Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) { IconButton(onClick = { onDifficultyThresholdChange(difficultyThreshold - 1) }, enabled = difficultyThreshold > 1) { Icon(Icons.Outlined.Remove, "کم کردن") }; Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(difficultyThreshold.toString(), style = MaterialTheme.typography.headlineMedium); Text("پاسخ پیاپی", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }; IconButton(onClick = { onDifficultyThresholdChange(difficultyThreshold + 1) }, enabled = difficultyThreshold < 20) { Icon(Icons.Outlined.Add, "زیاد کردن") } } }
        Spacer(Modifier.height(12.dp)); Section("داده‌ها"); SettingsRow(Icons.Outlined.Storage, "پشتیبان‌گیری و بازیابی", "ساخت، ذخیره و بازیابی فایل پشتیبان") { onBackup() }
        Spacer(Modifier.height(10.dp)); Section("درباره"); SettingsRow(Icons.Outlined.Language, "درباره برنامه", "نسخه 5.73 • تاریخچه و اطلاعات سازنده") { onAbout() }
    }
}
private fun difficultyLabel(difficulty: VocabularyDifficulty) = when (difficulty) { VocabularyDifficulty.EASY -> "آسان"; VocabularyDifficulty.MEDIUM -> "متوسط"; VocabularyDifficulty.HARD -> "سخت"; VocabularyDifficulty.VERY_HARD -> "خیلی سخت" }
private fun quizDifficultyLabel(difficulty: QuizDifficulty) = when (difficulty) { QuizDifficulty.EASY -> "آسان"; QuizDifficulty.MEDIUM -> "متوسط"; QuizDifficulty.HARD -> "سخت" }
@Composable private fun Section(text: String) { Text(text, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 6.dp)) }
@Composable private fun LanguageChoice(language: LearningLanguage, label: String, onClick: () -> Unit, modifier: Modifier) { OutlinedCard(onClick = onClick, modifier = modifier) { Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) { Text(language.flag, style = MaterialTheme.typography.titleLarge); Spacer(Modifier.width(7.dp)); Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(label, style = MaterialTheme.typography.labelSmall); Text(language.labelFa) } } } }
@Composable private fun LanguageLabel(language: LearningLanguage) { Row(verticalAlignment = Alignment.CenterVertically) { Text(language.flag); Spacer(Modifier.width(8.dp)); Text(language.labelFa) } }
@Composable private fun AppearanceChoice(label: String, mode: AppearanceMode, icon: androidx.compose.ui.graphics.vector.ImageVector, selected: AppearanceMode, onSelect: (AppearanceMode) -> Unit, modifier: Modifier) { OutlinedCard(onClick = { onSelect(mode) }, modifier = modifier, colors = CardDefaults.outlinedCardColors(containerColor = if (selected == mode) MaterialTheme.colorScheme.primary.copy(alpha = .08f) else MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, if (selected == mode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)) { Column(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.height(3.dp)); Text(label, style = MaterialTheme.typography.labelSmall) } } }
@Composable private fun AccentChoice(label: String, color: AccentColor, selected: AccentColor, onSelect: (AccentColor) -> Unit, tint: Color) { OutlinedCard(onClick = { onSelect(color) }, modifier = Modifier.width(68.dp), border = BorderStroke(2.dp, if (selected == color) tint else MaterialTheme.colorScheme.outlineVariant)) { Column(Modifier.fillMaxWidth().padding(vertical = 7.dp), horizontalAlignment = Alignment.CenterHorizontally) { Surface(shape = MaterialTheme.shapes.small, color = tint, modifier = Modifier.size(22.dp)) {}; Spacer(Modifier.height(3.dp)); Text(label, style = MaterialTheme.typography.labelSmall) } } }
@Composable private fun SettingsRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, value: String, onClick: (() -> Unit)? = null, modifier: Modifier = Modifier) { Card(modifier = modifier.padding(top = 5.dp), shape = MaterialTheme.shapes.medium) { Row(Modifier.fillMaxWidth().clickable(enabled = onClick != null, onClick = { onClick?.invoke() }).padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(9.dp)); Column(Modifier.weight(1f)) { Text(title, style = MaterialTheme.typography.titleSmall); Text(value, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall) }; if (onClick != null) Text("‹", color = MaterialTheme.colorScheme.onSurfaceVariant) } } }
