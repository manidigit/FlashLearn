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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.flashlearn.app.ui.library.LibraryUiState
import com.flashlearn.app.ui.components.FlashLearnScreenHeader
import com.flashlearn.app.ui.theme.LocalFlashLearnThemeTokens

@Composable
fun AddWordMethodScreen(
    onBack: () -> Unit,
    onSingleWord: () -> Unit,
    onBulkWords: () -> Unit,
    onRestoreBackup: () -> Unit,
    libraryState: LibraryUiState = LibraryUiState(),
    onRefreshLibrary: () -> Unit = {},
    onFindDuplicates: () -> Unit = {}
) {
    val tokens = LocalFlashLearnThemeTokens.current
    Column(
        Modifier
            .fillMaxSize()
            .background(tokens.background)
            .padding(horizontal = tokens.screenPadding, vertical = tokens.screenVerticalPadding)
    ) {
        FlashLearnScreenHeader(
            title = "افزودن واژه جدید",
            onBack = onBack
        )
        Spacer(Modifier.height(tokens.compactGap))
        MethodCard("لغات تکی", "افزودن یک واژه جدید", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = .07f), Icons.Outlined.Description, onSingleWord)
        Spacer(Modifier.height(tokens.itemGap + tokens.compactGap))
        MethodCard("لغات گروهی", "وارد کردن چند واژه همزمان", MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.secondary.copy(alpha = .07f), Icons.Outlined.Group, onBulkWords)
        Spacer(Modifier.height(tokens.itemGap + tokens.compactGap))
        MethodCard("ریستور بکاپ", "بازیابی واژه‌ها و اطلاعات از فایل پشتیبان", tokens.success, tokens.success.copy(alpha = .07f), Icons.Outlined.Restore, onRestoreBackup)
        Spacer(Modifier.height(tokens.sectionGap))
        Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(tokens.dp(1f), MaterialTheme.colorScheme.outlineVariant.copy(alpha = .45f))) {
            Row(Modifier.fillMaxWidth().padding(tokens.contentGap), horizontalArrangement = Arrangement.spacedBy(tokens.compactGap), verticalAlignment = Alignment.CenterVertically) {
                StatAction(Icons.Outlined.Search, "پیدا کردن تکراری‌ها", onFindDuplicates, Modifier.weight(1f))
                StatAction(Icons.Outlined.Refresh, "رفرش", onRefreshLibrary, Modifier.weight(1f))
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("تعداد کل واژگان", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(libraryState.totalCount.toString(), style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
        Spacer(Modifier.weight(1f))
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
private fun StatAction(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit, modifier: Modifier) {
    val tokens = LocalFlashLearnThemeTokens.current
    FilledTonalButton(onClick = onClick, modifier = modifier.height(tokens.largeChoiceHeight), shape = MaterialTheme.shapes.medium) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(tokens.iconLarge))
            Text(label, style = MaterialTheme.typography.labelMedium, maxLines = 1)
        }
    }
}
