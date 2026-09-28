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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.flashlearn.app.R
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
        // ── Top bar: greeting + flags ───────────────────────────
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.home_greeting),
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
            modifier = Modifier.fillMaxWidth().height(tokens.homeHeroHeight),
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
                        stringResource(R.string.home_streak_days, streakDays),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
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

        // ── Progress ───────────────────────────────────────────
        Column(verticalArrangement = Arrangement.spacedBy(tokens.tinyGap)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.home_learning_progress),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = tokens.onSurface
                )
                Spacer(Modifier.weight(1f))
                Text(
                    stringResource(R.string.home_percent, progress),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
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
        }

        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val compact = maxWidth < tokens.design.metric("adaptiveMediumBreakpoint").dp
            if (compact) {
                Column(
                    Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(tokens.itemGap)
                ) {
                    FlashLearnStatTile(stringResource(R.string.home_stat_total_words), total.toString(), Modifier.fillMaxWidth())
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.itemGap)) {
                        FlashLearnStatTile(stringResource(R.string.home_stat_practiced_words), (stats?.practicedWords ?: 0).toString(), Modifier.weight(1f))
                        FlashLearnStatTile(stringResource(R.string.home_stat_unpracticed_words), (stats?.unpracticedWords ?: total).toString(), Modifier.weight(1f))
                    }
                    FlashLearnStatTile(stringResource(R.string.home_stat_learned), (stats?.learnedWords ?: (summary?.learnedConceptCount ?: 0)).toString(), Modifier.fillMaxWidth())
                }
            } else {
                when (tokens.statsLayout) {
                    com.flashlearn.app.ui.theme.StatsLayoutStrategy.GRID_4_COLUMNS ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.itemGap)) {
                            FlashLearnStatTile(stringResource(R.string.home_stat_total), total.toString(), Modifier.weight(1f))
                            FlashLearnStatTile(stringResource(R.string.home_stat_practiced), (stats?.practicedWords ?: 0).toString(), Modifier.weight(1f))
                            FlashLearnStatTile(stringResource(R.string.home_stat_remaining), (stats?.unpracticedWords ?: total).toString(), Modifier.weight(1f))
                            FlashLearnStatTile(stringResource(R.string.home_stat_learned), (stats?.learnedWords ?: (summary?.learnedConceptCount ?: 0)).toString(), Modifier.weight(1f))
                        }
                    com.flashlearn.app.ui.theme.StatsLayoutStrategy.VERTICAL_LIST ->
                        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(tokens.itemGap)) {
                            FlashLearnStatTile(stringResource(R.string.home_stat_total_words), total.toString(), Modifier.fillMaxWidth())
                            FlashLearnStatTile(stringResource(R.string.home_stat_practiced_words), (stats?.practicedWords ?: 0).toString(), Modifier.fillMaxWidth())
                            FlashLearnStatTile(stringResource(R.string.home_stat_unpracticed_words), (stats?.unpracticedWords ?: total).toString(), Modifier.fillMaxWidth())
                            FlashLearnStatTile(stringResource(R.string.home_stat_learned), (stats?.learnedWords ?: (summary?.learnedConceptCount ?: 0)).toString(), Modifier.fillMaxWidth())
                        }
                    else ->
                        FlashLearnCard(modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.fillMaxWidth().padding(tokens.cardPadding), verticalArrangement = Arrangement.spacedBy(tokens.contentGap)) {
                                Text(stringResource(R.string.home_stats_summary), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = tokens.onSurface)
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.compactGap)) {
                                    FlashLearnStatTile(stringResource(R.string.home_stat_total_words), total.toString(), Modifier.weight(1f))
                                    FlashLearnStatTile(stringResource(R.string.home_stat_practiced_words), (stats?.practicedWords ?: 0).toString(), Modifier.weight(1f))
                                }
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.compactGap)) {
                                    FlashLearnStatTile(stringResource(R.string.home_stat_unpracticed_words), (stats?.unpracticedWords ?: total).toString(), Modifier.weight(1f))
                                    FlashLearnStatTile(stringResource(R.string.home_stat_learned), (stats?.learnedWords ?: (summary?.learnedConceptCount ?: 0)).toString(), Modifier.weight(1f))
                                }
                            }
                        }
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
                stringResource(R.string.home_ready_of_total, readyCount, totalCount),
                style = MaterialTheme.typography.labelMedium,
                color = tokens.onSurfaceVariant
            )
        }
    }
}
