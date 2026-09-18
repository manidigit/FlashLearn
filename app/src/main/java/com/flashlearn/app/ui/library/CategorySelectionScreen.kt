package com.flashlearn.app.ui.library

import com.flashlearn.app.ui.icons.mdiIcon

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import com.flashlearn.app.ui.theme.LocalFlashLearnThemeTokens
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.flashlearn.domain.model.Category
import java.util.UUID


@Composable
fun CategorySelectionScreen(categories: List<Category>, selectedIds: Set<UUID>, counts: Map<UUID, Int>, allCount: Int, onBack: () -> Unit, onApply: (Set<UUID>) -> Unit) {
    val tokens = LocalFlashLearnThemeTokens.current
    var selected by remember(selectedIds) { mutableStateOf(selectedIds) }
    val allSelected = selected.isEmpty()
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 10.dp)) {
        Box(Modifier.fillMaxWidth().height(58.dp)) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterEnd)) { Icon(mdiIcon("arrow-left"), "بازگشت", tint = MaterialTheme.colorScheme.onSurface) }
            Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) { Text("دسته‌بندی‌ها", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)); Text(if (allSelected) "همه دسته‌ها" else "${toFaDigits(selected.size)} دسته انتخاب شده", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall) }
        }
        Spacer(Modifier.height(12.dp))
        CategoryRow("همه دسته‌ها", "${toFaDigits(allCount)} لغت", allSelected) { selected = emptySet() }
        Spacer(Modifier.height(16.dp))
        Text("دسته‌های موجود", Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), textAlign = TextAlign.End)
        Spacer(Modifier.height(8.dp))
        LazyColumn(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 12.dp)) {
            items(categories, key = { it.id }) { category -> CategoryRow(category.name, "${toFaDigits(counts[category.id] ?: 0)} لغت", category.id in selected) { selected = if (category.id in selected) selected - category.id else selected + category.id } }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = { selected = emptySet() }, Modifier.weight(1f).height(54.dp), shape = MaterialTheme.shapes.large) { Text("پاک کردن همه", color = MaterialTheme.colorScheme.primary) }
            Button(onClick = { onApply(selected) }, Modifier.weight(1.2f).height(54.dp), shape = MaterialTheme.shapes.large) { Icon(mdiIcon("check"), contentDescription = null); Spacer(Modifier.width(8.dp)); Text("اعمال") }
        }
    }
}

@Composable
private fun CategoryRow(title: String, subtitle: String, checked: Boolean, onClick: () -> Unit) {
    val tokens = LocalFlashLearnThemeTokens.current
    val icon = categoryIcon(title)
    Card(onClick = onClick, Modifier.fillMaxWidth().height(84.dp), shape = MaterialTheme.shapes.medium, colors = CardDefaults.cardColors(containerColor = tokens.surface), border = androidx.compose.foundation.BorderStroke(1.dp, if (checked) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.outlineVariant)) {
        Row(Modifier.fillMaxSize().padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = checked, onCheckedChange = { onClick() }, colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)); Spacer(Modifier.weight(1f))
            Column(Modifier.weight(3f), horizontalAlignment = Alignment.End) { Text(title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)); Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall) }
            Spacer(Modifier.width(14.dp)); Surface(Modifier.size(48.dp), shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surfaceVariant) { Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(27.dp)) } }
        }
    }
}

@Composable
@Composable
private fun categoryIcon(title: String) = when (title.trim().lowercase()) {
    "همه دسته‌ها" -> mdiIcon("folder-outline")
    "عمومی" -> mdiIcon("book")
    "روزمره", "خانه و زندگی", "زندگی روزمره" -> mdiIcon("home")
    "کار و کسب", "کسب‌وکار", "کار" -> mdiIcon("briefcase-outline")
    "سفر", "گردشگری", "حمل و نقل", "حمل‌ونقل" -> if (title.contains("سفر")) mdiIcon("airplane") else mdiIcon("car-outline")
    "آکادمیک", "تحصیل", "دانشگاه", "آموزش" -> mdiIcon("school-outline")
    "فناوری", "تکنولوژی", "کامپیوتر", "اینترنت" -> mdiIcon("memory")
    "سلامت", "پزشکی", "بدن" -> mdiIcon("hospital-box-outline")
    "غذا و آشپزی", "غذا", "آشپزی", "رستوران" -> mdiIcon("silverware-fork-knife")
    "خرید", "فروشگاه", "بازار" -> mdiIcon("cart-outline")
    "ورزش", "ورزش و تناسب اندام" -> mdiIcon("soccer")
    "هنر", "هنر و فرهنگ", "فرهنگ" -> mdiIcon("palette-outline")
    "فیلم", "سینما", "رسانه" -> mdiIcon("movie-outline")
    "طبیعت", "محیط زیست" -> mdiIcon("leaf")
    "زمان", "تاریخ" -> mdiIcon("clock-outline")
    "احساسات", "خانواده", "روابط" -> mdiIcon("heart-outline")
    else -> when {
        title.contains("سفر") || title.contains("گردش") -> mdiIcon("airplane")
        title.contains("سلامت") || title.contains("پزشک") -> mdiIcon("hospital-box-outline")
        title.contains("غذا") || title.contains("آشپز") -> mdiIcon("silverware-fork-knife")
        title.contains("خرید") -> mdiIcon("cart-outline")
        title.contains("ورزش") -> mdiIcon("soccer")
        title.contains("هنر") || title.contains("فرهنگ") -> mdiIcon("palette-outline")
        title.contains("فناوری") || title.contains("تکنولوژی") -> mdiIcon("memory")
        title.contains("کار") -> mdiIcon("briefcase-outline")
        title.contains("آموز") || title.contains("دانش") -> mdiIcon("school-outline")
        else -> mdiIcon("folder-outline")
    }
}

private fun toFaDigits(value: Int): String = value.toString().map { when (it) { '0' -> '۰'; '1' -> '۱'; '2' -> '۲'; '3' -> '۳'; '4' -> '۴'; '5' -> '۵'; '6' -> '۶'; '7' -> '۷'; '8' -> '۸'; '9' -> '۹'; else -> it } }.joinToString("")
