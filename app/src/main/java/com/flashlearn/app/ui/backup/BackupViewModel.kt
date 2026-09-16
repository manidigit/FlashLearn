package com.flashlearn.app.ui.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flashlearn.data.backup.LegacyFullBackupRepository
import com.flashlearn.data.backup.TypedBackupRepository
import com.flashlearn.data.backup.VocabularyBackupRepository
import com.flashlearn.domain.backup.BackupType
import com.flashlearn.domain.repository.BackupRepository
import com.flashlearn.domain.repository.DataExportRepository
import com.flashlearn.domain.repository.ExportFormat
import com.flashlearn.domain.repository.RestoreResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import javax.inject.Inject

data class BackupUiState(
    val busy: Boolean = false,
    val message: String? = null,
    val exportedJson: String? = null,
    val exportedType: BackupType = BackupType.FULL,
    val exportedFile: File? = null,
    val exportedFormat: ExportFormat? = null
)

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val repo: BackupRepository,
    private val vocabularyRepo: VocabularyBackupRepository,
    private val legacyFullRepo: LegacyFullBackupRepository,
    private val typedRepo: TypedBackupRepository,
    private val dataExportRepo: DataExportRepository
) : ViewModel() {
    private val _state = MutableStateFlow(BackupUiState())
    val state: StateFlow<BackupUiState> = _state

    fun export(type: BackupType = BackupType.FULL) = viewModelScope.launch {
        if (_state.value.busy) return@launch
        _state.value = BackupUiState(busy = true, message = "در حال ساخت ${type.name}…", exportedType = type)
        runCatching {
            withContext(Dispatchers.IO) {
                if (type == BackupType.FULL) repo.exportFull() else typedRepo.export(type)
            }
        }.onSuccess { json ->
            _state.value = BackupUiState(
                message = "پشتیبان ${type.name} آماده است.",
                exportedJson = json,
                exportedType = type
            )
        }.onFailure { e ->
            _state.value = BackupUiState(message = "ساخت پشتیبان ناموفق بود: ${e.message ?: "خطای نامشخص"}")
        }
    }

    fun exportData(format: ExportFormat) = viewModelScope.launch {
        if (_state.value.busy) return@launch
        _state.value = _state.value.copy(
            busy = true,
            message = "در حال ساخت خروجی ${format.name}…",
            exportedFile = null,
            exportedFormat = format
        )
        runCatching {
            withContext(Dispatchers.IO) { dataExportRepo.export(format) }
        }.onSuccess { file ->
            _state.value = _state.value.copy(
                busy = false,
                message = "خروجی ${format.name} آماده است.",
                exportedFile = file,
                exportedFormat = format
            )
        }.onFailure { e ->
            _state.value = _state.value.copy(
                busy = false,
                message = "ساخت خروجی ناموفق بود: ${e.message ?: "خطای نامشخص"}"
            )
        }
    }

    fun restore(json: String, onSuccess: () -> Unit = {}) = viewModelScope.launch {
        if (_state.value.busy) return@launch
        _state.value = BackupUiState(busy = true, message = "در حال بازیابی پشتیبان…")
        runCatching {
            withContext(Dispatchers.IO) {
                val root = JSONObject(json)
                when {
                    root.optString("backupMode") == "VOCABULARY" -> vocabularyRepo.restore(json)
                    root.optString("backupMode") == "FULL" &&
                        root.has("concepts") &&
                        root.optJSONArray("concepts")?.optJSONObject(0)?.has("uuid") == true ->
                        legacyFullRepo.restore(json)
                    root.optString("backupType") == "FULL" &&
                        root.optInt("schemaVersion", -1) in 1..2 ->
                        repo.restoreFull(json)
                    else -> repo.restoreFull(json)
                }
            }
        }.onSuccess { result ->
            _state.value = BackupUiState(message = restoreMessage(result))
            if (result.issues.isEmpty()) onSuccess()
        }.onFailure { e ->
            _state.value = BackupUiState(message = "بازیابی ناموفق بود: ${e.message ?: "خطای نامشخص"}")
        }
    }

    private fun restoreMessage(result: RestoreResult): String {
        val issues = if (result.issues.isNotEmpty()) {
            " ${result.issues.joinToString()}"
        } else {
            ""
        }
        return "${result.newCount} رکورد جدید بازیابی شد و ${result.mergedCount} رکورد موجود ادغام شد.$issues"
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }

    fun showMessage(message: String) {
        _state.value = _state.value.copy(message = message)
    }
}
