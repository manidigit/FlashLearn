package com.flashlearn.app.ui.addword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flashlearn.domain.model.Category
import com.flashlearn.domain.model.EntryType
import com.flashlearn.domain.model.ReviewQueueItem
import com.flashlearn.domain.usecase.CreateConceptCommand
import com.flashlearn.domain.usecase.CreateConceptUseCase
import com.flashlearn.domain.usecase.GetAllCategoriesUseCase
import com.flashlearn.domain.usecase.GetOrCreateCategoryUseCase
import com.flashlearn.domain.usecase.ApproveReviewQueueItemUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AddWordUiState(
    val sourceText: String = "", val targetText: String = "", val notes: String = "", val categoryName: String = "", val entryType: EntryType = EntryType.WORD,
    val sourceLanguage: String = "es", val targetLanguage: String = "fa", val categories: List<Category> = emptyList(), val isSaving: Boolean = false, val lastSavedText: String? = null, val error: String? = null, val pendingReviewItem: ReviewQueueItem? = null, val reviewApprovalCompleted: Boolean = false
) { val canSave: Boolean get() = sourceText.trim().isNotEmpty() && targetText.trim().isNotEmpty() && !isSaving }

@HiltViewModel
class AddWordViewModel @Inject constructor(private val createConcept: CreateConceptUseCase, private val approveReviewQueueItem: ApproveReviewQueueItemUseCase, private val getAllCategories: GetAllCategoriesUseCase, private val getOrCreateCategory: GetOrCreateCategoryUseCase) : ViewModel() {
    private val _state = MutableStateFlow(AddWordUiState())
    val state: StateFlow<AddWordUiState> = _state.asStateFlow()
    private var categoryLoadGeneration = 0L
    init { loadCategories() }
    private fun loadCategories() {
        val generation = ++categoryLoadGeneration
        viewModelScope.launch { runCatching { getAllCategories() }.onSuccess { categories -> if (generation == categoryLoadGeneration) _state.value = _state.value.copy(categories = categories) }.onFailure { error -> if (generation == categoryLoadGeneration) _state.value = _state.value.copy(error = error.message ?: "خطا در بارگذاری دسته‌ها") } }
    }
    fun refreshCategories() { loadCategories() }
    fun beginReviewApproval(item: ReviewQueueItem) { _state.value = _state.value.copy(sourceText = item.sourceText, targetText = item.targetText.orEmpty(), notes = item.warning.orEmpty(), pendingReviewItem = item, reviewApprovalCompleted = false, error = null, lastSavedText = null) }
    fun cancelReviewApproval() { _state.value = _state.value.copy(sourceText = "", targetText = "", notes = "", pendingReviewItem = null, reviewApprovalCompleted = false, error = null, lastSavedText = null) }
    fun consumeReviewApprovalCompletion() { _state.value = _state.value.copy(reviewApprovalCompleted = false, pendingReviewItem = null) }
    fun setLanguagePair(source: String, target: String) { _state.value = _state.value.copy(sourceLanguage = source, targetLanguage = target) }
    fun onSourceTextChange(value: String) { _state.value = _state.value.copy(sourceText = value, error = null) }
    fun onTargetTextChange(value: String) { _state.value = _state.value.copy(targetText = value, error = null) }
    fun onNotesChange(value: String) { _state.value = _state.value.copy(notes = value) }
    fun onCategoryNameChange(value: String) { _state.value = _state.value.copy(categoryName = value) }
    fun onEntryTypeChange(value: EntryType) { _state.value = _state.value.copy(entryType = value) }
    fun save() {
        val current = _state.value
        if (!current.canSave) return
        viewModelScope.launch {
            _state.value = current.copy(isSaving = true, error = null)
            try {
                val categoryId = current.categoryName.trim().takeIf { it.isNotEmpty() }?.let { getOrCreateCategory(it) }
                val pendingReview = current.pendingReviewItem
                if (pendingReview != null) {
                    approveReviewQueueItem(pendingReview, current.sourceText.trim(), current.targetText.trim(), current.sourceLanguage, current.targetLanguage, categoryId, current.notes.trim().ifBlank { null }, current.entryType)
                    _state.value = _state.value.copy(isSaving = false, lastSavedText = current.sourceText.trim(), reviewApprovalCompleted = true)
                } else {
                    createConcept(CreateConceptCommand(sourceText = current.sourceText.trim(), targetText = current.targetText.trim(), sourceLanguage = current.sourceLanguage, targetLanguage = current.targetLanguage, categoryId = categoryId, notes = current.notes.trim().ifBlank { null }, pronunciation = current.pronunciation.trim().ifBlank { null }, example = current.example.trim().ifBlank { null }, entryType = current.entryType))
                    val latest = _state.value
                    val unchanged = latest.sourceText == current.sourceText && latest.targetText == current.targetText && latest.notes == current.notes && latest.categoryName == current.categoryName && latest.entryType == current.entryType
                    _state.value = if (unchanged) latest.copy(sourceText = "", targetText = "", notes = "", pronunciation = "", example = "", isSaving = false, lastSavedText = current.sourceText.trim()) else latest.copy(isSaving = false, lastSavedText = current.sourceText.trim())
                }
                loadCategories()
            } catch (e: Exception) { _state.value = _state.value.copy(isSaving = false, error = e.message ?: "خطا در ذخیره‌سازی") }
        }
    }
}
