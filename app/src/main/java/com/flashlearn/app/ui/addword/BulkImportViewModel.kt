package com.flashlearn.app.ui.addword

import com.flashlearn.app.ui.LanguagePair
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flashlearn.domain.parser.ParsedEntry
import com.flashlearn.domain.parser.ParseWarning
import com.flashlearn.domain.parser.VocabularyParser
import com.flashlearn.domain.usecase.computeCanonicalKey
import com.flashlearn.domain.usecase.ImportParsedEntryUseCase
import com.flashlearn.domain.usecase.DuplicateConceptException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

enum class BulkImportItemStatus { READY, INCOMPLETE, IMPORTED, DUPLICATE, FAILED }

fun BulkImportItemStatus.label(): String = when (this) {
    BulkImportItemStatus.READY -> "آماده"
    BulkImportItemStatus.INCOMPLETE -> "ناقص"
    BulkImportItemStatus.IMPORTED -> "وارد شد"
    BulkImportItemStatus.DUPLICATE -> "تکراری"
    BulkImportItemStatus.FAILED -> "خطا"
}

data class BulkImportItemResult(
    val entry: ParsedEntry,
    val status: BulkImportItemStatus,
    val message: String? = null
) {
    val confidencePercent: Int get() = (entry.confidence.coerceIn(0.0, 1.0) * 100).toInt()
    val breakdownCount: Int get() = entry.breakdown.size
    val relationshipCount: Int get() = entry.relationships.size
    val variantCount: Int get() = entry.variants.size
}

data class BulkImportUiState(
    val rawText: String = "",
    val preview: List<ParsedEntry> = emptyList(),
    val results: List<BulkImportItemResult> = emptyList(),
    val warnings: List<ParseWarning> = emptyList(),
    val isImporting: Boolean = false,
    val isPreviewing: Boolean = false,
    val importedCount: Int = 0,
    val skippedDuplicateCount: Int = 0,
    val invalidCount: Int = 0,
    val failedCount: Int = 0,
    val error: String? = null,
    val done: Boolean = false,
    val sourceLanguage: String = "es",
    val targetLanguage: String = "fa"
)

@HiltViewModel
class BulkImportViewModel @Inject constructor(
    private val importParsedEntry: ImportParsedEntryUseCase
) : ViewModel() {
    companion object {
        const val PREVIEW_LIMIT = 30
    }

    private val parser = VocabularyParser()
    private val _state = MutableStateFlow(BulkImportUiState())
    val state: StateFlow<BulkImportUiState> = _state.asStateFlow()

    fun setLanguagePair(pair: LanguagePair) {
        _state.value = _state.value.copy(sourceLanguage = pair.source.code, targetLanguage = pair.target.code)
    }

    fun resetForEntry(pair: LanguagePair) {
        _state.value = BulkImportUiState(sourceLanguage = pair.source.code, targetLanguage = pair.target.code)
    }

    fun showError(message: String) {
        _state.value = _state.value.copy(error = message, isPreviewing = false, isImporting = false)
    }

    fun onTextChange(value: String) {
        _state.value = _state.value.copy(
            rawText = value,
            preview = emptyList(),
            results = emptyList(),
            warnings = emptyList(),
            importedCount = 0,
            skippedDuplicateCount = 0,
            invalidCount = 0,
            failedCount = 0,
            error = null,
            done = false
        )
    }

    fun preview() {
        val current = _state.value
        if (current.isImporting || current.isPreviewing || current.rawText.isBlank()) return
        _state.value = current.copy(isPreviewing = true, error = null, done = false)
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.Default) { parser.parseDetailed(current.rawText) }
            }.onSuccess { result ->
                _state.value = _state.value.copy(
                    preview = result.entries,
                    results = result.entries.map(::readyResult),
                    warnings = result.warnings,
                    isPreviewing = false,
                    error = if (result.entries.isEmpty()) "مورد قابل وارد کردن پیدا نشد." else null
                )
            }.onFailure { error ->
                _state.value = _state.value.copy(isPreviewing = false, error = error.message ?: "خطا در پردازش متن")
            }
        }
    }

    fun importAll() {
        val current = _state.value
        if (current.isImporting || current.isPreviewing) return
        val batch = current.preview.toList()
        if (batch.isEmpty()) {
            val raw = current.rawText
            if (raw.isBlank()) return
            _state.value = current.copy(isPreviewing = true, error = null)
            viewModelScope.launch {
                val parsed = runCatching { withContext(Dispatchers.Default) { parser.parseDetailed(raw) } }
                parsed.onSuccess { result ->
                    _state.value = _state.value.copy(preview = result.entries, results = result.entries.map(::readyResult), warnings = result.warnings, isPreviewing = false)
                    importBatch(result.entries, current.sourceLanguage, current.targetLanguage)
                }.onFailure { error ->
                    _state.value = _state.value.copy(isPreviewing = false, error = error.message ?: "خطا در پردازش متن")
                }
            }
            return
        }
        importBatch(batch, current.sourceLanguage, current.targetLanguage)
    }

    private fun importBatch(batch: List<ParsedEntry>, sourceLanguage: String, targetLanguage: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isImporting = true, isPreviewing = false, error = null, done = false, failedCount = 0)
            try {
                var imported = 0
                var skippedDuplicates = 0
                var invalid = 0
                var failed = 0
                val seenPairs = mutableSetOf<String>()
                val results = mutableListOf<BulkImportItemResult>()
                withContext(Dispatchers.IO) {
                    batch.forEachIndexed { index, entry ->
                        val source = entry.sourceText.trim()
                        val translation = entry.translationText?.trim()
                        if (source.isBlank() || translation.isNullOrBlank()) {
                            invalid++
                            results += BulkImportItemResult(entry, BulkImportItemStatus.INCOMPLETE, "مدخل ناقص")
                        } else {
                            val pairKey = computeCanonicalKey(source) + "\u0000" + computeCanonicalKey(translation)
                            if (seenPairs.contains(pairKey)) {
                                skippedDuplicates++
                                results += BulkImportItemResult(entry, BulkImportItemStatus.DUPLICATE, "تکراری در همین دسته")
                            } else {
                                try {
                                    importParsedEntry(entry, sourceLanguage, targetLanguage)
                                    imported++
                                    seenPairs.add(pairKey)
                                    results += BulkImportItemResult(entry, BulkImportItemStatus.IMPORTED)
                                } catch (_: DuplicateConceptException) {
                                    skippedDuplicates++
                                    results += BulkImportItemResult(entry, BulkImportItemStatus.DUPLICATE, "قبلاً در کتابخانه وجود دارد")
                                } catch (e: Exception) {
                                    failed++
                                    results += BulkImportItemResult(entry, BulkImportItemStatus.FAILED, e.message ?: "خطای نامشخص")
                                }
                            }
                        }
                        if (index % 25 == 0) kotlinx.coroutines.yield()
                    }
                }
                _state.value = _state.value.copy(
                    results = results,
                    isImporting = false,
                    importedCount = imported,
                    skippedDuplicateCount = skippedDuplicates,
                    invalidCount = invalid,
                    failedCount = failed,
                    done = true,
                    error = if (failed > 0) "$failed مورد با خطا مواجه شد." else null
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isImporting = false, error = e.message ?: "خطا در وارد کردن اطلاعات")
            }
        }
    }

    fun readyResultForDisplay(entry: ParsedEntry): BulkImportItemResult = readyResult(entry)

    private fun readyResult(entry: ParsedEntry) = BulkImportItemResult(
        entry,
        if (entry.sourceText.isBlank() || entry.translationText.isNullOrBlank()) BulkImportItemStatus.INCOMPLETE else BulkImportItemStatus.READY
    )
}
