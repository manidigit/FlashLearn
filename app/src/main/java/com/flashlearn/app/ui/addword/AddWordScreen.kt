package com.flashlearn.app.ui.addword

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.*
import com.flashlearn.app.ui.theme.LocalFlashLearnThemeTokens
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.flashlearn.app.R
import com.flashlearn.app.ui.components.FlashLearnScreenHeader
import com.flashlearn.app.ui.components.FlashLearnPrimaryButton
import com.flashlearn.app.ui.LanguagePair
import com.flashlearn.app.ui.LearningLanguage
import com.flashlearn.domain.model.EntryType

@Composable
fun AddWordScreen(viewModel: AddWordViewModel, languagePair: LanguagePair = LanguagePair(), onBack: () -> Unit, onReviewApprovalSaved: () -> Unit = {}) {
    val tokens = LocalFlashLearnThemeTokens.current
    val state by viewModel.state.collectAsState()
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    var typeMenuExpanded by remember { mutableStateOf(false) }
    var sourceMenuExpanded by remember { mutableStateOf(false) }
    var targetMenuExpanded by remember { mutableStateOf(false) }
    var addingNewCategory by remember { mutableStateOf(false) }
    LaunchedEffect(languagePair) { viewModel.setLanguagePair(languagePair.source.code, languagePair.target.code) }
    LaunchedEffect(state.reviewApprovalCompleted) { if (state.reviewApprovalCompleted) { viewModel.consumeReviewApprovalCompletion(); onReviewApprovalSaved() } }

    Column(Modifier.fillMaxSize()) {
        FlashLearnScreenHeader(title = if (state.pendingReviewItem != null) stringResource(R.string.addword_edit_approval) else stringResource(R.string.addword_title), onBack = onBack)
        Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = tokens.screenPadding), verticalArrangement = Arrangement.spacedBy(tokens.itemGap)) {
            Text(stringResource(R.string.addword_learning_languages), style = MaterialTheme.typography.titleMedium)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.compactGap)) {
                Box(Modifier.weight(1f)) { LanguageField(state.sourceLanguage, stringResource(R.string.addword_source_language), { targetMenuExpanded = false; sourceMenuExpanded = true }, Modifier.fillMaxWidth()); LanguageMenu(sourceMenuExpanded, { sourceMenuExpanded = false }, state.targetLanguage) { lang -> viewModel.setLanguagePair(lang.code, state.targetLanguage); sourceMenuExpanded = false } }
                Box(Modifier.weight(1f)) { LanguageField(state.targetLanguage, stringResource(R.string.addword_target_language), { sourceMenuExpanded = false; targetMenuExpanded = true }, Modifier.fillMaxWidth()); LanguageMenu(targetMenuExpanded, { targetMenuExpanded = false }, state.sourceLanguage) { lang -> viewModel.setLanguagePair(state.sourceLanguage, lang.code); targetMenuExpanded = false } }
            }
            OutlinedTextField(state.sourceText, viewModel::onSourceTextChange, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.addword_word_or_phrase)) }, placeholder = { Text(stringResource(R.string.addword_source_example)) }, singleLine = true)
            OutlinedTextField(state.targetText, viewModel::onTargetTextChange, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.addword_translation)) }, placeholder = { Text(stringResource(R.string.addword_target_example)) }, singleLine = true)
            Box(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = if (addingNewCategory) state.categoryName else state.categoryName.ifBlank { stringResource(R.string.addword_choose_category) },
                    onValueChange = { if (addingNewCategory) viewModel.onCategoryNameChange(it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(if (addingNewCategory) stringResource(R.string.addword_new_category) else stringResource(R.string.addword_category)) },
                    placeholder = { Text(stringResource(R.string.addword_category_hint)) },
                    readOnly = !addingNewCategory,
                    singleLine = true
                )
                if (!addingNewCategory) {
                    Spacer(Modifier.matchParentSize().clickable { categoryMenuExpanded = true })
                }
                DropdownMenu(categoryMenuExpanded, { categoryMenuExpanded = false }) {
                    state.categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.name) },
                            onClick = {
                                viewModel.onCategoryNameChange(category.name)
                                addingNewCategory = false
                                categoryMenuExpanded = false
                            }
                        )
                    }
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.addword_add_category)) },
                        onClick = {
                            viewModel.onCategoryNameChange("")
                            addingNewCategory = true
                            categoryMenuExpanded = false
                        }
                    )
                }
            }
            if (addingNewCategory) {
                TextButton(onClick = { addingNewCategory = false; viewModel.onCategoryNameChange("") }) {
                    Text(stringResource(R.string.addword_existing_category))
                }
            }
            Box(Modifier.fillMaxWidth()) {
                OutlinedTextField(state.entryType.labelFa(), {}, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.addword_entry_type)) }, readOnly = true, singleLine = true)
                Spacer(Modifier.matchParentSize().clickable { typeMenuExpanded = true })
                DropdownMenu(typeMenuExpanded, { typeMenuExpanded = false }) { EntryType.entries.forEach { type -> DropdownMenuItem(text = { Text(type.labelFa()) }, onClick = { viewModel.onEntryTypeChange(type); typeMenuExpanded = false }) } }
            }
            OutlinedTextField(state.notes, viewModel::onNotesChange, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.addword_notes)) }, minLines = 2, maxLines = 3)
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            state.lastSavedText?.let { Text(stringResource(R.string.addword_saved, it), color = MaterialTheme.colorScheme.primary) }
        }
        FlashLearnPrimaryButton(
            onClick = viewModel::save,
            enabled = state.canSave,
            modifier = Modifier.fillMaxWidth().padding(tokens.contentPadding)
        ) {
            Icon(Icons.Outlined.Save, null)
            Spacer(Modifier.width(tokens.compactGap))
            Text(if (state.isSaving) stringResource(R.string.addword_saving) else if (state.pendingReviewItem != null) stringResource(R.string.addword_save_and_library) else stringResource(R.string.addword_save))
        }
    }
}

@Composable private fun LanguageField(code: String, label: String, onClick: () -> Unit, modifier: Modifier) {
    val tokens = LocalFlashLearnThemeTokens.current
    val language = LearningLanguage.entries.firstOrNull { it.code == code } ?: LearningLanguage.PERSIAN
    OutlinedCard(onClick = onClick, modifier = modifier) { Row(Modifier.fillMaxWidth().padding(tokens.compactGap), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) { Text(language.flag, style = MaterialTheme.typography.titleLarge); Spacer(Modifier.width(tokens.tinyGap)); Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(label, style = MaterialTheme.typography.labelSmall); Text(language.labelFa) } } }
}

@Composable private fun LanguageMenu(expanded: Boolean, dismiss: () -> Unit, excludedCode: String, onSelect: (LearningLanguage) -> Unit) {
    val tokens = LocalFlashLearnThemeTokens.current
    DropdownMenu(expanded = expanded, onDismissRequest = dismiss) { LearningLanguage.entries.filter { it.code != excludedCode }.forEach { lang -> DropdownMenuItem(text = { Row(verticalAlignment = Alignment.CenterVertically) { Text(lang.flag); Spacer(Modifier.width(tokens.compactGap)); Text(lang.labelFa) } }, onClick = { onSelect(lang) }) } }
}

@Composable private fun EntryType.labelFa(): String = when (this) { EntryType.WORD -> stringResource(R.string.entry_word); EntryType.PHRASE -> stringResource(R.string.entry_phrase); EntryType.SENTENCE -> stringResource(R.string.entry_sentence); EntryType.IDIOM -> stringResource(R.string.entry_idiom); EntryType.COLLOCATION -> stringResource(R.string.entry_collocation); EntryType.STRUCTURE -> stringResource(R.string.entry_structure) }
