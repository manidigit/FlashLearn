package com.flashlearn.app.ui.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.FileOpen
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.SettingsBackupRestore
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.flashlearn.domain.backup.BackupType
import com.flashlearn.domain.repository.ExportFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

private val BackupPurple = Color(0xFF7C4DFF)
private val BackupBlue = Color(0xFF4F7CFF)
private val BackupGreen = Color(0xFF19A974)

@Composable
fun BackupScreen(viewModel: BackupViewModel, onBack: () -> Unit, onRestored: () -> Unit = {}) {
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
        if (uri != null) scope.launch(Dispatchers.IO) { runCatching { context.contentResolver.openInputStream(uri)?.use { BufferedReader(InputStreamReader(it, Charsets.UTF_8)).readText() } ?: error("فایل قابل خواندن نیست") }.onSuccess { json -> if (json.trimStart().startsWith("{")) viewModel.restore(json, onRestored) else viewModel.showMessage("این فایل پشتیبان معتبر JSON نیست.") }.onFailure { viewModel.showMessage("خواندن فایل ناموفق بود: ${it.message ?: "خطا"}") } }
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Box(Modifier.fillMaxWidth().height(56.dp)) {
            IconButton(onClick = onBack, enabled = !state.busy, modifier = Modifier.align(Alignment.CenterEnd)) { Icon(Icons.Outlined.ArrowBack, "بازگشت") }
            Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("پشتیبان‌گیری و بازیابی", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("حفظ واژه‌ها، پیشرفت و تنظیمات", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = BackupPurple.copy(alpha = .09f))) {
            Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(Modifier.size(56.dp), shape = RoundedCornerShape(18.dp), color = BackupPurple.copy(alpha = .16f)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Outlined.SettingsBackupRestore, null, tint = BackupPurple, modifier = Modifier.size(30.dp)) } }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text("بازیابی از فایل پشتیبان", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                    Text("یک فایل JSON قبلی را انتخاب کن و اطلاعات موجود را بازیابی کن.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.End)
                }
            }
        }

        Text("نوع پشتیبان", Modifier.fillMaxWidth(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BackupType.entries.forEach { type ->
                val label = when (type) { BackupType.VOCABULARY -> "واژگان"; BackupType.PROGRESS -> "پیشرفت و تنظیمات"; BackupType.FULL -> "کامل" }
                FilterChip(selected = false, onClick = { viewModel.export(type) }, enabled = !state.busy, label = { Text(label) }, leadingIcon = { Icon(if (type == BackupType.VOCABULARY) Icons.Outlined.Backup else Icons.Outlined.SettingsBackupRestore, null) })
            }
        }

        Button(onClick = { open.launch(arrayOf("application/json", "text/plain", "*/*")) }, enabled = !state.busy, modifier = Modifier.fillMaxWidth().height(58.dp), shape = RoundedCornerShape(29.dp), colors = ButtonDefaults.buttonColors(containerColor = BackupPurple)) {
            Icon(Icons.Outlined.FileOpen, null); Spacer(Modifier.width(8.dp)); Text("انتخاب فایل برای Import / Restore", fontWeight = FontWeight.Bold)
        }

        Text("خروجی داده", Modifier.fillMaxWidth(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ExportFormat.values().forEach { format -> OutlinedButton(onClick = { viewModel.exportData(format) }, enabled = !state.busy, modifier = Modifier.weight(1f), shape = RoundedCornerShape(22.dp)) { Text(format.name) } }
        }

        state.exportedJson?.let { json ->
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp)) { Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.End) { Text("${state.exportedType.name} آماده است", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); Text("${json.length} نویسه", color = MaterialTheme.colorScheme.onSurfaceVariant); Button(onClick = { pendingJson = json; save.launch("flashlearn-${state.exportedType.name.lowercase()}-backup.json") }, enabled = !state.busy) { Icon(Icons.Outlined.FileUpload, null); Spacer(Modifier.width(6.dp)); Text("ذخیره JSON") } } }
        }
        state.exportedFile?.let { file ->
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp)) { Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.End) { Text("خروجی ${state.exportedFormat?.name} آماده است", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); Text(file.name, color = MaterialTheme.colorScheme.onSurfaceVariant); Button(onClick = { pendingFile = file; saveData.launch(file.name) }, enabled = !state.busy) { Icon(Icons.Outlined.Upload, null); Spacer(Modifier.width(6.dp)); Text("ذخیره فایل") } } }
        }
        if (state.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        state.message?.let { Text(it, Modifier.fillMaxWidth(), color = if (it.contains("ناموفق") || it.contains("معتبر")) MaterialTheme.colorScheme.error else BackupGreen, textAlign = TextAlign.End) }
    }
}
