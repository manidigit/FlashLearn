package com.flashlearn.app.ui.review

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.flashlearn.domain.model.QuizDifficulty
import com.flashlearn.domain.model.ReviewType
import com.flashlearn.domain.model.VocabularyDifficulty

private val QuizCorrect = Color(0xFF2E7D32)
private val QuizWrong = Color(0xFFC62828)
private val QuizSelected = Color(0xFF7C4DFF)
private val QuizCorrectContainer = Color(0xFFE8F5E9)
private val QuizWrongContainer = Color(0xFFFFEBEE)
private val QuizSelectedContainer = Color(0xFFF0E7FF)

@Composable
fun ReviewScreen(viewModel: ReviewViewModel, personalDifficulty: VocabularyDifficulty? = null, quizDifficulty: QuizDifficulty = QuizDifficulty.MEDIUM, onFinished: () -> Unit) {
    val state by viewModel.state.collectAsState()
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        state.error?.takeIf { state.selectedMode != ReviewMode.QUIZ || state.quizCard == null }?.let { Text("خطا: $it", color = MaterialTheme.colorScheme.error, modifier = Modifier.fillMaxWidth()) }
        when {
            state.isSelectingMode -> ReviewSetup(state, viewModel, personalDifficulty, quizDifficulty)
            state.isLoading -> CircularProgressIndicator()
            state.isFinished -> FinishCard(state, onFinished)
            state.selectedMode == ReviewMode.QUIZ && state.quizCard != null -> QuizCard(state, viewModel)
            state.answerFeedback != null -> FeedbackCard(state)
            state.selectedMode == ReviewMode.QUIZ -> QuizUnavailableCard()
            state.card != null -> FlashCard(state, viewModel)
        }
    }
}

@Composable private fun ReviewSetup(state: ReviewUiState, vm: ReviewViewModel, personalDifficulty: VocabularyDifficulty?, quizDifficulty: QuizDifficulty) {
    Text("مرور روزانه", style = MaterialTheme.typography.headlineSmall)
    Text("حالت مرور", style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth())
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { SelectCard("فلش‌کارت", state.selectedMode == ReviewMode.FLASHCARD, { vm.chooseMode(ReviewMode.FLASHCARD) }, Modifier.weight(1f)); SelectCard("آزمون چهارگزینه‌ای", state.selectedMode == ReviewMode.QUIZ, { vm.chooseMode(ReviewMode.QUIZ) }, Modifier.weight(1f)) }
    Text("نوع مرور", style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth()); ChoiceRow(ReviewType.entries.map { reviewTypeLabel(it) to (it == state.selectedReviewType) }) { index -> vm.chooseReviewType(ReviewType.entries[index]) }
    Text("سختی واژه‌ها", style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth()); Text("سطح شخصی: ${personalDifficulty?.let(::difficultyLabel) ?: "همه"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth())
    val difficultyOptions = listOf("همه" to (state.selectedDifficulty == null)) + VocabularyDifficulty.entries.map { difficultyLabel(it) to (it == state.selectedDifficulty) }; ChoiceRow(difficultyOptions) { index -> vm.chooseDifficulty(if (index == 0) null else VocabularyDifficulty.entries[index - 1]) }
    Text("سختی آزمون", style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth()); Text("سطح انتخاب Distractorهای آزمون: ${quizDifficultyLabel(quizDifficulty)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth())
    if (state.categories.isNotEmpty()) { Text("دسته‌بندی", style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth()); val cats = listOf("همه" to (state.selectedCategoryId == null)) + state.categories.map { it.name to (it.id == state.selectedCategoryId) }; Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) { cats.forEachIndexed { i, (label, selected) -> ChoiceChip(label, selected) { vm.chooseCategory(if (i == 0) null else state.categories[i - 1].id) } } } }
    Button(onClick = vm::startNewSession, modifier = Modifier.fillMaxWidth().height(48.dp), shape = MaterialTheme.shapes.medium) { Text("شروع مرور") }
}

@Composable private fun ChoiceRow(options: List<Pair<String, Boolean>>, onChoice: (Int) -> Unit) { Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) { options.forEachIndexed { i, (label, selected) -> ChoiceChip(label, selected) { onChoice(i) } } } }
@Composable private fun ChoiceChip(label: String, selected: Boolean, onClick: () -> Unit) { FilterChip(selected = selected, onClick = onClick, label = { Text(label, style = MaterialTheme.typography.labelMedium) }) }
@Composable private fun SelectCard(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier) { Card(modifier = modifier.clickable(onClick = onClick), shape = MaterialTheme.shapes.medium, colors = CardDefaults.cardColors(containerColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = .10f) else MaterialTheme.colorScheme.surfaceVariant)) { Box(Modifier.fillMaxWidth().padding(vertical = 18.dp), contentAlignment = Alignment.Center) { Text(label, style = MaterialTheme.typography.titleSmall) } } }

@Composable private fun FlashCard(state: ReviewUiState, vm: ReviewViewModel) { val card = state.card ?: return; val progress = if (state.total <= 0) 0f else state.answered.toFloat() / state.total.toFloat()
    LinearProgressIndicator(progress = { progress.coerceIn(0f,1f) }, modifier = Modifier.fillMaxWidth()); Text("${state.answered} / ${state.total}", style = MaterialTheme.typography.labelMedium); Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) { Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) { Text(card.sourceText, style = MaterialTheme.typography.headlineMedium); if (card.isFlipped) Text(card.targetText, style = MaterialTheme.typography.headlineSmall); else Button(onClick = vm::flipCard) { Text("نمایش پاسخ") }; card.sourceNotes?.let { if (card.noteVisible) Text(it, style = MaterialTheme.typography.bodyMedium); TextButton(onClick = vm::toggleNote) { Text(if (card.noteVisible) "پنهان کردن یادداشت" else "نمایش یادداشت") } }; if (card.hintRevealed) Text("راهنما: ${card.targetText.take(1)}…", color = MaterialTheme.colorScheme.primary); TextButton(onClick = vm::revealHint, enabled = !card.hintRevealed) { Text("راهنما") } } }; if (card.isFlipped) Button(onClick = { vm.submitAnswer(true) }, enabled = state.canSubmitAnswer, modifier = Modifier.fillMaxWidth()) { Text("بلدم") }; if (card.isFlipped) OutlinedButton(onClick = { vm.submitAnswer(false) }, enabled = state.canSubmitAnswer, modifier = Modifier.fillMaxWidth()) { Text("بلد نیستم") }
}

@Composable private fun QuizCard(state: ReviewUiState, vm: ReviewViewModel) { val quiz = state.quizCard ?: return; val progress = if (state.total <= 0) 0f else state.answered.toFloat() / state.total.toFloat(); LinearProgressIndicator(progress = { progress.coerceIn(0f,1f) }, modifier = Modifier.fillMaxWidth()); Text("${state.answered} / ${state.total}", style = MaterialTheme.typography.labelMedium); Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) { Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) { Text(quiz.promptText, style = MaterialTheme.typography.headlineMedium); quiz.options.forEach { option -> val selected = option == quiz.selectedOption; val correct = state.answerFeedback?.let { option == quiz.correctAnswerText }; val wrong = state.answerFeedback?.let { selected && !it.isCorrect }; val border = when { correct == true -> QuizCorrect; wrong == true -> QuizWrong; selected -> QuizSelected; else -> MaterialTheme.colorScheme.outline }; OutlinedButton(onClick = { vm.selectQuizOption(option) }, enabled = state.answerFeedback == null && !state.isSubmitting, modifier = Modifier.fillMaxWidth(), border = BorderStroke(2.dp, border)) { Text(option) } }; Button(onClick = vm::submitQuizAnswer, enabled = quiz.selectedOption != null && state.canSubmitAnswer, modifier = Modifier.fillMaxWidth()) { Text("ثبت پاسخ") } } } }

@Composable private fun FinishCard(state: ReviewUiState, onFinished: () -> Unit) { Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) { Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) { Text("مرور تمام شد", style = MaterialTheme.typography.headlineSmall); Text("درست: ${state.correct} • غلط: ${state.wrong}"); Button(onClick = onFinished) { Text("بازگشت") } } } }
@Composable private fun FeedbackCard(state: ReviewUiState) { val feedback = state.answerFeedback ?: return; Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) { Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) { Text(if (feedback.isCorrect) "پاسخ درست" else "پاسخ نادرست", color = if (feedback.isCorrect) QuizCorrect else QuizWrong, style = MaterialTheme.typography.headlineSmall); feedback.correctAnswerText?.let { Text("پاسخ صحیح: $it", color = QuizCorrect) }; Text("مرحله: ${feedback.stageLabel} • سختی واژه: ${feedback.difficultyLabel}"); Text("درست: ${feedback.correct} • غلط: ${feedback.wrong}") } } }
@Composable private fun QuizUnavailableCard() { Card(Modifier.fillMaxWidth()) { Text("برای این سؤال چهار گزینهٔ معتبر پیدا نشد.", modifier = Modifier.padding(20.dp)) } }
private fun difficultyLabel(value: VocabularyDifficulty): String = when (value) { VocabularyDifficulty.EASY -> "آسان"; VocabularyDifficulty.MEDIUM -> "متوسط"; VocabularyDifficulty.HARD -> "سخت"; VocabularyDifficulty.VERY_HARD -> "خیلی سخت" }
private fun quizDifficultyLabel(value: QuizDifficulty): String = when (value) { QuizDifficulty.EASY -> "آسان"; QuizDifficulty.MEDIUM -> "متوسط"; QuizDifficulty.HARD -> "سخت" }
private fun reviewTypeLabel(value: ReviewType): String = when (value) { ReviewType.DAILY -> "روزانه"; ReviewType.WEEKLY -> "هفتگی"; ReviewType.MONTHLY -> "ماهانه"; ReviewType.LEARNED -> "یادگرفته‌شده"; ReviewType.RANDOM -> "تصادفی" }
