package com.flashlearn.app.ui.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flashlearn.domain.repository.BackupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class BackupUiState(val busy: Boolean=false, val message: String?=null, val exportedJson: String?=null)

@HiltViewModel
class BackupViewModel @Inject constructor(private val repo: BackupRepository): ViewModel() {
    private val _state=MutableStateFlow(BackupUiState())
    val state: StateFlow<BackupUiState> = _state

    fun export() = viewModelScope.launch {
        if (_state.value.busy) return@launch
        _state.value=BackupUiState(busy=true, message="در حال ساخت پشتیبان…")
        runCatching { withContext(Dispatchers.IO) { repo.exportFull() } }
            .onSuccess { _state.value=BackupUiState(message="پشتیبان آماده است؛ فایل JSON خروجی را ذخیره کن.", exportedJson=it) }
            .onFailure { _state.value=BackupUiState(message="ساخت پشتیبان ناموفق بود: ${it.message ?: "خطای نامشخص"}") }
    }

    fun restore(json: String, onSuccess: () -> Unit = {}) = viewModelScope.launch {
        if (_state.value.busy) return@launch
        _state.value=BackupUiState(busy=true, message="در حال بازیابی پشتیبان…")
        runCatching { withContext(Dispatchers.IO) { repo.restoreFull(json) } }
            .onSuccess { r ->
                _state.value=BackupUiState(
                    message="${r.newCount} رکورد جدید بازیابی شد و ${r.mergedCount} رکورد موجود ادغام شد.${if(r.issues.isNotEmpty()) " ${r.issues.joinToString()}" else ""}"
                )
                if (r.issues.isEmpty()) onSuccess()
            }
            .onFailure { e ->
                _state.value=BackupUiState(message="بازیابی ناموفق بود: ${e.message ?: "خطای نامشخص"}")
            }
    }

    fun clearMessage(){ _state.value=_state.value.copy(message=null) }
    fun showMessage(message: String) { _state.value = _state.value.copy(message = message) }
}
