package com.flashlearn.app.ui.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ProgressScreen(
    viewModel: ProgressViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("پیشرفت یادگیری", style = MaterialTheme.typography.headlineMedium)
            OutlinedButton(onClick = onBack) { Text("بازگشت") }
        }
        when {
            state.loading -> CircularProgressIndicator()
            state.error != null -> Text("خطا: ${state.error}", color = MaterialTheme.colorScheme.error)
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                state.streak?.let { streak ->
                    item {
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("تداوم یادگیری", style = MaterialTheme.typography.titleLarge)
                                Text("زنجیره فعلی: ${streak.currentStreakDays} روز")
                                Text("بهترین زنجیره: ${streak.longestStreakDays} روز")
                            }
                        }
                    }
                }

                state.summary?.let { summary ->
                    item {
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("وضعیت امروز", style = MaterialTheme.typography.titleLarge)
                                Text("${summary.dueConceptCount} واژه آماده مرور است")
                                Text("روزانه: ${summary.dailyDueConceptCount}  •  هفتگی: ${summary.weeklyDueConceptCount}  •  ماهانه: ${summary.monthlyDueConceptCount}")
                                Text("کل واژه‌ها: ${summary.activeConceptCount}  •  یادگرفته‌شده: ${summary.learnedConceptCount}")
                                val learnedRatio = if (summary.activeConceptCount == 0) 0f else summary.learnedConceptCount.toFloat() / summary.activeConceptCount.toFloat()
                                LinearProgressIndicator(
                                    progress = { learnedRatio.coerceIn(0f, 1f) },
                                    modifier = Modifier.fillMaxWidth().height(8.dp)
                                )
                                Text("پیشرفت یادگیری: ${(learnedRatio * 100).toInt()}٪")
                            }
                        }
                    }
                }

                state.statistics?.let { stats ->
                    item {
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("آمار مرور", style = MaterialTheme.typography.titleLarge)
                                Text("تعداد مرورها: ${stats.totalReviews}")
                                Text("صحیح: ${stats.totalCorrect}  •  غلط: ${stats.totalWrong}")
                                Text("دقت کلی: ${stats.accuracyPercent}٪")
                                Text("واژه‌های مرورشده: ${stats.reviewedConceptCount}")
                            }
                        }
                    }
                }

                state.progress?.let { progress ->
                    item {
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("توزیع مرحله‌های یادگیری", style = MaterialTheme.typography.titleLarge)
                                Text("روزانه: ${progress.dailyConcepts}")
                                Text("هفتگی: ${progress.weeklyConcepts}")
                                Text("ماهانه: ${progress.monthlyConcepts}")
                                Text("یادگرفته‌شده: ${progress.learnedConcepts}")
                                Text("شکست مسیر: ${progress.pathFailureConcepts}")
                                Text("خیلی سخت: ${progress.veryHardConcepts}")
                            }
                        }
                    }
                }
            }
        }
        OutlinedButton(onClick = viewModel::refresh, modifier = Modifier.fillMaxWidth()) {
            Text("به‌روزرسانی آمار")
        }
    }
}
