package com.flashlearn.app.ui.addword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flashlearn.domain.model.Category
import com.flashlearn.domain.usecase.CreateConceptCommand
import com.flashlearn.domain.usecase.CreateConceptUseCase
import com.flashlearn.domain.usecase.GetAllCategoriesUseCase
import com.flashlearn.domain.usecase.GetOrCreateCategoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AddWordUiState(
    val sourceText: String = "",
    val targetText: String = "",
    val notes: String = "",
    val pronunciation: String = "",
    val example: String = "",
    val categoryName: String = "",
    val categories: List<Category> = emptyList(),
    val isSaving: Boolean = false,
    val lastSavedText: String? = null,
    val error: String? = null
) {
    val canSave: Boolean
        get() = sourceText.trim().isNotEmpty() && targetText.trim().isNotEmpty() && !isSaving
}

@HiltViewModel
class AddWordViewModel @Inject constructor(
    private val createConcept: CreateConceptUseCase,
    private val getAllCategories: GetAllCategoriesUseCase,
    private val getOrCreateCategory: GetOrCreateCategoryUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AddWordUiState())
    val state: StateFlow<AddWordUiState> = _state.asStateFlow()
    private var categoryLoadGeneration = 0L

    init {
        loadCategories()
    }

    private fun loadCategories() {
        val generation = ++categoryLoadGeneration
        viewModelScope.launch {
            runCatching { getAllCategories() }
                .onSuccess { categories ->
                    if (generation == categoryLoadGeneration) {
                        _state.value = _state.value.copy(categories = categories)
                    }
                }
                .onFailure { error ->
                    if (generation == categoryLoadGeneration) {
                        _state.value = _state.value.copy(
                            error = error.message ?: "خطا در بارگذاری دسته‌ها"
                        )
                    }
                }
        }
    }

    fun onSourceTextChange(value: String) {
        _state.value = _state.value.copy(sourceText = value, error = null)
    }

    fun onTargetTextChange(value: String) {
        _state.value = _state.value.copy(targetText = value, error = null)
    }

    fun onNotesChange(value: String) {
        _state.value = _state.value.copy(notes = value)
    }

    fun onPronunciationChange(value: String) {
        _state.value = _state.value.copy(pronunciation = value)
    }

    fun onExampleChange(value: String) {
        _state.value = _state.value.copy(example = value)
    }

    fun onCategoryNameChange(value: String) {
        _state.value = _state.value.copy(categoryName = value)
    }

    fun save() {
        val current = _state.value
        if (!current.canSave) return

        viewModelScope.launch {
            _state.value = current.copy(isSaving = true, error = null)
            try {
                val categoryId = if (current.categoryName.isNotBlank()) {
                    getOrCreateCategory(current.categoryName)
                } else null

                createConcept(
                    CreateConceptCommand(
                        sourceText = current.sourceText.trim(),
                        targetText = current.targetText.trim(),
                        categoryId = categoryId,
                        notes = current.notes.ifBlank { null },
                        pronunciation = current.pronunciation.ifBlank { null },
                        example = current.example.ifBlank { null }
                    )
                )

                // Do not wipe newer input entered while the persistence operation was running.
                // Normally fields are unchanged during a save, but this guard keeps batch entry
                // safe even if the UI receives edits before the transaction completes.
                val latest = _state.value
                val unchangedSinceSave = latest.sourceText == current.sourceText &&
                    latest.targetText == current.targetText &&
                    latest.notes == current.notes &&
                    latest.pronunciation == current.pronunciation &&
                    latest.example == current.example &&
                    latest.categoryName == current.categoryName

                _state.value = if (unchangedSinceSave) {
                    latest.copy(
                        sourceText = "",
                        targetText = "",
                        notes = "",
                        pronunciation = "",
                        example = "",
                        isSaving = false,
                        lastSavedText = current.sourceText.trim()
                    )
                } else {
                    latest.copy(
                        isSaving = false,
                        lastSavedText = current.sourceText.trim()
                    )
                }
                loadCategories()
            } catch (e: Exception) {
                _state.value = _state.value.copy(isSaving = false, error = e.message ?: "خطا در ذخیره‌سازی")
            }
        }
    }
}
