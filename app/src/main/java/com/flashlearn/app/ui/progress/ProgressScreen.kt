package com.flashlearn.app.ui.progress

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material3.*
import com.flashlearn.app.ui.components.FlashLearnScreenHeader
import com.flashlearn.app.ui.theme.LocalFlashLearnThemeTokens
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


@Composable
fun ProgressScreen(viewModel: ProgressViewModel, onBack: () -> Unit) {
    val tokens = LocalFlashLearnThemeTokens.current
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        FlashLearnScreenHeader(
            title = "آمار و گزارش",
            subtitle = "تصویر واقعی از پیشرفت و نقاط قوت.",
            onBack = onBack
        )

        when {
            state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            state.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("خطا: ${state.error}", color = MaterialTheme.colorScheme.error) }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = tokens.screenPadding, top = 0.dp, end = tokens.screenPadding, bottom = tokens.sectionGap + tokens.compactGap),
                verticalArrangement = Arrangement.spacedBy(tokens.contentGap)
            ) {
                item { SummaryTiles(state) }
                item { LearningMotivationCard(state) }
                item { RetentionCard(state) }
                item { WeeklyChart(state.activityReviews, state.activityRange, viewModel::setActivityRange) }
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
    val tokens = LocalFlashLearnThemeTokens.current
    val stats = state.statistics
    val summary = state.summary
    Row(horizontalArrangement = Arrangement.spacedBy(tokens.compactGap), modifier = Modifier.fillMaxWidth()) {
        SummaryTile("کل واژه‌ها", fa(summary?.activeConceptCount ?: 0), Icons.Outlined.MenuBook, MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
        SummaryTile("یادگرفته", fa(summary?.learnedConceptCount ?: 0), Icons.Outlined.School, tokens.success, Modifier.weight(1f))
        SummaryTile("دقت کل", "${fa(stats?.accuracyPercent ?: 0)}٪", Icons.Outlined.CheckCircle, tokens.warning, Modifier.weight(1f))
    }
}

@Composable
private fun SummaryTile(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier) {
    val tokens = LocalFlashLearnThemeTokens.current
    Card(modifier, shape = MaterialTheme.shapes.medium, colors = CardDefaults.cardColors(containerColor = color.copy(alpha = .10f))) {
        Column(Modifier.fillMaxWidth().padding(vertical = tokens.contentGap), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = color, modifier = Modifier.size(tokens.iconLarge))
            Spacer(Modifier.height(tokens.tinyGap))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun RetentionCard(state: ProgressUiState) {
    val tokens = LocalFlashLearnThemeTokens.current
    val stats = state.statistics
    val accuracy = (stats?.accuracyPercent ?: 0).coerceIn(0, 100)
    Card(shape = MaterialTheme.shapes.medium, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = .08f))) {
        Column(Modifier.fillMaxWidth().padding(tokens.cardPadding), horizontalAlignment = Alignment.Start) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.ShowChart, null, tint = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.width(tokens.tinyGap))
                Text(
                    "حفظ ماندگار",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )
                Spacer(Modifier.width(tokens.compactGap))
                Text(
                    "${fa(accuracy)}٪",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
            LinearProgressIndicator(
                progress = { accuracy / 100f },
                modifier = Modifier.fillMaxWidth().height(tokens.dp(9f)).clip(MaterialTheme.shapes.extraSmall),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surface
            )
            Spacer(Modifier.height(tokens.dp(7f)))
            Text("${fa(stats?.totalCorrect ?: 0)} پاسخ درست از ${fa(stats?.totalReviews ?: 0)}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun LearningMotivationCard(state: ProgressUiState) {
    val tokens = LocalFlashLearnThemeTokens.current
    val percentage = state.progressPercentage.coerceIn(0.0, 100.0)
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = .08f))
    ) {
        Column(
            Modifier.fillMaxWidth().padding(tokens.cardPadding),
            verticalArrangement = Arrangement.spacedBy(tokens.compactGap),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "پیشرفت یادگیری",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )
                Spacer(Modifier.width(tokens.compactGap))
                Text(
                    "${percentage.toInt()}٪",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
            LinearProgressIndicator(
                progress = { (percentage / 100f).toFloat() },
                modifier = Modifier.fillMaxWidth().height(tokens.dp(9f)).clip(MaterialTheme.shapes.extraSmall),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surface
            )
            Text("امتیاز بر اساس مرحله فعلی یادگیری هر واژه محاسبه می‌شود.", color = tokens.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            Text("بدون مرور ۰٪ • پس از اولین مرور، امتیاز بر اساس مرحله: روزانه ۳۵٪ • هفتگی ۶۰٪ • ماهانه ۸۰٪ • یادگرفته ۱۰۰٪", color = tokens.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
    }
}
@Composable
private fun WeeklyChart(
    activity: List<ActivityReviewStat>,
    selectedRange: ActivityRange,
    onRangeSelected: (ActivityRange) -> Unit
) {
    val tokens = LocalFlashLearnThemeTokens.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val outlineColor = MaterialTheme.colorScheme.outline
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    Card(shape = MaterialTheme.shapes.medium) {
        Column(Modifier.fillMaxWidth().padding(tokens.cardPadding), verticalArrangement = Arrangement.spacedBy(tokens.compactGap)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("فعالیت مرور", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(tokens.microGap)
                ) {
                    ActivityRange.entries.forEach { range ->
                        FilterChip(
                            selected = range == selectedRange,
                            onClick = { onRangeSelected(range) },
                            label = { Text(range.label) }
                        )
                    }
                }
            }
            if (activity.isEmpty() || activity.all { it.total == 0 }) {
                Text(
                    "هنوز داده‌ای برای نمایش وجود ندارد.",
                    modifier = Modifier.align(Alignment.Start),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val maxValue = activity.maxOf { it.total }.coerceAtLeast(1)
                val axisMax = when {
                    maxValue <= 5 -> 5
                    maxValue <= 10 -> 10
                    else -> ((maxValue + 9) / 10) * 10
                }
                val gridSteps = 5
                val labelStep = if (activity.size <= 7) 1 else if (activity.size <= 14) 2 else 5
                Row(Modifier.fillMaxWidth().height(tokens.dp(210f)), verticalAlignment = Alignment.Top) {
                    Column(
                        modifier = Modifier.width(tokens.dp(32f)).fillMaxHeight().padding(top = tokens.tinyGap, bottom = tokens.dp(30f)),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.Start
                    ) {
                        for (step in gridSteps downTo 0) {
                            Text(
                                fa((axisMax * step) / gridSteps),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(Modifier.width(tokens.microGap))
                    Column(Modifier.weight(1f).fillMaxHeight()) {
                        Canvas(Modifier.fillMaxWidth().weight(1f)) {
                            val chartTop = 8f
                            val chartBottom = size.height - 4f
                            val chartHeight = chartBottom - chartTop
                            for (step in 0..gridSteps) {
                                val y = chartBottom - chartHeight * step / gridSteps
                                drawLine(
                                    color = outlineColor.copy(alpha = if (step == 0) .35f else .12f),
                                    start = Offset(0f, y),
                                    end = Offset(size.width, y),
                                    strokeWidth = 1f
                                )
                            }
                            val gap = 8f
                            val slot = size.width / activity.size
                            val barWidth = (slot - gap).coerceAtLeast(3f)
                            activity.forEachIndexed { index, item ->
                                val barHeight = chartHeight * item.total.toFloat() / axisMax
                                val left = index * slot + (slot - barWidth) / 2f
                                val top = chartBottom - barHeight
                                drawRoundRect(
                                    color = primaryColor,
                                    topLeft = Offset(left, top),
                                    size = androidx.compose.ui.geometry.Size(barWidth, barHeight.coerceAtLeast(2f)),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                                )
                            }
                        }
                        Row(
                            Modifier.fillMaxWidth().height(tokens.dp(28f)),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            activity.forEachIndexed { index, item ->
                                val show = index % labelStep == 0 || index == activity.lastIndex
                                Text(
                                    if (show) item.label else "",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
                Text(
                    "تعداد مرور",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Start)
                )
            }
        }
    }
}

@Composable
private fun LearningStagesCard(state: ProgressUiState) {
    val tokens = LocalFlashLearnThemeTokens.current
    val p = state.progress ?: return
    Card(shape = MaterialTheme.shapes.medium) {
        Column(Modifier.fillMaxWidth().padding(tokens.cardPadding), verticalArrangement = Arrangement.spacedBy(tokens.contentGap)) {
            Text("مراحل یادگیری", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
            val maxStageValue = listOf(p.dailyConcepts, p.weeklyConcepts, p.monthlyConcepts, p.learnedConcepts).maxOrNull()?.coerceAtLeast(1) ?: 1
            StageRow("روزانه", p.dailyConcepts, maxStageValue, MaterialTheme.colorScheme.secondary)
            StageRow("هفتگی", p.weeklyConcepts, maxStageValue, MaterialTheme.colorScheme.secondary)
            StageRow("ماهانه", p.monthlyConcepts, maxStageValue, MaterialTheme.colorScheme.primary)
            StageRow("یادگرفته", p.learnedConcepts, maxStageValue, tokens.success)
        }
    }
}

@Composable
private fun StageRow(label: String, value: Int, maxValue: Int, color: Color) {
    val tokens = LocalFlashLearnThemeTokens.current
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(fa(value), color = color, fontWeight = FontWeight.Bold, modifier = Modifier.width(tokens.dp(58f)))
        LinearProgressIndicator(progress = { (value.toFloat() / maxValue.toFloat()).coerceIn(.03f, 1f) }, modifier = Modifier.weight(1f).height(tokens.dp(7f)).clip(MaterialTheme.shapes.extraSmall), color = color, trackColor = color.copy(alpha = .10f))
        Spacer(Modifier.width(tokens.compactGap))
        Text(label, modifier = Modifier.width(tokens.dp(70f)), textAlign = androidx.compose.ui.text.style.TextAlign.Start)
    }
}

@Composable
private fun DifficultyCard(state: ProgressUiState) {
    val tokens = LocalFlashLearnThemeTokens.current
    val p = state.progress ?: return
    val rows = listOf(
        "آسان" to p.easyConcepts,
        "متوسط" to p.mediumConcepts,
        "سخت" to p.hardConcepts,
        "خیلی سخت" to p.veryHardConcepts
    )
    Card(shape = MaterialTheme.shapes.medium) {
        Column(Modifier.fillMaxWidth().padding(tokens.cardPadding), verticalArrangement = Arrangement.spacedBy(tokens.dp(11f))) {
            Text("پروفایل سختی", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
            rows.forEachIndexed { index, row ->
                val color = listOf(tokens.success, tokens.warning, MaterialTheme.colorScheme.secondary, tokens.error)[index]
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(fa(row.second), color = color, fontWeight = FontWeight.Bold, modifier = Modifier.width(tokens.dp(58f)))
                    LinearProgressIndicator(progress = { (row.second / max(1f, rows.maxOf { it.second }.toFloat())).coerceIn(.02f, 1f) }, modifier = Modifier.weight(1f).height(tokens.dp(7f)).clip(MaterialTheme.shapes.extraSmall), color = color, trackColor = color.copy(alpha = .10f))
                    Spacer(Modifier.width(tokens.compactGap))
                    Text(row.first, modifier = Modifier.width(tokens.dp(78f)), textAlign = androidx.compose.ui.text.style.TextAlign.Start)
                }
            }
        }
    }
}

@Composable
private fun ReviewAccuracyCard(state: ProgressUiState) {
    val tokens = LocalFlashLearnThemeTokens.current
    val stats = state.statistics ?: return
    Card(shape = MaterialTheme.shapes.medium) {
        Column(Modifier.fillMaxWidth().padding(tokens.cardPadding), verticalArrangement = Arrangement.spacedBy(tokens.itemGap)) {
            Text("دقت مرور", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
            AccuracyRow("امروز", state.todayReviews.total, state.todayReviews.correct, tokens.success)
            AccuracyRow("این هفته", state.weekReviews.total, state.weekReviews.correct, MaterialTheme.colorScheme.secondary)
            AccuracyRow("این ماه", state.monthReviews.total, state.monthReviews.correct, MaterialTheme.colorScheme.primary)
            AccuracyRow("مجموع کل", stats.totalReviews, stats.totalCorrect, tokens.warning)
        }
    }
}

@Composable
private fun AccuracyRow(label: String, total: Int, correct: Int, color: Color) {
    val tokens = LocalFlashLearnThemeTokens.current
    val percent = if (total == 0) 0 else (correct * 100) / total
    Row(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).background(color.copy(alpha = .07f)).padding(horizontal = tokens.contentGap, vertical = tokens.compactGap), verticalAlignment = Alignment.CenterVertically) {
        Text("${fa(percent)}٪", color = color, fontWeight = FontWeight.Bold)
        Spacer(Modifier.weight(1f))
        Text("${fa(correct)}/${fa(total)}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
        Spacer(Modifier.width(tokens.contentGap))
        Text(label, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun AchievementsCard(state: ProgressUiState) {
    val tokens = LocalFlashLearnThemeTokens.current
    if (state.achievements.isEmpty()) return
    val unlocked = state.achievements.count { it.second.unlocked }
    Card(shape = MaterialTheme.shapes.medium) {
        Column(Modifier.fillMaxWidth().padding(tokens.cardPadding), verticalArrangement = Arrangement.spacedBy(tokens.tinyGap)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.EmojiEvents, null, tint = tokens.warning)
                Spacer(Modifier.width(tokens.tinyGap))
                Text("دستاوردها", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Start)
                Text(fa(unlocked), color = tokens.warning, fontWeight = FontWeight.Bold)
            }
            state.achievements.forEach { (definition, achievement) ->
                Text(if (achievement.unlocked) "✓ ${definition.title}" else "○ ${definition.title}", color = if (achievement.unlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun RefreshCard(viewModel: ProgressViewModel) {
    val tokens = LocalFlashLearnThemeTokens.current
    OutlinedButton(onClick = viewModel::refresh, modifier = Modifier.fillMaxWidth().height(tokens.controlHeight), shape = MaterialTheme.shapes.small) {
        Icon(tokens.icons.refresh, null)
        Spacer(Modifier.width(tokens.tinyGap))
        Text("به‌روزرسانی آمار")
    }
}

private fun fa(value: Int): String = value.toString().map { c ->
    when (c) {
        '0' -> '۰'; '1' -> '۱'; '2' -> '۲'; '3' -> '۳'; '4' -> '۴'; '5' -> '۵'; '6' -> '۶'; '7' -> '۷'; '8' -> '۸'; '9' -> '۹'; else -> c
    }
}.joinToString("")
