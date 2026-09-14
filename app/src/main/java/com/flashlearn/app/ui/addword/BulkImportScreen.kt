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
fun BulkImportScreen(
    viewModel: BulkImportViewModel,
    languagePair: LanguagePair = LanguagePair(),
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { viewModel.resetForEntry(languagePair) }

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

    if (state.preview.isNotEmpty() || state.done) {
        BulkImportPreview(
            state = state,
            onBack = {
                viewModel.resetForEntry(languagePair)
                onBack()
            },
            onImport = viewModel::importAll
        )
    } else {
        BulkImportEditor(
            state = state,
            languagePair = languagePair,
            onBack = onBack,
            onTextChange = viewModel::onTextChange,
            onPreview = viewModel::preview,
            onPickFile = { openTextFile.launch(arrayOf("text/plain", "text/csv", "text/*", "application/json", "*/*")) }
        )
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
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 24.dp, top = 20.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = "بازگشت")
                }
                Text(
                    "جای‌گذاری متن",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.End
                )
            }
        }
        item {
            Text(
                "${languagePair.source.flag} ${languagePair.source.labelFa}  →  ${languagePair.target.flag} ${languagePair.target.labelFa}",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                textAlign = TextAlign.End
            )
        }
        item {
            Text(
                "چند کلمه را با فرمت: متن مبدأ / ترجمه / (اختیاری) دسته، هر مورد در یک بلوک جدا با خط خالی، Paste کنید.",
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.End
            )
        }
        item {
            OutlinedButton(
                onClick = onPickFile,
                enabled = !state.isImporting && !state.isPreviewing,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(28.dp)
            ) {
                Icon(Icons.Outlined.FileOpen, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("انتخاب فایل واژگان", fontWeight = FontWeight.SemiBold)
            }
        }
        item {
            OutlinedTextField(
                value = state.rawText,
                onValueChange = onTextChange,
                modifier = Modifier.fillMaxWidth().heightIn(min = 230.dp, max = 360.dp),
                label = { Text("متن واژگان") },
                placeholder = { Text("la piedra\nسنگ\n\nel nivel\nسطح، درجه") },
                minLines = 8,
                maxLines = 16,
                shape = RoundedCornerShape(20.dp)
            )
        }
        item {
            Button(
                onClick = onPreview,
                enabled = state.rawText.isNotBlank() && !state.isImporting && !state.isPreviewing,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(if (state.isPreviewing) "در حال پردازش..." else "پیش‌نمایش", fontWeight = FontWeight.Bold)
            }
        }
        state.error?.let { error ->
            item {
                Text(error, modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.error, textAlign = TextAlign.End)
            }
        }
    }
}

@Composable
private fun BulkImportPreview(
    state: BulkImportUiState,
    onBack: () -> Unit,
    onImport: () -> Unit
) {
    val results = if (state.done) state.results else state.preview.map { entry ->
        BulkImportItemResult(
            entry,
            if (entry.sourceText.isBlank() || entry.translationText.isNullOrBlank()) BulkImportItemStatus.INCOMPLETE else BulkImportItemStatus.READY
        )
    }
    val validCount = results.count { it.status != BulkImportItemStatus.INCOMPLETE }
    val visibleResults = results.take(BulkImportViewModel.PREVIEW_LIMIT)

    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(onClick = onBack, shape = RoundedCornerShape(24.dp)) {
                Text("بازگشت")
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("جای‌گذاری متن", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))
                Text(
                    "پیش‌نمایش (${results.size} مورد)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }

        Text(
            "موارد تشخیص‌داده‌شده را بررسی کن و سپس همه موارد را وارد کن.",
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 14.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(visibleResults) { result -> PreviewEntryCard(result) }
            if (results.size > BulkImportViewModel.PREVIEW_LIMIT) {
                item {
                    Text(
                        "فقط ${BulkImportViewModel.PREVIEW_LIMIT} مورد اول برای پیش‌نمایش نمایش داده می‌شود؛ همه ${results.size} مورد وارد خواهند شد.",
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End
                    )
                }
            }
        }

        Surface(shadowElevation = 6.dp) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!state.done) {
                    Text(
                        "قابل ورود: $validCount  •  ناقص: ${results.size - validCount}",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    Text(
                        "نتیجه: ${state.importedCount} جدید  •  ${state.skippedDuplicateCount} تکراری  •  ${state.invalidCount} ناقص",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Button(
                    onClick = onImport,
                    enabled = !state.isImporting && !state.isPreviewing && !state.done,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Icon(Icons.Outlined.Upload, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (state.isImporting) "در حال وارد کردن..." else "Import همه (${results.size})", fontWeight = FontWeight.Bold)
                }
                state.error?.let { error ->
                    Text(error, modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.error, textAlign = TextAlign.End)
                }
            }
        }
    }
}

@Composable
private fun PreviewEntryCard(result: BulkImportItemResult) {
    val incomplete = result.status == BulkImportItemStatus.INCOMPLETE
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                if (incomplete) "!" else "✓",
                modifier = Modifier.size(42.dp).background(
                    if (incomplete) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                    RoundedCornerShape(12.dp)
                ).wrapContentHeight(Alignment.CenterVertically),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                OutlinedTextField(
                    value = result.entry.sourceText,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.titleMedium,
                    shape = RoundedCornerShape(14.dp)
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = result.entry.translationText.orEmpty(),
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyLarge,
                    shape = RoundedCornerShape(14.dp)
                )
            }
        }
    }
}
