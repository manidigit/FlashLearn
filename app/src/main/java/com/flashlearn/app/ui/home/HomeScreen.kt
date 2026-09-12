package com.flashlearn.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedButton
import com.flashlearn.domain.model.ReviewType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onStartReview: (ReviewType) -> Unit,
    onAddWord: () -> Unit,
    onBulkImport: () -> Unit,
    onLibrary: () -> Unit,
    onProgress: () -> Unit,
    onSettings: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("FlashLearn", style = MaterialTheme.typography.headlineMedium)

        if (state.isLoading) {
            CircularProgressIndicator()
        } else if (state.error != null) {
            Text(requireNotNull(state.error), color = MaterialTheme.colorScheme.error)
            OutlinedButton(onClick = viewModel::refresh) {
                Text("تلاش دوباره")
            }
        } else {
            val summary = state.summary
            val dueCount = summary?.dueConceptCount ?: 0

            if (dueCount == 0) {
                Text("امروز کارتی برای مرور نداری.")
            } else {
                Text("$dueCount کارت آماده‌ی مرور است.")
            }

            if (summary != null) {
                Text("مجموع واژه‌ها: ${summary.activeConceptCount} | یادگرفته‌شده: ${summary.learnedConceptCount}")
                if ((summary.totalCorrect + summary.totalWrong) > 0) {
                    Text("دقت کلی: ${summary.accuracyPercent}%")
                }
            }

            state.streak?.let {
                Text("زنجیره فعلی: ${it.currentStreakDays} روز | بهترین: ${it.longestStreakDays} روز")
            }

            Text("نوع مرور", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(ReviewType.DAILY, ReviewType.WEEKLY, ReviewType.MONTHLY, ReviewType.RANDOM, ReviewType.LEARNED).forEach { type ->
                    val enabled = when (type) {
                        ReviewType.DAILY -> (summary?.dailyDueConceptCount ?: 0) > 0
                        ReviewType.WEEKLY -> (summary?.weeklyDueConceptCount ?: 0) > 0
                        ReviewType.MONTHLY -> (summary?.monthlyDueConceptCount ?: 0) > 0
                        ReviewType.RANDOM -> dueCount > 0
                        ReviewType.LEARNED -> (summary?.learnedConceptCount ?: 0) > 0
                    }
                    OutlinedButton(
                        onClick = { onStartReview(type) },
                        enabled = enabled
                    ) {
                        Text(reviewTypeLabel(type))
                    }
                }
            }

            Button(onClick = onAddWord) {
                Text("افزودن لغت")
            }
            OutlinedButton(onClick = onBulkImport) {
                Text("ورود گروهی واژگان")
            }
            OutlinedButton(onClick = onLibrary) {
                Text("کتابخانه واژگان")
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onProgress, modifier = Modifier.weight(1f)) {
                    Text("پیشرفت")
                }
                OutlinedButton(onClick = onSettings, modifier = Modifier.weight(1f)) {
                    Text("تنظیمات")
                }
            }
        }
    }
}


private fun reviewTypeLabel(type: ReviewType): String = when (type) {
    ReviewType.DAILY -> "روزانه"
    ReviewType.WEEKLY -> "هفتگی"
    ReviewType.MONTHLY -> "ماهانه"
    ReviewType.LEARNED -> "یادگرفته‌شده"
    ReviewType.RANDOM -> "تصادفی"
}
