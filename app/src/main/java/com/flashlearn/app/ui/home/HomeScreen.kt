package com.flashlearn.app.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.flashlearn.app.ui.LanguagePair
import com.flashlearn.app.ui.theme.LocalFlashLearnThemeTokens
import com.flashlearn.domain.model.ReviewType
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    languagePair: LanguagePair = LanguagePair(),
    onStartReview: (ReviewType) -> Unit,
    onAddWord: () -> Unit
) {
    val tokens = LocalFlashLearnThemeTokens.current
    val state by viewModel.state.collectAsState()
    val summary = state.summary
    val stats = state.basicStats
    val total = stats?.totalActiveWords ?: summary?.activeConceptCount ?: 0
    val due = summary?.dueConceptCount ?: 0
    val daily = summary?.dailyDueConceptCount ?: 0
    val weekly = summary?.weeklyDueConceptCount ?: 0
    val monthly = summary?.monthlyDueConceptCount ?: 0

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("سلام!", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.weight(1f))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("🔥", style = MaterialTheme.typography.titleLarge)
                    Text("${state.streak?.currentStreakDays ?: 0} روز پیوسته", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(languagePair.source.flag, style = MaterialTheme.typography.titleLarge)
                    Text(languagePair.target.flag, style = MaterialTheme.typography.titleLarge)
                }
            }

            Text(
                "درصد پیشرفت یادگیری: ${state.progressPercentage.roundToInt()}٪",
                style = MaterialTheme.typography.bodyMedium,
                color = tokens.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )

            Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("خلاصه آمار", style = MaterialTheme.typography.titleLarge)
                    StatRow("تعداد کل کلمات", total)
                    StatRow("کلمات تمرین‌شده", stats?.practicedWords ?: 0)
                    StatRow("کلمات تمرین‌نشده", stats?.unpracticedWords ?: total)
                    StatRow("کلمات یادگرفته‌شده", stats?.learnedWords ?: (summary?.learnedConceptCount ?: 0))
                }
            }

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("مرورهای آماده", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.weight(1f))
                Text("$due کلمه", color = tokens.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
            }

            ReviewCard("روزانه", daily, state.dailyTotal) { onStartReview(ReviewType.DAILY) }
            ReviewCard("هفتگی", weekly, state.weeklyTotal) { onStartReview(ReviewType.WEEKLY) }
            ReviewCard("ماهانه", monthly, state.monthlyTotal) { onStartReview(ReviewType.MONTHLY) }

            OutlinedButton(onClick = onAddWord, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("افزودن واژه")
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: Int) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.weight(1f))
        Text(value.toString(), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun ReviewCard(title: String, readyCount: Int, totalCount: Int, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
        Row(
            Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(
                    when (title) {
                        "روزانه" -> "مرور امروز"
                        "هفتگی" -> "تقویت ماندگاری"
                        else -> "حافظه بلندمدت"
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.Start) {
                Text(readyCount.toString(), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                Text("آماده از $totalCount کلمه", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.width(12.dp))
            Icon(Icons.Outlined.CalendarMonth, null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}
