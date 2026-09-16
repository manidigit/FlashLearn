package com.flashlearn.app.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.flashlearn.app.ui.LanguagePair
import com.flashlearn.domain.model.VocabularyDifficulty
import java.util.UUID

private val Navy = Color(0xFF0B236B); private val Purple = Color(0xFF7C2BEF); private val Green = Color(0xFF0BAA6A); private val Blue = Color(0xFF147BE8); private val Orange = Color(0xFFF59E0B)
private val SoftPurple = Color(0xFFF7F1FF); private val SoftGreen = Color(0xFFF0FFF8); private val SoftBlue = Color(0xFFF1F8FF); private val SoftOrange = Color(0xFFFFFBF0); private val Border = Color(0xFFE3E8F5)

@Composable
fun LibraryScreen(viewModel: LibraryViewModel, languagePair: LanguagePair = LanguagePair(), onBack: () -> Unit = {}, onOpen: (UUID) -> Unit = {}, onAddWord: () -> Unit = {}, onBulkImport: () -> Unit = {}) {
    val state by viewModel.state.collectAsState(); LaunchedEffect(languagePair) { viewModel.setLanguagePair(languagePair) }; LaunchedEffect(Unit) { viewModel.refresh() }
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(horizontal = 16.dp, vertical = 8.dp)) {
        Box(Modifier.fillMaxWidth().height(58.dp)) { IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterEnd)) { Icon(imageVector = Icons.Outlined.ArrowBack, contentDescription = "بازگشت", tint = Navy) }; Text("واژگان", Modifier.align(Alignment.Center), color = Navy, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)) }
        OutlinedTextField(value = state.query, onValueChange = viewModel::onQueryChange, modifier = Modifier.fillMaxWidth().height(58.dp), placeholder = { Text("جستجو در واژگان...", color = Color(0xFF8C92A7), textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth()) }, trailingIcon = { Row(verticalAlignment = Alignment.CenterVertically) { Icon(imageVector = Icons.Outlined.Search, contentDescription = "جستجو", tint = Navy); IconButton(onClick = viewModel::refresh, enabled = !state.isLoading) { Icon(imageVector = Icons.Outlined.Refresh, contentDescription = "رفرش واژگان", tint = Purple) } } }, singleLine = true, shape = RoundedCornerShape(20.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFC8D0EA), unfocusedBorderColor = Color(0xFFD6DCF0), focusedContainerColor = Color.White, unfocusedContainerColor = Color.White))
        Spacer(Modifier.height(24.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) { LibraryStatCard("جدید", state.newCount, Orange, SoftOrange, Icons.Outlined.Add, Modifier.weight(1f)); LibraryStatCard("در حال یادگیری", state.learningCount, Blue, SoftBlue, Icons.Outlined.History, Modifier.weight(1f)); LibraryStatCard("یادگرفته", state.learnedCount, Green, SoftGreen, Icons.Outlined.CheckCircle, Modifier.weight(1f)); LibraryStatCard("کل واژگان", state.totalCount, Purple, SoftPurple, Icons.Outlined.Book, Modifier.weight(1f)) }
        Spacer(Modifier.height(24.dp)); Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) { Text("فیلترها", color = Navy, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)); Spacer(Modifier.width(8.dp)); Icon(imageVector = Icons.Outlined.FilterList, contentDescription = "فیلترها", tint = Navy) }; Spacer(Modifier.height(12.dp))
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = androidx.compose.foundation.BorderStroke(1.dp, Border)) { Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) { Surface(Modifier.size(48.dp), shape = RoundedCornerShape(15.dp), color = SoftPurple) { Box(contentAlignment = Alignment.Center) { Icon(imageVector = Icons.Outlined.Folder, contentDescription = "دسته‌بندی‌ها", tint = Purple, modifier = Modifier.size(28.dp)) } }; Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) { Text("دسته‌بندی‌ها", color = Navy, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)); Text("همه دسته‌ها • ${toFaDigits(state.categories.size)} دسته انتخاب شده", color = Color(0xFF858CA4), style = MaterialTheme.typography.bodySmall) }; Icon(imageVector = Icons.Outlined.ArrowBack, contentDescription = "انتخاب دسته", tint = Navy) } }
        Spacer(Modifier.height(6.dp)); if (state.categories.isNotEmpty()) { Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) { FilterChip(selected = state.selectedCategoryId == null, onClick = { viewModel.onCategoryChange(null) }, label = { Text("همه") }); state.categories.forEach { category -> FilterChip(selected = state.selectedCategoryId == category.id, onClick = { viewModel.onCategoryChange(category.id) }, label = { Text(category.name) }) } }; Spacer(Modifier.height(6.dp)) }
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) { FilterChip(selected = !state.favoritesOnly, onClick = { viewModel.onFavoritesChange(false) }, label = { Text("همه") }); FilterChip(selected = state.favoritesOnly, onClick = { viewModel.onFavoritesChange(true) }, label = { Text("موردعلاقه‌ها") }) }; Spacer(Modifier.height(8.dp))
        when { state.isLoading -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Purple) }; state.error != null -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { Text("خطا: ${state.error}", color = MaterialTheme.colorScheme.error) }; state.items.isEmpty() -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { Text("واژه‌ای پیدا نشد.") }; else -> LazyColumn(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(vertical = 2.dp)) { items(state.items, key = { it.concept.id }) { item -> VocabularyCard(item, languagePair) { onOpen(item.concept.id) } } } }
    }
}

@Composable private fun LibraryStatCard(title:String,value:Int,color:Color,background:Color,icon:androidx.compose.ui.graphics.vector.ImageVector,modifier:Modifier){Card(modifier=modifier.height(132.dp),shape=RoundedCornerShape(22.dp),colors=CardDefaults.cardColors(containerColor=background),border=androidx.compose.foundation.BorderStroke(1.dp,color.copy(alpha=.14f))){Column(Modifier.fillMaxSize().padding(vertical=13.dp,horizontal=6.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){Icon(imageVector=icon,contentDescription=title,tint=color,modifier=Modifier.size(42.dp));Spacer(Modifier.height(5.dp));Text(title,color=color,style=MaterialTheme.typography.titleSmall.copy(fontWeight=FontWeight.Bold),textAlign=TextAlign.Center);Text(toFaDigits(value),color=color,style=MaterialTheme.typography.headlineSmall.copy(fontWeight=FontWeight.Bold))}}}

@Composable private fun VocabularyCard(item:LibraryItem,languagePair:LanguagePair,onOpen:()->Unit){Card(Modifier.fillMaxWidth().clickable(onClick=onOpen),shape=RoundedCornerShape(20.dp),colors=CardDefaults.cardColors(containerColor=Color.White),border=androidx.compose.foundation.BorderStroke(1.dp,Border)){Row(Modifier.fillMaxWidth().padding(horizontal=14.dp,vertical=12.dp),verticalAlignment=Alignment.CenterVertically){Icon(imageVector=Icons.Outlined.MoreVert,contentDescription="گزینه‌ها",tint=Color(0xFF727B99),modifier=Modifier.size(25.dp));Spacer(Modifier.width(8.dp));DifficultyPill(item.difficulty);Spacer(Modifier.weight(1f));Column(horizontalAlignment=Alignment.End){Row(verticalAlignment=Alignment.CenterVertically){Text(item.source?.text?:"—",color=Navy,style=MaterialTheme.typography.titleMedium.copy(fontWeight=FontWeight.Bold));Spacer(Modifier.width(7.dp));Text(languagePair.source.flag,style=MaterialTheme.typography.titleMedium)}; Spacer(Modifier.height(4.dp)); if(item.targets.isEmpty()){Text("—",color=Color(0xFF747B94),style=MaterialTheme.typography.bodyMedium)}else{item.targets.forEachIndexed{index,target->Row(verticalAlignment=Alignment.CenterVertically){Text(target.text,color=Color(0xFF747B94),style=MaterialTheme.typography.bodyMedium);Spacer(Modifier.width(7.dp));Text(languagePair.target.flag,style=MaterialTheme.typography.titleMedium);if(index<item.targets.lastIndex)Text("،",color=Color(0xFF747B94))};if(index<item.targets.lastIndex)Spacer(Modifier.height(2.dp))}};Spacer(Modifier.height(3.dp));item.category?.let{Text(it.name,color=Blue,style=MaterialTheme.typography.labelSmall.copy(fontWeight=FontWeight.Bold))}};Spacer(Modifier.width(12.dp));Icon(imageVector=Icons.Outlined.StarBorder,contentDescription="موردعلاقه",tint=Color(0xFF7A84A4),modifier=Modifier.size(31.dp))}}}

@Composable private fun DifficultyPill(difficulty:VocabularyDifficulty?){val (label,background,foreground)=when(difficulty){VocabularyDifficulty.EASY->Triple("آسان",Color(0xFFE6FFF1),Green);VocabularyDifficulty.MEDIUM->Triple("متوسط",Color(0xFFFFF3D9),Orange);VocabularyDifficulty.HARD->Triple("سخت",Color(0xFFFFE7EC),Color(0xFFE83D5A));VocabularyDifficulty.VERY_HARD->Triple("خیلی سخت",Color(0xFFFFE0E8),Color(0xFFE52E51));null->Triple("آسان",Color(0xFFE6FFF1),Green)};Surface(shape=RoundedCornerShape(18.dp),color=background){Text(label,color=foreground,style=MaterialTheme.typography.labelLarge.copy(fontWeight=FontWeight.Bold),modifier=Modifier.padding(horizontal=16.dp,vertical=7.dp))}}
private fun toFaDigits(value:Int):String=value.toString().map{when(it){'0'->'۰';'1'->'۱';'2'->'۲';'3'->'۳';'4'->'۴';'5'->'۵';'6'->'۶';'7'->'۷';'8'->'۸';'9'->'۹';else->it}}.joinToString("")
