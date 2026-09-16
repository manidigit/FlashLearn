package com.flashlearn.app.ui.addword

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.FileOpen
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.flashlearn.app.ui.LanguagePair
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

@Composable
fun BulkImportScreen(viewModel: BulkImportViewModel, languagePair: LanguagePair = LanguagePair(), onBack: () -> Unit) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) { viewModel.resetForEntry(languagePair) }
    val openTextFile = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            val text = runCatching { withContext(Dispatchers.IO) { context.contentResolver.openInputStream(uri)?.use { BufferedReader(InputStreamReader(it, Charsets.UTF_8)).readText() } ?: error("فایل قابل خواندن نیست") } }
                .getOrElse { viewModel.showError(it.message ?: "خطا در خواندن فایل"); return@launch }
            viewModel.onTextChange(text)
        }
    }
    if (state.preview.isNotEmpty() || state.done) {
        BulkImportPreview(state = state, onBack = { viewModel.resetForEntry(languagePair); onBack() }, onRefresh = viewModel::preview, onImport = viewModel::importAll)
    } else {
        BulkImportEditor(state, languagePair, onBack, viewModel::onTextChange, viewModel::preview) { openTextFile.launch(arrayOf("text/plain", "text/csv", "text/*", "application/json", "*/*")) }
    }
}

@Composable
private fun BulkImportEditor(state: BulkImportUiState, languagePair: LanguagePair, onBack: () -> Unit, onTextChange: (String) -> Unit, onPreview: () -> Unit, onPickFile: () -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(24.dp, 20.dp, 24.dp, 32.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) { IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, contentDescription = "بازگشت") }; Text("لغات گروهی", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), textAlign = TextAlign.End) } }
        item { Text("${languagePair.source.flag} ${languagePair.source.labelFa}  →  ${languagePair.target.flag} ${languagePair.target.labelFa}", Modifier.fillMaxWidth(), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), textAlign = TextAlign.End) }
        item { Text("چند کلمه را با فرمت: متن مبدأ / ترجمه / (اختیاری) دسته، هر مورد در یک بلوک جدا با خط خالی، Paste کنید.", Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.End) }
        item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedButton(onClick = onPickFile, enabled = !state.isImporting && !state.isPreviewing, modifier = Modifier.weight(1f).height(54.dp), shape = RoundedCornerShape(28.dp)) { Icon(Icons.Outlined.FileOpen, null); Spacer(Modifier.width(8.dp)); Text("انتخاب فایل واژگان", fontWeight = FontWeight.SemiBold) }; OutlinedButton(onClick = { if (state.rawText.isNotBlank()) onPreview() }, enabled = state.rawText.isNotBlank() && !state.isImporting && !state.isPreviewing, modifier = Modifier.weight(.55f).height(54.dp), shape = RoundedCornerShape(28.dp)) { Icon(Icons.Outlined.Refresh, null); Spacer(Modifier.width(4.dp)); Text("رفرش") } } }
        item { OutlinedTextField(value = state.rawText, onValueChange = onTextChange, modifier = Modifier.fillMaxWidth().heightIn(min = 230.dp, max = 360.dp), label = { Text("متن واژگان") }, placeholder = { Text("la piedra\nسنگ\n\nel nivel\nسطح، درجه") }, minLines = 8, maxLines = 16, shape = RoundedCornerShape(20.dp)) }
        item { Button(onClick = onPreview, enabled = state.rawText.isNotBlank() && !state.isImporting && !state.isPreviewing, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(28.dp)) { Text(if (state.isPreviewing) "در حال پردازش..." else "پیش‌نمایش", fontWeight = FontWeight.Bold) } }
        state.error?.let { item { Text(it, Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.error, textAlign = TextAlign.End) } }
    }
}

@Composable
private fun BulkImportPreview(state: BulkImportUiState, onBack: () -> Unit, onRefresh: () -> Unit, onImport: () -> Unit) {
    var duplicateOnly by remember(state.done) { mutableStateOf(false) }
    val results = if (state.done) state.results else state.preview.map { entry -> BulkImportItemResult(entry, if (entry.sourceText.isBlank() || entry.translationText.isNullOrBlank()) BulkImportItemStatus.INCOMPLETE else BulkImportItemStatus.READY) }
    val validCount = results.count { it.status != BulkImportItemStatus.INCOMPLETE && it.status != BulkImportItemStatus.DUPLICATE }
    val visibleResults = if (duplicateOnly) results.filter { it.status == BulkImportItemStatus.DUPLICATE } else results

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 1.dp) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onRefresh, enabled = !state.isImporting && !state.isPreviewing && state.rawText.isNotBlank(), modifier = Modifier.height(44.dp), shape = RoundedCornerShape(22.dp), contentPadding = PaddingValues(horizontal = 14.dp)) {
                            Icon(Icons.Outlined.Refresh, null)
                            Spacer(Modifier.width(5.dp))
                            Text("رفرش", fontWeight = FontWeight.SemiBold)
                        }
                        OutlinedButton(onClick = onBack, modifier = Modifier.height(44.dp), shape = RoundedCornerShape(22.dp), contentPadding = PaddingValues(horizontal = 14.dp)) {
                            Text("بازگشت", fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("لغات گروهی", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
                        Text("پیش‌نمایش (${results.size} مورد)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Text("موارد تشخیص‌داده‌شده را بررسی کن و سپس همه موارد را وارد کن.", Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.End)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    FilterChip(selected = !duplicateOnly, onClick = { duplicateOnly = false }, label = { Text("همه کلمات") })
                    Spacer(Modifier.width(8.dp))
                    FilterChip(selected = duplicateOnly, onClick = { duplicateOnly = true }, enabled = state.done, label = { Text("کلمات تکراری (${state.skippedDuplicateCount})") })
                }
            }
        }

        if (state.warnings.isNotEmpty()) {
            Card(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(5.dp), horizontalAlignment = Alignment.End) {
                    Text("گزارش هشدارها و موارد حل‌نشده (${state.warnings.size})", modifier = Modifier.fillMaxWidth(), fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                    state.warnings.forEach { warning -> Text("خط ${warning.lineNumber} • ${warning.warningType} • ${warning.message}\n${warning.rawText}", modifier = Modifier.fillMaxWidth(), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.End) }
                }
            }
        }

        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(visibleResults) { result -> PreviewEntryCard(result, state.lineNumbers[result.entry.rawLines.firstOrNull()?.trim()]) }
            if (visibleResults.isEmpty() && duplicateOnly) item { Text("هنوز مورد تکراری ثبت‌شده‌ای وجود ندارد.", Modifier.fillMaxWidth().padding(24.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center) }
            if (results.size > BulkImportViewModel.PREVIEW_LIMIT && !duplicateOnly) item { Text("فقط ${BulkImportViewModel.PREVIEW_LIMIT} مورد اول برای پیش‌نمایش نمایش داده می‌شود؛ همه ${results.size} مورد وارد خواهند شد.", Modifier.fillMaxWidth().padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.End, style = MaterialTheme.typography.bodySmall) }
        }

        Surface(shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp), tonalElevation = 4.dp, shadowElevation = 4.dp, color = MaterialTheme.colorScheme.surface) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (!state.done) Text("قابل ورود: $validCount  •  ناقص: ${results.size - validCount}", Modifier.fillMaxWidth(), textAlign = TextAlign.End, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                else Text("نتیجه: ${state.importedCount} جدید  •  ${state.skippedDuplicateCount} تکراری  •  ${state.invalidCount} ناقص  •  ${state.needsReviewCount} نیازمند بررسی  •  ${state.failedCount} خطادار", Modifier.fillMaxWidth(), textAlign = TextAlign.End, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Button(onClick = onImport, enabled = !state.isImporting && !state.isPreviewing && !state.done && !duplicateOnly, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(27.dp)) {
                    Icon(Icons.Outlined.Upload, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (state.isImporting) "در حال وارد کردن..." else "Import همه (${results.size})", fontWeight = FontWeight.Bold)
                }
                state.error?.let { Text(it, Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.error, textAlign = TextAlign.End) }
            }
        }
    }
}

@Composable
private fun PreviewEntryCard(result: BulkImportItemResult, lineNumber: Int?) {
    val incomplete = result.status == BulkImportItemStatus.INCOMPLETE
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.Top) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(40.dp).background(if (incomplete) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))) {
                Text(if (incomplete) "!" else "✓", textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp), horizontalAlignment = Alignment.End) {
                lineNumber?.let { Text("خط $it", modifier = Modifier.fillMaxWidth(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.End) }
                OutlinedTextField(value = result.entry.sourceText, onValueChange = {}, readOnly = true, modifier = Modifier.fillMaxWidth(), textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), shape = RoundedCornerShape(14.dp), singleLine = true)
                OutlinedTextField(value = result.entry.translationText.orEmpty(), onValueChange = {}, readOnly = true, modifier = Modifier.fillMaxWidth(), textStyle = MaterialTheme.typography.bodyLarge, shape = RoundedCornerShape(14.dp), minLines = 1, maxLines = 2)
                if (result.status == BulkImportItemStatus.DUPLICATE) Text("کلمه تکراری — قبلاً در کتابخانه وجود دارد", Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.error, textAlign = TextAlign.End, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
