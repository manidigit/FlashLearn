package com.flashlearn.app.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.flashlearn.app.ui.LanguagePair
import com.flashlearn.app.ui.components.FlashLearnCard
import com.flashlearn.app.ui.components.FlashLearnIconTile
import com.flashlearn.app.ui.components.FlashLearnPrimaryButton
import com.flashlearn.app.ui.components.FlashLearnSectionTitle
import com.flashlearn.app.ui.components.FlashLearnStatTile
import com.flashlearn.app.ui.theme.LocalFlashLearnThemeTokens
import com.flashlearn.domain.model.ReviewType
import kotlin.math.roundToInt

/**
 * Home dashboard — structure matches the Grok luxury mockup.
 * All colors, radii, elevation and accents come from theme tokens
 * so switching theme changes the personality without hard-coded luxury.
 */
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
    val progress = state.progressPercentage.roundToInt()
    val streakDays = state.streak?.currentStreakDays ?: 0

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = tokens.screenPadding, vertical = tokens.screenVerticalPadding),
        verticalArrangement = Arrangement.spacedBy(tokens.sectionGap)
    ) {
        // ── Top bar: greeting + flags ───────────────────────────
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "سلام!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = tokens.onBackground
            )
            Spacer(Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(tokens.compactGap)) {
                Text(languagePair.source.flag, style = MaterialTheme.typography.headlineSmall)
                Text(languagePair.target.flag, style = MaterialTheme.typography.headlineSmall)
            }
        }

        // ── Streak hero (mockup: gold fire pill / card) ─────────
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = tokens.primary.copy(alpha = tokens.accentSurfaceAlpha),
            border = BorderStroke(tokens.borderThin, tokens.primary.copy(alpha = tokens.cardBorderStrongAlpha)),
            tonalElevation = tokens.cardElevation
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = tokens.cardPadding, vertical = tokens.contentPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = tokens.primary.copy(alpha = 0.22f),
                    modifier = Modifier.size(tokens.iconTileSize)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Outlined.LocalFireDepartment,
                            contentDescription = null,
                            tint = tokens.primary,
                            modifier = Modifier.size(tokens.iconLarge)
                        )
                    }
                }
                Spacer(Modifier.width(tokens.contentGap))
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        "$streakDays روز پیوسته",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = tokens.primary
                    )
                    Text(
                        "استریک یادگیری",
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.onSurfaceVariant
                    )
                }
            }
        }

        // ── Progress ───────────────────────────────────────────
        Column(verticalArrangement = Arrangement.spacedBy(tokens.tinyGap)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "پیشرفت یادگیری",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = tokens.onSurface
                )
                Spacer(Modifier.weight(1f))
                Text(
                    "$progress٪",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = tokens.primary
                )
            }
            LinearProgressIndicator(
                progress = { (progress / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(tokens.progressTrackHeight + 2.dp)
                    .clip(RoundedCornerShape(tokens.cornerSmall)),
                color = tokens.primary,
                trackColor = tokens.surfaceVariant
            )
        }

        // ── Stats 2×2 (mockup: big gold numbers) ───────────────
        FlashLearnCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(tokens.cardPadding),
                verticalArrangement = Arrangement.spacedBy(tokens.contentGap)
            ) {
                Text(
                    "خلاصه آمار",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = tokens.onSurface
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.compactGap)) {
                    FlashLearnStatTile("کل واژه‌ها", total.toString(), Modifier.weight(1f))
                    FlashLearnStatTile("تمرین‌شده", (stats?.practicedWords ?: 0).toString(), Modifier.weight(1f))
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.compactGap)) {
                    FlashLearnStatTile("تمرین‌نشده", (stats?.unpracticedWords ?: total).toString(), Modifier.weight(1f))
                    FlashLearnStatTile(
                        "یادگرفته",
                        (stats?.learnedWords ?: (summary?.learnedConceptCount ?: 0)).toString(),
                        Modifier.weight(1f)
                    )
                }
            }
        }

        // ── Ready reviews ──────────────────────────────────────
        FlashLearnSectionTitle(
            text = "مرورهای آماده",
            trailing = {
                Text(
                    "$due کلمه",
                    style = MaterialTheme.typography.labelLarge,
                    color = tokens.onSurfaceVariant
                )
            }
        )

        ReviewReadyCard("روزانه", "مرور امروز", daily, state.dailyTotal) {
            onStartReview(ReviewType.DAILY)
        }
        ReviewReadyCard("هفتگی", "تقویت ماندگاری", weekly, state.weeklyTotal) {
            onStartReview(ReviewType.WEEKLY)
        }
        ReviewReadyCard("ماهانه", "حافظه بلندمدت", monthly, state.monthlyTotal) {
            onStartReview(ReviewType.MONTHLY)
        }

        // ── CTA ────────────────────────────────────────────────
        FlashLearnPrimaryButton(onClick = onAddWord, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(tokens.iconMedium))
            Spacer(Modifier.width(tokens.compactGap))
            Text("افزودن واژه", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(tokens.sectionGap))
    }
}

@Composable
private fun ReviewReadyCard(
    title: String,
    subtitle: String,
    readyCount: Int,
    totalCount: Int,
    onClick: () -> Unit
) {
    val tokens = LocalFlashLearnThemeTokens.current
    FlashLearnCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = tokens.cardPadding, vertical = tokens.contentPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FlashLearnIconTile(icon = Icons.Outlined.CalendarMonth)
            Spacer(Modifier.width(tokens.contentGap))
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = tokens.onSurface
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = tokens.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    readyCount.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = tokens.primary
                )
                Text(
                    "از $totalCount",
                    style = MaterialTheme.typography.labelSmall,
                    color = tokens.onSurfaceVariant
                )
            }
        }
    }
}
