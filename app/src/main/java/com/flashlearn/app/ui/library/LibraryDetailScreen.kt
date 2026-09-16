package com.flashlearn.app.ui.library

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import com.flashlearn.app.ui.theme.LocalFlashLearnThemeTokens
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flashlearn.domain.repository.ConceptRepository
import com.flashlearn.domain.repository.ContentRepository
import com.flashlearn.domain.usecase.DeleteConceptUseCase
import com.flashlearn.domain.usecase.ToggleFavoriteUseCase
import com.flashlearn.domain.usecase.UpdateConceptCommand
import com.flashlearn.domain.usecase.UpdateConceptUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class LibraryDetailViewModel @Inject constructor(
    private val concepts: ConceptRepository,
    private val contents: ContentRepository,
    private val toggleFavorite: ToggleFavoriteUseCase,
    private val updateConcept: UpdateConceptUseCase,
    private val deleteConcept: DeleteConceptUseCase
) : ViewModel() {
    private val _item = MutableStateFlow<LibraryItem?>(null)
    val item: StateFlow<LibraryItem?> = _item.asStateFlow()
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()
    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    fun load(id: UUID) = viewModelScope.launch {
        runCatching {
            val c = concepts.get(id) ?: error("لغت پیدا نشد")
            val cc = contents.getAll().filter { it.conceptId == id }
            LibraryItem(c, cc.firstOrNull { it.languageCode == "es" }, cc.filter { it.languageCode == "fa" }.sortedBy { it.translationIndex }, null)
        }.onSuccess { _item.value = it }.onFailure { _message.value = it.message }
    }

    fun toggleFavorite() = viewModelScope.launch {
        if (_isBusy.value) return@launch
        val id = _item.value?.concept?.id ?: return@launch
        _isBusy.value = true
        runCatching { toggleFavorite(id) }.onSuccess { load(id) }.onFailure { _message.value = it.message }.also { _isBusy.value = false }
    }

    fun save(source: String, target: String, notes: String?, pronunciation: String?, example: String?, onSuccess: () -> Unit = {}) = viewModelScope.launch {
        if (_isBusy.value) return@launch
        val current = _item.value?.concept ?: return@launch
        if (source.isBlank() || target.isBlank()) { _message.value = "متن اسپانیایی و فارسی الزامی است"; return@launch }
        _isBusy.value = true
        runCatching { updateConcept(UpdateConceptCommand(current.id, source.trim(), target.trim(), notes?.trim()?.ifBlank { null }, pronunciation?.trim()?.ifBlank { null }, example?.trim()?.ifBlank { null })) }
            .onSuccess { _message.value = "ذخیره شد"; load(current.id); onSuccess() }
            .onFailure { _message.value = it.message }
            .also { _isBusy.value = false }
    }

    fun delete(onDeleted: () -> Unit) = viewModelScope.launch {
        if (_isBusy.value) return@launch
        val id = _item.value?.concept?.id ?: return@launch
        _isBusy.value = true
        runCatching { deleteConcept(id) }.onSuccess { onDeleted() }.onFailure { _message.value = it.message }.also { _isBusy.value = false }
    }
}

@Composable
fun LibraryDetailScreen(viewModel: LibraryDetailViewModel, conceptId: UUID, onBack: () -> Unit, onDeleted: () -> Unit = {}) {
    val tokens = LocalFlashLearnThemeTokens.current
    LaunchedEffect(conceptId) { viewModel.load(conceptId) }
    val item by viewModel.item.collectAsState()
    val message by viewModel.message.collectAsState()
    val isBusy by viewModel.isBusy.collectAsState()
    var editing by remember { mutableStateOf(false) }
    var source by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var pronunciation by remember { mutableStateOf("") }
    var example by remember { mutableStateOf("") }
    var confirmDelete by remember { mutableStateOf(false) }

    LaunchedEffect(item?.concept?.id) {
        item?.let {
            source = it.source?.text.orEmpty()
            target = it.targets.joinToString(" / ") { c -> c.text }
            notes = it.source?.notes.orEmpty()
            pronunciation = it.source?.pronunciation.orEmpty()
            example = it.source?.example.orEmpty()
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("جزئیات لغت", style = MaterialTheme.typography.headlineSmall)
        item?.let { current ->
            if (editing) {
                OutlinedTextField(source, { source = it }, modifier = Modifier.fillMaxWidth(), label = { Text("اسپانیایی") })
                OutlinedTextField(target, { target = it }, modifier = Modifier.fillMaxWidth(), label = { Text("معنی‌ها (با / جدا کنید)") })
                OutlinedTextField(notes, { notes = it }, modifier = Modifier.fillMaxWidth(), label = { Text("یادداشت") })
                OutlinedTextField(pronunciation, { pronunciation = it }, modifier = Modifier.fillMaxWidth(), label = { Text("تلفظ") })
                OutlinedTextField(example, { example = it }, modifier = Modifier.fillMaxWidth(), label = { Text("مثال") })
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { viewModel.save(source, target, notes, pronunciation, example) { editing = false } }, enabled = !isBusy) { Text("ذخیره") }
                    OutlinedButton(onClick = { editing = false }, enabled = !isBusy) { Text("انصراف") }
                }
            } else {
                Text("اسپانیایی", style = MaterialTheme.typography.labelLarge)
                Text(current.source?.text ?: "—", style = MaterialTheme.typography.titleLarge)
                Text("معنی‌ها", style = MaterialTheme.typography.labelLarge)
                if (current.targets.isEmpty()) Text("—", style = MaterialTheme.typography.titleLarge)
                else current.targets.forEachIndexed { i, c -> Text("${i + 1}. ${c.text}", style = MaterialTheme.typography.titleLarge) }
                current.source?.notes?.let { Text("یادداشت: $it") }
                current.source?.pronunciation?.let { Text("تلفظ: $it") }
                current.source?.example?.let { Text("مثال: $it") }
                Text("نوع: ${current.concept.entryType}")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { editing = true }, enabled = !isBusy) { Text("ویرایش") }
                    OutlinedButton(onClick = viewModel::toggleFavorite, enabled = !isBusy) { Text(if (current.concept.favorite) "★ موردعلاقه" else "☆ افزودن به موردعلاقه") }
                    OutlinedButton(onClick = { confirmDelete = true }, enabled = !isBusy) { Text("حذف") }
                }
            }
        } ?: CircularProgressIndicator()
        message?.let { Text(it) }
        OutlinedButton(onClick = onBack) { Text("بازگشت") }
    }
    if (confirmDelete) AlertDialog(onDismissRequest = { confirmDelete = false }, title = { Text("حذف لغت؟") }, text = { Text("این لغت از کتابخانه فعال حذف می‌شود؛ سابقه مرور آن حفظ می‌شود.") }, confirmButton = { TextButton(onClick = { confirmDelete = false; viewModel.delete(onDeleted) }, enabled = !isBusy) { Text("حذف") } }, dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("انصراف") } })
}
