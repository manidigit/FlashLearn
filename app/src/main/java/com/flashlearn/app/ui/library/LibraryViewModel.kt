package com.flashlearn.app.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flashlearn.app.ui.LanguagePair
import com.flashlearn.domain.model.Category
import com.flashlearn.domain.model.Concept
import com.flashlearn.domain.model.Content
import com.flashlearn.domain.model.Tag
import com.flashlearn.domain.repository.CategoryRepository
import com.flashlearn.domain.repository.ConceptRepository
import com.flashlearn.domain.repository.ConceptTagRepository
import com.flashlearn.domain.repository.ContentRepository
import com.flashlearn.domain.usecase.CreateTagUseCase
import com.flashlearn.domain.usecase.DeleteTagUseCase
import com.flashlearn.domain.usecase.UpdateTagUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject

data class LibraryItem(val concept: Concept, val source: Content?, val target: Content?, val category: Category?)
data class LibraryUiState(
    val query: String = "",
    val selectedCategoryId: UUID? = null,
    val selectedTagId: UUID? = null,
    val favoritesOnly: Boolean = false,
    val categories: List<Category> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val categoryCounts: Map<UUID, Int> = emptyMap(),
    val tagCounts: Map<UUID, Int> = emptyMap(),
    val totalCount: Int = 0,
    val items: List<LibraryItem> = emptyList(),
    val isLoading: Boolean = true,
    val isTagBusy: Boolean = false,
    val error: String? = null,
    val sourceLanguage: String = "es",
    val targetLanguage: String = "fa"
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val concepts: ConceptRepository,
    private val contents: ContentRepository,
    private val categoriesRepo: CategoryRepository,
    private val tagsRepo: com.flashlearn.domain.repository.TagRepository,
    private val conceptTags: ConceptTagRepository,
    private val createTag: CreateTagUseCase,
    private val updateTag: UpdateTagUseCase,
    private val deleteTag: DeleteTagUseCase
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
    fun onCategoryChange(id: UUID?) { _state.value = _state.value.copy(selectedCategoryId = id); refresh() }
    fun onTagChange(id: UUID?) { _state.value = _state.value.copy(selectedTagId = id); refresh() }

    fun createTag(name: String, onDone: (String?) -> Unit = {}) = runTagMutation(onDone) { createTag(name) }
    fun renameTag(id: UUID, name: String, onDone: (String?) -> Unit = {}) = runTagMutation(onDone) { updateTag(id, name) }
    fun removeTag(id: UUID, onDone: (String?) -> Unit = {}) = runTagMutation(onDone) { deleteTag(id); if (_state.value.selectedTagId == id) _state.value = _state.value.copy(selectedTagId = null) }

    private fun runTagMutation(onDone: (String?) -> Unit, block: suspend () -> Unit) {
        if (_state.value.isTagBusy) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isTagBusy = true, error = null)
            runCatching { block(); refresh() }
                .onSuccess { onDone(null) }
                .onFailure { onDone(it.message ?: "خطا در مدیریت Tag") }
            _state.value = _state.value.copy(isTagBusy = false)
        }
    }

    fun refresh() {
        val generation = ++refreshGeneration
        val snapshot = _state.value
        val query = snapshot.query.trim()
        val selectedCategory = snapshot.selectedCategoryId
        val selectedTag = snapshot.selectedTagId
        val favoritesOnly = snapshot.favoritesOnly
        val sourceLanguage = snapshot.sourceLanguage
        val targetLanguage = snapshot.targetLanguage
        viewModelScope.launch {
            if (generation != refreshGeneration) return@launch
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val cats = categoriesRepo.getAll()
                val tags = tagsRepo.getAll().sortedBy { it.name.lowercase() }
                val cs = if (query.isBlank()) concepts.getAllActive() else concepts.searchActive(query)
                val tagLinks = conceptTags.getAll().groupBy { it.conceptId }.mapValues { (_, links) -> links.map { it.tagId }.toSet() }
                val filteredBase = cs.asSequence()
                    .filter { !favoritesOnly || it.favorite }
                    .filter { selectedTag == null || selectedTag in tagLinks[it.id].orEmpty() }
                    .toList()
                val counts = filteredBase.asSequence().mapNotNull { it.categoryId }.groupingBy { it }.eachCount()
                val tagCounts = filteredBase.asSequence().flatMap { tagLinks[it.id].orEmpty().asSequence() }.groupingBy { it }.eachCount()
                val filteredConcepts = filteredBase.asSequence().filter { selectedCategory == null || it.categoryId == selectedCategory }.toList()
                val catMap = cats.associateBy { it.id }
                val contentMap = contents.findForConcepts(filteredConcepts.map { it.id }).groupBy { it.conceptId }
                val items = filteredConcepts.map { c ->
                    val cc = contentMap[c.id].orEmpty()
                    LibraryItem(c, cc.firstOrNull { it.languageCode == sourceLanguage }, cc.firstOrNull { it.languageCode == targetLanguage }, c.categoryId?.let(catMap::get))
                }
                if (generation != refreshGeneration) return@launch
                _state.value = _state.value.copy(tags = tags, categories = cats, tagCounts = tagCounts, categoryCounts = counts, totalCount = filteredConcepts.size, items = items, isLoading = false)
            } catch (e: Exception) {
                if (generation != refreshGeneration) return@launch
                _state.value = _state.value.copy(isLoading = false, error = e.message ?: "خطا در بارگذاری لغات")
            }
        }
    }
}
