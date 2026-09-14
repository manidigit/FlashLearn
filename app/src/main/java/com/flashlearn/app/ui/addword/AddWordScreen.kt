package com.flashlearn.app.ui.addword

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flashlearn.app.ui.LanguagePair
import com.flashlearn.app.ui.LearningLanguage
import com.flashlearn.domain.model.EntryType

@Composable
fun AddWordScreen(viewModel: AddWordViewModel, languagePair: LanguagePair = LanguagePair(), onBack: () -> Unit, onBulkImport: () -> Unit = {}, onBackupRestore: () -> Unit = {}) {
    val state by viewModel.state.collectAsState()
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    var typeMenuExpanded by remember { mutableStateOf(false) }
    var sourceMenuExpanded by remember { mutableStateOf(false) }
    var targetMenuExpanded by remember { mutableStateOf(false) }
    LaunchedEffect(languagePair) { viewModel.setLanguagePair(languagePair.source.code, languagePair.target.code) }
    val filteredCategories = state.categories.filter { state.categoryName.isBlank() || it.name.contains(state.categoryName, ignoreCase = true) }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, contentDescription = "بازگشت") }
            Text("افزودن واژه", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
        }
        Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Text("زبان‌های یادگیری", style = MaterialTheme.typography.titleMedium)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.weight(1f)) {
                    LanguageField(state.sourceLanguage, "زبان مبدأ", { targetMenuExpanded = false; sourceMenuExpanded = true }, Modifier.fillMaxWidth())
                    LanguageMenu(sourceMenuExpanded, { sourceMenuExpanded = false }, state.targetLanguage) { lang -> viewModel.setLanguagePair(lang.code, state.targetLanguage); sourceMenuExpanded = false }
                }
                Box(Modifier.weight(1f)) {
                    LanguageField(state.targetLanguage, "زبان مقصد", { sourceMenuExpanded = false; targetMenuExpanded = true }, Modifier.fillMaxWidth())
                    LanguageMenu(targetMenuExpanded, { targetMenuExpanded = false }, state.sourceLanguage) { lang -> viewModel.setLanguagePair(state.sourceLanguage, lang.code); targetMenuExpanded = false }
                }
            }
            OutlinedTextField(state.sourceText, viewModel::onSourceTextChange, Modifier.fillMaxWidth(), label = { Text("واژه یا عبارت") }, placeholder = { Text("مثال: casa") }, singleLine = true)
            OutlinedTextField(state.targetText, viewModel::onTargetTextChange, Modifier.fillMaxWidth(), label = { Text("ترجمه") }, placeholder = { Text("مثال: خانه") }, singleLine = true)
            OutlinedTextField(state.pronunciation, viewModel::onPronunciationChange, Modifier.fillMaxWidth(), label = { Text("تلفظ") }, singleLine = true)
            OutlinedTextField(state.example, viewModel::onExampleChange, Modifier.fillMaxWidth(), label = { Text("جمله نمونه") }, minLines = 2, maxLines = 3)
            OutlinedTextField(state.notes, viewModel::onNotesChange, Modifier.fillMaxWidth(), label = { Text("یادداشت") }, minLines = 2, maxLines = 3)
            Box(Modifier.fillMaxWidth()) {
                OutlinedTextField(state.entryType.labelFa(), {}, Modifier.fillMaxWidth(), label = { Text("نوع ورودی") }, readOnly = true, singleLine = true)
                Spacer(Modifier.matchParentSize().clickable { typeMenuExpanded = true })
                DropdownMenu(typeMenuExpanded, { typeMenuExpanded = false }) { EntryType.entries.forEach { type -> DropdownMenuItem(text = { Text(type.labelFa()) }, onClick = { viewModel.onEntryTypeChange(type); typeMenuExpanded = false }) } }
            }
            Box(Modifier.fillMaxWidth()) {
                OutlinedTextField(state.categoryName, { viewModel.onCategoryNameChange(it); categoryMenuExpanded = true }, Modifier.fillMaxWidth(), label = { Text("دسته‌بندی") }, singleLine = true)
                DropdownMenu(categoryMenuExpanded && filteredCategories.isNotEmpty(), { categoryMenuExpanded = false }) { filteredCategories.forEach { category -> DropdownMenuItem(text = { Text(category.name) }, onClick = { viewModel.onCategoryNameChange(category.name); categoryMenuExpanded = false }) } }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onBulkImport, modifier = Modifier.weight(1f).height(46.dp)) { Icon(Icons.Outlined.FileUpload, null); Spacer(Modifier.width(6.dp)); Text("افزودن گروهی") }
                OutlinedButton(onClick = onBackupRestore, modifier = Modifier.weight(1f).height(46.dp)) { Icon(Icons.Outlined.Backup, null); Spacer(Modifier.width(6.dp)); Text("از فایل پشتیبان") }
            }
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            state.lastSavedText?.let { Text("«$it» ذخیره شد.", color = MaterialTheme.colorScheme.primary) }
        }
        Button(onClick = viewModel::save, enabled = state.canSave, modifier = Modifier.fillMaxWidth().padding(16.dp).height(48.dp), shape = MaterialTheme.shapes.medium) { Icon(Icons.Outlined.Save, null); Spacer(Modifier.width(8.dp)); Text(if (state.isSaving) "در حال ذخیره..." else "ذخیره واژه") }
    }
}

@Composable private fun LanguageField(code: String, label: String, onClick: () -> Unit, modifier: Modifier) {
    val language = LearningLanguage.entries.firstOrNull { it.code == code } ?: LearningLanguage.PERSIAN
    OutlinedCard(onClick = onClick, modifier = modifier) { Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) { Text(language.flag, style = MaterialTheme.typography.titleLarge); Spacer(Modifier.width(7.dp)); Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(label, style = MaterialTheme.typography.labelSmall); Text(language.labelFa) } } }
}

@Composable private fun LanguageMenu(expanded: Boolean, dismiss: () -> Unit, excludedCode: String, onSelect: (LearningLanguage) -> Unit) {
    DropdownMenu(expanded = expanded, onDismissRequest = dismiss) { LearningLanguage.entries.filter { it.code != excludedCode }.forEach { lang -> DropdownMenuItem(text = { Row(verticalAlignment = Alignment.CenterVertically) { Text(lang.flag); Spacer(Modifier.width(8.dp)); Text(lang.labelFa) } }, onClick = { onSelect(lang) }) } }
}

private fun EntryType.labelFa(): String = when (this) { EntryType.WORD -> "واژه"; EntryType.PHRASE -> "عبارت"; EntryType.SENTENCE -> "جمله"; EntryType.IDIOM -> "اصطلاح"; EntryType.COLLOCATION -> "هم‌آیند"; EntryType.STRUCTURE -> "ساختار" }
