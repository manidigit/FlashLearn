package com.flashlearn.app.ui.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.flashlearn.domain.model.Category
import java.util.UUID

private val Navy = Color(0xFF0B236B)
private val Purple = Color(0xFF7C2BEF)
private val Border = Color(0xFFE1E7F3)

@Composable
fun CategorySelectionScreen(
    categories: List<Category>,
    selectedIds: Set<UUID>,
    counts: Map<UUID, Int>,
    onBack: () -> Unit,
    onApply: (Set<UUID>) -> Unit
) {
    var selected by remember(selectedIds) { mutableStateOf(selectedIds) }
    val allSelected = selected.isEmpty()
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 10.dp)) {
        Box(Modifier.fillMaxWidth().height(58.dp)) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterEnd)) { Icon(Icons.Outlined.ArrowBack, "بازگشت", tint = Navy) }
            Text("دسته‌بندی‌ها", Modifier.align(Alignment.Center), color = Navy, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
        }
        Spacer(Modifier.height(12.dp))
        CategoryRow("همه دسته‌ها", "انتخاب همه دسته‌ها", null, allSelected, null) { selected = emptySet() }
        Spacer(Modifier.height(20.dp))
        Text("دسته‌های موجود", Modifier.fillMaxWidth(), color = Navy, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), textAlign = TextAlign.End)
        Spacer(Modifier.height(10.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            categories.forEach { category ->
                CategoryRow(category.name, category.description.orEmpty(), counts[category.id], category.id in selected, category.id) {
                    selected = if (category.id in selected) selected - category.id else selected + category.id
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = { selected = emptySet() }, Modifier.weight(1f).height(54.dp), shape = RoundedCornerShape(27.dp)) { Text("پاک کردن همه", color = Purple) }
            Button(onClick = { onApply(selected) }, Modifier.weight(1.2f).height(54.dp), shape = RoundedCornerShape(27.dp)) { Text("اعمال") }
        }
    }
}

@Composable
private fun CategoryRow(
    title: String,
    subtitle: String,
    count: Int?,
    checked: Boolean,
    categoryId: UUID?,
    onClick: () -> Unit
) {
    val icon = when (title) {
        "همه دسته‌ها" -> Icons.Outlined.Folder
        "عمومی" -> Icons.Outlined.Book
        "روزمره" -> Icons.Outlined.Home
        "کار و کسب" -> Icons.Outlined.BusinessCenter
        "سفر" -> Icons.Outlined.Flight
        "آکادمیک" -> Icons.Outlined.School
        "فناوری" -> Icons.Outlined.Memory
        "سلامت" -> Icons.Outlined.FavoriteBorder
        "غذا و آشپزی" -> Icons.Outlined.Restaurant
        else -> Icons.Outlined.Folder
    }
    Card(onClick = onClick, Modifier.fillMaxWidth().height(84.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = androidx.compose.foundation.BorderStroke(1.dp, if (checked) Color(0xFFD9C5FF) else Border)) {
        Row(Modifier.fillMaxSize().padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = checked, onCheckedChange = { onClick() }, colors = CheckboxDefaults.colors(checkedColor = Purple))
            Spacer(Modifier.weight(1f))
            Column(Modifier.weight(3f), horizontalAlignment = Alignment.End) {
                Text(title, color = Navy, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                if (subtitle.isNotBlank()) Text(subtitle, color = Color(0xFF8A92A8), style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.width(14.dp))
            Surface(Modifier.size(48.dp), shape = RoundedCornerShape(14.dp), color = Color(0xFFF6F7FB)) { Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = Purple, modifier = Modifier.size(27.dp)) } }
        }
    }
}
