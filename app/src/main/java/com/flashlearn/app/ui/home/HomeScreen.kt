package com.flashlearn.app.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flashlearn.app.ui.components.PurpleHeroCard
import com.flashlearn.domain.model.ReviewType

@Composable
fun HomeScreen(viewModel: HomeViewModel, onStartReview: (ReviewType) -> Unit, onAddWord: () -> Unit, onBulkImport: () -> Unit, onLibrary: () -> Unit, onProgress: () -> Unit, onSettings: () -> Unit) {
    val state by viewModel.state.collectAsState()
    val summary = state.summary
    val due = summary?.dueConceptCount ?: 0
    val total = summary?.activeConceptCount ?: 0
    val daily = summary?.dailyDueConceptCount ?: 0
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("سلام مانی!", style = MaterialTheme.typography.headlineSmall)
                Text("به فلش‌لرن خوش آمدی 👋", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Surface(shape = MaterialTheme.shapes.medium, tonalElevation = 3.dp) { Text("☰", modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.titleLarge) }
        }
        PurpleHeroCard("روز پیوسته", "${state.streak?.currentStreakDays ?: 0}", "روز پیوسته")
        Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
            Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("📖", style = MaterialTheme.typography.displaySmall)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) { Text("$total", style = MaterialTheme.typography.headlineMedium); Text("کلمه منتظر شماست", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
        Text("ادامه بده", style = MaterialTheme.typography.titleMedium)
        Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("☀️", style = MaterialTheme.typography.titleLarge); Spacer(Modifier.width(10.dp)); Text("روزانه", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f)); Text("$daily از $due", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                LinearProgressIndicator(progress = { if (due == 0) 0f else (daily.toFloat() / due).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().height(7.dp), trackColor = MaterialTheme.colorScheme.surfaceVariant)
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickAction("مرور کلمات", Icons.Outlined.Sync, Modifier.weight(1f)) { onStartReview(ReviewType.DAILY) }
            QuickAction("آمار و گزارش", Icons.Outlined.BarChart, Modifier.weight(1f), onProgress)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickAction("واژگان", Icons.Outlined.Book, Modifier.weight(1f), onLibrary)
            QuickAction("تنظیمات", Icons.Outlined.Settings, Modifier.weight(1f), onSettings)
        }
        if (due > 0) Button(onClick = { onStartReview(if (daily > 0) ReviewType.DAILY else ReviewType.RANDOM) }, modifier = Modifier.fillMaxWidth().height(52.dp), shape = MaterialTheme.shapes.medium) { Text(if (daily > 0) "شروع مرور روزانه" else "شروع مرور") }
    }
}

@Composable
private fun QuickAction(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = modifier, shape = MaterialTheme.shapes.medium) {
        Column(Modifier.fillMaxWidth().padding(vertical = 18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(8.dp)); Text(label)
        }
    }
}
