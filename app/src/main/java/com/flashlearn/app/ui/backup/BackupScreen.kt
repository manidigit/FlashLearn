package com.flashlearn.app.ui.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.flashlearn.app.ui.components.FlashLearnScreenHeader
import com.flashlearn.app.ui.theme.LocalFlashLearnThemeTokens
import com.flashlearn.domain.backup.BackupType
import com.flashlearn.domain.repository.ExportFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

@Composable
fun BackupScreen(viewModel: BackupViewModel, onBack: () -> Unit, onRestored: () -> Unit = {}) {
    val tokens = LocalFlashLearnThemeTokens.current
    val state by viewModel.state.collectAsState()
    var pendingJson by remember { mutableStateOf<String?>(null) }
    var pendingFile by remember { mutableStateOf<File?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val save = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        val json = pendingJson
        if (uri != null && !json.isNullOrBlank()) scope.launch(Dispatchers.IO) {
            runCatching { context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) } }
                .onFailure { viewModel.showMessage("ذخیره فایل ناموفق بود: ${it.message ?: "خطا"}") }
        }
        pendingJson = null
    }
    val saveData = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        val file = pendingFile
        if (uri != null && file != null) scope.launch(Dispatchers.IO) {
            runCatching { file.inputStream().use { input -> context.contentResolver.openOutputStream(uri)?.use { output -> input.copyTo(output) } } }
                .onFailure { viewModel.showMessage("ذخیره خروجی ناموفق بود: ${it.message ?: "خطا"}") }
        }
        pendingFile = null
    }
    val open = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch(Dispatchers.IO) {
            runCatching { context.contentResolver.openInputStream(uri)?.use { BufferedReader(InputStreamReader(it, Charsets.UTF_8)).readText() } ?: error("فایل قابل خواندن نیست") }
                .map { it.removePrefix("\uFEFF").trimStart() }
                .onSuccess { json -> if (json.startsWith("{")) viewModel.restore(json, onRestored) else viewModel.showMessage("این فایل پشتیبان معتبر JSON نیست.") }
                .onFailure { viewModel.showMessage("خواندن فایل ناموفق بود: ${it.message ?: "خطا"}") }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = tokens.screenPadding, vertical = tokens.compactGap),
        verticalArrangement = Arrangement.spacedBy(tokens.contentGap)
    ) {
        item {
            FlashLearnScreenHeader(
                title = "پشتیبان‌گیری و بازیابی",
                subtitle = "حفظ واژه‌ها، پیشرفت و تنظیمات",
                onBack = onBack
            )
        }
        item { SectionHeader(icon = Icons.Outlined.SettingsBackupRestore, title = "بازیابی", subtitle = "اطلاعات قبلی را از یک فایل پشتیبان JSON وارد کن.") }
        item {
            Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, colors = CardDefaults.cardColors(containerColor = tokens.primary.copy(alpha = .08f))) {
                Column(Modifier.fillMaxWidth().padding(tokens.contentGap), verticalArrangement = Arrangement.spacedBy(tokens.compactGap), horizontalAlignment = Alignment.Start) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Surface(Modifier.size(tokens.iconLarge.plus(tokens.compactGap)), shape = MaterialTheme.shapes.medium, color = tokens.primary.copy(alpha = .14f)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Outlined.FileOpen, null, tint = tokens.primary, modifier = Modifier.size(tokens.iconLarge)) } }
                        Spacer(Modifier.width(tokens.contentGap))
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                            Text("بازیابی از فایل پشتیبان", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.Start)
                            Text("یک فایل JSON قبلی را انتخاب کن. نوع پشتیبان از خود فایل تشخیص داده می‌شود.", style = MaterialTheme.typography.bodySmall, color = tokens.onSurfaceVariant, textAlign = TextAlign.Start)
                        }
                    }
                    Button(onClick = { open.launch(arrayOf("application/json", "text/plain", "*/*")) }, enabled = !state.busy, modifier = Modifier.fillMaxWidth().height(tokens.controlHeight), shape = MaterialTheme.shapes.large, colors = ButtonDefaults.buttonColors(containerColor = tokens.primary)) {
                        Icon(Icons.Outlined.FileOpen, null); Spacer(Modifier.width(tokens.compactGap)); Text("انتخاب فایل پشتیبان", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        item { HorizontalDivider(modifier = Modifier.padding(vertical = tokens.compactGap)) }
        item { SectionHeader(icon = Icons.Outlined.Backup, title = "پشتیبان‌گیری", subtitle = "نوع اطلاعاتی را که می‌خواهی ذخیره شود انتخاب کن.") }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(tokens.compactGap)) {
                BackupType.entries.forEach { type ->
                    val label = when (type) { BackupType.VOCABULARY -> "واژگان"; BackupType.PROGRESS -> "پیشرفت و تنظیمات"; BackupType.FULL -> "پشتیبان کامل" }
                    OutlinedButton(onClick = { viewModel.export(type) }, enabled = !state.busy, modifier = Modifier.fillMaxWidth().height(tokens.controlHeight), shape = MaterialTheme.shapes.medium) {
                        Icon(if (type == BackupType.VOCABULARY) Icons.Outlined.MenuBook else Icons.Outlined.SettingsBackupRestore, null); Spacer(Modifier.width(tokens.compactGap)); Text(label, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        item { SectionHeader(icon = Icons.Outlined.FileDownload, title = "خروجی داده", subtitle = "خروجی قابل استفاده در قالب‌های مختلف دریافت کن.") }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.compactGap)) {
                ExportFormat.values().forEach { format ->
                    OutlinedButton(onClick = { viewModel.exportData(format) }, enabled = !state.busy, modifier = Modifier.weight(1f).height(tokens.controlHeight), shape = MaterialTheme.shapes.medium) { Text(format.name, fontWeight = FontWeight.Bold) }
                }
            }
        }
        state.exportedJson?.let { json -> item { ResultCard(title = "${state.exportedType.name} آماده است", detail = "${json.length} نویسه") { Button(onClick = { pendingJson = json; save.launch("flashlearn-${state.exportedType.name.lowercase()}-backup.json") }, enabled = !state.busy) { Icon(Icons.Outlined.FileUpload, null); Spacer(Modifier.width(tokens.compactGap)); Text("ذخیره JSON") } } } }
        state.exportedFile?.let { file -> item { ResultCard(title = "خروجی ${state.exportedFormat?.name} آماده است", detail = file.name) { Button(onClick = { pendingFile = file; saveData.launch(file.name) }, enabled = !state.busy) { Icon(tokens.icons.add, null); Spacer(Modifier.width(tokens.compactGap)); Text("ذخیره فایل") } } } }
        if (state.busy) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
        state.message?.let { message -> item { Text(message, Modifier.fillMaxWidth(), color = if (message.contains("ناموفق") || message.contains("معتبر")) tokens.error else tokens.success, textAlign = TextAlign.Start) } }
    }
}

@Composable private fun SectionHeader(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    val tokens = LocalFlashLearnThemeTokens.current
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = tokens.primary, modifier = Modifier.size(tokens.iconMedium)); Spacer(Modifier.width(tokens.compactGap))
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.Start) { Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.Start); Text(subtitle, style = MaterialTheme.typography.bodySmall, color = tokens.onSurfaceVariant, textAlign = TextAlign.Start) }
    }
}

@Composable private fun ResultCard(title: String, detail: String, action: @Composable () -> Unit) {
    val tokens = LocalFlashLearnThemeTokens.current
    Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
        Row(Modifier.fillMaxWidth().padding(tokens.contentGap), verticalAlignment = Alignment.CenterVertically) { action(); Spacer(Modifier.width(tokens.compactGap)); Column(Modifier.weight(1f), horizontalAlignment = Alignment.Start) { Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Start); Text(detail, color = tokens.onSurfaceVariant, textAlign = TextAlign.Start) } }
    }
}
