package com.flashlearn.app.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flashlearn.app.ui.LanguagePair
import java.util.UUID

@Composable
fun LibraryScreen(viewModel: LibraryViewModel, languagePair: LanguagePair = LanguagePair(), onBack: () -> Unit = {}, onOpen: (UUID) -> Unit = {}, onAddWord: () -> Unit = {}, onBulkImport: () -> Unit = {}) {
    val state by viewModel.state.collectAsState()
    var manageTags by remember { mutableStateOf(false) }
    LaunchedEffect(languagePair) { viewModel.setLanguagePair(languagePair) }
    LaunchedEffect(Unit) { viewModel.refresh() }
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 10.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("واژگان", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
            TextButton(onClick = { manageTags = true }) { Text("مدیریت Tag") }
            TextButton(onClick = onBack) { Text("←") }
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = state.query, onValueChange = viewModel::onQueryChange, modifier = Modifier.fillMaxWidth().height(54.dp), placeholder = { Text("جستجو در واژگان...") }, leadingIcon = { Icon(Icons.Outlined.Search, null) }, singleLine = true, shape = MaterialTheme.shapes.medium)
        Spacer(Modifier.height(7.dp))
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FilterChip(selected = !state.favoritesOnly, onClick = { viewModel.onFavoritesChange(false) }, label = { Text("همه") })
            FilterChip(selected = state.favoritesOnly, onClick = { viewModel.onFavoritesChange(true) }, label = { Text("موردعلاقه‌ها") })
        }
        if (state.tags.isNotEmpty()) {
            Text("Tagها", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 7.dp, bottom = 4.dp))
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(selected = state.selectedTagId == null, onClick = { viewModel.onTagChange(null) }, label = { Text("همه") })
                state.tags.forEach { tag ->
                    FilterChip(selected = state.selectedTagId == tag.id, onClick = { viewModel.onTagChange(tag.id) }, label = { Text("${tag.name} • ${state.tagCounts[tag.id] ?: 0}") })
                }
            }
        }
        if (state.categories.isNotEmpty()) {
            Text("دسته‌بندی‌ها", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 7.dp, bottom = 4.dp))
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(selected = state.selectedCategoryId == null, onClick = { viewModel.onCategoryChange(null) }, label = { Text("همه • ${if (state.selectedCategoryId == null) state.totalCount else state.categoryCounts.values.sum()}") })
                state.categories.forEach { cat ->
                    val count = state.categoryCounts[cat.id] ?: 0
                    FilterChip(selected = state.selectedCategoryId == cat.id, onClick = { viewModel.onCategoryChange(cat.id) }, label = { Text("${cat.name} • $count") })
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(if (state.selectedCategoryId == null) "${state.totalCount} واژه" else "${state.totalCount} واژه در این دسته", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.fillMaxWidth())
        when {
            state.isLoading -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            state.error != null -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { Text("خطا: ${state.error}", color = MaterialTheme.colorScheme.error) }
            state.items.isEmpty() -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { Text("واژه‌ای پیدا نشد.") }
            else -> LazyColumn(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 4.dp)) {
                items(state.items, key = { it.concept.id }) { item ->
                    Card(Modifier.fillMaxWidth().clickable { onOpen(item.concept.id) }, shape = MaterialTheme.shapes.large) {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.StarBorder, null, tint = MaterialTheme.colorScheme.outline)
                            Spacer(Modifier.width(9.dp))
                            Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                Row(verticalAlignment = Alignment.CenterVertically) { Text(languagePair.source.flag, style = MaterialTheme.typography.labelLarge); Spacer(Modifier.width(5.dp)); Text(item.source?.text ?: "—", style = MaterialTheme.typography.titleMedium) }
                                Row(verticalAlignment = Alignment.CenterVertically) { Text(item.target?.text ?: "—", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall); Spacer(Modifier.width(5.dp)); Text(languagePair.target.flag, style = MaterialTheme.typography.labelSmall) }
                                item.category?.let { Text(it.name, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall) }
                            }
                            Icon(Icons.Outlined.MoreVert, null, tint = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onAddWord, modifier = Modifier.weight(1f).height(48.dp), shape = MaterialTheme.shapes.medium) { Icon(Icons.Outlined.Add, null); Spacer(Modifier.width(6.dp)); Text("افزودن واژه") }
            OutlinedButton(onClick = onBulkImport, modifier = Modifier.weight(1f).height(48.dp), shape = MaterialTheme.shapes.medium) { Icon(Icons.Outlined.FileUpload, null); Spacer(Modifier.width(6.dp)); Text("افزودن گروهی") }
        }
    }
    if (manageTags) TagManagementDialog(state, viewModel, onDismiss = { manageTags = false })
}

@Composable
private fun TagManagementDialog(state: LibraryUiState, viewModel: LibraryViewModel, onDismiss: () -> Unit) {
    var newName by remember { mutableStateOf("") }
    var editingId by remember { mutableStateOf<UUID?>(null) }
    var editingName by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = { if (!state.isTagBusy) onDismiss() },
        title = { Text("مدیریت Tagها") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = newName, onValueChange = { newName = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("Tag جدید") }, trailingIcon = { IconButton(onClick = { if (newName.isNotBlank()) viewModel.createTag(newName) { error -> message = error; if (error == null) newName = "" } }) { Icon(Icons.Outlined.Add, null) } })
                if (state.tags.isEmpty()) Text("هنوز Tagای ساخته نشده است.")
                state.tags.forEach { tag ->
                    if (editingId == tag.id) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(value = editingName, onValueChange = { editingName = it }, modifier = Modifier.weight(1f), singleLine = true)
                            TextButton(onClick = { viewModel.renameTag(tag.id, editingName) { error -> message = error; if (error == null) editingId = null } }) { Text("ذخیره") }
                            TextButton(onClick = { editingId = null }) { Text("لغو") }
                        }
                    } else {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(tag.name, Modifier.weight(1f))
                            IconButton(onClick = { editingId = tag.id; editingName = tag.name }) { Icon(Icons.Outlined.Edit, null) }
                            IconButton(onClick = { viewModel.removeTag(tag.id) { error -> message = error } }) { Icon(Icons.Outlined.DeleteOutline, null) }
                        }
                    }
                }
                message?.let { Text(it, color = if (it.isBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss, enabled = !state.isTagBusy) { Text("بستن") } }
    )
}
