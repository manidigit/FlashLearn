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
            else -> LazyColumn(Modifier.fillMaxWidth().weight(1f), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                state.summary?.let { summary ->
                    item {
                        PurpleHeroCard(
                            "نمای این هفته",
                            "+${summary.activeConceptCount}",
                            "واژه فعال"
                        )
                    }
                    item {
                        Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                SectionTitle("وضعیت مرور")
                                MetricRow("کل مرورهای آماده", summary.dueConceptCount.toString())
                                MetricRow("روزانه", summary.dailyDueConceptCount.toString())
                                MetricRow("هفتگی", summary.weeklyDueConceptCount.toString())
                                MetricRow("ماهانه", summary.monthlyDueConceptCount.toString())
                            }
                        }
                    }
                }
                state.streak?.let { streak ->
                    item {
                        Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                            Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("🔥", style = MaterialTheme.typography.displaySmall)
                                Spacer(Modifier.width(14.dp))
                                Column(Modifier.weight(1f)) {
                                    Text("روز پیوسته", style = MaterialTheme.typography.titleMedium)
                                    Text("زنجیره فعلی: ${streak.currentStreakDays} روز")
                                    Text("بهترین زنجیره: ${streak.longestStreakDays} روز", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
                state.statistics?.let { stats ->
                    item {
                        Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                SectionTitle("آمار مرور")
                                MetricRow("مرورهای انجام‌شده", stats.totalReviews.toString())
                                MetricRow("صحیح", stats.totalCorrect.toString())
                                MetricRow("غلط", stats.totalWrong.toString())
                                MetricRow("واژه‌های مرورشده", stats.reviewedConceptCount.toString())
                                LinearProgressIndicator(progress = { (stats.accuracyPercent / 100f).coerceIn(0f, 1f) }, Modifier.fillMaxWidth().height(8.dp))
                                Text("دقت کلی: ${stats.accuracyPercent}٪")
                            }
                        }
                    }
                }
                state.progress?.let { progress ->
                    item {
                        Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                SectionTitle("توزیع مرحله‌های یادگیری")
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
                        Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.EmojiEvents, null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.width(8.dp))
                                    SectionTitle("دستاوردها")
                                }
                                state.achievements.forEach { (definition, achievement) ->
                                    Text(if (achievement.unlocked) "✓ ${definition.title}" else "○ ${definition.title}")
                                    Text(definition.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
                item {
                    OutlinedButton(onClick = viewModel::refresh, Modifier.fillMaxWidth()) {
                        Icon(Icons.Outlined.Refresh, null)
                        Spacer(Modifier.width(8.dp))
                        Text("به‌روزرسانی آمار")
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium)
    }
}
