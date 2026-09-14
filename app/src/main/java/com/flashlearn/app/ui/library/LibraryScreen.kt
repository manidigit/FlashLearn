package com.flashlearn.app.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.UUID

@Composable
fun LibraryScreen(viewModel: LibraryViewModel, onBack: () -> Unit = {}, onOpen: (UUID) -> Unit = {}, onAddWord: () -> Unit = {}) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.refresh() }
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 10.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text("واژگان", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f)); Text("→", style = MaterialTheme.typography.titleLarge) }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = state.query, onValueChange = viewModel::onQueryChange, modifier = Modifier.fillMaxWidth().height(54.dp), placeholder = { Text("جستجو در واژگان...") }, leadingIcon = { Icon(Icons.Outlined.Search, null) }, singleLine = true, shape = MaterialTheme.shapes.medium)
        Spacer(Modifier.height(7.dp))
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) { FilterChip(selected = !state.favoritesOnly, onClick = { viewModel.onFavoritesChange(false) }, label = { Text("همه") }); FilterChip(selected = state.favoritesOnly, onClick = { viewModel.onFavoritesChange(true) }, label = { Text("موردعلاقه‌ها") }) }
        if (state.categories.isNotEmpty()) {
            Text("دسته‌بندی‌ها", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 7.dp, bottom = 4.dp))
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) { FilterChip(selected = state.selectedCategoryId == null, onClick = { viewModel.onCategoryChange(null) }, label = { Text("همه") }); state.categories.take(6).forEach { cat -> FilterChip(selected = state.selectedCategoryId == cat.id, onClick = { viewModel.onCategoryChange(cat.id) }, label = { Text(cat.name) }) } }
        }
        Spacer(Modifier.height(6.dp))
        when {
            state.isLoading -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            state.error != null -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { Text("خطا: ${state.error}", color = MaterialTheme.colorScheme.error) }
            state.items.isEmpty() -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { Text("واژه‌ای پیدا نشد.") }
            else -> LazyColumn(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(7.dp), contentPadding = PaddingValues(vertical = 4.dp)) {
                items(state.items, key = { it.concept.id }) { item ->
                    Card(Modifier.fillMaxWidth().clickable { onOpen(item.concept.id) }, shape = MaterialTheme.shapes.medium) {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) { Text(item.source?.text ?: "—", style = MaterialTheme.typography.titleMedium); Text(item.target?.text ?: "—", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall); item.category?.let { Text(it.name, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall) } }
                            Icon(Icons.Outlined.StarBorder, null, tint = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(7.dp))
        Button(onClick = onAddWord, modifier = Modifier.fillMaxWidth().height(48.dp), shape = MaterialTheme.shapes.medium) { Text("+  افزودن واژه جدید") }
    }
}
