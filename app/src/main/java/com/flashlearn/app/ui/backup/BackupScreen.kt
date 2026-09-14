package com.flashlearn.app.ui.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
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
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text("پشتیبان‌گیری و بازیابی", style=MaterialTheme.typography.headlineMedium)
        OutlinedButton(onClick=onBack, enabled=!state.busy){ Text("بازگشت") }
        Text("پشتیبان کامل شامل واژگان، پیشرفت یادگیری، سابقه مرور، تنظیمات، برچسب‌ها، دسته‌بندی‌ها، فراداده Parser و دستاوردهاست.")
        Button(onClick={ viewModel.export() }, enabled=!state.busy){ Text("ساخت پشتیبان") }
        Button(onClick={ open.launch(arrayOf("application/json","text/plain")) }, enabled=!state.busy){ Text("بازیابی پشتیبان") }
        state.message?.let { Text(it) }
        state.exportedJson?.let {
            Text("فایل JSON پشتیبان آماده است (${it.length} نویسه).")
            Text(it.take(4000), style=MaterialTheme.typography.bodySmall)
            Button(onClick={ pendingJson=it; save.launch("flashlearn-backup.json") }, enabled=!state.busy) { Text("ذخیره فایل JSON") }
        }
    }
}
