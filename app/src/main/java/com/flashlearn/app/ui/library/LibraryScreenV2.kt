package com.flashlearn.app.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.flashlearn.app.ui.LanguagePair
import com.flashlearn.domain.model.VocabularyDifficulty
import java.util.UUID

private val Navy = Color(0xFF0B236B)
private val Purple = Color(0xFF7C2BEF)
private val Green = Color(0xFF0BAA6A)
private val Blue = Color(0xFF147BE8)
private val Orange = Color(0xFFF59E0B)
private val SoftPurple = Color(0xFFF7F1FF)
private val SoftGreen = Color(0xFFF0FFF8)
private val SoftBlue = Color(0xFFF1F8FF)
private val SoftOrange = Color(0xFFFFFBF0)
private val Border = Color(0xFFE1E7F3)

@Composable
fun LibraryScreenV2(viewModel: LibraryViewModel, languagePair: LanguagePair, onBack: () -> Unit, onOpen: (UUID) -> Unit, onCategories: () -> Unit, onAddWord: () -> Unit) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(languagePair) { viewModel.setLanguagePair(languagePair) }
    LaunchedEffect(Unit) { viewModel.refresh() }
    Column(Modifier.fillMaxSize().background(Color(0xFFFBFCFF)).padding(horizontal = 16.dp, vertical = 8.dp)) {
        Box(Modifier.fillMaxWidth().height(58.dp)) {
            IconButton(onClick = onBack, Modifier.align(Alignment.CenterEnd)) { Icon(Icons.Outlined.ArrowBack, "بازگشت", tint = Navy) }
            Text("واژگان", Modifier.align(Alignment.Center), color = Navy, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))
        }
        OutlinedTextField(state.query, viewModel::onQueryChange, Modifier.fillMaxWidth().height(58.dp), placeholder={Text("جستجو در واژگان...", Modifier.fillMaxWidth(), textAlign=TextAlign.End, color=Color(0xFF8C92A7))}, trailingIcon={Icon(Icons.Outlined.Search,"جستجو",tint=Navy)}, singleLine=true, shape=RoundedCornerShape(20.dp), colors=OutlinedTextFieldDefaults.colors(unfocusedContainerColor=Color.White,focusedContainerColor=Color.White,unfocusedBorderColor=Color(0xFFD6DCF0),focusedBorderColor=Color(0xFFC8D0EA)))
        Spacer(Modifier.height(22.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.spacedBy(9.dp)) {
            StatCard("جدید", state.newCount, Orange, SoftOrange, Icons.Outlined.Add, Modifier.weight(1f), state.filter == LibraryFilter.NEW) { viewModel.onFilterChange(LibraryFilter.NEW) }
            StatCard("در حال یادگیری", state.learningCount, Blue, SoftBlue, Icons.Outlined.History, Modifier.weight(1f), state.filter == LibraryFilter.LEARNING) { viewModel.onFilterChange(LibraryFilter.LEARNING) }
            StatCard("یادگرفته", state.learnedCount, Green, SoftGreen, Icons.Outlined.CheckCircle, Modifier.weight(1f), state.filter == LibraryFilter.LEARNED) { viewModel.onFilterChange(LibraryFilter.LEARNED) }
            StatCard("کل واژگان", state.totalCount, Purple, SoftPurple, Icons.Outlined.Book, Modifier.weight(1f), state.filter == LibraryFilter.ALL) { viewModel.onFilterChange(LibraryFilter.ALL) }
        }
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment=Alignment.CenterVertically, horizontalArrangement=Arrangement.End) { Text("فیلترها", color=Navy, style=MaterialTheme.typography.titleLarge.copy(fontWeight=FontWeight.Bold)); Spacer(Modifier.width(8.dp)); Icon(Icons.Outlined.FilterList,"فیلترها",tint=Navy) }
        Spacer(Modifier.height(10.dp))
        Card(Modifier.fillMaxWidth().clickable(onClick=onCategories), shape=RoundedCornerShape(20.dp), colors=CardDefaults.cardColors(Color.White), border=androidx.compose.foundation.BorderStroke(1.dp,Border)) {
            Row(Modifier.fillMaxWidth().padding(horizontal=14.dp,vertical=12.dp),verticalAlignment=Alignment.CenterVertically) {
                Icon(Icons.Outlined.ArrowBack,"انتخاب دسته",tint=Navy);Spacer(Modifier.weight(1f))
                Column(horizontalAlignment=Alignment.End){Text("دسته‌بندی‌ها",color=Navy,style=MaterialTheme.typography.titleMedium.copy(fontWeight=FontWeight.Bold));Text(if(state.selectedCategoryIds.isEmpty()) "همه دسته‌ها" else "${toFaDigits(state.selectedCategoryIds.size)} دسته انتخاب شده",color=Color(0xFF858CA4),style=MaterialTheme.typography.bodySmall)}
                Spacer(Modifier.width(12.dp));Surface(Modifier.size(48.dp),RoundedCornerShape(15.dp),color=SoftPurple){Box(contentAlignment=Alignment.Center){Icon(Icons.Outlined.Folder,"دسته‌بندی‌ها",tint=Purple,modifier=Modifier.size(28.dp))}}
            }
        }
        Spacer(Modifier.height(10.dp))
        when { state.isLoading -> Box(Modifier.weight(1f).fillMaxWidth(),contentAlignment=Alignment.Center){CircularProgressIndicator(color=Purple)}; state.error!=null -> Box(Modifier.weight(1f).fillMaxWidth(),contentAlignment=Alignment.Center){Text(state.error.orEmpty(),color=MaterialTheme.colorScheme.error,textAlign=TextAlign.Center)}; state.items.isEmpty()->Box(Modifier.weight(1f).fillMaxWidth(),contentAlignment=Alignment.Center){Text("واژه‌ای پیدا نشد.")}; else->LazyColumn(Modifier.weight(1f).fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(10.dp),contentPadding=PaddingValues(bottom=8.dp)){items(state.items,key={it.concept.id}){item->VocabularyCardV2(item,languagePair){onOpen(item.concept.id)}}} }
    }
}

@Composable private fun StatCard(title:String,value:Int,color:Color,background:Color,icon:androidx.compose.ui.graphics.vector.ImageVector,modifier:Modifier,selected:Boolean,onClick:()->Unit){
    Card(onClick=onClick,modifier=modifier.height(132.dp),shape=RoundedCornerShape(22.dp),colors=CardDefaults.cardColors(background),border=androidx.compose.foundation.BorderStroke(if(selected)2.dp else 1.dp,color.copy(alpha=if(selected).35f else .14f))){Column(Modifier.fillMaxSize().padding(6.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){Icon(icon,title,tint=color,modifier=Modifier.size(38.dp));Spacer(Modifier.height(4.dp));Text(title,color=color,style=MaterialTheme.typography.labelLarge.copy(fontWeight=FontWeight.Bold),textAlign=TextAlign.Center);Text(toFaDigits(value),color=color,style=MaterialTheme.typography.headlineSmall.copy(fontWeight=FontWeight.Bold))}}
}

@Composable private fun VocabularyCardV2(item:LibraryItem,languagePair:LanguagePair,onClick:()->Unit){Card(Modifier.fillMaxWidth().clickable(onClick=onClick),shape=RoundedCornerShape(20.dp),colors=CardDefaults.cardColors(Color.White),border=androidx.compose.foundation.BorderStroke(1.dp,Border)){Row(Modifier.fillMaxWidth().padding(horizontal=12.dp,vertical=11.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Outlined.MoreVert,"گزینه‌ها",tint=Color(0xFF727B99),modifier=Modifier.size(24.dp));Spacer(Modifier.width(8.dp));DifficultyPillV2(item.difficulty);Spacer(Modifier.weight(1f));Column(horizontalAlignment=Alignment.End){Row(verticalAlignment=Alignment.CenterVertically){Text(item.source?.text ?: "—",color=Navy,style=MaterialTheme.typography.titleMedium.copy(fontWeight=FontWeight.Bold));Spacer(Modifier.width(7.dp));Text(languagePair.source.flag)};Text(item.target?.text ?: "—",color=Color(0xFF747B94),style=MaterialTheme.typography.bodyMedium);item.category?.let{Text(it.name,color=Blue,style=MaterialTheme.typography.labelSmall.copy(fontWeight=FontWeight.Bold))}};Spacer(Modifier.width(12.dp));Icon(Icons.Outlined.StarBorder,"موردعلاقه",tint=Color(0xFF7A84A4),modifier=Modifier.size(30.dp))}}}

@Composable private fun DifficultyPillV2(difficulty:VocabularyDifficulty?){val v=when(difficulty){VocabularyDifficulty.EASY->Triple("آسان",Color(0xFFE6FFF1),Green);VocabularyDifficulty.MEDIUM->Triple("متوسط",Color(0xFFFFF3D9),Orange);VocabularyDifficulty.HARD->Triple("سخت",Color(0xFFFFE7EC),Color(0xFFE83D5A));VocabularyDifficulty.VERY_HARD->Triple("خیلی سخت",Color(0xFFFFE0E8),Color(0xFFE52E51));null->Triple("آسان",Color(0xFFE6FFF1),Green)};Surface(shape=RoundedCornerShape(18.dp),color=v.second){Text(v.first,color=v.third,style=MaterialTheme.typography.labelLarge.copy(fontWeight=FontWeight.Bold),modifier=Modifier.padding(horizontal=12.dp,vertical=7.dp))}}
private fun toFaDigits(value:Int):String=value.toString().map{when(it){'0'->'۰';'1'->'۱';'2'->'۲';'3'->'۳';'4'->'۴';'5'->'۵';'6'->'۶';'7'->'۷';'8'->'۸';'9'->'۹';else->it}}.joinToString("")
