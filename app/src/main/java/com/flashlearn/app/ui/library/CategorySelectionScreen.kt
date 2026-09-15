package com.flashlearn.app.ui.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun CategorySelectionScreen(categories: List<Category>, selectedIds: Set<UUID>, counts: Map<UUID, Int>, allCount: Int, onBack: () -> Unit, onApply: (Set<UUID>) -> Unit) {
    var selected by remember(selectedIds) { mutableStateOf(selectedIds) }
    val allSelected = selected.isEmpty()
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 10.dp)) {
        Box(Modifier.fillMaxWidth().height(58.dp)) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterEnd)) { Icon(Icons.Outlined.ArrowBack, "بازگشت", tint = Navy) }
            Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) { Text("دسته‌بندی‌ها", color = Navy, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)); Text(if (allSelected) "همه دسته‌ها" else "${toFaDigits(selected.size)} دسته انتخاب شده", color = Color(0xFF8A92A8), style = MaterialTheme.typography.labelSmall) }
        }
        Spacer(Modifier.height(12.dp))
        CategoryRow("همه دسته‌ها", "${toFaDigits(allCount)} لغت", allSelected) { selected = emptySet() }
        Spacer(Modifier.height(16.dp))
        Text("دسته‌های موجود", Modifier.fillMaxWidth(), color = Navy, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), textAlign = TextAlign.End)
        Spacer(Modifier.height(8.dp))
        LazyColumn(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 12.dp)) {
            items(categories, key = { it.id }) { category -> CategoryRow(category.name, "${toFaDigits(counts[category.id] ?: 0)} لغت", category.id in selected) { selected = if (category.id in selected) selected - category.id else selected + category.id } }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = { selected = emptySet() }, Modifier.weight(1f).height(54.dp), shape = RoundedCornerShape(27.dp)) { Text("پاک کردن همه", color = Purple) }
            Button(onClick = { onApply(selected) }, Modifier.weight(1.2f).height(54.dp), shape = RoundedCornerShape(27.dp)) { Text("اعمال ✓") }
        }
    }
}

@Composable
private fun CategoryRow(title: String, subtitle: String, checked: Boolean, onClick: () -> Unit) {
    val icon = categoryIcon(title)
    Card(onClick = onClick, Modifier.fillMaxWidth().height(84.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = androidx.compose.foundation.BorderStroke(1.dp, if (checked) Color(0xFFD9C5FF) else Border)) {
        Row(Modifier.fillMaxSize().padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = checked, onCheckedChange = { onClick() }, colors = CheckboxDefaults.colors(checkedColor = Purple)); Spacer(Modifier.weight(1f))
            Column(Modifier.weight(3f), horizontalAlignment = Alignment.End) { Text(title, color = Navy, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)); Text(subtitle, color = Color(0xFF8A92A8), style = MaterialTheme.typography.bodySmall) }
            Spacer(Modifier.width(14.dp)); Surface(Modifier.size(48.dp), shape = RoundedCornerShape(14.dp), color = Color(0xFFF6F7FB)) { Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = Purple, modifier = Modifier.size(27.dp)) } }
        }
    }
}

private fun categoryIcon(title: String) = when (title.trim().lowercase()) {
    "همه دسته‌ها" -> Icons.Outlined.Folder
    "عمومی" -> Icons.Outlined.Book
    "روزمره", "خانه و زندگی", "زندگی روزمره" -> Icons.Outlined.Home
    "کار و کسب", "کسب‌وکار", "کار" -> Icons.Outlined.BusinessCenter
    "سفر", "گردشگری", "حمل و نقل", "حمل‌ونقل" -> if (title.contains("سفر")) Icons.Outlined.Flight else Icons.Outlined.DirectionsCar
    "آکادمیک", "تحصیل", "دانشگاه", "آموزش" -> Icons.Outlined.School
    "فناوری", "تکنولوژی", "کامپیوتر", "اینترنت" -> Icons.Outlined.Memory
    "سلامت", "پزشکی", "بدن" -> Icons.Outlined.LocalHospital
    "غذا و آشپزی", "غذا", "آشپزی", "رستوران" -> Icons.Outlined.Restaurant
    "خرید", "فروشگاه", "بازار" -> Icons.Outlined.ShoppingCart
    "ورزش", "ورزش و تناسب اندام" -> Icons.Outlined.SportsSoccer
    "هنر", "هنر و فرهنگ", "فرهنگ" -> Icons.Outlined.Palette
    "فیلم", "سینما", "رسانه" -> Icons.Outlined.Movie
    "طبیعت", "محیط زیست" -> Icons.Outlined.Nature
    "زمان", "تاریخ" -> Icons.Outlined.AccessTime
    "احساسات", "خانواده", "روابط" -> Icons.Outlined.FavoriteBorder
    else -> when {
        title.contains("سفر") || title.contains("گردش") -> Icons.Outlined.Flight
        title.contains("سلامت") || title.contains("پزشک") -> Icons.Outlined.LocalHospital
        title.contains("غذا") || title.contains("آشپز") -> Icons.Outlined.Restaurant
        title.contains("خرید") -> Icons.Outlined.ShoppingCart
        title.contains("ورزش") -> Icons.Outlined.SportsSoccer
        title.contains("هنر") || title.contains("فرهنگ") -> Icons.Outlined.Palette
        title.contains("فناوری") || title.contains("تکنولوژی") -> Icons.Outlined.Memory
        title.contains("کار") -> Icons.Outlined.BusinessCenter
        title.contains("آموز") || title.contains("دانش") -> Icons.Outlined.School
        else -> Icons.Outlined.Folder
    }
}

private fun toFaDigits(value: Int): String = value.toString().map { when (it) { '0' -> '۰'; '1' -> '۱'; '2' -> '۲'; '3' -> '۳'; '4' -> '۴'; '5' -> '۵'; '6' -> '۶'; '7' -> '۷'; '8' -> '۸'; '9' -> '۹'; else -> it } }.joinToString("")
