package com.flashlearn.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    onBackup: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)
        Text("Difficulty threshold")
        Text("Current V1 value: 3")
        Text("User editing is intentionally disabled in V1.")
        androidx.compose.material3.OutlinedButton(onClick = onBackup) { Text("Backup & Restore") }
        androidx.compose.material3.OutlinedButton(onClick = onBack) { Text("بازگشت به خانه") }
    }
}
