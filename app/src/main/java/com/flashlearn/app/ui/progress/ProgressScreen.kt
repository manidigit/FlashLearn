package com.flashlearn.app.ui.progress

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ProgressScreen(viewModel: ProgressViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsState()
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) { Text("آمار و گزارش", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f)); TextButton(onClick = onBack) { Text("←") } }
        when {
            state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            state.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("خطا: ${state.error}", color = MaterialTheme.colorScheme.error) }
            else -> LazyColumn(Modifier.fillMaxWidth().weight(1f), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.summary?.let { s ->
                    item { Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)) { Column(Modifier.fillMaxWidth().padding(vertical = 17.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text("نمای این هفته", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.labelMedium); Text("${s.totalCorrect + s.totalWrong}+", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.headlineLarge); Text("واژه فعال", color = MaterialTheme.colorScheme.onPrimary) } } }
                    item { Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) { Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { Text("وضعیت مرور", style = MaterialTheme.typography.titleMedium); MetricRow("کل مرورهای آماده", s.dueConceptCount.toString()); MetricRow("روزانه", s.dailyDueConceptCount.toString()); MetricRow("هفتگی", s.weeklyDueConceptCount.toString()); MetricRow("ماهانه", s.monthlyDueConceptCount.toString()) } } }
                }
                item { state.statistics?.let { stats -> Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) { Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { Text("آمار مرور", style = MaterialTheme.typography.titleMedium); MetricRow("مرورهای انجام‌شده", stats.totalReviews.toString()); MetricRow("صحیح", stats.totalCorrect.toString()); MetricRow("غلط", stats.totalWrong.toString()); MetricRow("واژه‌های مرورشده", stats.reviewedConceptCount.toString()); LinearProgressIndicator(progress = { (stats.accuracyPercent / 100f).coerceIn(0f, 1f) }, Modifier.fillMaxWidth().height(7.dp)); Text("دقت کلی: ${stats.accuracyPercent}٪", style = MaterialTheme.typography.labelSmall) } } } }
                item { state.progress?.let { p -> Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) { Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) { Text("توزیع مراحل یادگیری", style = MaterialTheme.typography.titleMedium); MetricRow("روزانه", p.dailyConcepts.toString()); MetricRow("هفتگی", p.weeklyConcepts.toString()); MetricRow("ماهانه", p.monthlyConcepts.toString()); MetricRow("یادگرفته‌شده", p.learnedConcepts.toString()); MetricRow("شکست مسیر", p.pathFailureConcepts.toString()); MetricRow("خیلی سخت", p.veryHardConcepts.toString()) } } } }
                if (state.achievements.isNotEmpty()) item { Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) { Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.EmojiEvents, null, tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(6.dp)); Text("دستاوردها", style = MaterialTheme.typography.titleMedium) }; state.achievements.forEach { (definition, achievement) -> Text(if (achievement.unlocked) "✓ ${definition.title}" else "○ ${definition.title}", style = MaterialTheme.typography.labelSmall) } } } }
                item { OutlinedButton(onClick = viewModel::refresh, Modifier.fillMaxWidth().height(44.dp)) { Icon(Icons.Outlined.Refresh, null); Spacer(Modifier.width(5.dp)); Text("به‌روزرسانی آمار") } }
            }
        }
    }
}

@Composable private fun MetricRow(label: String, value: String) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall); Text(value, style = MaterialTheme.typography.titleSmall) } }
