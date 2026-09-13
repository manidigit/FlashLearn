package com.flashlearn.app.ui.addword

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BulkImportScreen(viewModel: BulkImportViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsState()
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("ورود گروهی واژگان", style = MaterialTheme.typography.headlineMedium)
        Text("هر مدخل را به شکل «اسپانیایی → فارسی» یا در دو خط وارد کنید.")
        OutlinedTextField(
            value = state.rawText,
            onValueChange = viewModel::onTextChange,
            label = { Text("متن واژگان") },
            modifier = Modifier.fillMaxWidth().weight(1f)
        )
        Button(onClick = viewModel::preview, enabled = !state.isImporting && state.rawText.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("پیش‌نمایش") }
        if (state.preview.isNotEmpty()) {
            val validCount = state.preview.count { it.sourceText.isNotBlank() && !it.translationText.isNullOrBlank() }
            val incompleteCount = state.preview.size - validCount
            val noteCount = state.preview.count { !it.notes.isNullOrBlank() }
            val breakdownCount = state.preview.sumOf { it.breakdown.size }
            val relationshipCount = state.preview.sumOf { it.relationships.size }
            val variantCount = state.preview.sumOf { it.variants.size }
            Text("تعداد تشخیص‌داده‌شده: ${state.preview.size}")
            Text("قابل ورود: $validCount | ناقص: $incompleteCount | دارای یادداشت: $noteCount")
            Text("Metadata: تجزیه $breakdownCount | ارتباط $relationshipCount | شکل‌های دیگر $variantCount")
            if (state.warnings.isNotEmpty()) {
                Text("هشدارهای Parser: ${state.warnings.size}", color = MaterialTheme.colorScheme.error)
                LazyColumn(modifier = Modifier.weight(0.45f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(state.warnings) { warning ->
                        Text("خط ${warning.lineNumber}: ${warning.message} — ${warning.rawText}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(if (state.done && state.results.isNotEmpty()) state.results else state.preview.map {
                    BulkImportItemResult(
                        it,
                        if (it.sourceText.isBlank() || it.translationText.isNullOrBlank()) BulkImportItemStatus.INCOMPLETE
                        else BulkImportItemStatus.READY
                    )
                }) { itemResult ->
                    val item = itemResult.entry
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("${item.sourceText} → ${item.translationText ?: "بدون ترجمه"}")
                        Text(
                            "وضعیت: ${itemResult.status.label()} | نوع: ${item.entryType} | اطمینان Parser: ${itemResult.confidencePercent}%",
                            style = MaterialTheme.typography.labelSmall
                        )
                        if (itemResult.breakdownCount + itemResult.relationshipCount + itemResult.variantCount > 0) {
                            Text(
                                "تجزیه: ${itemResult.breakdownCount} | ارتباط: ${itemResult.relationshipCount} | شکل/مشتق: ${itemResult.variantCount}",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        itemResult.message?.let {
                            Text(it, style = MaterialTheme.typography.bodySmall)
                        }
                        item.notes?.takeIf { it.isNotBlank() }?.let {
                            Text("یادداشت: $it", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            Button(onClick = viewModel::importAll, enabled = !state.isImporting, modifier = Modifier.fillMaxWidth()) {
                Text(if (state.isImporting) "در حال وارد کردن..." else "وارد کردن همه")
            }
        }
        if (state.done) {
            Text("نتیجه ورود: ${state.importedCount} مدخل جدید، ${state.skippedDuplicateCount} تکراری رد شد، ${state.invalidCount} مدخل ناقص رد شد.")
        }
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        OutlinedButton(onClick = onBack, enabled = !state.isImporting, modifier = Modifier.fillMaxWidth()) { Text("بازگشت") }
    }
}

fun BulkImportItemStatus.label() = when (this) {
    BulkImportItemStatus.READY -> "آماده"
    BulkImportItemStatus.INCOMPLETE -> "ناقص"
    BulkImportItemStatus.IMPORTED -> "وارد شد"
    BulkImportItemStatus.DUPLICATE -> "تکراری"
    BulkImportItemStatus.FAILED -> "خطا"
}
