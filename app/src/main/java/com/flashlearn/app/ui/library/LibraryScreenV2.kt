package com.flashlearn.app.ui.library

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.flashlearn.app.R
import com.flashlearn.app.ui.LanguagePair
import com.flashlearn.app.ui.components.FlashLearnScreenHeader
import com.flashlearn.app.ui.theme.LocalFlashLearnThemeTokens
import com.flashlearn.domain.model.VocabularyDifficulty
import java.util.UUID

@Composable
fun LibraryScreenV2(
    viewModel: LibraryViewModel,
    languagePair: LanguagePair,
    onBack: () -> Unit,
    onOpen: (UUID) -> Unit,
    onCategories: () -> Unit,
    onAddWord: () -> Unit
) {
    val tokens = LocalFlashLearnThemeTokens.current
    val state by viewModel.state.collectAsState()
    var duplicateMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(languagePair) { viewModel.setLanguagePair(languagePair) }
    
    Column(
        Modifier.fillMaxSize().padding(horizontal = tokens.screenPadding, vertical = tokens.dp(8f))
    ) {
        FlashLearnScreenHeader(title = stringResource(R.string.library_title), onBack = onBack)

        OutlinedTextField(
            value = state.query,
            onValueChange = viewModel::onQueryChange,
            modifier = Modifier.fillMaxWidth().height(tokens.controlHeight),
            placeholder = {
                Text(stringResource(R.string.library_search), Modifier.fillMaxWidth(), textAlign = TextAlign.Start, color = tokens.onSurfaceVariant)
            },
            trailingIcon = { Icon(Icons.Outlined.Search, stringResource(R.string.library_search), tint = tokens.primary) },
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = tokens.surface,
                focusedContainerColor = tokens.surface,
                unfocusedBorderColor = tokens.outlineColor,
                focusedBorderColor = tokens.primary
            )
        )

        Spacer(Modifier.height(tokens.sectionGap))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.dp(9f))) {
            StatCard(stringResource(R.string.library_new), state.newCount, tokens.warning, tokens.warning.copy(alpha = .10f), Icons.Outlined.Add, Modifier.weight(1f), state.filter == LibraryFilter.NEW) { viewModel.onFilterChange(LibraryFilter.NEW) }
            StatCard(stringResource(R.string.library_learning), state.learningCount, tokens.secondary, tokens.secondary.copy(alpha = .10f), Icons.Outlined.History, Modifier.weight(1f), state.filter == LibraryFilter.LEARNING) { viewModel.onFilterChange(LibraryFilter.LEARNING) }
            StatCard(stringResource(R.string.library_learned), state.learnedCount, tokens.success, tokens.success.copy(alpha = .10f), Icons.Outlined.CheckCircle, Modifier.weight(1f), state.filter == LibraryFilter.LEARNED) { viewModel.onFilterChange(LibraryFilter.LEARNED) }
            StatCard(stringResource(R.string.library_total), state.totalCount, tokens.primary, tokens.surfaceVariant, Icons.Outlined.Book, Modifier.weight(1f), state.filter == LibraryFilter.ALL) { viewModel.onFilterChange(LibraryFilter.ALL) }
        }

        Spacer(Modifier.height(tokens.sectionGap))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
            Text(stringResource(R.string.library_filters), color = tokens.onSurface, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.width(tokens.compactGap))
            Icon(Icons.Outlined.FilterList, stringResource(R.string.library_filters), tint = tokens.onSurface)
        }
        Spacer(Modifier.height(tokens.compactGap))

        Card(
            Modifier.fillMaxWidth().clickable(onClick = onCategories),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = tokens.surface),
            border = BorderStroke(tokens.dp(1f), tokens.outlineColor)
        ) {
            Row(Modifier.fillMaxWidth().padding(horizontal = tokens.dp(14f), vertical = tokens.dp(12f)), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.ChevronLeft, stringResource(R.string.library_choose_category), tint = tokens.onSurface)
                Spacer(Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.Start) {
                    Text(stringResource(R.string.library_categories), color = tokens.onSurface, style = MaterialTheme.typography.titleMedium)
                    Text(
                        if (state.selectedCategoryIds.isEmpty()) stringResource(R.string.category_all) else stringResource(R.string.library_selected_categories, toFaDigits(state.selectedCategoryIds.size)),
                        color = tokens.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(Modifier.width(tokens.compactGap))
                Surface(Modifier.size(tokens.dp(48f)), MaterialTheme.shapes.small, color = tokens.surfaceVariant) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Folder, stringResource(R.string.library_categories), tint = tokens.primary, modifier = Modifier.size(tokens.iconLarge))
                    }
                }
            }
        }

        Spacer(Modifier.height(tokens.compactGap))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(tokens.dp(8f)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalButton(
                onClick = { duplicateMessage = null; viewModel.refresh() },
                enabled = !state.isLoading && !state.isDuplicateCleanupBusy,
                modifier = Modifier.weight(1f).height(tokens.controlHeight),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Outlined.Refresh, stringResource(R.string.library_refresh_words), modifier = Modifier.size(tokens.iconMedium))
                Spacer(Modifier.width(tokens.compactGap))
                Text(stringResource(R.string.library_refresh))
            }
            FilledTonalButton(
                onClick = {
                    duplicateMessage = null
                    viewModel.removeExactDuplicates { count ->
                        duplicateMessage = if (count == 0) stringResource(R.string.library_find_duplicates) else stringResource(R.string.library_merged_duplicates, toFaDigits(count))
                    }
                },
                enabled = !state.isLoading && !state.isDuplicateCleanupBusy,
                modifier = Modifier.weight(1f).height(tokens.controlHeight),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(
                    if (state.isDuplicateCleanupBusy) Icons.Outlined.Sync else Icons.Outlined.Search,
                    stringResource(R.string.library_duplicates),
                    modifier = Modifier.size(tokens.iconMedium)
                )
                Spacer(Modifier.width(tokens.compactGap))
                Text(if (state.isDuplicateCleanupBusy) stringResource(R.string.library_merging) else stringResource(R.string.library_duplicates))
            }
        }

        duplicateMessage?.let {
            Spacer(Modifier.height(tokens.dp(6f)))
            Text(it, Modifier.fillMaxWidth(), textAlign = TextAlign.Start, color = tokens.primary, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(tokens.compactGap))
        when {
            state.isLoading && state.items.isEmpty() -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = tokens.primary)
            }
            state.error != null -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(state.error.orEmpty(), color = tokens.error, textAlign = TextAlign.Center)
            }
            state.items.isEmpty() -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.library_not_found))
            }
            else -> LazyColumn(
                Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(tokens.itemGap),
                contentPadding = PaddingValues(bottom = tokens.compactGap)
            ) {
                items(state.items, key = { it.concept.id }) { item -> VocabularyCardV2(item, languagePair, onFavorite = { viewModel.toggleFavorite(item.concept.id) }) { onOpen(item.concept.id) } }
            }
        }
    }
}

@Composable
private fun StatCard(title: String, value: Int, color: Color, background: Color, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier, selected: Boolean, onClick: () -> Unit) {
    val tokens = LocalFlashLearnThemeTokens.current
    Card(
        onClick = onClick,
        modifier = modifier.height(tokens.dp(132f)),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = background),
        border = BorderStroke(if (selected) tokens.dp(2f) else tokens.dp(1f), color.copy(alpha = if (selected) .35f else .14f))
    ) {
        Column(Modifier.fillMaxSize().padding(tokens.dp(6f)), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(tokens.dp(38f)))
            Spacer(Modifier.height(tokens.compactGap))
            Text(title, color = color, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
            Text(toFaDigits(value), color = color, style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@Composable
private fun VocabularyCardV2(item: LibraryItem, languagePair: LanguagePair, onFavorite: () -> Unit, onClick: () -> Unit) {
    val tokens = LocalFlashLearnThemeTokens.current
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick), shape = MaterialTheme.shapes.medium, colors = CardDefaults.cardColors(containerColor = tokens.surface), border = BorderStroke(tokens.dp(1f), tokens.outlineColor)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = tokens.dp(12f), vertical = tokens.dp(11f)), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.MoreVert, stringResource(R.string.library_options), tint = tokens.onSurfaceVariant, modifier = Modifier.size(tokens.iconMedium))
            Spacer(Modifier.width(tokens.compactGap))
            DifficultyPillV2(item.difficulty)
            Spacer(Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.Start) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(item.source?.text ?: "—", color = tokens.onSurface, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.width(tokens.compactGap))
                    Text(languagePair.source.flag)
                }
                if (item.targets.isEmpty()) {
                    Text("—", color = tokens.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                } else {
                    item.targets.forEachIndexed { index, target ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(target.text, color = tokens.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.width(tokens.compactGap))
                            Text(languagePair.target.flag)
                            if (index < item.targets.lastIndex) Text("،", color = tokens.onSurfaceVariant)
                        }
                    }
                }
                item.category?.let { Text(it.name, color = tokens.secondary, style = MaterialTheme.typography.labelSmall) }
            }
            Spacer(Modifier.width(tokens.compactGap))
            IconButton(onClick = onFavorite) { Icon(if (item.concept.favorite) Icons.Outlined.Star else Icons.Outlined.StarBorder, stringResource(R.string.library_favorite), tint = if (item.concept.favorite) tokens.primary else tokens.onSurfaceVariant, modifier = Modifier.size(tokens.iconLarge)) }
        }
    }
}

@Composable
private fun DifficultyPillV2(difficulty: VocabularyDifficulty?) {
    val tokens = LocalFlashLearnThemeTokens.current
    val v = when (difficulty) {
        VocabularyDifficulty.EASY -> Triple(stringResource(R.string.difficulty_easy), tokens.success.copy(alpha = .10f), tokens.success)
        VocabularyDifficulty.MEDIUM -> Triple(stringResource(R.string.difficulty_medium), tokens.warning.copy(alpha = .10f), tokens.warning)
        VocabularyDifficulty.HARD -> Triple(stringResource(R.string.difficulty_hard), tokens.error.copy(alpha = .10f), tokens.error)
        VocabularyDifficulty.VERY_HARD -> Triple(stringResource(R.string.difficulty_very_hard), tokens.error.copy(alpha = .10f), tokens.error)
        null -> Triple(stringResource(R.string.difficulty_unknown), tokens.surfaceVariant, tokens.onSurfaceVariant)
    }
    Surface(shape = MaterialTheme.shapes.medium, color = v.second) {
        Text(v.first, color = v.third, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(horizontal = tokens.dp(12f), vertical = tokens.dp(7f)))
    }
}

private fun toFaDigits(value: Int): String = value.toString().map {
    when (it) {
        '0' -> '۰'; '1' -> '۱'; '2' -> '۲'; '3' -> '۳'; '4' -> '۴'
        '5' -> '۵'; '6' -> '۶'; '7' -> '۷'; '8' -> '۸'; '9' -> '۹'
        else -> it
    }
}.joinToString("")
