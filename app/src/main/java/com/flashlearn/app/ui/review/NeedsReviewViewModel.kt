package com.flashlearn.app.ui.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flashlearn.domain.model.ReviewQueueItem
import com.flashlearn.domain.model.ReviewQueueStatus
import com.flashlearn.domain.repository.ReviewQueueRepository
import com.flashlearn.app.R
import com.flashlearn.core.util.runCatchingCancellable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NeedsReviewViewModel @Inject constructor(private val repository: ReviewQueueRepository) : ViewModel() {
    private val _items = MutableStateFlow<List<ReviewQueueItem>>(emptyList())
    private val _error = MutableStateFlow<Int?>(null)
    init { refresh() }
    val items: StateFlow<List<ReviewQueueItem>> = _items.asStateFlow()
    val error: StateFlow<Int?> = _error.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _error.value = null
            runCatchingCancellable { repository.getPending() }
                .onSuccess { _items.value = it }
                .onFailure { _error.value = R.string.needs_review_load_error }
        }
    }

    fun approve(item: ReviewQueueItem) {
        viewModelScope.launch {
            _error.value = null
            runCatchingCancellable { repository.update(item.copy(status = ReviewQueueStatus.APPROVED)) }
                .onSuccess { refresh() }
                .onFailure { _error.value = R.string.needs_review_approve_error }
        }
    }

    fun reject(item: ReviewQueueItem) {
        viewModelScope.launch {
            _error.value = null
            runCatchingCancellable { repository.update(item.copy(status = ReviewQueueStatus.REJECTED)) }
                .onSuccess { refresh() }
                .onFailure { _error.value = R.string.needs_review_reject_error }
        }
    }
}
