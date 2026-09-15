package com.flashlearn.app.ui.addword

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private val Navy = Color(0xFF0B236B)
private val Purple = Color(0xFF7C2BEF)
private val Blue = Color(0xFF147BE8)
private val Green = Color(0xFF0BAA6A)
private val SoftPurple = Color(0xFFF7F1FF)
private val SoftBlue = Color(0xFFF1F8FF)
private val SoftGreen = Color(0xFFF0FFF8)
private val Border = Color(0xFFE1E7F3)

@Composable
fun AddWordMethodScreen(
    onBack: () -> Unit,
    onSingleWord: () -> Unit,
    onBulkWords: () -> Unit,
    onRestore: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().background(Color.White).padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Box(Modifier.fillMaxWidth().height(58.dp)) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterEnd)) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "بازگشت", tint = Navy)
            }
            Text(
                "افزودن واژه",
                modifier = Modifier.align(Alignment.Center),
                color = Navy,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "روش مورد نظر خود را انتخاب کنید",
            Modifier.fillMaxWidth(),
            color = Color(0xFF78839D),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(26.dp))
        MethodCard("لغات تکی", "افزودن یک واژه جدید", Purple, SoftPurple, Icons.Outlined.Description, onSingleWord)
        Spacer(Modifier.height(16.dp))
        MethodCard("لغات گروهی", "وارد کردن چند واژه همزمان", Blue, SoftBlue, Icons.Outlined.Group, onBulkWords)
        Spacer(Modifier.height(16.dp))
        MethodCard("ریستور بکاپ", "بازیابی از فایل پشتیبان", Green, SoftGreen, Icons.Outlined.CloudUpload, onRestore)
        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun MethodCard(
    title: String,
    subtitle: String,
    color: Color,
    background: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        Modifier.fillMaxWidth().height(148.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = background),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = .24f))
    ) {
        Row(Modifier.fillMaxSize().padding(horizontal = 22.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("›", color = color, style = MaterialTheme.typography.displaySmall)
            Spacer(Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.End) {
                Text(title, color = Navy, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                Spacer(Modifier.height(6.dp))
                Text(subtitle, color = Color(0xFF78839D), style = MaterialTheme.typography.bodyLarge)
            }
            Spacer(Modifier.width(18.dp))
            Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(42.dp))
        }
    }
}
