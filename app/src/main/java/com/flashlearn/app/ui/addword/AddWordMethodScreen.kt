package com.flashlearn.app.ui.addword

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
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
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Box(Modifier.fillMaxWidth().height(58.dp)) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterEnd)) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "بازگشت", tint = MaterialTheme.colorScheme.onSurface)
            }
            Text(
                "افزودن واژه جدید",
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
        }
        Spacer(Modifier.height(12.dp))
        MethodCard("لغات تکی", "افزودن یک واژه جدید", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = .07f), Icons.Outlined.Description, onSingleWord)
        Spacer(Modifier.height(14.dp))
        MethodCard("لغات گروهی", "وارد کردن چند واژه همزمان", MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.secondary.copy(alpha = .07f), Icons.Outlined.Group, onBulkWords)
        Spacer(Modifier.height(14.dp))
        MethodCard("ریستور بکاپ", "بازیابی واژه‌ها و اطلاعات از فایل پشتیبان", tokens.success, tokens.success.copy(alpha = .07f), Icons.Outlined.Restore, onRestoreBackup)
        Spacer(Modifier.height(18.dp))
        Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = .45f))) {
            Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
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
    Card(Modifier.fillMaxWidth().height(132.dp).clickable(onClick = onClick), shape = MaterialTheme.shapes.medium, colors = CardDefaults.cardColors(containerColor = background), border = BorderStroke(1.dp, color.copy(alpha = .24f))) {
        Row(Modifier.fillMaxSize().padding(horizontal = 22.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(42.dp))
            Spacer(Modifier.width(18.dp))
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                Text(title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), textAlign = TextAlign.End)
                Spacer(Modifier.height(6.dp))
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.End)
            }
            Spacer(Modifier.width(12.dp))
            Text("‹", color = color, style = MaterialTheme.typography.displaySmall)
        }
    }
}

@Composable
private fun StatAction(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit, modifier: Modifier) {
    FilledTonalButton(onClick = onClick, modifier = modifier.height(72.dp), shape = MaterialTheme.shapes.medium) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(25.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, maxLines = 1)
        }
    }
}
