package com.flashlearn.app.ui.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.CloudUpload
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
        if (uri != null && pendingJson != null) context.contentResolver.openOutputStream(uri)?.use { it.write(pendingJson!!.toByteArray()) }
    }
    val open = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val json = context.contentResolver.openInputStream(uri)?.use { BufferedReader(InputStreamReader(it)).readText() }
            if (json != null) viewModel.restore(json, onRestored)
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
                    Text("پشتیبان‌گیری و بازیابی", style = MaterialTheme.typography.titleLarge)
                    Text("پشتیبان کامل شامل واژگان، پیشرفت یادگیری، سابقه مرور، تنظیمات، برچسب‌ها، دسته‌بندی‌ها، فراداده Parser و دستاوردهاست.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Button(onClick = { viewModel.export() }, enabled = !state.busy, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Icon(Icons.Outlined.CloudUpload, null); Spacer(Modifier.width(8.dp)); Text("ساخت پشتیبان")
            }
            OutlinedButton(onClick = { open.launch(arrayOf("application/json", "text/plain")) }, enabled = !state.busy, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Icon(Icons.Outlined.CloudDownload, null); Spacer(Modifier.width(8.dp)); Text("بازیابی پشتیبان")
            }
            state.message?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
            state.exportedJson?.let {
                Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("فایل JSON آماده است", style = MaterialTheme.typography.titleMedium)
                        Text("${it.length} نویسه", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Button(onClick = { pendingJson = it; save.launch("flashlearn-backup.json") }, enabled = !state.busy) { Text("ذخیره فایل JSON") }
                    }
                }
            }
        }
    }
}
