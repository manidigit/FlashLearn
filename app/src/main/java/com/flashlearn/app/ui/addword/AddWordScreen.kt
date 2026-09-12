package com.flashlearn.app.ui.addword

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AddWordScreen(
    viewModel: AddWordViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var categoryMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("افزودن لغت", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = state.sourceText,
            onValueChange = viewModel::onSourceTextChange,
            label = { Text("اسپانیایی") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.targetText,
            onValueChange = viewModel::onTargetTextChange,
            label = { Text("فارسی") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.notes,
            onValueChange = viewModel::onNotesChange,
            label = { Text("یادداشت (اختیاری)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.pronunciation,
            onValueChange = viewModel::onPronunciationChange,
            label = { Text("تلفظ (اختیاری)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.example,
            onValueChange = viewModel::onExampleChange,
            label = { Text("مثال (اختیاری)") },
            modifier = Modifier.fillMaxWidth()
        )

        val filteredCategories = state.categories.filter {
            state.categoryName.isBlank() || it.name.contains(state.categoryName, ignoreCase = true)
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = state.categoryName,
                onValueChange = {
                    viewModel.onCategoryNameChange(it)
                    categoryMenuExpanded = true
                },
                label = { Text("دسته‌بندی (اختیاری)") },
                modifier = Modifier.fillMaxWidth()
            )
            DropdownMenu(
                expanded = categoryMenuExpanded && filteredCategories.isNotEmpty(),
                onDismissRequest = { categoryMenuExpanded = false }
            ) {
                filteredCategories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category.name) },
                        onClick = {
                            viewModel.onCategoryNameChange(category.name)
                            categoryMenuExpanded = false
                        }
                    )
                }
            }
        }

        if (state.error != null) {
            Text(state.error!!, color = MaterialTheme.colorScheme.error)
        }

        if (state.lastSavedText != null) {
            Text("«${state.lastSavedText}» ذخیره شد.")
        }

        Button(
            onClick = { viewModel.save() },
            enabled = state.canSave,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (state.isSaving) "در حال ذخیره..." else "ذخیره و افزودن بعدی")
        }

        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("بازگشت به خانه")
        }
    }
}
