package com.flashlearn.app.ui.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flashlearn.domain.repository.BackupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class BackupUiState(val busy: Boolean=false, val message: String?=null, val exportedJson: String?=null)

@HiltViewModel
class BackupViewModel @Inject constructor(private val repo: BackupRepository): ViewModel() {
    private val _state=MutableStateFlow(BackupUiState())
    val state: StateFlow<BackupUiState> = _state
    fun export() = viewModelScope.launch {
        if (_state.value.busy) return@launch
        _state.value=BackupUiState(busy=true)
        runCatching { repo.exportFull() }
            .onSuccess { _state.value=BackupUiState(message="Backup ready. Save the exported JSON file.", exportedJson=it) }
            .onFailure { _state.value=BackupUiState(message="Backup failed: ${it.message}") }
    }
    fun restore(json: String, onSuccess: () -> Unit = {}) = viewModelScope.launch {
        if (_state.value.busy) return@launch
        _state.value=BackupUiState(busy=true)
        runCatching { repo.restoreFull(json) }
            .onSuccess { r ->
                _state.value=BackupUiState(
                    message="Restored ${r.imported} records; skipped ${r.skipped}.${if(r.issues.isNotEmpty()) " ${r.issues.joinToString()}" else ""}"
                )
                if (r.issues.isEmpty()) onSuccess()
            }
            .onFailure { e ->
                _state.value=BackupUiState(message="Restore failed: ${e.message ?: "unknown error"}")
            }
    }
    fun clearMessage(){ _state.value=_state.value.copy(message=null) }
    fun showMessage(message: String) { _state.value = _state.value.copy(message = message) }
}
