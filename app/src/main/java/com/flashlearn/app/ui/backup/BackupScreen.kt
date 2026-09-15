package com.flashlearn.app.ui.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.FileOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.flashlearn.domain.backup.BackupType
import com.flashlearn.domain.repository.ExportFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

@Composable
fun BackupScreen(viewModel:BackupViewModel,onBack:()->Unit,onRestored:()->Unit={}){
    val state by viewModel.state.collectAsState();var pendingJson by remember{mutableStateOf<String?>(null)};var pendingFile by remember{mutableStateOf<File?>(null)};val context=LocalContext.current;val scope=rememberCoroutineScope()
    val save=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")){uri->val json=pendingJson;if(uri!=null&&!json.isNullOrBlank())scope.launch(Dispatchers.IO){runCatching{context.contentResolver.openOutputStream(uri)?.use{it.write(json.toByteArray())}}.onFailure{viewModel.showMessage("ذخیره فایل ناموفق بود: ${it.message?:"خطا"}")}};pendingJson=null}
    val saveData=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")){uri->val file=pendingFile;if(uri!=null&&file!=null)scope.launch(Dispatchers.IO){runCatching{file.inputStream().use{input->context.contentResolver.openOutputStream(uri)?.use{output->input.copyTo(output)}}}.onFailure{viewModel.showMessage("ذخیره خروجی ناموفق بود: ${it.message?:"خطا"}")}};pendingFile=null}
    val open=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){uri->if(uri!=null)scope.launch(Dispatchers.IO){runCatching{context.contentResolver.openInputStream(uri)?.use{BufferedReader(InputStreamReader(it,Charsets.UTF_8)).readText()}?:error("فایل قابل خواندن نیست")}.onSuccess{json->if(json.trimStart().startsWith("{"))viewModel.restore(json,onRestored)else viewModel.showMessage("این فایل پشتیبان معتبر JSON نیست.")}.onFailure{viewModel.showMessage("خواندن فایل ناموفق بود: ${it.message?:"خطا"}")}}}
    Column(Modifier.fillMaxSize().padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("پشتیبان‌گیری و بازیابی",style=MaterialTheme.typography.headlineSmall);IconButton(onClick=onBack,enabled=!state.busy){Icon(Icons.Outlined.ArrowBack,"بازگشت")}}
        Text("پشتیبان JSON:",style=MaterialTheme.typography.titleMedium)
        BackupType.entries.forEach{type->Button(onClick={viewModel.export(type)},enabled=!state.busy,modifier=Modifier.fillMaxWidth().height(50.dp)){Text(when(type){BackupType.VOCABULARY->"واژگان (VOCABULARY)";BackupType.PROGRESS->"پیشرفت (PROGRESS)";BackupType.FULL->"کامل (FULL)"})}}
        Text("خروجی داده:",style=MaterialTheme.typography.titleMedium)
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){
            ExportFormat.values().forEach { format ->
                OutlinedButton(onClick={viewModel.exportData(format)},enabled=!state.busy,modifier=Modifier.weight(1f)) { Text(format.name) }
            }
        }
        OutlinedButton(onClick={open.launch(arrayOf("application/json","text/plain","*/*"))},enabled=!state.busy,modifier=Modifier.fillMaxWidth().height(50.dp)){Icon(Icons.Outlined.FileOpen,null);Spacer(Modifier.width(8.dp));Text("بازیابی فایل پشتیبان")}
        if(state.busy)LinearProgressIndicator(Modifier.fillMaxWidth());state.message?.let{Text(it,color=if(it.contains("ناموفق")||it.contains("معتبر"))MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)}
        state.exportedJson?.let{json->Card(Modifier.fillMaxWidth()){Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){Text("${state.exportedType.name} آماده است",style=MaterialTheme.typography.titleMedium);Text("${json.length} نویسه");Button(onClick={pendingJson=json;save.launch("flashlearn-${state.exportedType.name.lowercase()}-backup.json")},enabled=!state.busy){Text("ذخیره JSON")}}}}
        state.exportedFile?.let{file->Card(Modifier.fillMaxWidth()){Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){Text("خروجی ${state.exportedFormat?.name} آماده است",style=MaterialTheme.typography.titleMedium);Text(file.name);Button(onClick={pendingFile=file;saveData.launch(file.name)},enabled=!state.busy){Text("ذخیره فایل")}}}}
    }
}
