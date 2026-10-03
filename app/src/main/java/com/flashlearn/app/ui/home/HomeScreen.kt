package com.flashlearn.app.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.flashlearn.app.R
import com.flashlearn.app.ui.LanguagePair
import com.flashlearn.app.ui.components.FlashLearnCard
import com.flashlearn.app.ui.components.FlashLearnIconTile
import com.flashlearn.app.ui.components.FlashLearnPrimaryButton
import com.flashlearn.app.ui.components.FlashLearnSectionTitle
import com.flashlearn.app.ui.theme.LocalFlashLearnThemeTokens
import com.flashlearn.domain.model.ReviewType
import kotlin.math.roundToInt

/**
 * Home dashboard — faithfully styled to match the clean web design.
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    languagePair: LanguagePair = LanguagePair(),
    onStartReview: (ReviewType) -> Unit,
    onAddWord: () -> Unit,
    onViewStats: () -> Unit = {}
) {
    val tokens = LocalFlashLearnThemeTokens.current
    val state by viewModel.state.collectAsState()
    LaunchedEffect(languagePair) { viewModel.refresh(languagePair) }
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
        // ── Top bar: greeting + tagline + flags ─────────────────
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.home_greeting),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = tokens.onBackground
                )
                Text(
                    stringResource(R.string.home_tagline),
                    style = MaterialTheme.typography.bodySmall,
                    color = tokens.onSurfaceVariant
                )
            }
            Surface(
                shape = RoundedCornerShape(tokens.cornerMedium),
                border = BorderStroke(tokens.borderThin, tokens.outlineColor.copy(alpha = tokens.cardBorderAlpha)),
                color = tokens.cardColor
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(tokens.compactGap)
                ) {
                    Text(languagePair.source.flag, style = MaterialTheme.typography.titleMedium)
                    Text("⇄", style = MaterialTheme.typography.bodySmall, color = tokens.onSurfaceVariant)
                    Text(languagePair.target.flag, style = MaterialTheme.typography.titleMedium)
                }
            }
        }

        // ── Streak hero (warm golden pill card) ─────────────────
        Surface(
            modifier = Modifier.fillMaxWidth().height(tokens.homeHeroHeight),
            shape = RoundedCornerShape(tokens.cornerLarge),
            color = tokens.primary.copy(alpha = tokens.accentSurfaceAlpha),
            border = BorderStroke(tokens.borderThin, tokens.primary.copy(alpha = tokens.cardBorderStrongAlpha)),
            tonalElevation = tokens.cardElevation
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = tokens.cardPadding, vertical = tokens.contentPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(tokens.cornerMedium),
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
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                    Text(
                        stringResource(R.string.home_streak_days, streakDays),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = tokens.primary
                    )
                    Text(
                        stringResource(R.string.home_learning_streak),
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.onSurfaceVariant
                    )
                }
            }
        }

        // ── Progress Card (with sparkles, track and footer) ─────
        FlashLearnCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(tokens.cardPadding),
                verticalArrangement = Arrangement.spacedBy(tokens.compactGap)
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(tokens.compactGap)
                    ) {
                        Icon(
                            Icons.Outlined.AutoAwesome,
                            contentDescription = null,
                            tint = tokens.primary,
                            modifier = Modifier.size(tokens.iconSmall)
                        )
                        Text(
                            stringResource(R.string.home_learning_progress),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = tokens.onSurface
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        stringResource(R.string.home_percent, progress),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = tokens.primary
                    )
                }
                LinearProgressIndicator(
                    progress = { (progress / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(tokens.progressTrackHeight)
                        .clip(RoundedCornerShape(tokens.cornerSmall)),
                    color = tokens.primary,
                    trackColor = tokens.surfaceVariant
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    val learned = stats?.learnedWords ?: (summary?.learnedConceptCount ?: 0)
                    Text(
                        stringResource(R.string.home_stat_learned_count, learned),
                        style = MaterialTheme.typography.labelSmall,
                        color = tokens.onSurfaceVariant
                    )
                    Text(
                        stringResource(R.string.home_stat_total_count, total),
                        style = MaterialTheme.typography.labelSmall,
                        color = tokens.onSurfaceVariant
                    )
                }
            }
        }

        // ── Statistics Summary Tiles (2x2 Grid) ─────────────────
        Column(verticalArrangement = Arrangement.spacedBy(tokens.compactGap)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.home_stats_summary),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = tokens.onSurface
                )
                Text(
                    stringResource(R.string.home_view_all_stats),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = tokens.primary,
                    modifier = Modifier.clickable { onViewStats() }
                )
            }

            Column(
                Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(tokens.itemGap)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.itemGap)) {
                    WebStyleStatCard(
                        label = stringResource(R.string.home_stat_total_words),
                        value = total.toString(),
                        valueColor = tokens.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    WebStyleStatCard(
                        label = stringResource(R.string.home_stat_practiced_words),
                        value = (stats?.practicedWords ?: 0).toString(),
                        valueColor = Color(0xFF2563EB),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.itemGap)) {
                    WebStyleStatCard(
                        label = stringResource(R.string.home_stat_unpracticed_words),
                        value = (stats?.unpracticedWords ?: total).toString(),
                        valueColor = Color(0xFFD97706),
                        modifier = Modifier.weight(1f)
                    )
                    WebStyleStatCard(
                        label = stringResource(R.string.home_stat_learned),
                        value = (stats?.learnedWords ?: (summary?.learnedConceptCount ?: 0)).toString(),
                        valueColor = Color(0xFF10B981),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // ── Ready reviews ──────────────────────────────────────
        FlashLearnSectionTitle(
            text = stringResource(R.string.home_ready_reviews),
            trailing = {
                Text(
                    stringResource(R.string.home_word_count, due),
                    style = MaterialTheme.typography.labelLarge,
                    color = tokens.onSurfaceVariant
                )
            }
        )

        ReviewReadyCard(stringResource(R.string.home_daily), stringResource(R.string.home_daily_subtitle), daily, state.dailyTotal) {
            onStartReview(ReviewType.DAILY)
        }
        ReviewReadyCard(stringResource(R.string.home_weekly), stringResource(R.string.home_weekly_subtitle), weekly, state.weeklyTotal) {
            onStartReview(ReviewType.WEEKLY)
        }
        ReviewReadyCard(stringResource(R.string.home_monthly), stringResource(R.string.home_monthly_subtitle), monthly, state.monthlyTotal) {
            onStartReview(ReviewType.MONTHLY)
        }

        // ── CTA ────────────────────────────────────────────────
        FlashLearnPrimaryButton(onClick = onAddWord, modifier = Modifier.fillMaxWidth().height(tokens.homeCtaHeight)) {
            Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(tokens.iconMedium))
            Spacer(Modifier.width(tokens.compactGap))
            Text(stringResource(R.string.home_add_word), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(tokens.homeBottomGap))
    }
}

@Composable
private fun WebStyleStatCard(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    val tokens = LocalFlashLearnThemeTokens.current
    Surface(
        modifier = modifier.height(tokens.dp(92f)),
        shape = RoundedCornerShape(tokens.cornerMedium),
        color = tokens.cardColor,
        border = BorderStroke(tokens.borderThin, tokens.outlineColor.copy(alpha = tokens.cardBorderAlpha)),
        tonalElevation = tokens.cardElevation
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(tokens.cardPadding),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = tokens.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Text(
                value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = valueColor
            )
        }
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
    FlashLearnCard(modifier = Modifier.fillMaxWidth().height(tokens.homeReviewHeight), onClick = onClick) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = tokens.cardPadding, vertical = tokens.contentPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(tokens.contentGap)
        ) {
            FlashLearnIconTile(icon = Icons.Outlined.CalendarMonth)
            Spacer(Modifier.width(tokens.compactGap))
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = tokens.onSurface
            )
            Spacer(Modifier.weight(1f))
            Text(
                readyCount.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = tokens.primary
            )
            Text(
                stringResource(R.string.home_ready_of_total, totalCount),
                style = MaterialTheme.typography.labelMedium,
                color = tokens.onSurfaceVariant
            )
        }
    }
}
