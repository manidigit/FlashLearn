package com.flashlearn.app.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun LibraryScreen(
    viewModel: LibraryViewModel,
    onBack: () -> Unit = {},
    onOpen: (UUID) -> Unit = {},
    onAddWord: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.refresh() }

    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 14.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("واژگان", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
            Text("→", style = MaterialTheme.typography.titleLarge)
        }
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.query,
            onValueChange = viewModel::onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("جستجو در واژگان...") },
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
            singleLine = true,
            shape = MaterialTheme.shapes.medium
        )
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = !state.favoritesOnly, onClick = { viewModel.onFavoritesChange(false) }, label = { Text("همه") })
            FilterChip(selected = state.favoritesOnly, onClick = { viewModel.onFavoritesChange(true) }, label = { Text("موردعلاقه‌ها") })
        }
        Spacer(Modifier.height(10.dp))
        if (state.categories.isNotEmpty()) {
            Text("دسته‌بندی‌ها", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = state.selectedCategoryId == null, onClick = { viewModel.onCategoryChange(null) }, label = { Text("همه") })
                state.categories.take(4).forEach { cat ->
                    FilterChip(selected = state.selectedCategoryId == cat.id, onClick = { viewModel.onCategoryChange(cat.id) }, label = { Text(cat.name) })
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            state.error != null -> Text("خطا: ${state.error}", color = MaterialTheme.colorScheme.error)
            state.items.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("واژه‌ای پیدا نشد.") }
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(9.dp), modifier = Modifier.weight(1f)) {
                items(state.items, key = { it.concept.id }) { item ->
                    Card(Modifier.fillMaxWidth().clickable { onOpen(item.concept.id) }, shape = MaterialTheme.shapes.medium) {
                        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.StarBorder, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(item.source?.text ?: "—", style = MaterialTheme.typography.titleMedium)
                                Text(item.target?.text ?: "—", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            item.category?.let { AssistChip(onClick = {}, enabled = false, label = { Text(it.name) }) }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Button(onClick = onAddWord, modifier = Modifier.fillMaxWidth().height(50.dp), shape = MaterialTheme.shapes.medium) {
            Text("+  افزودن واژه جدید")
        }
    }
}
