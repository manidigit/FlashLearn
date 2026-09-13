package com.flashlearn.app.ui.addword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flashlearn.domain.model.EntryType
import com.flashlearn.domain.parser.EntryKind
import com.flashlearn.domain.parser.ParsedEntry
import com.flashlearn.domain.parser.ParseWarning
import com.flashlearn.domain.parser.VocabularyParser
import com.flashlearn.domain.usecase.CreateConceptCommand
import com.flashlearn.domain.usecase.CreateConceptUseCase
import com.flashlearn.domain.usecase.DuplicateConceptException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

enum class BulkImportItemStatus { READY, INCOMPLETE, IMPORTED, DUPLICATE, FAILED }

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
    val importedCount: Int = 0,
    val skippedDuplicateCount: Int = 0,
    val invalidCount: Int = 0,
    val error: String? = null,
    val done: Boolean = false
)

@HiltViewModel
class BulkImportViewModel @Inject constructor(
    private val createConcept: CreateConceptUseCase
) : ViewModel() {
    private val parser = VocabularyParser()
    private val _state = MutableStateFlow(BulkImportUiState())
    val state: StateFlow<BulkImportUiState> = _state.asStateFlow()

    fun onTextChange(value: String) {
        _state.value = _state.value.copy(
            rawText = value,
            preview = emptyList(),
            results = emptyList(),
            warnings = emptyList(),
            importedCount = 0,
            skippedDuplicateCount = 0,
            invalidCount = 0,
            error = null,
            done = false
        )
    }

    fun preview() {
        if (_state.value.isImporting) return
        val result = parser.parseDetailed(_state.value.rawText)
        _state.value = _state.value.copy(
            preview = result.entries,
            results = result.entries.map(::readyResult),
            warnings = result.warnings,
            error = if (result.entries.isEmpty()) "مورد قابل وارد کردن پیدا نشد." else null
        )
    }

    fun importAll() {
        if (_state.value.isImporting) return
        val entries = _state.value.preview.ifEmpty { parser.parseDetailed(_state.value.rawText).entries }
        if (entries.isEmpty()) return
        val batch = entries.toList()
        viewModelScope.launch {
            _state.value = _state.value.copy(isImporting = true, error = null, done = false)
            try {
                var imported = 0
                var skippedDuplicates = 0
                var invalid = 0
                val seenPairs = mutableSetOf<String>()
                val results = mutableListOf<BulkImportItemResult>()
                batch.forEach { entry ->
                    val source = entry.sourceText.trim()
                    val translation = entry.translationText?.trim()
                    if (source.isBlank() || translation.isNullOrBlank()) {
                        invalid++
                        results += BulkImportItemResult(entry, BulkImportItemStatus.INCOMPLETE, "مدخل ناقص")
                    } else {
                        val pairKey = "${source.lowercase()}\u0000${translation.lowercase()}"
                        if (!seenPairs.add(pairKey)) {
                            skippedDuplicates++
                            results += BulkImportItemResult(entry, BulkImportItemStatus.DUPLICATE, "تکراری در همین دسته")
                        } else {
                            try {
                                createConcept(CreateConceptCommand(
                                    sourceText = source,
                                    targetText = translation,
                                    notes = entry.notes,
                                    entryType = entry.entryType.toDomainEntryType()
                                ))
                                imported++
                                results += BulkImportItemResult(entry, BulkImportItemStatus.IMPORTED)
                            } catch (_: DuplicateConceptException) {
                                skippedDuplicates++
                                results += BulkImportItemResult(entry, BulkImportItemStatus.DUPLICATE, "قبلاً در کتابخانه وجود دارد")
                            } catch (e: Exception) {
                                results += BulkImportItemResult(entry, BulkImportItemStatus.FAILED, e.message ?: "خطای نامشخص")
                                throw e
                            }
                        }
                    }
                }
                _state.value = _state.value.copy(
                    results = results,
                    isImporting = false,
                    importedCount = imported,
                    skippedDuplicateCount = skippedDuplicates,
                    invalidCount = invalid,
                    done = true
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isImporting = false, error = e.message ?: "خطا در وارد کردن اطلاعات")
            }
        }
    }

    private fun readyResult(entry: ParsedEntry) = BulkImportItemResult(
        entry,
        if (entry.sourceText.isBlank() || entry.translationText.isNullOrBlank()) BulkImportItemStatus.INCOMPLETE else BulkImportItemStatus.READY
    )

    private fun EntryKind.toDomainEntryType() = when (this) {
        EntryKind.WORD -> EntryType.WORD
        EntryKind.PHRASE -> EntryType.PHRASE
        EntryKind.SENTENCE -> EntryType.SENTENCE
        EntryKind.IDIOM -> EntryType.IDIOM
        EntryKind.COLLOCATION -> EntryType.COLLOCATION
        EntryKind.STRUCTURE -> EntryType.STRUCTURE
    }
}
