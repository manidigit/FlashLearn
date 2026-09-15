package com.flashlearn.app.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flashlearn.app.ui.LanguagePair
import com.flashlearn.domain.model.Category
import com.flashlearn.domain.model.Concept
import com.flashlearn.domain.model.Content
import com.flashlearn.domain.model.LearningState
import com.flashlearn.domain.model.Stage
import com.flashlearn.domain.model.Tag
import com.flashlearn.domain.model.VocabularyDifficulty
import com.flashlearn.domain.progress.CalculateProgressUseCase
import com.flashlearn.domain.repository.CategoryRepository
import com.flashlearn.domain.repository.ConceptRepository
import com.flashlearn.domain.repository.ConceptTagRepository
import com.flashlearn.domain.repository.ContentRepository
import com.flashlearn.domain.repository.DifficultyStateRepository
import com.flashlearn.domain.repository.LearningStateRepository
import com.flashlearn.domain.repository.ReviewHistoryRepository
import com.flashlearn.domain.repository.TagRepository
import com.flashlearn.domain.usecase.CreateTagUseCase
import com.flashlearn.domain.usecase.DeleteTagUseCase
import com.flashlearn.domain.usecase.UpdateTagUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class LibraryItem(val concept: Concept,val source: Content?,val target: Content?,val category: Category?,val difficulty: VocabularyDifficulty? = null)
enum class LibraryFilter { ALL, LEARNED, LEARNING, NEW }

data class LibraryUiState(
    val query: String = "", val selectedCategoryIds: Set<UUID> = emptySet(), val selectedTagId: UUID? = null,
    val favoritesOnly: Boolean = false, val filter: LibraryFilter = LibraryFilter.ALL,
    val categories: List<Category> = emptyList(), val tags: List<Tag> = emptyList(),
    val categoryCounts: Map<UUID, Int> = emptyMap(), val categoryTotalCount: Int = 0,
    val tagCounts: Map<UUID, Int> = emptyMap(), val totalCount: Int = 0, val learnedCount: Int = 0,
    val learningCount: Int = 0, val newCount: Int = 0, val items: List<LibraryItem> = emptyList(),
    val isLoading: Boolean = true, val isTagBusy: Boolean = false, val error: String? = null,
    val sourceLanguage: String = "es", val targetLanguage: String = "fa"
) {
    @Deprecated("Use selectedCategoryIds for multi-category filtering")
    val selectedCategoryId: UUID? get() = selectedCategoryIds.singleOrNull()
}

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val concepts: ConceptRepository, private val contents: ContentRepository,
    private val categoriesRepo: CategoryRepository, private val tagsRepo: TagRepository,
    private val conceptTags: ConceptTagRepository, private val difficultyRepository: DifficultyStateRepository,
    private val learningRepository: LearningStateRepository, private val reviewHistoryRepository: ReviewHistoryRepository,
    private val calculateProgress: CalculateProgressUseCase, private val createTagUseCase: CreateTagUseCase,
    private val updateTagUseCase: UpdateTagUseCase, private val deleteTagUseCase: DeleteTagUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(LibraryUiState())
    val state: StateFlow<LibraryUiState> = _state.asStateFlow()
    private var refreshGeneration = 0L

    init { refresh() }
    fun setLanguagePair(pair: LanguagePair) { if (_state.value.sourceLanguage == pair.source.code && _state.value.targetLanguage == pair.target.code) return; _state.value = _state.value.copy(sourceLanguage = pair.source.code,targetLanguage = pair.target.code);refresh() }
    fun onQueryChange(value:String){_state.value=_state.value.copy(query=value);refresh()}
    fun onFavoritesChange(value:Boolean){_state.value=_state.value.copy(favoritesOnly=value);refresh()}
    fun onFilterChange(value:LibraryFilter){_state.value=_state.value.copy(filter=value);refresh()}
    fun onCategoryChange(ids:Set<UUID>){_state.value=_state.value.copy(selectedCategoryIds=ids);refresh()}
    @Deprecated("Use onCategoryChange(Set<UUID>) for multi-category filtering") fun onCategoryChange(id:UUID?){onCategoryChange(id?.let(::setOf) ?: emptySet())}
    fun onTagChange(id:UUID?){_state.value=_state.value.copy(selectedTagId=id);refresh()}
    fun createTag(name:String,onDone:(String?)->Unit={})=runTagMutation(onDone){createTagUseCase(name)}
    fun renameTag(id:UUID,name:String,onDone:(String?)->Unit={})=runTagMutation(onDone){updateTagUseCase(id,name)}
    fun removeTag(id:UUID,onDone:(String?)->Unit={})=runTagMutation(onDone){deleteTagUseCase(id);if(_state.value.selectedTagId==id)_state.value=_state.value.copy(selectedTagId=null)}
    private fun runTagMutation(onDone:(String?)->Unit,block:suspend()->Unit){if(_state.value.isTagBusy)return;viewModelScope.launch{_state.value=_state.value.copy(isTagBusy=true,error=null);runCatching{block();refresh()}.onSuccess{onDone(null)}.onFailure{onDone(it.message?:"خطا در مدیریت Tag")};_state.value=_state.value.copy(isTagBusy=false)}}

    fun refresh(){
        val generation=++refreshGeneration;val snapshot=_state.value;val query=snapshot.query.trim();val selectedCategories=snapshot.selectedCategoryIds;val selectedTag=snapshot.selectedTagId;val favoritesOnly=snapshot.favoritesOnly;val filter=snapshot.filter;val sourceLanguage=snapshot.sourceLanguage;val targetLanguage=snapshot.targetLanguage
        viewModelScope.launch{
            if(generation!=refreshGeneration)return@launch;_state.value=_state.value.copy(isLoading=true,error=null)
            try{
                val cats=categoriesRepo.getAll();val tags=tagsRepo.getAll().sortedBy{it.name.lowercase()};val cs=if(query.isBlank())concepts.getAllActive() else concepts.searchActive(query)
                val allHistory=reviewHistoryRepository.getAll();val reviewedIds=allHistory.asSequence().map{it.conceptId}.toSet()
                val learningById=learningRepository.getAll().associateBy{it.conceptId};val tagLinks=conceptTags.getAll().groupBy{it.conceptId}.mapValues{(_,links)->links.map{it.tagId}.toSet()};val difficultyById=difficultyRepository.getAll().associateBy{it.conceptId}
                val filteredBase=cs.asSequence().filter{!favoritesOnly||it.favorite}.filter{selectedTag==null||selectedTag in tagLinks[it.id].orEmpty()}.toList()
                val counts=filteredBase.asSequence().mapNotNull{it.categoryId}.groupingBy{it}.eachCount();val tagCounts=filteredBase.asSequence().flatMap{tagLinks[it.id].orEmpty().asSequence()}.groupingBy{it}.eachCount()
                val learnedIds=filteredBase.asSequence().filter{learningById[it.id]?.stage==Stage.LEARNED}.map{it.id}.toSet()
                val learningIds=filteredBase.asSequence().filter{it.id in reviewedIds && it.id !in learnedIds}.map{it.id}.toSet()
                val newIds=filteredBase.asSequence().filter{it.id !in reviewedIds}.map{it.id}.toSet()
                val filteredConcepts=filteredBase.asSequence().filter{selectedCategories.isEmpty()||it.categoryId in selectedCategories}.filter{concept->when(filter){LibraryFilter.ALL->true;LibraryFilter.LEARNED->concept.id in learnedIds;LibraryFilter.LEARNING->concept.id in learningIds;LibraryFilter.NEW->concept.id in newIds}}.toList()
                val catMap=cats.associateBy{it.id};val contentMap=contents.findForConcepts(filteredConcepts.map{it.id}).groupBy{it.conceptId};val items=filteredConcepts.map{c->val cc=contentMap[c.id].orEmpty();LibraryItem(c,cc.firstOrNull{it.languageCode==sourceLanguage},cc.firstOrNull{it.languageCode==targetLanguage},c.categoryId?.let(catMap::get),difficultyById[c.id]?.current)}
                if(generation!=refreshGeneration)return@launch
                _state.value=_state.value.copy(tags=tags,categories=cats,tagCounts=tagCounts,categoryCounts=counts,categoryTotalCount=filteredBase.size,totalCount=filteredBase.size,learnedCount=learnedIds.size,learningCount=learningIds.size,newCount=newIds.size,items=items,isLoading=false)
            }catch(e:Exception){if(generation!=refreshGeneration)return@launch;_state.value=_state.value.copy(isLoading=false,error=e.message?:"خطا در بارگذاری لغات")}
        }
    }
}
