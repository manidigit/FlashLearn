package com.flashlearn.app.ui.addword

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.flashlearn.app.R
import com.flashlearn.app.ui.library.LibraryUiState
import com.flashlearn.app.ui.components.FlashLearnScreenHeader
import com.flashlearn.app.ui.theme.LocalFlashLearnThemeTokens
import com.flashlearn.domain.usecase.ExactDuplicateGroup

@Composable
fun AddWordMethodScreen(
    onBack: () -> Unit,
    onSingleWord: () -> Unit,
    onBulkWords: () -> Unit,
    onRestoreBackup: () -> Unit,
    libraryState: LibraryUiState = LibraryUiState(),
    onRefreshLibrary: () -> Unit = {},
    onFindDuplicates: () -> Unit = {},
    isFindingDuplicates: Boolean = false,
    duplicateGroups: List<ExactDuplicateGroup>? = null,
    onDismissDuplicateResults: () -> Unit = {},
    needsReviewCount: Int = 0,
    onNeedsReview: () -> Unit = {}
) {
    val tokens = LocalFlashLearnThemeTokens.current
    Column(Modifier.fillMaxSize().background(tokens.background).padding(horizontal = tokens.screenPadding, vertical = tokens.screenVerticalPadding)) {
        FlashLearnScreenHeader(title = stringResource(R.string.addword_method_title), onBack = onBack)
        Spacer(Modifier.height(tokens.compactGap))
        MethodCard(stringResource(R.string.addword_single_words), stringResource(R.string.addword_single_words_summary), MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = .07f), Icons.Outlined.Description, onSingleWord)
        Spacer(Modifier.height(tokens.itemGap + tokens.compactGap))
        MethodCard(stringResource(R.string.addword_bulk_words), stringResource(R.string.addword_bulk_words_summary), MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.secondary.copy(alpha = .07f), Icons.Outlined.Group, onBulkWords)
        Spacer(Modifier.height(tokens.itemGap + tokens.compactGap))
        MethodCard(stringResource(R.string.addword_restore_backup), stringResource(R.string.addword_restore_backup_summary), tokens.success, tokens.success.copy(alpha = .07f), Icons.Outlined.Restore, onRestoreBackup)
        Spacer(Modifier.height(tokens.sectionGap))
        Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(tokens.dp(1f), MaterialTheme.colorScheme.outlineVariant.copy(alpha = .45f))) {
            Row(Modifier.fillMaxWidth().padding(tokens.contentGap), horizontalArrangement = Arrangement.spacedBy(tokens.compactGap), verticalAlignment = Alignment.CenterVertically) {
                StatAction(Icons.Outlined.Search, if (isFindingDuplicates) stringResource(R.string.addword_finding_duplicates) else stringResource(R.string.addword_find_duplicates), onFindDuplicates, Modifier.weight(1f), enabled = !isFindingDuplicates)
                StatAction(Icons.Outlined.Search, stringResource(R.string.addword_needs_review, needsReviewCount), onNeedsReview, Modifier.weight(1f))
                StatAction(Icons.Outlined.Refresh, stringResource(R.string.addword_refresh), onRefreshLibrary, Modifier.weight(1f))
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.addword_total_words), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(libraryState.totalCount.toString(), style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
        Spacer(Modifier.weight(1f))
    }
    if (duplicateGroups != null) {
        AlertDialog(onDismissRequest = onDismissDuplicateResults, title = { Text(stringResource(R.string.addword_duplicates_found_title)) }, text = {
            if (duplicateGroups.isEmpty()) Text(stringResource(R.string.addword_no_duplicates_found)) else {
                val extraCount = duplicateGroups.sumOf { it.count - 1 }
                Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                    Text(stringResource(R.string.addword_duplicates_found_summary, duplicateGroups.size, extraCount))
                    Spacer(Modifier.height(tokens.compactGap))
                    duplicateGroups.take(20).forEach { group -> Text("• ${group.sourceText} (${group.count})") }
                    if (duplicateGroups.size > 20) { Spacer(Modifier.height(tokens.compactGap)); Text(stringResource(R.string.addword_duplicates_more, duplicateGroups.size - 20)) }
                }
            }
        }, confirmButton = { TextButton(onClick = onDismissDuplicateResults) { Text(stringResource(R.string.addword_close)) } })
    }
}

@Composable
private fun MethodCard(title: String, subtitle: String, color: Color, background: Color, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    val tokens = LocalFlashLearnThemeTokens.current
    Card(Modifier.fillMaxWidth().height(tokens.dp(132f)).clickable(onClick = onClick), shape = MaterialTheme.shapes.medium, colors = CardDefaults.cardColors(containerColor = background), border = BorderStroke(tokens.dp(1f), color.copy(alpha = .24f))) {
        Row(Modifier.fillMaxSize().padding(horizontal = tokens.dp(22f)), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(tokens.dp(42f)))
            Spacer(Modifier.width(tokens.contentGap))
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                Text(title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), textAlign = TextAlign.Start)
                Spacer(Modifier.height(tokens.microGap))
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Start)
            }
            Spacer(Modifier.width(tokens.contentGap))
            Icon(Icons.AutoMirrored.Outlined.KeyboardArrowLeft, contentDescription = null, tint = color, modifier = Modifier.size(tokens.iconLarge))
        }
    }
}

@Composable
private fun StatAction(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit, modifier: Modifier, enabled: Boolean = true) {
    val tokens = LocalFlashLearnThemeTokens.current
    FilledTonalButton(onClick = onClick, enabled = enabled, modifier = modifier.height(tokens.largeChoiceHeight), shape = MaterialTheme.shapes.medium) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(tokens.iconLarge))
            Text(label, style = MaterialTheme.typography.labelMedium, maxLines = 1)
        }
    }
}