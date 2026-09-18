package com.flashlearn.app.ui.settings

import com.flashlearn.app.ui.icons.mdiIcon

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.flashlearn.app.ui.*
import com.flashlearn.app.ui.theme.FlashLearnThemeSpec
import com.flashlearn.app.ui.theme.LocalFlashLearnThemeTokens
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
    onImportTheme: (Uri) -> Boolean = { false },
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
    val tokens = LocalFlashLearnThemeTokens.current
    var sourceMenu by remember { mutableStateOf(false) }
    var targetMenu by remember { mutableStateOf(false) }
    var importError by remember { mutableStateOf(false) }
    var exportError by remember { mutableStateOf(false) }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) importError = !onImportTheme(uri)
    }
    var pendingExport by remember { mutableStateOf<String?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        val json = pendingExport
        if (uri != null && !json.isNullOrBlank()) {
            runCatching { context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) } ?: error("openOutputStream failed") }
                .onFailure { exportError = true }
        }
        pendingExport = null
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = tokens.screenPadding, vertical = tokens.dp(12f))) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("تنظیمات", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
            IconButton(onClick = onBack) { Icon(mdiIcon("arrow-left"), contentDescription = "بازگشت") }
        }
        Spacer(Modifier.height(tokens.compactGap))
        Section("ظاهر برنامه")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.dp(7f))) {
            AppearanceChoice("سیستمی", AppearanceMode.SYSTEM, mdiIcon("monitor"), appearance, onAppearanceChange, Modifier.weight(1f))
            AppearanceChoice("تم تاریک", AppearanceMode.DARK, mdiIcon("weather-night"), appearance, onAppearanceChange, Modifier.weight(1f))
            AppearanceChoice("تم روشن", AppearanceMode.LIGHT, mdiIcon("white-balance-sunny"), appearance, onAppearanceChange, Modifier.weight(1f))
        }
        Spacer(Modifier.height(tokens.sectionGap))
        Section("تم کامل برنامه")
        Text("تم روی رنگ‌ها، پس‌زمینه‌ها، کارت‌ها، تایپوگرافی، تراکم، گوشه‌ها، ارتفاع کنترل‌ها، elevation و سبک آیکون‌ها اثر می‌گذارد.", color = tokens.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(tokens.compactGap))
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(tokens.compactGap)) {
            themes.forEach { spec -> ThemeChoice(spec, themeId, onThemeChange) }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.compactGap)) {
            OutlinedButton(onClick = { importLauncher.launch(arrayOf("application/json", "text/json")) }, modifier = Modifier.weight(1f)) { Icon(mdiIcon("file-upload-outline"), null); Spacer(Modifier.width(tokens.compactGap)); Text("وارد کردن") }
            OutlinedButton(onClick = { val spec = themes.firstOrNull { it.id == themeId } ?: FlashLearnThemeSpec.MODERN_PURPLE; pendingExport = spec.toJson(); exportLauncher.launch("flashlearn-theme-${spec.id}.json") }, modifier = Modifier.weight(1f)) { Icon(mdiIcon("file-download-outline"), null); Spacer(Modifier.width(tokens.compactGap)); Text("خروجی JSON") }
        }
        if (importError) Text("فایل تم معتبر نیست یا قابل خواندن نیست.", color = tokens.error, style = MaterialTheme.typography.bodySmall)
        if (exportError) Text("ذخیره فایل تم ناموفق بود.", color = tokens.error, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(tokens.sectionGap))
        Section("زبان برنامه")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.compactGap)) {
            SettingsRow(mdiIcon("language"), "زبان رابط کاربری", "فارسی", null, Modifier.weight(1f))
            SettingsRow(mdiIcon("translate"), "جهت نمایش زبان", if (layoutDirection == AppLayoutDirection.RTL) "راست‌به‌چپ" else "چپ‌به‌راست", { onLayoutDirectionChange(if (layoutDirection == AppLayoutDirection.RTL) AppLayoutDirection.LTR else AppLayoutDirection.RTL) }, Modifier.weight(1f))
        }
        Spacer(Modifier.height(tokens.sectionGap)); Section("زبان پیش‌فرض یادگیری")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.compactGap)) {
            Box(Modifier.weight(1f)) { LanguageChoice(languagePair.source, "مبدأ", { targetMenu = false; sourceMenu = true }, Modifier.fillMaxWidth()); DropdownMenu(expanded = sourceMenu, onDismissRequest = { sourceMenu = false }) { LearningLanguage.entries.filter { it != languagePair.target }.forEach { lang -> DropdownMenuItem(text = { LanguageLabel(lang) }, onClick = { onLanguagePairChange(LanguagePair(lang, languagePair.target)); sourceMenu = false }) } } }
            Box(Modifier.weight(1f)) { LanguageChoice(languagePair.target, "مقصد", { sourceMenu = false; targetMenu = true }, Modifier.fillMaxWidth()); DropdownMenu(expanded = targetMenu, onDismissRequest = { targetMenu = false }) { LearningLanguage.entries.filter { it != languagePair.source }.forEach { lang -> DropdownMenuItem(text = { LanguageLabel(lang) }, onClick = { onLanguagePairChange(LanguagePair(languagePair.source, lang)); targetMenu = false }) } } }
        }
        TextButton(onClick = { onLanguagePairChange(languagePair.reversed()) }, modifier = Modifier.align(Alignment.CenterHorizontally)) { Icon(mdiIcon("swap-horizontal"), contentDescription = null); Spacer(Modifier.width(tokens.compactGap)); Text("جابه‌جایی زبان‌ها") }
        Spacer(Modifier.height(tokens.sectionGap)); Section("تعداد پاسخ برای تغییر سطح")
        Text("تعداد پاسخ صحیح یا غلط پیاپی برای جابه‌جایی بین آسان، متوسط، سخت و خیلی سخت", color = tokens.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(tokens.compactGap))
        Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) { Row(Modifier.fillMaxWidth().padding(horizontal = tokens.dp(14f), vertical = tokens.dp(8f)), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) { IconButton(onClick = { onDifficultyThresholdChange(difficultyThreshold - 1) }, enabled = difficultyThreshold > 1) { Icon(mdiIcon("minus"), "کم کردن") }; Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(difficultyThreshold.toString(), style = MaterialTheme.typography.headlineMedium); Text("پاسخ پیاپی", style = MaterialTheme.typography.labelSmall, color = tokens.onSurfaceVariant) }; IconButton(onClick = { onDifficultyThresholdChange(difficultyThreshold + 1) }, enabled = difficultyThreshold < 20) { Icon(mdiIcon("plus"), "زیاد کردن") } } }
        Spacer(Modifier.height(tokens.sectionGap)); Section("داده‌ها"); SettingsRow(mdiIcon("database-outline"), "پشتیبان‌گیری و بازیابی", "ساخت، ذخیره و بازیابی فایل پشتیبان", onBackup)
        Spacer(Modifier.height(tokens.compactGap)); Section("درباره"); SettingsRow(mdiIcon("information-outline"), "درباره برنامه", "نسخه ${BuildConfig.VERSION_NAME} • تاریخچه و به‌روزرسانی‌ها", onAbout)
    }
}

@Composable private fun ThemeChoice(spec: FlashLearnThemeSpec, selected: String, onSelect: (String) -> Unit) { val tokens = LocalFlashLearnThemeTokens.current; val chosen = spec.id == selected; OutlinedCard(onClick = { onSelect(spec.id) }, modifier = Modifier.width(tokens.dp(190f)), border = BorderStroke(if (chosen) tokens.dp(2f) else tokens.dp(1f), if (chosen) tokens.primary else tokens.outlineColor)) { Column(Modifier.fillMaxWidth().padding(tokens.dp(10f))) { Row(verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(spec.name, style = MaterialTheme.typography.titleSmall, maxLines = 1); Text(if (spec.id in FlashLearnThemeSpec.BUILT_IN.map { it.id }) "تم داخلی" else "تم واردشده", style = MaterialTheme.typography.labelSmall, color = tokens.onSurfaceVariant) }; if (chosen) Icon(mdiIcon("check-circle-outline"), "انتخاب شده", tint = tokens.primary) }; Spacer(Modifier.height(tokens.compactGap)); Row(horizontalArrangement = Arrangement.spacedBy(tokens.dp(5f))) { listOf(spec.lightPrimary, spec.lightSecondary, spec.lightBackground, spec.lightCard, spec.darkBackground).forEach { color -> Surface(color = Color(color), shape = MaterialTheme.shapes.small, border = BorderStroke(tokens.dp(1f), tokens.outlineColor), modifier = Modifier.size(tokens.dp(22f))) {} }; }; Spacer(Modifier.height(tokens.compactGap)); Row(horizontalArrangement = Arrangement.spacedBy(tokens.dp(4f))) { AssistChip(onClick = {}, label = { Text("تراکم ${spec.densityScale}") }); AssistChip(onClick = {}, label = { Text(if (spec.iconStyle.equals("filled", true)) "پر" else "خطی") }) } } } }
@Composable private fun Section(text: String) { val tokens = LocalFlashLearnThemeTokens.current; Text(text, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = tokens.compactGap)) }
@Composable private fun LanguageChoice(language: LearningLanguage, label: String, onClick: () -> Unit, modifier: Modifier) { val tokens = LocalFlashLearnThemeTokens.current; OutlinedCard(onClick = onClick, modifier = modifier) { Row(Modifier.fillMaxWidth().padding(tokens.dp(10f)), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) { Text(language.flag, style = MaterialTheme.typography.titleLarge); Spacer(Modifier.width(tokens.compactGap)); Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(label, style = MaterialTheme.typography.labelSmall); Text(language.labelFa) } } } }
@Composable private fun LanguageLabel(language: LearningLanguage) { val tokens = LocalFlashLearnThemeTokens.current; Row(verticalAlignment = Alignment.CenterVertically) { Text(language.flag); Spacer(Modifier.width(tokens.compactGap)); Text(language.labelFa) } }
@Composable private fun AppearanceChoice(label: String, mode: AppearanceMode, icon: androidx.compose.ui.graphics.vector.ImageVector, selected: AppearanceMode, onSelect: (AppearanceMode) -> Unit, modifier: Modifier) { val tokens = LocalFlashLearnThemeTokens.current; OutlinedCard(onClick = { onSelect(mode) }, modifier = modifier, colors = CardDefaults.outlinedCardColors(containerColor = if (selected == mode) tokens.primary.copy(alpha = .08f) else tokens.surface), border = BorderStroke(tokens.dp(1f), if (selected == mode) tokens.primary else tokens.outlineColor)) { Column(Modifier.fillMaxWidth().padding(vertical = tokens.dp(10f)), horizontalAlignment = Alignment.CenterHorizontally) { Icon(icon, null, tint = tokens.primary); Spacer(Modifier.height(tokens.compactGap)); Text(label, style = MaterialTheme.typography.labelSmall) } } }
@Composable private fun AccentChoice(label: String, color: AccentColor, selected: AccentColor, onSelect: (AccentColor) -> Unit, tint: Color) { val tokens = LocalFlashLearnThemeTokens.current; OutlinedCard(onClick = { onSelect(color) }, modifier = Modifier.width(tokens.dp(68f)), border = BorderStroke(tokens.dp(2f), if (selected == color) tint else tokens.outlineColor)) { Column(Modifier.fillMaxWidth().padding(vertical = tokens.dp(7f)), horizontalAlignment = Alignment.CenterHorizontally) { Surface(shape = MaterialTheme.shapes.small, color = tint, modifier = Modifier.size(tokens.dp(22f))) {}; Spacer(Modifier.height(tokens.compactGap)); Text(label, style = MaterialTheme.typography.labelSmall) } } }
@Composable private fun SettingsRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, value: String, onClick: (() -> Unit)?, modifier: Modifier = Modifier) { val tokens = LocalFlashLearnThemeTokens.current; Card(modifier = modifier.padding(top = tokens.dp(5f)), shape = MaterialTheme.shapes.medium) { Row(Modifier.fillMaxWidth().clickable(enabled = onClick != null, onClick = { onClick?.invoke() }).padding(horizontal = tokens.dp(12f), vertical = tokens.dp(10f)), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = tokens.primary); Spacer(Modifier.width(tokens.compactGap)); Column(Modifier.weight(1f)) { Text(title, style = MaterialTheme.typography.titleSmall); Text(value, color = tokens.onSurfaceVariant, style = MaterialTheme.typography.labelSmall) }; if (onClick != null) Icon(mdiIcon("chevron-left"), contentDescription = null, tint = tokens.onSurfaceVariant) } } }
