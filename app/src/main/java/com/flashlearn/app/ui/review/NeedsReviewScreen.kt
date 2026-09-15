package com.flashlearn.app.ui.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flashlearn.domain.model.ReviewQueueItem

@Composable
fun NeedsReviewScreen(viewModel: NeedsReviewViewModel, onBack: () -> Unit) {
    val items by viewModel.items.collectAsState()
    LaunchedEffect(Unit) { viewModel.refresh() }
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("نیازمند بررسی دستی", style = MaterialTheme.typography.headlineSmall)
            Button(onClick = onBack) { Text("بازگشت") }
        }
        if (items.isEmpty()) {
            Text("صف بررسی خالی است.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(items, key = { it.id }) { item ->
                    ReviewQueueCard(item, viewModel)
                }
            }
        }
    }
}

@Composable
private fun ReviewQueueCard(item: ReviewQueueItem, viewModel: NeedsReviewViewModel) {
    Card(Modifier.fillMaxWidth()) {
        Column(
            Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Text(item.sourceText, style = MaterialTheme.typography.titleMedium)
            Text(item.targetText ?: "بدون ترجمه")
            Text("اعتماد: ${(item.confidence * 100).toInt()}٪")
            item.possibleCorrection?.let { Text("پیشنهاد اصلاح: $it") }
            item.lineNumber?.let { Text("خط: $it") }
            item.warning?.let { Text(it) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { viewModel.approve(item) }) { Text("تأیید") }
                Button(onClick = { viewModel.reject(item) }) { Text("رد") }
            }
        }
    }
}
