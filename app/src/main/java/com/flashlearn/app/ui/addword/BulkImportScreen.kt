package com.flashlearn.app.ui.addword

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BulkImportScreen(viewModel: BulkImportViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsState()
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("ورود گروهی واژگان", style = MaterialTheme.typography.headlineSmall)
            IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, contentDescription = "بازگشت") }
        }
        Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("واژه‌ها را یکجا وارد کن", style = MaterialTheme.typography.titleLarge)
            Text("هر مدخل را به شکل «اسپانیایی → فارسی» یا در دو خط وارد کن.", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            state.done && state.importedCount >= 0.let { state.done } .also { } // keeps the result section visually separated without changing import semantics
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
