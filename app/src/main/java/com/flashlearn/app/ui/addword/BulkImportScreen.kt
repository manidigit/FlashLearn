package com.flashlearn.app.ui.addword

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.FileOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.flashlearn.app.ui.LanguagePair
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

@Composable
fun BulkImportScreen(
    viewModel: BulkImportViewModel,
    languagePair: LanguagePair = LanguagePair(),
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.resetForEntry(languagePair)
    }
    LaunchedEffect(languagePair) {
        viewModel.setLanguagePair(languagePair)
    }

    val openTextFile = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            scope.launch {
                val text = runCatching {
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.use { input ->
                            BufferedReader(InputStreamReader(input, Charsets.UTF_8)).readText()
                        } ?: error("فایل قابل خواندن نیست")
                    }
                }.getOrElse {
                    viewModel.showError(it.message ?: "خطا در خواندن فایل")
                    return@launch
                }
                viewModel.onTextChange(text)
            }
        }
    }

    val results = remember(state.results, state.preview, state.done) {
        if (state.done && state.results.isNotEmpty()) {
            state.results
        } else {
            state.preview.map(viewModel::readyResultForDisplay)
        }
    }
    val validCount = remember(state.preview) {
        state.preview.count { it.sourceText.isNotBlank() && !it.translationText.isNullOrBlank() }
    }
    val incompleteCount = state.preview.size - validCount
    val visibleResults = results.take(BulkImportViewModel.PREVIEW_LIMIT)

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "بازگشت")
            }
            Text(
                "ورود گروهی واژگان",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.End
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    "${languagePair.source.flag} ${languagePair.source.labelFa}  →  ${languagePair.target.flag} ${languagePair.target.labelFa}",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            item {
                Text(
                    "واژه‌ها را یکجا وارد کن؛ هر مدخل را به شکل «مبدأ → مقصد» یا در دو خط وارد کن.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            item {
                OutlinedButton(
                    onClick = { openTextFile.launch(arrayOf("text/plain", "text/csv", "text/*", "application/json", "*/*")) },
                    enabled = !state.isImporting,
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Icon(Icons.Outlined.FileOpen, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("انتخاب فایل واژگان")
                }
            }
            item {
                OutlinedTextField(
                    value = state.rawText,
                    onValueChange = viewModel::onTextChange,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 150.dp, max = 220.dp),
                    label = { Text("متن واژگان") },
                    minLines = 5,
                    maxLines = 9
                )
            }
            item {
                Button(
                    onClick = viewModel::preview,
                    enabled = !state.isImporting && state.rawText.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text("پیش‌نمایش")
                }
            }

            if (state.preview.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column(
                            Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("پیش‌نمایش", style = MaterialTheme.typography.titleMedium)
                            Text("${state.preview.size} مدخل تشخیص داده شد")
                            Text("قابل ورود: $validCount  •  ناقص: $incompleteCount")
                            if (results.size > BulkImportViewModel.PREVIEW_LIMIT) {
                                Text(
                                    "${BulkImportViewModel.PREVIEW_LIMIT} مورد اول نمایش داده می‌شود؛ ورود همه موارد همچنان انجام می‌شود.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                if (state.warnings.isNotEmpty()) {
                    item {
                        Text(
                            "هشدارهای Parser: ${state.warnings.size}",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                items(visibleResults) { result ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(
                            Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(
                                "${result.entry.sourceText}  →  ${result.entry.translationText ?: "بدون ترجمه"}",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.End
                            )
                            Text(
                                buildString {
                                    append(result.status.label())
                                    append("  •  ")
                                    append(result.confidencePercent)
                                    append('%')
                                    result.message?.let { append("  •  ").append(it) }
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                item {
                    Button(
                        onClick = viewModel::importAll,
                        enabled = !state.isImporting,
                        modifier = Modifier.fillMaxWidth().height(54.dp)
                    ) {
                        Text(if (state.isImporting) "در حال وارد کردن..." else "وارد کردن همه")
                    }
                }
            }

            if (state.done) {
                item {
                    Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("نتیجه ورود", style = MaterialTheme.typography.titleMedium)
                            Text("${state.importedCount} مدخل جدید")
                            Text("${state.skippedDuplicateCount} تکراری  •  ${state.invalidCount} ناقص  •  ${state.failedCount} خطادار")
                        }
                    }
                }
            }
            state.error?.let { error ->
                item {
                    Text(error, color = MaterialTheme.colorScheme.error)
                }
            }
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
