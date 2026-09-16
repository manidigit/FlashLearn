package com.flashlearn.app.ui.addword

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material3.*
import com.flashlearn.app.ui.theme.LocalFlashLearnThemeTokens
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp


@Composable
fun AddWordMethodScreen(
    onBack: () -> Unit,
    onSingleWord: () -> Unit,
    onBulkWords: () -> Unit,
    onRestoreBackup: () -> Unit
) {
    val tokens = LocalFlashLearnThemeTokens.current
    Column(
        Modifier.fillMaxSize().background(tokens.background).padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Box(Modifier.fillMaxWidth().height(58.dp)) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterEnd)) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "بازگشت", tint = MaterialTheme.colorScheme.onSurface)
            }
            Text("افزودن واژه", modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
        }
        Spacer(Modifier.height(12.dp))
        Text("روش مورد نظر خود را انتخاب کنید", Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(26.dp))
        MethodCard("لغات تکی", "افزودن یک واژه جدید", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.surfaceVariant, Icons.Outlined.Description, onSingleWord)
        Spacer(Modifier.height(16.dp))
        MethodCard("لغات گروهی", "وارد کردن چند واژه همزمان", MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.secondary.copy(alpha = .10f), Icons.Outlined.Group, onBulkWords)
        Spacer(Modifier.height(16.dp))
        MethodCard("ریستور بکاپ", "بازیابی واژه‌ها و اطلاعات از فایل پشتیبان", tokens.success, tokens.success.copy(alpha = .10f), Icons.Outlined.Restore, onRestoreBackup)
        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun MethodCard(title: String, subtitle: String, color: Color, background: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    val tokens = LocalFlashLearnThemeTokens.current
    Card(Modifier.fillMaxWidth().height(132.dp).clickable(onClick = onClick), shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = background),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = .24f))) {
        Row(Modifier.fillMaxSize().padding(horizontal = 22.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("›", color = color, style = MaterialTheme.typography.displaySmall)
            Spacer(Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.End) {
                Text(title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                Spacer(Modifier.height(6.dp))
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.End)
            }
            Spacer(Modifier.width(18.dp))
            Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(42.dp))
        }
    }
}
