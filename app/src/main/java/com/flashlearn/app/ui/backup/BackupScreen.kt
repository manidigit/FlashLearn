package com.flashlearn.app.ui.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
        if (uri != null && !json.isNullOrBlank()) scope.launch(Dispatchers.IO) { runCatching { context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) } }.onFailure { viewModel.showMessage("ذخیره فایل ناموفق بود: ${it.message ?: "خطا"}") } }
        pendingJson = null
    }
    val saveData = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        val file = pendingFile
        if (uri != null && file != null) scope.launch(Dispatchers.IO) { runCatching { file.inputStream().use { input -> context.contentResolver.openOutputStream(uri)?.use { output -> input.copyTo(output) } } }.onFailure { viewModel.showMessage("ذخیره خروجی ناموفق بود: ${it.message ?: "خطا"}") } }
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
    Column(Modifier.fillMaxSize().padding(horizontal = tokens.screenPadding, vertical = tokens.compactGap), verticalArrangement = Arrangement.spacedBy(tokens.contentGap)) {
        Box(Modifier.fillMaxWidth().height(tokens.navHeight)) {
            IconButton(onClick = onBack, enabled = !state.busy, modifier = Modifier.align(Alignment.CenterEnd)) { Icon(Icons.Outlined.ArrowBack, "بازگشت") }
            Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("پشتیبان‌گیری و بازیابی", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("حفظ واژه‌ها، پیشرفت و تنظیمات", style = MaterialTheme.typography.labelSmall, color = tokens.onSurfaceVariant)
            }
        }
        Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, colors = CardDefaults.cardColors(containerColor = tokens.primary.copy(alpha = .09f))) {
            Row(Modifier.fillMaxWidth().padding(tokens.contentGap), verticalAlignment = Alignment.CenterVertically) {
                Surface(Modifier.size(tokens.iconLarge.plus(tokens.compactGap)), shape = MaterialTheme.shapes.medium, color = tokens.primary.copy(alpha = .16f)) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Outlined.SettingsBackupRestore, null, tint = tokens.primary, modifier = Modifier.size(tokens.iconLarge)) }
                }
                Spacer(Modifier.width(tokens.contentGap))
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text("بازیابی از فایل پشتیبان", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                    Text("یک فایل JSON قبلی را انتخاب کن و اطلاعات موجود را بازیابی کن.", style = MaterialTheme.typography.bodySmall, color = tokens.onSurfaceVariant, textAlign = TextAlign.End)
                }
            }
        }
        Text("نوع پشتیبان", Modifier.fillMaxWidth(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.compactGap)) {
            BackupType.entries.forEach { type ->
                val label = when (type) { BackupType.VOCABULARY -> "واژگان"; BackupType.PROGRESS -> "پیشرفت و تنظیمات"; BackupType.FULL -> "کامل" }
                FilterChip(selected = false, onClick = { viewModel.export(type) }, enabled = !state.busy, label = { Text(label) }, leadingIcon = { Icon(if (type == BackupType.VOCABULARY) Icons.Outlined.Backup else Icons.Outlined.SettingsBackupRestore, null) })
            }
        }
        Button(onClick = { open.launch(arrayOf("application/json", "text/plain", "*/*")) }, enabled = !state.busy, modifier = Modifier.fillMaxWidth().height(tokens.controlHeight), shape = MaterialTheme.shapes.large, colors = ButtonDefaults.buttonColors(containerColor = tokens.primary)) {
            Icon(Icons.Outlined.FileOpen, null); Spacer(Modifier.width(tokens.compactGap)); Text("انتخاب فایل برای Import / Restore", fontWeight = FontWeight.Bold)
        }
        Text("خروجی داده", Modifier.fillMaxWidth(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.compactGap)) {
            ExportFormat.values().forEach { format -> OutlinedButton(onClick = { viewModel.exportData(format) }, enabled = !state.busy, modifier = Modifier.weight(1f), shape = MaterialTheme.shapes.medium) { Text(format.name) } }
        }
        state.exportedJson?.let { json -> Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) { Column(Modifier.fillMaxWidth().padding(tokens.contentGap), verticalArrangement = Arrangement.spacedBy(tokens.compactGap), horizontalAlignment = Alignment.End) { Text("${state.exportedType.name} آماده است", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); Text("${json.length} نویسه", color = tokens.onSurfaceVariant); Button(onClick = { pendingJson = json; save.launch("flashlearn-${state.exportedType.name.lowercase()}-backup.json") }, enabled = !state.busy) { Icon(Icons.Outlined.FileUpload, null); Spacer(Modifier.width(tokens.compactGap)); Text("ذخیره JSON") } } } }
        state.exportedFile?.let { file -> Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) { Column(Modifier.fillMaxWidth().padding(tokens.contentGap), verticalArrangement = Arrangement.spacedBy(tokens.compactGap), horizontalAlignment = Alignment.End) { Text("خروجی ${state.exportedFormat?.name} آماده است", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); Text(file.name, color = tokens.onSurfaceVariant); Button(onClick = { pendingFile = file; saveData.launch(file.name) }, enabled = !state.busy) { Icon(Icons.Outlined.Upload, null); Spacer(Modifier.width(tokens.compactGap)); Text("ذخیره فایل") } } } }
        if (state.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        state.message?.let { Text(it, Modifier.fillMaxWidth(), color = if (it.contains("ناموفق") || it.contains("معتبر")) tokens.error else tokens.success, textAlign = TextAlign.End) }
    }
}
