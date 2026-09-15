package com.flashlearn.app.ui.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flashlearn.data.backup.LegacyFullBackupRepository
import com.flashlearn.data.backup.VocabularyBackupRepository
import com.flashlearn.data.backup.TypedBackupRepository
import com.flashlearn.domain.backup.BackupType
import com.flashlearn.domain.repository.BackupRepository
import com.flashlearn.domain.repository.RestoreResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class BackupUiState(val busy:Boolean=false,val message:String?=null,val exportedJson:String?=null,val exportedType:BackupType=BackupType.FULL)
@HiltViewModel
class BackupViewModel @Inject constructor(private val repo:BackupRepository,private val vocabularyRepo:VocabularyBackupRepository,private val legacyFullRepo:LegacyFullBackupRepository,private val typedRepo:TypedBackupRepository):ViewModel(){
 private val _state=MutableStateFlow(BackupUiState());val state:StateFlow<BackupUiState> = _state
 fun export(type:BackupType=BackupType.FULL)=viewModelScope.launch{if(_state.value.busy)return@launch;_state.value=BackupUiState(busy=true,message="در حال ساخت ${type.name}…",exportedType=type);runCatching{withContext(Dispatchers.IO){typedRepo.export(type)}}.onSuccess{_state.value=BackupUiState(message="پشتیبان ${type.name} آماده است.",exportedJson=it,exportedType=type)}.onFailure{_state.value=BackupUiState(message="ساخت پشتیبان ناموفق بود: ${it.message?:"خطای نامشخص"}")}}
 fun restore(json:String,onSuccess:()->Unit={})=viewModelScope.launch{if(_state.value.busy)return@launch;_state.value=BackupUiState(busy=true,message="در حال بازیابی پشتیبان…");runCatching{withContext(Dispatchers.IO){val root=JSONObject(json);when{root.optString("backupMode")=="VOCABULARY"->vocabularyRepo.restore(json);root.optString("backupMode")=="FULL"&&root.has("concepts")&&root.optJSONArray("concepts")?.optJSONObject(0)?.has("uuid")==true->legacyFullRepo.restore(json);else->repo.restoreFull(json)}}}.onSuccess{r->_state.value=BackupUiState(message=restoreMessage(r));if(r.issues.isEmpty())onSuccess()}.onFailure{e->_state.value=BackupUiState(message="بازیابی ناموفق بود: ${e.message?:"خطای نامشخص"}")}}
 private fun restoreMessage(r:RestoreResult)="${r.newCount} رکورد جدید بازیابی شد و ${r.mergedCount} رکورد موجود ادغام شد.${if(r.issues.isNotEmpty())" ${r.issues.joinToString()}" else ""}"
 fun clearMessage(){_state.value=_state.value.copy(message=null)};fun showMessage(message:String){_state.value=_state.value.copy(message=message)}
}
