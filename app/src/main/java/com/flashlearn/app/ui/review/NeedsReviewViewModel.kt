package com.flashlearn.app.ui.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flashlearn.domain.model.ReviewQueueItem
import com.flashlearn.domain.model.ReviewQueueStatus
import com.flashlearn.domain.repository.ReviewQueueRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NeedsReviewViewModel @Inject constructor(private val repository: ReviewQueueRepository) : ViewModel() {
    private val _items = MutableStateFlow<List<ReviewQueueItem>>(emptyList())
    val items: StateFlow<List<ReviewQueueItem>> = _items.asStateFlow()

    fun refresh() { viewModelScope.launch { _items.value = repository.getPending() } }

    fun approve(item: ReviewQueueItem) { viewModelScope.launch { repository.update(item.copy(status = ReviewQueueStatus.APPROVED)); refresh() } }
    fun reject(item: ReviewQueueItem) { viewModelScope.launch { repository.update(item.copy(status = ReviewQueueStatus.REJECTED)); refresh() } }
}
