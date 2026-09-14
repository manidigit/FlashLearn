package com.flashlearn.app.ui.home

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.flashlearn.domain.model.ReviewType

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

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("FlashLearn", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text("یادگیری روزانه، ساده و قابل پیگیری", style = MaterialTheme.typography.bodyLarge)

            if (state.isLoading) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator()
                        Text("در حال آماده‌سازی داشبورد…")
                    }
                }
            } else if (state.error != null) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("بارگذاری انجام نشد", style = MaterialTheme.typography.titleLarge)
                        Text(requireNotNull(state.error), color = MaterialTheme.colorScheme.error)
                        OutlinedButton(onClick = viewModel::refresh) { Text("تلاش دوباره") }
                    }
                }
            } else {
                val summary = state.summary
                val dueCount = summary?.dueConceptCount ?: 0
                val dailyDue = summary?.dailyDueConceptCount ?: 0
                val learned = summary?.learnedConceptCount ?: 0
                val total = summary?.activeConceptCount ?: 0
                val accuracy = summary?.accuracyPercent ?: 0

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(if (dueCount > 0) "وقت مرور رسیده" else "امروز آماده‌ای؟", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            if (dueCount > 0) "$dueCount کارت برای مرور آماده است."
                            else "فعلاً مرور عقب‌افتاده‌ای نداری. می‌توانی واژه جدید اضافه کنی."
                        )
                        if (dueCount > 0) {
                            Button(
                                onClick = { onStartReview(if (dailyDue > 0) ReviewType.DAILY else ReviewType.RANDOM) },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text(if (dailyDue > 0) "شروع مرور روزانه" else "شروع مرور") }
                        }
                    }
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("وضعیت یادگیری", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatBlock("واژه‌ها", total.toString(), Modifier.weight(1f))
                            StatBlock("یادگرفته", learned.toString(), Modifier.weight(1f))
                            StatBlock("دقت", "$accuracy٪", Modifier.weight(1f))
                        }
                        state.streak?.let { Text("🔥 زنجیره: ${it.currentStreakDays} روز  •  بهترین: ${it.longestStreakDays} روز") }
                    }
                }

                Text("مرور بر اساس نوع", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(ReviewType.DAILY, ReviewType.WEEKLY, ReviewType.MONTHLY, ReviewType.RANDOM, ReviewType.LEARNED).forEach { type ->
                        val enabled = when (type) {
                            ReviewType.DAILY -> dailyDue > 0
                            ReviewType.WEEKLY -> (summary?.weeklyDueConceptCount ?: 0) > 0
                            ReviewType.MONTHLY -> (summary?.monthlyDueConceptCount ?: 0) > 0
                            ReviewType.RANDOM -> dueCount > 0
                            ReviewType.LEARNED -> learned > 0
                        }
                        OutlinedButton(onClick = { onStartReview(type) }, enabled = enabled) { Text(reviewTypeLabel(type)) }
                    }
                }

                Text("مدیریت واژه‌ها", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Button(onClick = onAddWord, modifier = Modifier.fillMaxWidth()) { Text("افزودن واژه") }
                OutlinedButton(onClick = onBulkImport, modifier = Modifier.fillMaxWidth()) { Text("ورود گروهی واژگان") }
                OutlinedButton(onClick = onLibrary, modifier = Modifier.fillMaxWidth()) { Text("کتابخانه واژگان") }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onProgress, modifier = Modifier.weight(1f)) { Text("پیشرفت") }
                    OutlinedButton(onClick = onSettings, modifier = Modifier.weight(1f)) { Text("تنظیمات") }
                }
            }
        }
    }
}

@Composable
private fun StatBlock(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodySmall)
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
