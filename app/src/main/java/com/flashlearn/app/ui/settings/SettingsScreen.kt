package com.flashlearn.app.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.flashlearn.app.R
import com.flashlearn.app.ui.*
import com.flashlearn.app.ui.theme.FlashLearnThemeSpec
import com.flashlearn.app.ui.localization.FlashLearnLocales
import com.flashlearn.app.ui.components.FlashLearnScreenHeader
import com.flashlearn.app.ui.components.FlashLearnBackButton
import com.flashlearn.app.ui.theme.LocalFlashLearnThemeTokens
import com.flashlearn.domain.model.QuizDifficulty
import com.flashlearn.domain.model.VocabularyDifficulty
import com.flashlearn.domain.settings.SettingsKeys

@Composable
fun SettingsScreen(
    uiLanguage: String = FlashLearnLocales.PERSIAN,
    onUiLanguageChange: (String) -> Unit = {},
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
    onHelp: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val tokens = LocalFlashLearnThemeTokens.current
    val currentUiLanguage = FlashLearnLocales.normalize(uiLanguage)
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
            FlashLearnBackButton(onBack)
            Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(tokens.compactGap))
        Section(stringResource(R.string.settings_appearance))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.dp(7f))) {
            AppearanceChoice(stringResource(R.string.appearance_system), AppearanceMode.SYSTEM, Icons.Outlined.Computer, appearance, onAppearanceChange, Modifier.weight(1f))
            AppearanceChoice(stringResource(R.string.appearance_dark), AppearanceMode.DARK, Icons.Outlined.Brightness4, appearance, onAppearanceChange, Modifier.weight(1f))
            AppearanceChoice(stringResource(R.string.appearance_light), AppearanceMode.LIGHT, Icons.Outlined.Brightness7, appearance, onAppearanceChange, Modifier.weight(1f))
        }
        Spacer(Modifier.height(tokens.sectionGap))
        Section(stringResource(R.string.settings_full_theme))
        Text(stringResource(R.string.settings_theme_summary), color = tokens.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(tokens.compactGap))
        ThemeDropdown(
            themes = themes,
            selectedThemeId = themeId,
            onThemeChange = onThemeChange
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.compactGap)) {
            OutlinedButton(onClick = { importLauncher.launch(arrayOf("application/json", "text/json")) }, modifier = Modifier.weight(1f)) { Icon(Icons.Outlined.FileUpload, null); Spacer(Modifier.width(tokens.compactGap)); Text(stringResource(R.string.settings_import)) }
            OutlinedButton(onClick = { val spec = themes.firstOrNull { it.id == themeId } ?: FlashLearnThemeSpec.GROK; pendingExport = spec.toJson(); exportLauncher.launch("flashlearn-theme-${spec.id}.json") }, modifier = Modifier.weight(1f)) { Icon(Icons.Outlined.FileDownload, null); Spacer(Modifier.width(tokens.compactGap)); Text(stringResource(R.string.settings_export_json)) }
        }
        if (importError) Text(stringResource(R.string.settings_theme_invalid), color = tokens.error, style = MaterialTheme.typography.bodySmall)
        if (exportError) Text(stringResource(R.string.settings_theme_save_failed), color = tokens.error, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(tokens.sectionGap))
        Section(stringResource(R.string.settings_language))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.compactGap)) {
            SettingsRow(
                Icons.Outlined.Language,
                stringResource(R.string.settings_ui_language),
                if (currentUiLanguage == FlashLearnLocales.PERSIAN) stringResource(R.string.settings_persian) else stringResource(R.string.settings_english),
                { onUiLanguageChange(if (currentUiLanguage == FlashLearnLocales.PERSIAN) FlashLearnLocales.ENGLISH else FlashLearnLocales.PERSIAN) },
                Modifier.weight(1f)
            )
            SettingsRow(Icons.Outlined.Translate, stringResource(R.string.settings_direction), if (layoutDirection == AppLayoutDirection.RTL) stringResource(R.string.settings_rtl) else stringResource(R.string.settings_ltr), { onLayoutDirectionChange(if (layoutDirection == AppLayoutDirection.RTL) AppLayoutDirection.LTR else AppLayoutDirection.RTL) }, Modifier.weight(1f))
        }
        Spacer(Modifier.height(tokens.sectionGap)); Section(stringResource(R.string.settings_learning_language))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.compactGap)) {
            Box(Modifier.weight(1f)) { LanguageChoice(languagePair.source, stringResource(R.string.settings_source), { targetMenu = false; sourceMenu = true }, Modifier.fillMaxWidth()); DropdownMenu(expanded = sourceMenu, onDismissRequest = { sourceMenu = false }) { LearningLanguage.entries.filter { it != languagePair.target }.forEach { lang -> DropdownMenuItem(text = { LanguageLabel(lang) }, onClick = { onLanguagePairChange(LanguagePair(lang, languagePair.target)); sourceMenu = false }) } } }
            Box(Modifier.weight(1f)) { LanguageChoice(languagePair.target, stringResource(R.string.settings_target), { sourceMenu = false; targetMenu = true }, Modifier.fillMaxWidth()); DropdownMenu(expanded = targetMenu, onDismissRequest = { targetMenu = false }) { LearningLanguage.entries.filter { it != languagePair.source }.forEach { lang -> DropdownMenuItem(text = { LanguageLabel(lang) }, onClick = { onLanguagePairChange(LanguagePair(languagePair.source, lang)); targetMenu = false }) } } }
        }
        TextButton(onClick = { onLanguagePairChange(languagePair.reversed()) }, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text(stringResource(R.string.settings_swap_languages)) }
        Spacer(Modifier.height(tokens.sectionGap)); Section(stringResource(R.string.settings_difficulty_threshold))
        Text(stringResource(R.string.settings_difficulty_summary), color = tokens.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(tokens.compactGap))
        Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) { Row(Modifier.fillMaxWidth().padding(horizontal = tokens.dp(14f), vertical = tokens.dp(8f)), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) { IconButton(onClick = { onDifficultyThresholdChange(difficultyThreshold - 1) }, enabled = difficultyThreshold > 1) { Icon(Icons.Outlined.Remove, stringResource(R.string.settings_decrease)) }; Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(difficultyThreshold.toString(), style = MaterialTheme.typography.headlineMedium); Text(stringResource(R.string.settings_consecutive_answers), style = MaterialTheme.typography.labelSmall, color = tokens.onSurfaceVariant) }; IconButton(onClick = { onDifficultyThresholdChange(difficultyThreshold + 1) }, enabled = difficultyThreshold < 20) { Icon(Icons.Outlined.Add, stringResource(R.string.settings_increase)) } } }
        Spacer(Modifier.height(tokens.sectionGap)); Section(stringResource(R.string.settings_data)); SettingsRow(Icons.Outlined.Storage, stringResource(R.string.settings_backup), stringResource(R.string.settings_backup_summary), onBackup)
        Spacer(Modifier.height(tokens.sectionGap)); Section(stringResource(R.string.settings_help)); SettingsRow(Icons.Outlined.HelpOutline, stringResource(R.string.settings_help_title), stringResource(R.string.settings_help_summary), onHelp)
        Spacer(Modifier.height(tokens.compactGap)); Section(stringResource(R.string.settings_about)); SettingsRow(Icons.Outlined.Info, stringResource(R.string.settings_about), stringResource(R.string.settings_about_summary), onAbout)
    }
}

@Composable
private fun ThemeDropdown(
    themes: List<FlashLearnThemeSpec>,
    selectedThemeId: String,
    onThemeChange: (String) -> Unit
) {
    val tokens = LocalFlashLearnThemeTokens.current
    var expanded by remember { mutableStateOf(false) }
    val selected = themes.firstOrNull { it.id == selectedThemeId } ?: themes.firstOrNull()

    Box(Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = tokens.dp(14f), vertical = tokens.dp(10f))
        ) {
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                Text(
                    selected?.name ?: stringResource(R.string.settings_theme_unavailable),
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1
                )
                Text(
                    if (selected?.id in FlashLearnThemeSpec.BUILT_IN.map { it.id }) {
                        stringResource(R.string.settings_builtin_theme)
                    } else {
                        stringResource(R.string.settings_imported_theme)
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = tokens.onSurfaceVariant
                )
            }
            Icon(
                if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                contentDescription = stringResource(R.string.settings_theme_toggle)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.92f)
        ) {
            themes.forEach { spec ->
                val chosen = spec.id == selectedThemeId
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(spec.name, maxLines = 1)
                            Text(
                                if (spec.id in FlashLearnThemeSpec.BUILT_IN.map { it.id }) {
                                    stringResource(R.string.settings_builtin_theme)
                                } else {
                                    stringResource(R.string.settings_imported_theme)
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = tokens.onSurfaceVariant
                            )
                        }
                    },
                    leadingIcon = {
                        Row(horizontalArrangement = Arrangement.spacedBy(tokens.dp(3f))) {
                            listOf(spec.lightPrimary, spec.lightSecondary, spec.lightBackground).forEach { color ->
                                Surface(
                                    color = Color(color),
                                    shape = MaterialTheme.shapes.small,
                                    border = BorderStroke(tokens.dp(1f), tokens.outlineColor),
                                    modifier = Modifier.size(tokens.dp(18f))
                                ) {}
                            }
                        }
                    },
                    trailingIcon = {
                        if (chosen) {
                            Icon(
                                Icons.Outlined.Check,
                                contentDescription = stringResource(R.string.settings_selected),
                                tint = tokens.primary
                            )
                        }
                    },
                    onClick = {
                        onThemeChange(spec.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable private fun Section(text: String) { val tokens = LocalFlashLearnThemeTokens.current; Text(text, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = tokens.compactGap)) }
@Composable private fun LanguageChoice(language: LearningLanguage, label: String, onClick: () -> Unit, modifier: Modifier) { val tokens = LocalFlashLearnThemeTokens.current; OutlinedCard(onClick = onClick, modifier = modifier) { Row(Modifier.fillMaxWidth().padding(tokens.dp(10f)), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) { Text(language.flag, style = MaterialTheme.typography.titleLarge); Spacer(Modifier.width(tokens.compactGap)); Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(label, style = MaterialTheme.typography.labelSmall); Text(language.labelFa) } } } }
@Composable private fun LanguageLabel(language: LearningLanguage) { val tokens = LocalFlashLearnThemeTokens.current; Row(verticalAlignment = Alignment.CenterVertically) { Text(language.flag); Spacer(Modifier.width(tokens.compactGap)); Text(language.labelFa) } }
@Composable private fun AppearanceChoice(label: String, mode: AppearanceMode, icon: androidx.compose.ui.graphics.vector.ImageVector, selected: AppearanceMode, onSelect: (AppearanceMode) -> Unit, modifier: Modifier) { val tokens = LocalFlashLearnThemeTokens.current; OutlinedCard(onClick = { onSelect(mode) }, modifier = modifier, colors = CardDefaults.outlinedCardColors(containerColor = if (selected == mode) tokens.primary.copy(alpha = .08f) else tokens.surface), border = BorderStroke(tokens.dp(1f), if (selected == mode) tokens.primary else tokens.outlineColor)) { Column(Modifier.fillMaxWidth().padding(vertical = tokens.dp(10f)), horizontalAlignment = Alignment.CenterHorizontally) { Icon(icon, null, tint = tokens.primary); Spacer(Modifier.height(tokens.compactGap)); Text(label, style = MaterialTheme.typography.labelSmall) } } }
@Composable private fun AccentChoice(label: String, color: AccentColor, selected: AccentColor, onSelect: (AccentColor) -> Unit, tint: Color) { val tokens = LocalFlashLearnThemeTokens.current; OutlinedCard(onClick = { onSelect(color) }, modifier = Modifier.width(tokens.dp(68f)), border = BorderStroke(tokens.dp(2f), if (selected == color) tint else tokens.outlineColor)) { Column(Modifier.fillMaxWidth().padding(vertical = tokens.dp(7f)), horizontalAlignment = Alignment.CenterHorizontally) { Surface(shape = MaterialTheme.shapes.small, color = tint, modifier = Modifier.size(tokens.dp(22f))) {}; Spacer(Modifier.height(tokens.compactGap)); Text(label, style = MaterialTheme.typography.labelSmall) } } }
@Composable private fun SettingsRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, value: String, onClick: (() -> Unit)?, modifier: Modifier = Modifier) { val tokens = LocalFlashLearnThemeTokens.current; Card(modifier = modifier.padding(top = tokens.dp(5f)), shape = MaterialTheme.shapes.medium) { Row(Modifier.fillMaxWidth().clickable(enabled = onClick != null, onClick = { onClick?.invoke() }).padding(horizontal = tokens.dp(12f), vertical = tokens.dp(10f)), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = tokens.primary); Spacer(Modifier.width(tokens.compactGap)); Column(Modifier.weight(1f)) { Text(title, style = MaterialTheme.typography.titleSmall); Text(value, color = tokens.onSurfaceVariant, style = MaterialTheme.typography.labelSmall) }; if (onClick != null) Icon(Icons.AutoMirrored.Outlined.KeyboardArrowLeft, contentDescription = null, tint = tokens.onSurfaceVariant) } } }
