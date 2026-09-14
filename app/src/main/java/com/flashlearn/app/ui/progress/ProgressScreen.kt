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
import com.flashlearn.app.ui.components.PurpleHeroCard
import com.flashlearn.app.ui.components.ScreenHeader
import com.flashlearn.app.ui.components.SectionTitle

@Composable
fun ProgressScreen(viewModel: ProgressViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsState()
    Column(Modifier.fillMaxSize()) {
        ScreenHeader("آمار و گزارش", onBack)
        when {
            state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            state.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("خطا: ${state.error}", color = MaterialTheme.colorScheme.error) }
            else -> LazyColumn(Modifier.fillMaxWidth().weight(1f), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                state.summary?.let { summary ->
                    item {
                        PurpleHeroCard("این هفته", "${summary.totalCorrect + summary.totalWrong}", "مرور انجام‌شده")
                    }
                    item {
                        Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
                            Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                CompactMetric("واژه فعال", summary.activeConceptCount.toString(), Modifier.weight(1f))
                                CompactMetric("یادگرفته", summary.learnedConceptCount.toString(), Modifier.weight(1f))
                                CompactMetric("دقت", "${summary.accuracyPercent}٪", Modifier.weight(1f))
                            }
                        }
                    }
                }
                if (state.dailyReviews.isNotEmpty()) {
                    item {
                        Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                SectionTitle("مرور روزانه — ۷ روز اخیر")
                                state.dailyReviews.forEach { day ->
                                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                        Text(day.dayLabel, modifier = Modifier.width(62.dp), style = MaterialTheme.typography.labelMedium)
                                        LinearProgressIndicator(
                                            progress = { (day.total / (state.dailyReviews.maxOfOrNull { it.total }?.coerceAtLeast(1) ?: 1f)).coerceIn(0f, 1f) },
                                            modifier = Modifier.weight(1f).height(7.dp)
                                        )
                                        Text("${day.total}", modifier = Modifier.width(28.dp), style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }
                        }
                    }
                }
                state.statistics?.let { stats ->
                    item {
                        Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                SectionTitle("نتیجه مرور")
                                MetricRow("صحیح", stats.totalCorrect.toString())
                                MetricRow("غلط", stats.totalWrong.toString())
                                MetricRow("واژه‌های مرورشده", stats.reviewedConceptCount.toString())
                                LinearProgressIndicator(progress = { (stats.accuracyPercent / 100f).coerceIn(0f, 1f) }, Modifier.fillMaxWidth().height(7.dp))
                                Text("دقت کلی: ${stats.accuracyPercent}٪", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
                state.progress?.let { progress ->
                    item {
                        Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                SectionTitle("مرحله‌های یادگیری")
                                MetricRow("روزانه", progress.dailyConcepts.toString())
                                MetricRow("هفتگی", progress.weeklyConcepts.toString())
                                MetricRow("ماهانه", progress.monthlyConcepts.toString())
                                MetricRow("یادگرفته‌شده", progress.learnedConcepts.toString())
                                MetricRow("شکست مسیر", progress.pathFailureConcepts.toString())
                                MetricRow("خیلی سخت", progress.veryHardConcepts.toString())
                            }
                        }
                    }
                }
                if (state.achievements.isNotEmpty()) {
                    item {
                        Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.EmojiEvents, null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.width(7.dp)); SectionTitle("دستاوردها")
                                }
                                state.achievements.forEach { (definition, achievement) ->
                                    Text(if (achievement.unlocked) "✓ ${definition.title}" else "○ ${definition.title}")
                                }
                            }
                        }
                    }
                }
                item { OutlinedButton(onClick = viewModel::refresh, Modifier.fillMaxWidth()) { Icon(Icons.Outlined.Refresh, null); Spacer(Modifier.width(6.dp)); Text("به‌روزرسانی") } }
            }
        }
    }
}

@Composable
private fun CompactMetric(label: String, value: String, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun MetricRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        Text(value, style = MaterialTheme.typography.titleSmall)
    }
}
