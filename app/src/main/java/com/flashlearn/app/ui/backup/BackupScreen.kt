package com.flashlearn.app.ui.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.FileOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.io.BufferedReader
import java.io.InputStreamReader

@Composable
fun BackupScreen(viewModel: BackupViewModel, onBack: () -> Unit, onRestored: () -> Unit = {}) {
    val state by viewModel.state.collectAsState()
    var pendingJson by remember { mutableStateOf<String?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val save = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        val json = pendingJson
        if (uri != null && !json.isNullOrBlank()) {
            runCatching { context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray(Charsets.UTF_8)) } }
                .onFailure { viewModel.showMessage("ذخیره فایل ناموفق بود: ${it.message ?: "خطای دسترسی"}") }
        }
        pendingJson = null
    }
    val open = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openInputStream(uri)?.use { BufferedReader(InputStreamReader(it, Charsets.UTF_8)).readText() }
                    ?: error("فایل قابل خواندن نیست")
            }.onSuccess { json ->
                if (json.trimStart().startsWith("{")) viewModel.restore(json, onRestored)
                else viewModel.showMessage("این فایل پشتیبان معتبر JSON نیست.")
            }.onFailure { viewModel.showMessage("خواندن فایل ناموفق بود: ${it.message ?: "خطای دسترسی"}") }
        }
    }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("پشتیبان‌گیری و بازیابی", style = MaterialTheme.typography.headlineSmall)
            IconButton(onClick = onBack, enabled = !state.busy) { Icon(Icons.Outlined.ArrowBack, contentDescription = "بازگشت") }
        }
        Column(Modifier.fillMaxWidth().weight(1f).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("پشتیبان کامل", style = MaterialTheme.typography.titleLarge)
                    Text("واژگان، پیشرفت، سابقه مرور، تنظیمات، برچسب‌ها، دسته‌بندی‌ها، فراداده Parser و دستاوردها.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Button(onClick = { viewModel.export() }, enabled = !state.busy, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Icon(Icons.Outlined.CloudUpload, null); Spacer(Modifier.width(8.dp)); Text("ساخت پشتیبان")
            }
            OutlinedButton(onClick = { open.launch(arrayOf("application/json", "text/plain", "*/*")) }, enabled = !state.busy, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Icon(Icons.Outlined.FileOpen, null); Spacer(Modifier.width(8.dp)); Text("انتخاب فایل پشتیبان برای بازیابی")
            }
            state.message?.let { Text(it, color = if (it.contains("ناموفق") || it.contains("معتبر")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary) }
            state.exportedJson?.let { json ->
                Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("فایل JSON آماده است", style = MaterialTheme.typography.titleMedium)
                        Text("${json.length} نویسه", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Button(onClick = { pendingJson = json; save.launch("flashlearn-backup.json") }, enabled = !state.busy) { Text("ذخیره فایل JSON") }
                    }
                }
            }
        }
    }
}
