package com.flashlearn.app.ui.addword

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.flashlearn.app.ui.theme.LocalFlashLearnThemeTokens
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
            val text = runCatching {
                withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use {
                        BufferedReader(InputStreamReader(it, Charsets.UTF_8)).readText()
                    } ?: error("فایل قابل خواندن نیست")
                }
            }.getOrElse { viewModel.showError(it.message ?: "خطا در خواندن فایل"); return@launch }
            viewModel.onTextChange(text)
        }
    }
    if (state.preview.isNotEmpty() || state.done) {
        BulkImportPreview(state, onBack = { viewModel.resetForEntry(languagePair); onBack() }, onRefresh = viewModel::preview, onImport = viewModel::importAll)
    } else {
        BulkImportEditor(state, languagePair, onBack, viewModel::onTextChange, viewModel::preview) {
            openTextFile.launch(arrayOf("text/plain", "text/csv", "text/*", "application/json", "*/*"))
        }
    }
}

@Composable
private fun BulkImportEditor(
    state: BulkImportUiState,
    languagePair: LanguagePair,
    onBack: () -> Unit,
    onTextChange: (String) -> Unit,
    onPreview: () -> Unit,
    onPickFile: () -> Unit
) {
    val tokens = LocalFlashLearnThemeTokens.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(tokens.screenPadding, tokens.dp(20f), tokens.screenPadding, tokens.dp(32f)),
        verticalArrangement = Arrangement.spacedBy(tokens.itemGap)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, "بازگشت", tint = tokens.onSurface) }
                Text("لغات گروهی", style = MaterialTheme.typography.headlineMedium, color = tokens.onSurface, textAlign = TextAlign.End)
            }
        }
        item {
            Text("${languagePair.source.flag} ${languagePair.source.labelFa}  →  ${languagePair.target.flag} ${languagePair.target.labelFa}", Modifier.fillMaxWidth(), style = MaterialTheme.typography.titleMedium, color = tokens.primary, textAlign = TextAlign.End)
        }
        item {
            Text("چند کلمه را با فرمت: متن مبدأ / ترجمه / (اختیاری) دسته، هر مورد در یک بلوک جدا با خط خالی، Paste کنید.", Modifier.fillMaxWidth(), color = tokens.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.End)
        }
        item {
            OutlinedTextField(
                value = state.rawText,
                onValueChange = onTextChange,
                modifier = Modifier.fillMaxWidth().heightIn(min = tokens.dp(230f), max = tokens.dp(360f)),
                label = { Text("متن واژگان") },
                placeholder = { Text("la piedra\nسنگ\n\nel nivel\nسطح، درجه", color = tokens.onSurfaceVariant) },
                minLines = 8,
                maxLines = 16,
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = tokens.surface,
                    focusedContainerColor = tokens.surface,
                    unfocusedBorderColor = tokens.outlineColor,
                    focusedBorderColor = tokens.primary
                )
            )
        }
        item {
            Button(
                onClick = onPreview,
                enabled = state.rawText.isNotBlank() && !state.isImporting && !state.isPreviewing,
                modifier = Modifier.fillMaxWidth().height(tokens.controlHeight),
                shape = MaterialTheme.shapes.medium
            ) { Text(if (state.isPreviewing) "در حال پردازش..." else "پیش‌نمایش", fontWeight = FontWeight.Bold) }
        }
        state.error?.let { item { ErrorText(it) } }
    }
}

@Composable
private fun BulkImportPreview(state: BulkImportUiState, onBack: () -> Unit, onRefresh: () -> Unit, onImport: () -> Unit) {
    val tokens = LocalFlashLearnThemeTokens.current
    var duplicateOnly by remember(state.done) { mutableStateOf(false) }
    val results = if (state.done) state.results else state.preview.map { entry ->
        BulkImportItemResult(entry, if (entry.sourceText.isBlank() || entry.translationText.isNullOrBlank()) BulkImportItemStatus.INCOMPLETE else if (entry.confidence < com.flashlearn.domain.usecase.ImportParsedEntryUseCase.LOW_CONFIDENCE_THRESHOLD) BulkImportItemStatus.NEEDS_REVIEW else BulkImportItemStatus.READY)
    }
    val validCount = results.count { it.status != BulkImportItemStatus.INCOMPLETE && it.status != BulkImportItemStatus.DUPLICATE }
    val visibleResults = if (duplicateOnly) results.filter { it.status == BulkImportItemStatus.DUPLICATE } else results

    Column(Modifier.fillMaxSize()) {
        Surface(color = tokens.surface, tonalElevation = tokens.dp(1f)) {
            Column(Modifier.fillMaxWidth().padding(horizontal = tokens.screenPadding, vertical = tokens.dp(16f)), verticalArrangement = Arrangement.spacedBy(tokens.compactGap)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    OutlinedButton(onClick = onBack, modifier = Modifier.height(tokens.controlHeight), shape = MaterialTheme.shapes.medium) { Text("بازگشت") }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("لغات گروهی", style = MaterialTheme.typography.headlineSmall, color = tokens.onSurface)
                        Text("پیش‌نمایش (${results.size} مورد)", style = MaterialTheme.typography.bodyMedium, color = tokens.onSurfaceVariant)
                    }
                }
                Text("موارد تشخیص‌داده‌شده را بررسی کن و سپس همه موارد را وارد کن.", Modifier.fillMaxWidth(), color = tokens.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.End)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    FilterChip(selected = !duplicateOnly, onClick = { duplicateOnly = false }, label = { Text("همه کلمات") })
                    Spacer(Modifier.width(tokens.compactGap))
                    FilterChip(selected = duplicateOnly, onClick = { duplicateOnly = true }, enabled = state.done, label = { Text("کلمات تکراری (${state.skippedDuplicateCount})") })
                }
            }
        }

        if (state.warnings.isNotEmpty()) {
            Card(Modifier.fillMaxWidth().padding(horizontal = tokens.screenPadding, vertical = tokens.dp(10f)), shape = MaterialTheme.shapes.medium, colors = CardDefaults.cardColors(containerColor = tokens.error.copy(alpha = .10f)), border = BorderStroke(tokens.dp(1f), tokens.error.copy(alpha = .25f))) {
                Column(Modifier.fillMaxWidth().padding(tokens.dp(16f)), verticalArrangement = Arrangement.spacedBy(tokens.compactGap), horizontalAlignment = Alignment.End) {
                    Text("گزارش هشدارها و موارد حل‌نشده (${state.warnings.size})", Modifier.fillMaxWidth(), color = tokens.error, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                    state.warnings.forEach { warning -> Text("خط ${warning.lineNumber} • ${warning.warningType} • ${warning.message}\n${warning.rawText}", Modifier.fillMaxWidth(), color = tokens.onSurfaceVariant, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.End) }
                }
            }
        }

        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(horizontal = tokens.screenPadding, vertical = tokens.dp(4f)), verticalArrangement = Arrangement.spacedBy(tokens.itemGap)) {
            items(visibleResults) { result -> PreviewEntryCard(result, state.lineNumbers[result.entry.rawLines.firstOrNull()?.trim()]) }
            if (visibleResults.isEmpty() && duplicateOnly) item { Text("هنوز مورد تکراری ثبت‌شده‌ای وجود ندارد.", Modifier.fillMaxWidth().padding(tokens.dp(24f)), color = tokens.onSurfaceVariant, textAlign = TextAlign.Center) }
        }

        Surface(shape = MaterialTheme.shapes.large, tonalElevation = tokens.dp(4f), shadowElevation = tokens.dp(4f), color = tokens.surface) {
            Column(Modifier.fillMaxWidth().padding(horizontal = tokens.screenPadding, vertical = tokens.dp(12f)), verticalArrangement = Arrangement.spacedBy(tokens.compactGap)) {
                if (!state.done) Text("قابل ورود: $validCount  •  ناقص: ${results.size - validCount}", Modifier.fillMaxWidth(), color = tokens.onSurface, textAlign = TextAlign.End, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                else Text("نتیجه: ${state.importedCount} جدید  •  ${state.skippedDuplicateCount} تکراری  •  ${state.invalidCount} ناقص  •  ${state.needsReviewCount} نیازمند بررسی  •  ${state.failedCount} خطادار", Modifier.fillMaxWidth(), color = tokens.onSurface, textAlign = TextAlign.End, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Button(onClick = onImport, enabled = !state.isImporting && !state.isPreviewing && !state.done && !duplicateOnly, modifier = Modifier.fillMaxWidth().height(tokens.controlHeight), shape = MaterialTheme.shapes.medium) {
                    Icon(Icons.Outlined.Upload, null); Spacer(Modifier.width(tokens.compactGap)); Text(if (state.isImporting) "در حال وارد کردن..." else "Import همه (${results.size})", fontWeight = FontWeight.Bold)
                }
                state.error?.let { ErrorText(it) }
            }
        }
    }
}

@Composable
private fun PreviewEntryCard(result: BulkImportItemResult, lineNumber: Int?) {
    val tokens = LocalFlashLearnThemeTokens.current
    val bad = result.status == BulkImportItemStatus.INCOMPLETE || result.status == BulkImportItemStatus.FAILED
    Card(
        Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = tokens.cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = tokens.dp(1f)),
        border = BorderStroke(tokens.dp(1f), if (bad) tokens.error.copy(alpha = .25f) else tokens.outlineColor)
    ) {
        Row(Modifier.fillMaxWidth().padding(tokens.dp(12f)), verticalAlignment = Alignment.Top) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(tokens.dp(40f))) {
                Surface(Modifier.fillMaxSize(), shape = MaterialTheme.shapes.small, color = if (bad) tokens.error.copy(alpha = .10f) else tokens.primary.copy(alpha = .10f)) {
                    Box(contentAlignment = Alignment.Center) { Text(if (bad) "!" else "✓", color = if (bad) tokens.error else tokens.primary, fontWeight = FontWeight.Bold) }
                }
            }
            Spacer(Modifier.width(tokens.compactGap))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(tokens.compactGap), horizontalAlignment = Alignment.End) {
                lineNumber?.let { Text("خط $it", Modifier.fillMaxWidth(), style = MaterialTheme.typography.labelSmall, color = tokens.primary, textAlign = TextAlign.End) }
                OutlinedTextField(value = result.entry.sourceText, onValueChange = {}, readOnly = true, modifier = Modifier.fillMaxWidth(), textStyle = MaterialTheme.typography.titleMedium, shape = MaterialTheme.shapes.small, singleLine = true, colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = tokens.surface, focusedContainerColor = tokens.surface, unfocusedBorderColor = tokens.outlineColor, focusedBorderColor = tokens.primary))
                OutlinedTextField(value = result.entry.translationText.orEmpty(), onValueChange = {}, readOnly = true, modifier = Modifier.fillMaxWidth(), textStyle = MaterialTheme.typography.bodyLarge, shape = MaterialTheme.shapes.small, minLines = 1, maxLines = 2, colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = tokens.surface, focusedContainerColor = tokens.surface, unfocusedBorderColor = tokens.outlineColor, focusedBorderColor = tokens.primary))
                result.message?.let { Text(it, Modifier.fillMaxWidth(), color = if (result.status == BulkImportItemStatus.FAILED || result.status == BulkImportItemStatus.DUPLICATE) tokens.error else tokens.onSurfaceVariant, textAlign = TextAlign.End, style = MaterialTheme.typography.labelSmall) }
            }
        }
    }
}

@Composable
private fun ErrorText(message: String) {
    val tokens = LocalFlashLearnThemeTokens.current
    Text(message, Modifier.fillMaxWidth(), color = tokens.error, textAlign = TextAlign.End, style = MaterialTheme.typography.bodySmall)
}
