package com.flashlearn.app.ui.library

import android.content.Context

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardDefaults.outlinedCardColors
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.flashlearn.app.R
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flashlearn.app.ui.components.FlashLearnScreenHeader
import com.flashlearn.app.ui.theme.LocalFlashLearnThemeTokens
import com.flashlearn.app.ui.LanguagePair
import com.flashlearn.app.ui.LearningLanguage
import com.flashlearn.domain.model.Category
import com.flashlearn.domain.model.EntryType
import com.flashlearn.domain.repository.CategoryRepository
import com.flashlearn.domain.repository.ConceptRepository
import com.flashlearn.domain.repository.ContentRepository
import com.flashlearn.domain.usecase.DeleteConceptUseCase
import com.flashlearn.domain.usecase.GetAllCategoriesUseCase
import com.flashlearn.domain.usecase.GetOrCreateCategoryUseCase
import com.flashlearn.domain.usecase.ToggleFavoriteUseCase
import com.flashlearn.domain.usecase.UpdateConceptCommand
import com.flashlearn.domain.usecase.UpdateConceptUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class LibraryDetailViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val concepts: ConceptRepository,
    private val contents: ContentRepository,
    private val categoryRepository: CategoryRepository,
    private val getAllCategories: GetAllCategoriesUseCase,
    private val getOrCreateCategory: GetOrCreateCategoryUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase,
    private val updateConcept: UpdateConceptUseCase,
    private val deleteConcept: DeleteConceptUseCase
) : ViewModel() {
    private val _item = MutableStateFlow<LibraryItem?>(null)
    val item: StateFlow<LibraryItem?> = _item.asStateFlow()
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()
    private val _isBusy = MutableStateFlow(false)
    private var activeSourceLanguage = "es"
    private var activeTargetLanguage = "fa"
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    fun load(id: UUID, sourceLanguage: String = "es", targetLanguage: String = "fa") = viewModelScope.launch {
        activeSourceLanguage = sourceLanguage; activeTargetLanguage = targetLanguage; _item.value = null; _message.value = null
        runCatching {
            val c = concepts.get(id) ?: error(context.getString(R.string.detail_not_found))
            val cc = contents.getAll().filter { it.conceptId == id }
            val category = c.categoryId?.let { categoryId -> categoryRepository.getAll().firstOrNull { it.id == categoryId } }
            LibraryItem(c, cc.firstOrNull { it.languageCode == sourceLanguage }, cc.filter { it.languageCode == targetLanguage }.sortedBy { it.translationIndex }, category)
        }.onSuccess { _item.value = it }.onFailure { _message.value = it.message }
        runCatching { getAllCategories() }.onSuccess { _categories.value = it }
    }

    fun toggleFavorite() = viewModelScope.launch {
        if (_isBusy.value) return@launch
        val id = _item.value?.concept?.id ?: return@launch
        _isBusy.value = true
        runCatching { toggleFavorite(id) }.onSuccess { load(id, activeSourceLanguage, activeTargetLanguage) }.onFailure { _message.value = it.message }.also { _isBusy.value = false }
    }

    fun save(source: String, target: String, notes: String?, entryType: EntryType, categoryId: UUID?, createCategoryName: String?, sourceLanguage: String, targetLanguage: String, onSuccess: () -> Unit = {}) = viewModelScope.launch {
        if (_isBusy.value) return@launch
        val current = _item.value?.concept ?: return@launch
        if (source.isBlank() || target.isBlank()) { _message.value = context.getString(R.string.detail_required); return@launch }
        _isBusy.value = true
        runCatching {
            val resolvedCategoryId = createCategoryName?.trim()?.takeIf { it.isNotEmpty() }?.let { getOrCreateCategory(it) }
            updateConcept(UpdateConceptCommand(current.id, source.trim(), target.trim(), notes?.trim()?.ifBlank { null }, entryType = entryType, categoryId = resolvedCategoryId ?: categoryId, preserveCategory = resolvedCategoryId == null && categoryId == current.categoryId, sourceLanguage = sourceLanguage, targetLanguage = targetLanguage))
        }.onSuccess { _message.value = context.getString(R.string.detail_saved); load(current.id, sourceLanguage, targetLanguage); onSuccess() }.onFailure { _message.value = it.message }.also { _isBusy.value = false }
    }

    fun delete(onDeleted: () -> Unit) = viewModelScope.launch {
        if (_isBusy.value) return@launch
        val id = _item.value?.concept?.id ?: return@launch
        _isBusy.value = true
        runCatching { deleteConcept(id) }.onSuccess { onDeleted() }.onFailure { _message.value = it.message }.also { _isBusy.value = false }
    }
}

@Composable
fun LibraryDetailScreen(viewModel: LibraryDetailViewModel, conceptId: UUID, languagePair: LanguagePair = LanguagePair(), onBack: () -> Unit, onDeleted: () -> Unit = {}) {
    val tokens = LocalFlashLearnThemeTokens.current

    LaunchedEffect(conceptId, languagePair) { viewModel.load(conceptId, languagePair.source.code, languagePair.target.code) }
    val item by viewModel.item.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val message by viewModel.message.collectAsState()
    val isBusy by viewModel.isBusy.collectAsState()
    var source by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
        var entryType by remember { mutableStateOf(EntryType.WORD) }
    var selectedCategoryId by remember { mutableStateOf<UUID?>(null) }
    var categoryName by remember { mutableStateOf("") }
    var addingNewCategory by remember { mutableStateOf(false) }
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    var typeMenuExpanded by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    LaunchedEffect(item?.concept?.id) {
        item?.let {
            source = it.source?.text.orEmpty()
            target = it.targets.joinToString(" / ") { c -> c.text }
            notes = it.source?.notes.orEmpty()
            entryType = it.concept.entryType
            selectedCategoryId = it.concept.categoryId
            categoryName = it.category?.name.orEmpty()
            addingNewCategory = false
        }
    }

    Column(Modifier.fillMaxSize()) {
        FlashLearnScreenHeader(title = stringResource(R.string.detail_title), onBack = onBack)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            color = MaterialTheme.colorScheme.background,
            shape = RectangleShape
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(tokens.itemGap)
            ) {
            Text(stringResource(R.string.detail_learning_languages), style = MaterialTheme.typography.titleMedium)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.compactGap)) {
                ReadOnlyLanguageField(languagePair.source.code, stringResource(R.string.detail_source_language), Modifier.weight(1f))
                ReadOnlyLanguageField(languagePair.target.code, stringResource(R.string.detail_target_language), Modifier.weight(1f))
            }
            OutlinedTextField(source, { source = it }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.detail_word_or_phrase)) }, singleLine = true)
            OutlinedTextField(target, { target = it }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.detail_translation)) }, singleLine = true)
            Box(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = if (addingNewCategory) categoryName else categories.firstOrNull { it.id == selectedCategoryId }?.name ?: categoryName.ifBlank { stringResource(R.string.detail_choose_category) },
                    onValueChange = { if (addingNewCategory) categoryName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(if (addingNewCategory) stringResource(R.string.detail_new_category) else stringResource(R.string.detail_category)) },
                    placeholder = { Text(stringResource(R.string.detail_category_hint)) },
                    readOnly = !addingNewCategory,
                    singleLine = true
                )
                if (!addingNewCategory) Spacer(Modifier.matchParentSize().clickable { categoryMenuExpanded = true })
                DropdownMenu(categoryMenuExpanded, { categoryMenuExpanded = false }) {
                    DropdownMenuItem(text = { Text(stringResource(R.string.detail_no_category)) }, onClick = { selectedCategoryId = null; categoryName = ""; addingNewCategory = false; categoryMenuExpanded = false })
                    categories.forEach { category ->
                        DropdownMenuItem(text = { Text(category.name) }, onClick = { selectedCategoryId = category.id; categoryName = category.name; addingNewCategory = false; categoryMenuExpanded = false })
                    }
                    HorizontalDivider()
                    DropdownMenuItem(text = { Text(stringResource(R.string.detail_add_category)) }, onClick = { selectedCategoryId = null; categoryName = ""; addingNewCategory = true; categoryMenuExpanded = false })
                }
            }
            if (addingNewCategory) TextButton(onClick = { addingNewCategory = false; selectedCategoryId = item?.concept?.categoryId; categoryName = item?.category?.name.orEmpty() }) { Text(stringResource(R.string.detail_existing_categories)) }
            Box(Modifier.fillMaxWidth()) {
                OutlinedTextField(entryType.labelFa(), {}, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.detail_entry_type)) }, readOnly = true, singleLine = true)
                Spacer(Modifier.matchParentSize().clickable { typeMenuExpanded = true })
                DropdownMenu(typeMenuExpanded, { typeMenuExpanded = false }) { EntryType.entries.forEach { type -> DropdownMenuItem(text = { Text(type.labelFa()) }, onClick = { entryType = type; typeMenuExpanded = false }) } }
            }
            OutlinedTextField(notes, { notes = it }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.detail_notes)) }, minLines = 2, maxLines = 3)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.compactGap)) {
                Button(onClick = { viewModel.save(source, target, notes, entryType, selectedCategoryId, if (addingNewCategory) categoryName else null, languagePair.source.code, languagePair.target.code) }, enabled = !isBusy && source.isNotBlank() && target.isNotBlank(), modifier = Modifier.weight(1f).height(tokens.controlHeight), shape = MaterialTheme.shapes.medium) { Icon(Icons.Outlined.Save, null); Spacer(Modifier.width(tokens.compactGap)); Text(if (isBusy) stringResource(R.string.detail_saving) else stringResource(R.string.detail_save)) }
                OutlinedButton(onClick = onBack, enabled = !isBusy, modifier = Modifier.weight(1f).height(tokens.controlHeight), shape = MaterialTheme.shapes.medium) { Text(stringResource(R.string.detail_cancel)) }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.compactGap)) {
                OutlinedButton(onClick = viewModel::toggleFavorite, enabled = !isBusy, modifier = Modifier.weight(1f), shape = MaterialTheme.shapes.medium) { Icon(Icons.Outlined.Star, null); Spacer(Modifier.width(tokens.microGap)); Text(if (item?.concept?.favorite == true) stringResource(R.string.detail_favorite) else stringResource(R.string.detail_add_favorite)) }
                OutlinedButton(onClick = { confirmDelete = true }, enabled = !isBusy, modifier = Modifier.weight(1f), shape = MaterialTheme.shapes.medium) { Icon(Icons.Outlined.DeleteOutline, null); Spacer(Modifier.width(tokens.microGap)); Text(stringResource(R.string.detail_delete)) }
            }
            message?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
            }
        }
    }
    if (confirmDelete) AlertDialog(onDismissRequest = { confirmDelete = false }, title = { Text(stringResource(R.string.detail_delete_title)) }, text = { Text(stringResource(R.string.detail_delete_message)) }, confirmButton = { TextButton(onClick = { confirmDelete = false; viewModel.delete(onDeleted) }, enabled = !isBusy) { Text(stringResource(R.string.detail_delete)) } }, dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.detail_cancel)) } })
}

@Composable private fun ReadOnlyLanguageField(code: String, label: String, modifier: Modifier) {
    val tokens = LocalFlashLearnThemeTokens.current
    val language = LearningLanguage.entries.firstOrNull { it.code == code } ?: LearningLanguage.PERSIAN
    OutlinedCard(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(tokens.compactPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(language.flag, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.width(tokens.microGap))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(label, style = MaterialTheme.typography.labelSmall)
                Text(language.labelFa)
            }
        }
    }
}

@Composable private fun EntryType.labelFa(): String = when (this) { EntryType.WORD -> stringResource(R.string.entry_word); EntryType.PHRASE -> stringResource(R.string.entry_phrase); EntryType.SENTENCE -> stringResource(R.string.entry_sentence); EntryType.IDIOM -> stringResource(R.string.entry_idiom); EntryType.COLLOCATION -> stringResource(R.string.entry_collocation); EntryType.STRUCTURE -> stringResource(R.string.entry_structure) }
