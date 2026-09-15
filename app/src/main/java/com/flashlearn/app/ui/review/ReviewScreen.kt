package com.flashlearn.app.ui.review

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
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
    LaunchedEffect(quizDifficulty) { viewModel.setQuizDifficulty(quizDifficulty) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        state.error?.takeIf { state.selectedMode != ReviewMode.QUIZ || state.quizCard == null }?.let { Text("خطا: $it", color = MaterialTheme.colorScheme.error, modifier = Modifier.fillMaxWidth()) }
        when {
            state.isSelectingMode -> ReviewSetup(state, viewModel, personalDifficulty)
            state.isLoading -> CircularProgressIndicator()
            state.isFinished -> FinishCard(state, onFinished)
            state.selectedMode == ReviewMode.QUIZ && state.quizCard != null -> QuizCard(state, viewModel)
            state.answerFeedback != null -> FeedbackCard(state)
            state.selectedMode == ReviewMode.QUIZ -> QuizUnavailableCard()
            state.card != null -> FlashCard(state, viewModel)
        }
    }
}

@Composable private fun ReviewSetup(state: ReviewUiState, vm: ReviewViewModel, personalDifficulty: VocabularyDifficulty?) {
    Text("مرور کلمات", style = MaterialTheme.typography.headlineSmall)
    Text("نوع مرور", style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth())
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        SelectCard("تستی", state.selectedMode == ReviewMode.QUIZ, { vm.chooseMode(ReviewMode.QUIZ) }, Modifier.weight(1f), "✓")
        SelectCard("فلش‌کارت", state.selectedMode == ReviewMode.FLASHCARD, { vm.chooseMode(ReviewMode.FLASHCARD) }, Modifier.weight(1f), "▣")
    }
    Text("حالت مرور", style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth())
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        SelectCard("تصادفی", state.selectedReviewType == ReviewType.RANDOM, { vm.chooseReviewType(ReviewType.RANDOM) }, Modifier.weight(1f), "⤨")
        SelectCard("یادگرفته", state.selectedReviewType == ReviewType.LEARNED, { vm.chooseReviewType(ReviewType.LEARNED) }, Modifier.weight(1f), "★")
    }
    Text("سطح دشواری کلمات", style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth())
    Text("چند انتخابی", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth())
    FlowChoiceRow(listOf("همه" to state.selectedDifficulties.isEmpty()) + VocabularyDifficulty.entries.map { difficultyLabel(it) to (it in state.selectedDifficulties) }) { index -> vm.toggleDifficulty(if (index == 0) null else VocabularyDifficulty.entries[index - 1]) }
    if (state.categories.isNotEmpty()) {
        Text("دسته‌بندی لغات", style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth())
        Text("چند انتخابی", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth())
        FlowChoiceRow(listOf("همه" to state.selectedCategoryIds.isEmpty()) + state.categories.map { it.name to (it.id in state.selectedCategoryIds) }) { index -> vm.toggleCategory(if (index == 0) null else state.categories[index - 1].id) }
    }
    Text("سطح دشواری آزمون", style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth())
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        listOf(QuizDifficulty.EASY, QuizDifficulty.MEDIUM, QuizDifficulty.HARD).forEach { level -> ChoiceChip(quizDifficultyLabel(level), state.selectedQuizDifficulty == level) { vm.chooseQuizDifficulty(level) } }
    }
    Text("تعداد کلمات", style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth())
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        listOf(10, 20, 30, 50, 100).forEach { count -> ChoiceChip(count.toString(), state.maximumReviewCards == count) { vm.setMaximumReviewCards(count) } }
    }
    personalDifficulty?.let { Text("سطح شخصی فعلی: ${difficultyLabel(it)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth()) }
    val filterSummary = buildList {
        add(if (state.selectedDifficulties.isEmpty()) "همه سطوح" else state.selectedDifficulties.sortedBy { it.ordinal }.joinToString(" و ") { difficultyLabel(it) })
        if (state.categories.isNotEmpty()) add(if (state.selectedCategoryIds.isEmpty()) "همه دسته‌ها" else "${state.selectedCategoryIds.size} دسته")
        add(if (state.selectedReviewType == ReviewType.RANDOM) "تصادفی" else "یادگرفته")
    }.joinToString(" · ")
    Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .65f))) {
        Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("${state.maximumReviewCards} کلمه آماده مرور", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(filterSummary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
    Button(onClick = vm::startNewSession, modifier = Modifier.fillMaxWidth().height(52.dp), shape = MaterialTheme.shapes.large) { Text("شروع مرور", style = MaterialTheme.typography.titleMedium) }
}

@Composable private fun FlowChoiceRow(options: List<Pair<String, Boolean>>, onChoice: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) { options.forEachIndexed { i, (label, selected) -> ChoiceChip(label, selected) { onChoice(i) } } }
}
@Composable private fun ChoiceChip(label: String, selected: Boolean, onClick: () -> Unit) { FilterChip(selected = selected, onClick = onClick, label = { Text(label, style = MaterialTheme.typography.labelMedium) }) }
@Composable private fun SelectCard(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier, icon: String) {
    Card(modifier = modifier.clickable(onClick = onClick), shape = MaterialTheme.shapes.large, colors = CardDefaults.cardColors(containerColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = .10f) else MaterialTheme.colorScheme.surfaceVariant), border = if (selected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = .55f)) else null) {
        Column(Modifier.fillMaxWidth().padding(vertical = 15.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(icon, style = MaterialTheme.typography.titleLarge, color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.height(5.dp)); Text(label, style = MaterialTheme.typography.titleSmall) }
    }
}

@Composable private fun FlashCard(state: ReviewUiState, vm: ReviewViewModel) { val card = state.card ?: return; val progress = if (state.total <= 0) 0f else state.answered.toFloat() / state.total.toFloat(); Text("${state.remaining} کارت باقی‌مانده از ${state.total}", style = MaterialTheme.typography.labelMedium); LinearProgressIndicator(progress = { progress.coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth()); Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) { Column(Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(card.sourceText, style = MaterialTheme.typography.headlineMedium); if (card.isFlipped) { Spacer(Modifier.height(14.dp)); Text(card.targetText, style = MaterialTheme.typography.headlineSmall) }; if (card.hintRevealed && !card.hintText.isNullOrBlank()) { Spacer(Modifier.height(8.dp)); Text(card.hintText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary) }; if (card.noteVisible && !card.sourceNotes.isNullOrBlank()) { Spacer(Modifier.height(8.dp)); Text(card.sourceNotes, style = MaterialTheme.typography.bodySmall) } } }; if (!card.isFlipped) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { if (!card.sourceNotes.isNullOrBlank()) OutlinedButton(onClick = vm::toggleNote, modifier = Modifier.weight(1f)) { Text(if (card.noteVisible) "مخفی کردن یادداشت" else "نمایش یادداشت") }; OutlinedButton(onClick = vm::revealHint, enabled = !card.hintRevealed, modifier = Modifier.weight(1f)) { Text("راهنما") } }; Button(onClick = vm::flipCard, modifier = Modifier.fillMaxWidth().height(48.dp)) { Text("نمایش پاسخ") } } else { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedButton(onClick = { vm.submitAnswer(false) }, enabled = state.canSubmitAnswer, modifier = Modifier.weight(1f)) { Text("غلط") }; Button(onClick = { vm.submitAnswer(true) }, enabled = state.canSubmitAnswer, modifier = Modifier.weight(1f)) { Text("صحیح") } } } }
@Composable private fun QuizCard(state: ReviewUiState, vm: ReviewViewModel) { val quiz = state.quizCard ?: return; val feedback = state.answerFeedback; val answered = feedback != null; val progress = if (state.total <= 0) 0f else state.answered.toFloat() / state.total.toFloat(); val selected = quiz.selectedOption; val correct = quiz.correctAnswerText; val card = state.card; Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) { Text("${state.correct} ✓", color = QuizCorrect, style = MaterialTheme.typography.titleMedium); Spacer(Modifier.width(22.dp)); Text("${state.answered} / ${state.total}", style = MaterialTheme.typography.titleMedium); Spacer(Modifier.width(22.dp)); Text("${state.wrong} ✕", color = QuizWrong, style = MaterialTheme.typography.titleMedium) }; LinearProgressIndicator(progress = { progress.coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth()); Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) { Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 26.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(quiz.promptText, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center); if (card?.hintRevealed == true && !card.hintText.isNullOrBlank()) { Spacer(Modifier.height(10.dp)); Text(card.hintText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center) }; if (card?.noteVisible == true && !card.sourceNotes.isNullOrBlank()) { Spacer(Modifier.height(8.dp)); Text(card.sourceNotes, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center) } } }; Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { if (!card?.sourceNotes.isNullOrBlank()) OutlinedButton(onClick = vm::toggleNote, enabled = !answered, modifier = Modifier.weight(1f)) { Text(if (card?.noteVisible == true) "مخفی کردن یادداشت" else "نمایش یادداشت") }; OutlinedButton(onClick = vm::revealHint, enabled = !answered && card != null && !card.hintRevealed, modifier = Modifier.weight(1f)) { Text("💡 راهنما") } }; Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) { quiz.options.forEach { option -> val isCorrect = option == correct; val isWrongSelection = answered && option == selected && !isCorrect; val isSelected = !answered && option == selected; val container = when { answered && isCorrect -> QuizCorrectContainer; isWrongSelection -> QuizWrongContainer; isSelected -> QuizSelectedContainer; else -> MaterialTheme.colorScheme.surface }; val content = when { answered && isCorrect -> QuizCorrect; isWrongSelection -> QuizWrong; isSelected -> QuizSelected; else -> MaterialTheme.colorScheme.onSurface }; val border = when { answered && isCorrect -> QuizCorrect; isWrongSelection -> QuizWrong; isSelected -> QuizSelected; else -> MaterialTheme.colorScheme.outline }; OutlinedButton(onClick = { vm.selectQuizOption(option) }, enabled = !answered && !state.isSubmitting, modifier = Modifier.fillMaxWidth().height(72.dp), shape = MaterialTheme.shapes.large, border = BorderStroke(if ((answered && (isCorrect || isWrongSelection)) || isSelected) 2.dp else 1.dp, border), colors = ButtonDefaults.outlinedButtonColors(containerColor = container, contentColor = content)) { Text(option, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center) } } }; if (!answered) Button(onClick = vm::submitQuizAnswer, enabled = selected != null && !state.isSubmitting, modifier = Modifier.fillMaxWidth().height(52.dp), shape = MaterialTheme.shapes.large) { Text("ثبت پاسخ", style = MaterialTheme.typography.titleMedium) } else Text(if (feedback?.isCorrect == true) "✓ پاسخ صحیح — چند لحظه صبر کنید…" else "✕ پاسخ غلط — پاسخ صحیح سبز شده است؛ چند لحظه صبر کنید…", color = if (feedback?.isCorrect == true) QuizCorrect else QuizWrong, style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth()) }
@Composable private fun QuizUnavailableCard() { Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) { Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text("آزمون چهارگزینه‌ای آماده نشد", style = MaterialTheme.typography.titleLarge); Text("حالت آزمون حفظ شده و به فلش‌کارت تبدیل نمی‌شود.", color = MaterialTheme.colorScheme.onSurfaceVariant) } } }
@Composable private fun FeedbackCard(state: ReviewUiState) { val f = state.answerFeedback ?: return; Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) { Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(7.dp)) { Text(if (f.isCorrect) "✓ پاسخ صحیح" else "✕ پاسخ نادرست", style = MaterialTheme.typography.headlineSmall, color = if (f.isCorrect) QuizCorrect else QuizWrong); Text("مرحله: ${f.stageLabel}"); Text("سختی: ${f.difficultyLabel}"); if (!f.isCorrect && !f.correctAnswerText.isNullOrBlank()) Text("پاسخ صحیح: ${f.correctAnswerText}"); Text("نتیجه: ${state.correct} صحیح، ${state.wrong} غلط") } } }
@Composable private fun FinishCard(state: ReviewUiState, onFinished: () -> Unit) { Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) { Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("مرور تمام شد!", style = MaterialTheme.typography.headlineSmall); Text("${state.answered} کارت پاسخ داده شد"); Text("صحیح: ${state.correct} • غلط: ${state.wrong}"); Text("دقت این جلسه: ${if (state.answered == 0) 0 else state.correct * 100 / state.answered}٪"); Button(onClick = onFinished, modifier = Modifier.fillMaxWidth()) { Text("بازگشت به خانه") } } } }
private fun difficultyLabel(difficulty: VocabularyDifficulty) = when (difficulty) { VocabularyDifficulty.EASY -> "آسان"; VocabularyDifficulty.MEDIUM -> "متوسط"; VocabularyDifficulty.HARD -> "سخت"; VocabularyDifficulty.VERY_HARD -> "خیلی سخت" }
private fun quizDifficultyLabel(difficulty: QuizDifficulty) = when (difficulty) { QuizDifficulty.EASY -> "ابتدایی"; QuizDifficulty.MEDIUM -> "متوسط"; QuizDifficulty.HARD -> "حرفه‌ای" }
