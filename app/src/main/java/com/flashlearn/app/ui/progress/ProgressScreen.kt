package com.flashlearn.app.ui.progress

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.max

private val Purple = Color(0xFF7C2BEF)
private val Blue = Color(0xFF3983E6)
private val Green = Color(0xFF14B77B)
private val Amber = Color(0xFFF0A51A)
private val Pink = Color(0xFFE33C86)
private val Red = Color(0xFFE5484D)

@Composable
fun ProgressScreen(viewModel: ProgressViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) { Text("←", style = MaterialTheme.typography.headlineMedium) }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                Text("آمار و گزارش", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("تصویر واقعی از پیشرفت و نقاط قوت.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        when {
            state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            state.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("خطا: ${state.error}", color = MaterialTheme.colorScheme.error) }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item { SummaryTiles(state) }
                item { RetentionCard(state) }
                item { WeeklyChart(state.dailyReviews) }
                item { LearningStagesCard(state) }
                item { DifficultyCard(state) }
                item { ReviewAccuracyCard(state) }
                item { AchievementsCard(state) }
                item { RefreshCard(viewModel) }
            }
        }
    }
}

@Composable
private fun SummaryTiles(state: ProgressUiState) {
    val stats = state.statistics
    val summary = state.summary
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        SummaryTile("کل واژه‌ها", fa(stats?.reviewedConceptCount ?: 0), Icons.Outlined.MenuBook, Blue, Modifier.weight(1f))
        SummaryTile("یادگرفته", fa(summary?.learnedConceptCount ?: 0), Icons.Outlined.School, Green, Modifier.weight(1f))
        SummaryTile("دقت کل", "${fa(stats?.accuracyPercent ?: 0)}٪", Icons.Outlined.CheckCircle, Amber, Modifier.weight(1f))
    }
}

@Composable
private fun SummaryTile(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier) {
    Card(modifier, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = color.copy(alpha = .10f))) {
        Column(Modifier.fillMaxWidth().padding(vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = color, modifier = Modifier.size(26.dp))
            Spacer(Modifier.height(5.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun RetentionCard(state: ProgressUiState) {
    val stats = state.statistics
    val accuracy = (stats?.accuracyPercent ?: 0).coerceIn(0, 100)
    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Blue.copy(alpha = .08f))) {
        Column(Modifier.fillMaxWidth().padding(18.dp), horizontalAlignment = Alignment.End) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.ShowChart, null, tint = Blue)
                Spacer(Modifier.width(7.dp))
                Text("حفظ ماندگار", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Text("${fa(accuracy)}٪", color = Purple, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
            LinearProgressIndicator(
                progress = { accuracy / 100f },
                modifier = Modifier.fillMaxWidth().height(9.dp).clip(RoundedCornerShape(8.dp)),
                color = Blue,
                trackColor = MaterialTheme.colorScheme.surface
            )
            Spacer(Modifier.height(7.dp))
            Text("${fa(stats?.totalCorrect ?: 0)} پاسخ درست از ${fa(stats?.totalReviews ?: 0)}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun WeeklyChart(daily: List<DailyReviewStat>) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    Card(shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.fillMaxWidth().padding(18.dp)) {
            Text("فعالیت مرور در هفت روز اخیر", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.End))
            Spacer(Modifier.height(10.dp))
            if (daily.isEmpty()) {
                Text("هنوز داده‌ای برای نمایش وجود ندارد.", modifier = Modifier.align(Alignment.End), color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Canvas(Modifier.fillMaxWidth().height(150.dp)) {
                    val maxValue = max(1, daily.maxOf { it.total })
                    val left = 12f
                    val right = size.width - 12f
                    val top = 12f
                    val bottom = size.height - 26f
                    val step = if (daily.size == 1) 0f else (right - left) / (daily.size - 1)
                    val path = Path()
                    daily.forEachIndexed { index, item ->
                        val x = left + step * index
                        val y = bottom - (item.total.toFloat() / maxValue) * (bottom - top)
                        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    drawPath(path, Purple, style = Stroke(width = 5f, cap = StrokeCap.Round))
                    daily.forEachIndexed { index, item ->
                        val x = left + step * index
                        val y = bottom - (item.total.toFloat() / maxValue) * (bottom - top)
                        drawCircle(surfaceColor, 7f, Offset(x, y))
                        drawCircle(Purple, 5f, Offset(x, y))
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    daily.forEach { Text(it.dayLabel.take(3), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }
        }
    }
}

@Composable
private fun LearningStagesCard(state: ProgressUiState) {
    val p = state.progress ?: return
    Card(shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("مراحل یادگیری", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.End))
            StageRow("روزانه", p.dailyConcepts, Blue)
            StageRow("هفتگی", p.weeklyConcepts, Blue)
            StageRow("ماهانه", p.monthlyConcepts, Purple)
            StageRow("یادگرفته", p.learnedConcepts, Green)
        }
    }
}

@Composable
private fun StageRow(label: String, value: Int, color: Color) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(fa(value), color = color, fontWeight = FontWeight.Bold, modifier = Modifier.width(58.dp))
        LinearProgressIndicator(progress = { (value / 100f).coerceIn(.03f, 1f) }, modifier = Modifier.weight(1f).height(7.dp).clip(RoundedCornerShape(8.dp)), color = color, trackColor = color.copy(alpha = .10f))
        Spacer(Modifier.width(10.dp))
        Text(label, modifier = Modifier.width(70.dp), textAlign = androidx.compose.ui.text.style.TextAlign.End)
    }
}

@Composable
private fun DifficultyCard(state: ProgressUiState) {
    val p = state.progress ?: return
    val rows = listOf("آسان" to p.dailyConcepts, "متوسط" to p.weeklyConcepts, "سخت" to p.monthlyConcepts, "خیلی سخت" to p.veryHardConcepts)
    Card(shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
            Text("پروفایل سختی", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.End))
            rows.forEachIndexed { index, row ->
                val color = listOf(Green, Amber, Pink, Red)[index]
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(fa(row.second), color = color, fontWeight = FontWeight.Bold, modifier = Modifier.width(58.dp))
                    LinearProgressIndicator(progress = { (row.second / max(1f, rows.maxOf { it.second }.toFloat())).coerceIn(.02f, 1f) }, modifier = Modifier.weight(1f).height(7.dp).clip(RoundedCornerShape(8.dp)), color = color, trackColor = color.copy(alpha = .10f))
                    Spacer(Modifier.width(10.dp))
                    Text(row.first, modifier = Modifier.width(78.dp), textAlign = androidx.compose.ui.text.style.TextAlign.End)
                }
            }
        }
    }
}

@Composable
private fun ReviewAccuracyCard(state: ProgressUiState) {
    val stats = state.statistics ?: return
    Card(shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Text("دقت مرور", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.End))
            AccuracyRow("امروز", stats.totalReviews, stats.totalCorrect, Green)
            AccuracyRow("این هفته", stats.totalReviews, stats.totalCorrect, Blue)
            AccuracyRow("این ماه", stats.totalReviews, stats.totalCorrect, Purple)
            AccuracyRow("مجموع کل", stats.totalReviews, stats.totalCorrect, Amber)
        }
    }
}

@Composable
private fun AccuracyRow(label: String, total: Int, correct: Int, color: Color) {
    val percent = if (total == 0) 0 else (correct * 100) / total
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(color.copy(alpha = .07f)).padding(horizontal = 14.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("${fa(percent)}٪", color = color, fontWeight = FontWeight.Bold)
        Spacer(Modifier.weight(1f))
        Text("${fa(correct)}/${fa(total)}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
        Spacer(Modifier.width(12.dp))
        Text(label, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun AchievementsCard(state: ProgressUiState) {
    if (state.achievements.isEmpty()) return
    val unlocked = state.achievements.count { it.second.unlocked }
    Card(shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.EmojiEvents, null, tint = Amber)
                Spacer(Modifier.width(7.dp))
                Text("دستاوردها", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.End)
                Text(fa(unlocked), color = Amber, fontWeight = FontWeight.Bold)
            }
            state.achievements.forEach { (definition, achievement) ->
                Text(if (achievement.unlocked) "✓ ${definition.title}" else "○ ${definition.title}", color = if (achievement.unlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun RefreshCard(viewModel: ProgressViewModel) {
    OutlinedButton(onClick = viewModel::refresh, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(16.dp)) {
        Icon(Icons.Outlined.Refresh, null)
        Spacer(Modifier.width(7.dp))
        Text("به‌روزرسانی آمار")
    }
}

private fun fa(value: Int): String = value.toString().map { c ->
    when (c) {
        '0' -> '۰'; '1' -> '۱'; '2' -> '۲'; '3' -> '۳'; '4' -> '۴'; '5' -> '۵'; '6' -> '۶'; '7' -> '۷'; '8' -> '۸'; '9' -> '۹'; else -> c
    }
}.joinToString("")
