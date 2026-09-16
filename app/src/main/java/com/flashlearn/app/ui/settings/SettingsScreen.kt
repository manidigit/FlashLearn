package com.flashlearn.app.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.flashlearn.app.ui.*
import com.flashlearn.app.ui.theme.FlashLearnThemeSpec
import com.flashlearn.domain.model.QuizDifficulty
import com.flashlearn.domain.model.VocabularyDifficulty
import com.flashlearn.domain.settings.SettingsKeys

@Composable
fun SettingsScreen(
    appearance: AppearanceMode,
    onAppearanceChange: (AppearanceMode) -> Unit,
    themeId: String = "modern_purple",
    themes: List<FlashLearnThemeSpec> = FlashLearnThemeSpec.BUILT_IN,
    onThemeChange: (String) -> Unit = {},
    onImportTheme: (android.net.Uri) -> Boolean = { false },
    accentColor: AccentColor = AccentColor.PURPLE,
    onAccentColorChange: (AccentColor) -> Unit = {},
    layoutDirection: AppLayoutDirection = AppLayoutDirection.RTL,
    onLayoutDirectionChange: (AppLayoutDirection) -> Unit = {},
    languagePair: LanguagePair = LanguagePair(),
    onLanguagePairChange: (LanguagePair) -> Unit = {},
    personalWordDifficulty: VocabularyDifficulty? = null,
    onPersonalWordDifficultyChange: (VocabularyDifficulty?) -> Unit = {},
    quizDifficulty: QuizDifficulty = QuizDifficulty.MEDIUM,
    onQuizDifficultyChange: (QuizDifficulty) -> Unit = {},
    difficultyThreshold: Int = SettingsKeys.DEFAULT_THRESHOLD_DIFFICULTY,
    onDifficultyThresholdChange: (Int) -> Unit = {},
    maximumReviewCards: Int = SettingsKeys.DEFAULT_MAXIMUM_REVIEW_CARDS,
    onMaximumReviewCardsChange: (Int) -> Unit = {},
    onBackup: () -> Unit = {},
    onAbout: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    var sourceMenu by remember { mutableStateOf(false) }
    var targetMenu by remember { mutableStateOf(false) }
    var importError by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) importError = !onImportTheme(uri)
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("تنظیمات", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
            TextButton(onClick = onBack) { Text("←") }
        }
        Spacer(Modifier.height(10.dp))
        Section("ظاهر برنامه")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            AppearanceChoice("سیستمی", AppearanceMode.SYSTEM, Icons.Outlined.Computer, appearance, onAppearanceChange, Modifier.weight(1f))
            AppearanceChoice("تم تاریک", AppearanceMode.DARK, Icons.Outlined.Brightness4, appearance, onAppearanceChange, Modifier.weight(1f))
            AppearanceChoice("تم روشن", AppearanceMode.LIGHT, Icons.Outlined.Brightness7, appearance, onAppearanceChange, Modifier.weight(1f))
        }
        Spacer(Modifier.height(14.dp))
        Section("تم برنامه")
        Text(
            "تم کامل ظاهر برنامه را انتخاب کنید. تم شامل رنگ‌ها، شکل کارت‌ها و مقیاس نوشته‌هاست.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(7.dp))
        themes.forEach { spec -> ThemeChoice(spec, themeId, onThemeChange) }
        OutlinedButton(
            onClick = { launcher.launch(arrayOf("application/json", "text/json")) },
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
        ) {
            Icon(Icons.Outlined.FileUpload, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("وارد کردن تم")
        }
        if (importError) {
            Text(
                "فایل تم معتبر نیست یا قابل خواندن نیست.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        Spacer(Modifier.height(12.dp))
        Section("رنگ برنامه")
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AccentChoice("بنفش", AccentColor.PURPLE, accentColor, onAccentColorChange, Color(0xFF7C3AED))
            AccentChoice("آبی", AccentColor.BLUE, accentColor, onAccentColorChange, Color(0xFF2563EB))
            AccentChoice("سبز", AccentColor.GREEN, accentColor, onAccentColorChange, Color(0xFF16A34A))
            AccentChoice("نارنجی", AccentColor.ORANGE, accentColor, onAccentColorChange, Color(0xFFEA580C))
            AccentChoice("صورتی", AccentColor.PINK, accentColor, onAccentColorChange, Color(0xFFDB2777))
        }
        Spacer(Modifier.height(12.dp))
        Section("زبان برنامه")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SettingsRow(Icons.Outlined.Language, "زبان رابط کاربری", "فارسی", null, Modifier.weight(1f))
            SettingsRow(
                Icons.Outlined.Translate,
                "جهت نمایش زبان",
                if (layoutDirection == AppLayoutDirection.RTL) "راست‌به‌چپ" else "چپ‌به‌راست",
                { onLayoutDirectionChange(if (layoutDirection == AppLayoutDirection.RTL) AppLayoutDirection.LTR else AppLayoutDirection.RTL) },
                Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(12.dp))
        Section("زبان پیش‌فرض یادگیری")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.weight(1f)) {
                LanguageChoice(languagePair.source, "مبدأ", { targetMenu = false; sourceMenu = true }, Modifier.fillMaxWidth())
                DropdownMenu(expanded = sourceMenu, onDismissRequest = { sourceMenu = false }) {
                    LearningLanguage.entries.filter { it != languagePair.target }.forEach { lang ->
                        DropdownMenuItem(
                            text = { LanguageLabel(lang) },
                            onClick = {
                                onLanguagePairChange(LanguagePair(lang, languagePair.target))
                                sourceMenu = false
                            }
                        )
                    }
                }
            }
            Box(Modifier.weight(1f)) {
                LanguageChoice(languagePair.target, "مقصد", { sourceMenu = false; targetMenu = true }, Modifier.fillMaxWidth())
                DropdownMenu(expanded = targetMenu, onDismissRequest = { targetMenu = false }) {
                    LearningLanguage.entries.filter { it != languagePair.source }.forEach { lang ->
                        DropdownMenuItem(
                            text = { LanguageLabel(lang) },
                            onClick = {
                                onLanguagePairChange(LanguagePair(languagePair.source, lang))
                                targetMenu = false
                            }
                        )
                    }
                }
            }
        }
        TextButton(onClick = { onLanguagePairChange(languagePair.reversed()) }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("↔ جابه‌جایی زبان‌ها")
        }
        Spacer(Modifier.height(12.dp))
        Section("تعداد پاسخ برای تغییر سطح")
        Text(
            "تعداد پاسخ صحیح یا غلط پیاپی برای جابه‌جایی بین آسان، متوسط، سخت و خیلی سخت",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(6.dp))
        Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { onDifficultyThresholdChange(difficultyThreshold - 1) }, enabled = difficultyThreshold > 1) {
                    Icon(Icons.Outlined.Remove, contentDescription = "کم کردن")
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(difficultyThreshold.toString(), style = MaterialTheme.typography.headlineMedium)
                    Text("پاسخ پیاپی", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { onDifficultyThresholdChange(difficultyThreshold + 1) }, enabled = difficultyThreshold < 20) {
                    Icon(Icons.Outlined.Add, contentDescription = "زیاد کردن")
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Section("داده‌ها")
        SettingsRow(Icons.Outlined.Storage, "پشتیبان‌گیری و بازیابی", "ساخت، ذخیره و بازیابی فایل پشتیبان", onBackup)
        Spacer(Modifier.height(10.dp))
        Section("درباره")
        SettingsRow(Icons.Outlined.Info, "درباره برنامه", "نسخه 5.74 • تاریخچه و اطلاعات سازنده", onAbout)
    }
}

@Composable
private fun ThemeChoice(spec: FlashLearnThemeSpec, selected: String, onSelect: (String) -> Unit) {
    val chosen = spec.id == selected
    OutlinedCard(
        onClick = { onSelect(spec.id) },
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        border = BorderStroke(
            if (chosen) 2.dp else 1.dp,
            if (chosen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                listOf(spec.lightPrimary, spec.lightSecondary, spec.lightBackground).forEach { color ->
                    Surface(
                        color = Color(color),
                        shape = MaterialTheme.shapes.small,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.size(28.dp)
                    ) {}
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(spec.name, style = MaterialTheme.typography.titleSmall)
                Text(
                    if (spec.id in FlashLearnThemeSpec.BUILT_IN.map { it.id }) "تم داخلی" else "تم واردشده",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (chosen) Icon(Icons.Outlined.CheckCircle, contentDescription = "انتخاب شده", tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable private fun Section(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 6.dp))
}

@Composable private fun LanguageChoice(language: LearningLanguage, label: String, onClick: () -> Unit, modifier: Modifier) {
    OutlinedCard(onClick = onClick, modifier = modifier) {
        Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Text(language.flag, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.width(7.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(label, style = MaterialTheme.typography.labelSmall)
                Text(language.labelFa)
            }
        }
    }
}

@Composable private fun LanguageLabel(language: LearningLanguage) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(language.flag)
        Spacer(Modifier.width(8.dp))
        Text(language.labelFa)
    }
}

@Composable private fun AppearanceChoice(label: String, mode: AppearanceMode, icon: androidx.compose.ui.graphics.vector.ImageVector, selected: AppearanceMode, onSelect: (AppearanceMode) -> Unit, modifier: Modifier) {
    OutlinedCard(
        onClick = { onSelect(mode) },
        modifier = modifier,
        colors = CardDefaults.outlinedCardColors(containerColor = if (selected == mode) MaterialTheme.colorScheme.primary.copy(alpha = .08f) else MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, if (selected == mode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(3.dp))
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable private fun AccentChoice(label: String, color: AccentColor, selected: AccentColor, onSelect: (AccentColor) -> Unit, tint: Color) {
    OutlinedCard(
        onClick = { onSelect(color) },
        modifier = Modifier.width(68.dp),
        border = BorderStroke(2.dp, if (selected == color) tint else MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.fillMaxWidth().padding(vertical = 7.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(shape = MaterialTheme.shapes.small, color = tint, modifier = Modifier.size(22.dp)) {}
            Spacer(Modifier.height(3.dp))
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable private fun SettingsRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, value: String, onClick: (() -> Unit)?, modifier: Modifier = Modifier) {
    Card(modifier = modifier.padding(top = 5.dp), shape = MaterialTheme.shapes.medium) {
        Row(
            Modifier.fillMaxWidth().clickable(enabled = onClick != null, onClick = { onClick?.invoke() }).padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(9.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(value, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
            }
            if (onClick != null) Text("‹", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
