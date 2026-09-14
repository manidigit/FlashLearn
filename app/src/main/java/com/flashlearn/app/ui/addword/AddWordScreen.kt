package com.flashlearn.app.ui.addword

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AddWordScreen(viewModel: AddWordViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsState()
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    val filteredCategories = state.categories.filter { state.categoryName.isBlank() || it.name.contains(state.categoryName, ignoreCase = true) }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("افزودن واژه", style = MaterialTheme.typography.headlineSmall)
            IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, contentDescription = "بازگشت") }
        }
        Column(
            Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("یک واژه جدید اضافه کن", style = MaterialTheme.typography.titleLarge)
            Text("اطلاعات واژه را وارد کن.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(state.sourceText, viewModel::onSourceTextChange, Modifier.fillMaxWidth(), label = { Text("واژه (به انگلیسی)") }, singleLine = true)
            OutlinedTextField(state.targetText, viewModel::onTargetTextChange, Modifier.fillMaxWidth(), label = { Text("معنی (به فارسی)") }, singleLine = true)
            OutlinedTextField(state.pronunciation, viewModel::onPronunciationChange, Modifier.fillMaxWidth(), label = { Text("تلفظ") }, singleLine = true)
            OutlinedTextField(state.example, viewModel::onExampleChange, Modifier.fillMaxWidth(), label = { Text("مثال") }, minLines = 2)
            OutlinedTextField(state.notes, viewModel::onNotesChange, Modifier.fillMaxWidth(), label = { Text("یادداشت") }, minLines = 2)
            Box(Modifier.fillMaxWidth()) {
                OutlinedTextField(state.categoryName, { viewModel.onCategoryNameChange(it); categoryMenuExpanded = true }, Modifier.fillMaxWidth(), label = { Text("دسته‌بندی") }, singleLine = true)
                DropdownMenu(categoryMenuExpanded && filteredCategories.isNotEmpty(), { categoryMenuExpanded = false }) {
                    filteredCategories.forEach { category ->
                        DropdownMenuItem(text = { Text(category.name) }, onClick = { viewModel.onCategoryNameChange(category.name); categoryMenuExpanded = false })
                    }
                }
            }
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            state.lastSavedText?.let { Text("«$it» ذخیره شد.", color = MaterialTheme.colorScheme.primary) }
        }
        Button(onClick = viewModel::save, enabled = state.canSave, modifier = Modifier.fillMaxWidth().padding(20.dp).height(52.dp), shape = MaterialTheme.shapes.medium) {
            Icon(Icons.Outlined.Save, null)
            Spacer(Modifier.width(8.dp))
            Text(if (state.isSaving) "در حال ذخیره..." else "ذخیره")
        }
    }
}
