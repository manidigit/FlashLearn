package com.flashlearn.app.ui.review

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flashlearn.domain.model.ReviewType
import com.flashlearn.domain.model.VocabularyDifficulty

@Composable
fun ReviewScreen(viewModel: ReviewViewModel, onFinished: () -> Unit) {
    val state by viewModel.state.collectAsState()
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(9.dp)) {
        state.error?.let { Text("خطا: $it", color = MaterialTheme.colorScheme.error, modifier = Modifier.fillMaxWidth()) }
        when {
            state.isSelectingMode -> ReviewSetup(state, viewModel)
            state.isLoading -> CircularProgressIndicator()
            state.isFinished -> FinishCard(state, onFinished)
            state.answerFeedback != null -> FeedbackCard(state)
            state.quizCard != null && state.answerFeedback == null -> QuizCard(state, viewModel)
            state.card != null -> FlashCard(state, viewModel)
        }
    }
}

@Composable private fun ReviewSetup(state: ReviewUiState, vm: ReviewViewModel) {
    Text("مرور روزانه", style = MaterialTheme.typography.headlineSmall)
    Text("حالت مرور", style = MaterialTheme.typography.titleMedium)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SelectCard("فلش‌کارت", state.selectedMode == ReviewMode.FLASHCARD, { vm.chooseMode(ReviewMode.FLASHCARD) }, Modifier.weight(1f))
        SelectCard("آزمون چهارگزینه‌ای", state.selectedMode == ReviewMode.QUIZ, { vm.chooseMode(ReviewMode.QUIZ) }, Modifier.weight(1f))
    }
    Text("نوع مرور", style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth())
    ChoiceRow(ReviewType.entries.map { reviewTypeLabel(it) to (it == state.selectedReviewType) }) { index -> vm.chooseReviewType(ReviewType.entries[index]) }
    Text("فیلتر سختی", style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth())
    val difficultyOptions = listOf("همه" to (state.selectedDifficulty == null)) + VocabularyDifficulty.entries.map { difficultyLabel(it) to (it == state.selectedDifficulty) }
    ChoiceRow(difficultyOptions) { index -> vm.chooseDifficulty(if (index == 0) null else VocabularyDifficulty.entries[index - 1]) }
    if (state.categories.isNotEmpty()) {
        Text("دسته‌بندی", style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth())
        val cats = listOf("همه" to (state.selectedCategoryId == null)) + state.categories.map { it.name to (it.id == state.selectedCategoryId) }
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) { cats.forEachIndexed { i, (label, selected) -> ChoiceChip(label, selected) { vm.chooseCategory(if (i == 0) null else state.categories[i - 1].id) } } }
    }
    Button(onClick = vm::startNewSession, modifier = Modifier.fillMaxWidth().height(48.dp), shape = MaterialTheme.shapes.medium) { Text("شروع مرور") }
}

@Composable private fun ChoiceRow(options: List<Pair<String, Boolean>>, onChoice: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) { options.forEachIndexed { i, (label, selected) -> ChoiceChip(label, selected) { onChoice(i) } } }
}

@Composable private fun ChoiceChip(label: String, selected: Boolean, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = Modifier.height(40.dp), border = ButtonDefaults.outlinedButtonBorder.copy(width = if (selected) 2.dp else 1.dp), colors = if (selected) ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary, containerColor = MaterialTheme.colorScheme.primary.copy(alpha = .08f)) else ButtonDefaults.outlinedButtonColors()) { Text(label, style = MaterialTheme.typography.labelMedium) }
}

@Composable private fun SelectCard(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    Card(modifier = modifier.clickable(onClick = onClick), shape = MaterialTheme.shapes.medium, colors = CardDefaults.cardColors(containerColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = .10f) else MaterialTheme.colorScheme.surfaceVariant)) { Box(Modifier.fillMaxWidth().padding(vertical = 18.dp), contentAlignment = Alignment.Center) { Text(label, style = MaterialTheme.typography.titleSmall) } }
}

@Composable private fun FlashCard(state: ReviewUiState, vm: ReviewViewModel) {
    val card = state.card ?: return
    val progress = if (state.total <= 0) 0f else state.answered.toFloat() / state.total.toFloat()
    Text("${state.remaining} کارت باقی‌مانده از ${state.total}", style = MaterialTheme.typography.labelMedium)
    LinearProgressIndicator(progress = { progress.coerceIn(0f, 1f) }, Modifier.fillMaxWidth())
    Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) { Column(Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(card.sourceText, style = MaterialTheme.typography.headlineMedium); if (card.isFlipped) { Spacer(Modifier.height(14.dp)); Text(card.targetText, style = MaterialTheme.typography.headlineSmall) }; if (card.noteVisible && !card.sourceNotes.isNullOrBlank()) { Spacer(Modifier.height(8.dp)); Text(card.sourceNotes, style = MaterialTheme.typography.bodySmall) } } }
    if (!card.isFlipped) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { if (!card.sourceNotes.isNullOrBlank()) OutlinedButton(onClick = vm::toggleNote, Modifier.weight(1f)) { Text(if (card.noteVisible) "مخفی کردن یادداشت" else "یادداشت") }; OutlinedButton(onClick = vm::revealHint, enabled = !card.hintRevealed, Modifier.weight(1f)) { Text("راهنما") } }
        Button(onClick = vm::flipCard, Modifier.fillMaxWidth().height(48.dp)) { Text("نمایش پاسخ") }
    } else Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedButton(onClick = { vm.submitAnswer(false) }, enabled = state.canSubmitAnswer, Modifier.weight(1f)) { Text("غلط") }; Button(onClick = { vm.submitAnswer(true) }, enabled = state.canSubmitAnswer, Modifier.weight(1f)) { Text("صحیح") } }
}

@Composable private fun QuizCard(state: ReviewUiState, vm: ReviewViewModel) {
    val quiz = state.quizCard ?: return
    val card = state.card
    Text("${state.remaining} سؤال باقی‌مانده از ${state.total}", style = MaterialTheme.typography.labelMedium)
    LinearProgressIndicator(progress = { if (state.total == 0) 0f else state.answered.toFloat() / state.total }, Modifier.fillMaxWidth())
    Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) { Column(Modifier.fillMaxWidth().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(7.dp)) { Text(quiz.promptText, style = MaterialTheme.typography.headlineSmall); quiz.options.forEachIndexed { index, option -> val selected = quiz.selectedOption == option; if (selected) Button(onClick = { vm.selectQuizOption(option) }, Modifier.fillMaxWidth()) { Text("${index + 1}. $option") } else OutlinedButton(onClick = { vm.selectQuizOption(option) }, Modifier.fillMaxWidth()) { Text("${index + 1}. $option") } }; if (card != null) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { if (!card.sourceNotes.isNullOrBlank()) OutlinedButton(onClick = vm::toggleNote, Modifier.weight(1f)) { Text("یادداشت") }; OutlinedButton(onClick = vm::revealHint, Modifier.weight(1f)) { Text("راهنما") } }; Button(onClick = vm::submitQuizAnswer, enabled = quiz.selectedOption != null && !state.isSubmitting, Modifier.fillMaxWidth()) { Text("ثبت پاسخ") } } } }
}

@Composable private fun FeedbackCard(state: ReviewUiState) { val f = state.answerFeedback ?: return; Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) { Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(7.dp)) { Text(if (f.isCorrect) "✓ پاسخ صحیح" else "✕ پاسخ نادرست", style = MaterialTheme.typography.headlineSmall, color = if (f.isCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error); Text("مرحله: ${f.stageLabel}"); Text("سختی: ${f.difficultyLabel}"); if (!f.isCorrect && !f.correctAnswerText.isNullOrBlank()) Text("پاسخ صحیح: ${f.correctAnswerText}"); Text("نتیجه: ${state.correct} صحیح، ${state.wrong} غلط") } } }

@Composable private fun FinishCard(state: ReviewUiState, onFinished: () -> Unit) { Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) { Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("مرور تمام شد!", style = MaterialTheme.typography.headlineSmall); Text("${state.answered} کارت پاسخ داده شد"); Text("صحیح: ${state.correct} • غلط: ${state.wrong}"); Text("دقت این جلسه: ${if (state.answered == 0) 0 else state.correct * 100 / state.answered}٪"); Button(onClick = onFinished, Modifier.fillMaxWidth()) { Text("بازگشت به خانه") } } } }

private fun reviewTypeLabel(type: ReviewType) = when (type) { ReviewType.DAILY -> "روزانه"; ReviewType.WEEKLY -> "هفتگی"; ReviewType.MONTHLY -> "ماهانه"; ReviewType.LEARNED -> "یادگرفته‌شده"; ReviewType.RANDOM -> "تصادفی" }
private fun difficultyLabel(difficulty: VocabularyDifficulty) = when (difficulty) { VocabularyDifficulty.EASY -> "آسان"; VocabularyDifficulty.MEDIUM -> "متوسط"; VocabularyDifficulty.HARD -> "سخت"; VocabularyDifficulty.VERY_HARD -> "خیلی سخت" }
