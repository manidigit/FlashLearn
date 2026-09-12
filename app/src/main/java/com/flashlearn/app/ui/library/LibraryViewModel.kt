package com.flashlearn.app.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val error: String? = null
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

    fun onQueryChange(value: String) {
        _state.value = _state.value.copy(query = value)
        refresh()
    }
    fun onFavoritesChange(value: Boolean) {
        _state.value = _state.value.copy(favoritesOnly = value)
        refresh()
    }
    fun onCategoryChange(id: java.util.UUID?) {
        _state.value = _state.value.copy(selectedCategoryId = id)
        refresh()
    }
    fun refresh() {
        val generation = ++refreshGeneration
        val snapshot = _state.value
        val query = snapshot.query.trim()
        val selectedCategory = snapshot.selectedCategoryId
        val favoritesOnly = snapshot.favoritesOnly
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val cats = categoriesRepo.getAll()
                val cs = if (query.isBlank()) concepts.getAllActive() else concepts.searchActive(query)
                val allContent = contents.getAll().groupBy { it.conceptId }
                val catMap = cats.associateBy { it.id }
                val items = cs.asSequence()
                    .filter { selectedCategory == null || it.categoryId == selectedCategory }
                    .filter { !favoritesOnly || it.favorite }
                    .map { c ->
                        val cc = allContent[c.id].orEmpty()
                        LibraryItem(c, cc.firstOrNull { it.languageCode == "es" }, cc.firstOrNull { it.languageCode == "fa" }, c.categoryId?.let(catMap::get))
                    }.toList()
                if (generation != refreshGeneration) return@launch
                _state.value = _state.value.copy(categories = cats, items = items, isLoading = false)
            } catch (e: Exception) {
                if (generation != refreshGeneration) return@launch
                _state.value = _state.value.copy(isLoading = false, error = e.message ?: "خطا در بارگذاری لغات")
            }
        }
    }
}
