package com.flashlearn.app.ui.addword

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.FileOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flashlearn.app.ui.LanguagePair
import java.io.BufferedReader
import java.io.InputStreamReader

@Composable
fun BulkImportScreen(viewModel: BulkImportViewModel, languagePair: LanguagePair = LanguagePair(), onBack: () -> Unit) {
    val state by viewModel.state.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(languagePair) { viewModel.setLanguagePair(languagePair) }
    val openTextFile = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openInputStream(uri)?.use { BufferedReader(InputStreamReader(it, Charsets.UTF_8)).readText() }
                    ?: error("فایل قابل خواندن نیست")
            }.onSuccess(viewModel::onTextChange).onFailure { viewModel.onTextChange(""); }
        }
    }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("ورود گروهی واژگان", style = MaterialTheme.typography.headlineSmall)
            IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, contentDescription = "بازگشت") }
        }
        Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("${languagePair.source.flag} ${languagePair.source.labelFa}  →  ${languagePair.target.flag} ${languagePair.target.labelFa}", style = MaterialTheme.typography.titleMedium)
            Text("واژه‌ها را یکجا وارد کن؛ هر مدخل را به شکل «مبدأ → مقصد» یا در دو خط وارد کن.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedButton(onClick = { openTextFile.launch(arrayOf("text/plain", "text/csv", "text/*", "application/json", "*/*")) }, enabled = !state.isImporting, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                Icon(Icons.Outlined.FileOpen, null); Spacer(Modifier.width(8.dp)); Text("انتخاب فایل واژگان")
            }
            OutlinedTextField(state.rawText, viewModel::onTextChange, Modifier.fillMaxWidth().height(180.dp), label = { Text("متن واژگان") })
            Button(onClick = viewModel::preview, enabled = !state.isImporting && state.rawText.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("پیش‌نمایش") }
            if (state.preview.isNotEmpty()) {
                val validCount = state.preview.count { it.sourceText.isNotBlank() && !it.translationText.isNullOrBlank() }
                val incompleteCount = state.preview.size - validCount
                Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("پیش‌نمایش", style = MaterialTheme.typography.titleMedium)
                        Text("${state.preview.size} مدخل تشخیص داده شد")
                        Text("قابل ورود: $validCount  •  ناقص: $incompleteCount")
                    }
                }
                if (state.warnings.isNotEmpty()) Text("هشدارهای Parser: ${state.warnings.size}", color = MaterialTheme.colorScheme.error)
                LazyColumn(Modifier.heightIn(max = 280.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(if (state.done && state.results.isNotEmpty()) state.results else state.preview.map { entry -> BulkImportItemResult(entry, if (entry.sourceText.isBlank() || entry.translationText.isNullOrBlank()) BulkImportItemStatus.INCOMPLETE else BulkImportItemStatus.READY) }) { result ->
                        Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
                            Column(Modifier.padding(12.dp)) {
                                Text("${result.entry.sourceText} → ${result.entry.translationText ?: "بدون ترجمه"}")
                                Text("${result.status.label()}  •  ${result.confidencePercent}%", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                Button(onClick = viewModel::importAll, enabled = !state.isImporting, modifier = Modifier.fillMaxWidth()) { Text(if (state.isImporting) "در حال وارد کردن..." else "وارد کردن همه") }
            }
            if (state.done) Text("نتیجه ورود: ${state.importedCount} مدخل جدید، ${state.skippedDuplicateCount} تکراری، ${state.invalidCount} ناقص، ${state.failedCount} خطادار")
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }
}

fun BulkImportItemStatus.label() = when (this) {
    BulkImportItemStatus.READY -> "آماده"
    BulkImportItemStatus.INCOMPLETE -> "ناقص"
    BulkImportItemStatus.IMPORTED -> "وارد شد"
    BulkImportItemStatus.DUPLICATE -> "تکراری"
    BulkImportItemStatus.FAILED -> "خطا"
}
