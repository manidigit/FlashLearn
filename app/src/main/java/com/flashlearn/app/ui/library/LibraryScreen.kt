package com.flashlearn.app.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.UUID

@Composable
fun LibraryScreen(viewModel: LibraryViewModel, onBack: () -> Unit = {}, onOpen: (UUID) -> Unit = {}) {
    val state by viewModel.state.collectAsState()

    // Refresh whenever the Library destination is entered so changes made in AddWord
    // or LibraryDetail are visible immediately without requiring a process restart.
    LaunchedEffect(Unit) { viewModel.refresh() }
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("کتابخانه واژگان", style = MaterialTheme.typography.headlineSmall)
            TextButton(onClick = onBack) { Text("بازگشت") }
        }
        OutlinedTextField(value = state.query, onValueChange = viewModel::onQueryChange, modifier = Modifier.fillMaxWidth(), label = { Text("جستجوی اسپانیایی یا فارسی") })
        FilterChip(selected = state.favoritesOnly, onClick = { viewModel.onFavoritesChange(!state.favoritesOnly) }, label = { Text("★ فقط موردعلاقه‌ها") })
        Text("دسته‌بندی", style = MaterialTheme.typography.titleSmall)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FilterChip(selected = state.selectedCategoryId == null, onClick = { viewModel.onCategoryChange(null) }, label = { Text("همه") })
            state.categories.forEach { cat ->
                FilterChip(selected = state.selectedCategoryId == cat.id, onClick = { viewModel.onCategoryChange(cat.id) }, label = { Text(cat.name) })
            }
        }
        when {
            state.isLoading -> CircularProgressIndicator()
            state.error != null -> Text("Error: ${state.error}")
            state.items.isEmpty() -> Text("لغتی پیدا نشد.")
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.items, key = { it.concept.id }) { item ->
                    Card(Modifier.fillMaxWidth().clickable { onOpen(item.concept.id) }) {
                        Column(Modifier.padding(14.dp)) {
                            Text(item.source?.text ?: "—", style = MaterialTheme.typography.titleMedium)
                            Text(item.target?.text ?: "—")
                            if (item.concept.favorite) Text("★ موردعلاقه", style = MaterialTheme.typography.bodySmall)
                            item.category?.let { Text("دسته: ${it.name}", style = MaterialTheme.typography.bodySmall) }
                        }
                    }
                }
            }
        }
    }
}
