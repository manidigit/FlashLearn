package com.flashlearn.app.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flashlearn.app.ui.LanguagePair
import com.flashlearn.domain.model.Category
import com.flashlearn.domain.model.Concept
import com.flashlearn.domain.model.Content
import com.flashlearn.domain.repository.CategoryRepository
import com.flashlearn.domain.repository.ConceptRepository
import com.flashlearn.domain.repository.ContentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class LibraryItem(val concept: Concept, val source: Content?, val target: Content?, val category: Category?)
data class LibraryUiState(
    val query: String = "",
    val selectedCategoryId: java.util.UUID? = null,
    val favoritesOnly: Boolean = false,
    val categories: List<Category> = emptyList(),
    val items: List<LibraryItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val sourceLanguage: String = "es",
    val targetLanguage: String = "fa"
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val concepts: ConceptRepository,
    private val contents: ContentRepository,
    private val categoriesRepo: CategoryRepository
) : ViewModel() {
    private val _state = MutableStateFlow(LibraryUiState())
    val state: StateFlow<LibraryUiState> = _state.asStateFlow()
    private var refreshGeneration = 0L

    init { refresh() }

    fun setLanguagePair(pair: LanguagePair) {
        if (_state.value.sourceLanguage == pair.source.code && _state.value.targetLanguage == pair.target.code) return
        _state.value = _state.value.copy(sourceLanguage = pair.source.code, targetLanguage = pair.target.code)
        refresh()
    }
    fun onQueryChange(value: String) { _state.value = _state.value.copy(query = value); refresh() }
    fun onFavoritesChange(value: Boolean) { _state.value = _state.value.copy(favoritesOnly = value); refresh() }
    fun onCategoryChange(id: java.util.UUID?) { _state.value = _state.value.copy(selectedCategoryId = id); refresh() }

    fun refresh() {
        val generation = ++refreshGeneration
        val snapshot = _state.value
        val query = snapshot.query.trim(); val selectedCategory = snapshot.selectedCategoryId; val favoritesOnly = snapshot.favoritesOnly
        val sourceLanguage = snapshot.sourceLanguage; val targetLanguage = snapshot.targetLanguage
        viewModelScope.launch {
            if (generation != refreshGeneration) return@launch
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val cats = categoriesRepo.getAll()
                val cs = if (query.isBlank()) concepts.getAllActive() else concepts.searchActive(query)
                val catMap = cats.associateBy { it.id }
                val filteredConcepts = cs.asSequence()
                    .filter { selectedCategory == null || it.categoryId == selectedCategory }
                    .filter { !favoritesOnly || it.favorite }
                    .toList()
                // Do not scan the complete contents table for every search/filter change.
                val contentMap = contents.findForConcepts(filteredConcepts.map { it.id }).groupBy { it.conceptId }
                val items = filteredConcepts.map { c ->
                    val cc = contentMap[c.id].orEmpty()
                    LibraryItem(c, cc.firstOrNull { it.languageCode == sourceLanguage }, cc.firstOrNull { it.languageCode == targetLanguage }, c.categoryId?.let(catMap::get))
                }
                if (generation != refreshGeneration) return@launch
                _state.value = _state.value.copy(categories = cats, items = items, isLoading = false)
            } catch (e: Exception) {
                if (generation != refreshGeneration) return@launch
                _state.value = _state.value.copy(isLoading = false, error = e.message ?: "خطا در بارگذاری لغات")
            }
        }
    }
}
