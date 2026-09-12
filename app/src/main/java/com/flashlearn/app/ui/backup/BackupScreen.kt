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
        Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
            Button(onClick=onBack, enabled=!state.busy){ Text("Back") }
            Text("Backup & Restore", style=MaterialTheme.typography.headlineMedium)
        }
        Text("Full backup includes vocabulary, learning progress, review history, settings, tags and categories.")
        Button(onClick={ viewModel.export() }, enabled=!state.busy){ Text("Create backup") }
        Button(onClick={ open.launch(arrayOf("application/json","text/plain")) }, enabled=!state.busy){ Text("Restore backup") }
        state.message?.let { Text(it) }
        state.exportedJson?.let {
            Text("Backup JSON is ready (${it.length} characters).")
            Text(it.take(4000), style=MaterialTheme.typography.bodySmall)
            Button(onClick={ pendingJson=it; save.launch("flashlearn-backup.json") }, enabled=!state.busy) { Text("Save JSON") }
        }
    }
}
