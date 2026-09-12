package com.flashlearn.app.ui.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
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
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Progress", style = MaterialTheme.typography.headlineMedium)
        when {
            state.loading -> CircularProgressIndicator()
            state.error != null -> Text("Error: ${state.error}")
            else -> {
                state.streak?.let {
                    Text("زنجیره فعلی: ${it.currentStreakDays} روز")
                    Text("بهترین زنجیره: ${it.longestStreakDays} روز")
                }
                state.summary?.let {
                    Text("آماده مرور: ${it.dueConceptCount}")
                    Text("روزانه: ${it.dailyDueConceptCount} | هفتگی: ${it.weeklyDueConceptCount} | ماهانه: ${it.monthlyDueConceptCount}")
                    Text("کل واژه‌ها: ${it.activeConceptCount} | یادگرفته‌شده: ${it.learnedConceptCount}")
                }
                state.statistics?.let {
                    Text("Reviews: ${it.totalReviews}")
                    Text("Correct: ${it.totalCorrect}  Wrong: ${it.totalWrong}")
                    Text("Accuracy: ${it.accuracyPercent}%")
                    Text("Reviewed concepts: ${it.reviewedConceptCount}")
                }
                state.progress?.let {
                    Text("Daily: ${it.dailyConcepts}")
                    Text("Weekly: ${it.weeklyConcepts}")
                    Text("Monthly: ${it.monthlyConcepts}")
                    Text("Learned: ${it.learnedConcepts}")
                    Text("Path failures: ${it.pathFailureConcepts}")
                    Text("Very hard: ${it.veryHardConcepts}")
                }
            }
        }
        OutlinedButton(onClick = viewModel::refresh) {
            Text("به‌روزرسانی")
        }
        OutlinedButton(onClick = onBack) {
            Text("بازگشت به خانه")
        }
    }
}
