package com.flashlearn.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.flashlearn.app.ui.components.PurpleHeroCard
import com.flashlearn.domain.model.ReviewType

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onStartReview: (ReviewType) -> Unit,
    onAddWord: () -> Unit,
    onBulkImport: () -> Unit,
    onLibrary: () -> Unit,
    onProgress: () -> Unit,
    onLanguage: () -> Unit,
    onBackup: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val summary = state.summary
    val due = summary?.dueConceptCount ?: 0
    val total = summary?.activeConceptCount ?: 0
    val daily = summary?.dailyDueConceptCount ?: 0
    var menuExpanded by remember { mutableStateOf(false) }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box {
                IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(48.dp)) {
                    Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = .12f)) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Menu, "منو", tint = MaterialTheme.colorScheme.primary) }
                    }
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(text = { Text("انتخاب زبان") }, leadingIcon = { Icon(Icons.Outlined.Language, null) }, onClick = { menuExpanded = false; onLanguage() })
                    DropdownMenuItem(text = { Text("پشتیبان‌گیری و بازیابی") }, leadingIcon = { Icon(Icons.Outlined.Backup, null) }, onClick = { menuExpanded = false; onBackup() })
                }
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                Text("سلام مانی!", style = MaterialTheme.typography.headlineSmall)
                Text("به فلش‌لرن خوش آمدی 👋", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PurpleHeroCard("روز پیوسته", "${state.streak?.currentStreakDays ?: 0}", "روز پیوسته")
                Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 15.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = .10f)), contentAlignment = Alignment.Center) {
                            Text("📖", style = MaterialTheme.typography.titleLarge)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                            Text("$total", style = MaterialTheme.typography.headlineMedium)
                            Text("کلمه منتظر شماست", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        Text("ادامه بده", style = MaterialTheme.typography.titleMedium)
        Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("☀️", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("روزانه", style = MaterialTheme.typography.titleMedium)
                        Text("$daily از $due", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                }
                LinearProgressIndicator(
                    progress = { if (due == 0) 0f else (daily.toFloat() / due).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(8.dp)),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickAction("مرور کلمات", Icons.Outlined.Sync, Modifier.weight(1f)) { onStartReview(ReviewType.DAILY) }
            QuickAction("آمار و گزارش", Icons.Outlined.BarChart, Modifier.weight(1f), onProgress)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickAction("واژگان", Icons.Outlined.Book, Modifier.weight(1f), onLibrary)
            QuickAction("افزودن واژه", Icons.Outlined.Add, Modifier.weight(1f), onAddWord)
        }
    }
}

@Composable
private fun QuickAction(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Card(modifier = modifier, shape = MaterialTheme.shapes.large) {
        Column(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 17.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = .09f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(25.dp))
            }
            Spacer(Modifier.height(7.dp))
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    }
}
