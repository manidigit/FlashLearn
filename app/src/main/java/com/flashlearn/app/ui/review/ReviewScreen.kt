package com.flashlearn.app.ui.review

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        state.error?.takeIf { state.selectedMode != ReviewMode.QUIZ || state.quizCard == null }?.let {
            Text("خطا: $it", color = MaterialTheme.colorScheme.error, modifier = Modifier.fillMaxWidth())
        }
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

@Composable
private fun ReviewSetup(state: ReviewUiState, vm: ReviewViewModel, personalDifficulty: VocabularyDifficulty?) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("مرور کلمات", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("جلسه مرور را مطابق نیازت تنظیم کن", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        SetupSection("نوع مرور", "یک حالت را انتخاب کن") {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ModeCard("تستی", "آزمون چهارگزینه‌ای", state.selectedMode == ReviewMode.QUIZ, { vm.chooseMode(ReviewMode.QUIZ) }, Icons.Outlined.Quiz, Modifier.weight(1f))
                ModeCard("فلش‌کارت", "مرور با کارت", state.selectedMode == ReviewMode.FLASHCARD, { vm.chooseMode(ReviewMode.FLASHCARD) }, Icons.Outlined.Style, Modifier.weight(1f))
            }
        }

        SetupSection("حالت مرور", "ترتیب انتخاب واژه‌ها") {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ModeCard("تصادفی", "ترتیب تصادفی", state.selectedReviewType == ReviewType.RANDOM, { vm.chooseReviewType(ReviewType.RANDOM) }, Icons.Outlined.Shuffle, Modifier.weight(1f))
                ModeCard("یادگرفته", "واژه‌های یادگرفته", state.selectedReviewType == ReviewType.LEARNED, { vm.chooseReviewType(ReviewType.LEARNED) }, Icons.Outlined.DoneAll, Modifier.weight(1f))
            }
        }

        SetupSection("فیلترهای واژه", "چند انتخابی • «همه» یعنی بدون فیلتر") {
            FilterBlock(title = "سطح دشواری کلمات", icon = Icons.Outlined.Tune) {
                ChoiceGrid(
                    options = listOf("همه" to state.selectedDifficulties.isEmpty()) + VocabularyDifficulty.entries.map { difficultyLabel(it) to (it in state.selectedDifficulties) },
                    onChoice = { index -> vm.toggleDifficulty(if (index == 0) null else VocabularyDifficulty.entries[index - 1]) }
                )
            }
            if (state.categories.isNotEmpty()) {
                Spacer(Modifier.height(14.dp))
                FilterBlock(title = "دسته‌بندی لغات", icon = Icons.Outlined.Category) {
                    ChoiceGrid(
                        options = listOf("همه" to state.selectedCategoryIds.isEmpty()) + state.categories.map { it.name to (it.id in state.selectedCategoryIds) },
                        onChoice = { index -> vm.toggleCategory(if (index == 0) null else state.categories[index - 1].id) }
                    )
                }
            }
        }

        if (state.selectedMode == ReviewMode.QUIZ) {
            SetupSection("سطح دشواری آزمون", "میزان چالش سؤال‌ها") {
                ChoiceGrid(
                    options = listOf(
                        quizDifficultyLabel(QuizDifficulty.EASY) to (state.selectedQuizDifficulty == QuizDifficulty.EASY),
                        quizDifficultyLabel(QuizDifficulty.MEDIUM) to (state.selectedQuizDifficulty == QuizDifficulty.MEDIUM),
                        quizDifficultyLabel(QuizDifficulty.HARD) to (state.selectedQuizDifficulty == QuizDifficulty.HARD)
                    ),
                    columns = 3,
                    onChoice = { index -> vm.chooseQuizDifficulty(listOf(QuizDifficulty.EASY, QuizDifficulty.MEDIUM, QuizDifficulty.HARD)[index]) }
                )
            }
        }

        SetupSection("تعداد کلمات", "تعداد واژه‌های این جلسه") {
            ChoiceGrid(
                options = listOf(10, 20, 30, 50, 100).map { it.toString() to (state.maximumReviewCards == it) },
                columns = 5,
                onChoice = { index -> vm.setMaximumReviewCards(listOf(10, 20, 30, 50, 100)[index]) }
            )
        }

        personalDifficulty?.let {
            Text("سطح شخصی فعلی: ${difficultyLabel(it)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth())
        }

        val filterSummary = buildList {
            add(if (state.selectedDifficulties.isEmpty()) "همه سطوح" else state.selectedDifficulties.sortedBy { it.ordinal }.joinToString("، ") { difficultyLabel(it) })
            if (state.categories.isNotEmpty()) add(if (state.selectedCategoryIds.isEmpty()) "همه دسته‌ها" else "${state.selectedCategoryIds.size} دسته")
            add(if (state.selectedReviewType == ReviewType.RANDOM) "تصادفی" else "یادگرفته")
        }.joinToString("  •  ")

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = .48f))
        ) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.primary.copy(alpha = .12f)) {
                    Icon(Icons.Outlined.FormatListNumbered, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(11.dp).size(25.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("${state.maximumReviewCards} کلمه آماده مرور", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(3.dp))
                    Text(filterSummary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                }
            }
        }

        Button(
            onClick = vm::startNewSession,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = MaterialTheme.shapes.extraLarge,
            contentPadding = PaddingValues(horizontal = 20.dp)
        ) {
            Icon(Icons.Outlined.PlayArrow, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("شروع مرور", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SetupSection(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = .55f))
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(Modifier.fillMaxWidth()) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(2.dp))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            content()
        }
    }
}

@Composable
private fun FilterBlock(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, content: @Composable () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(19.dp))
        Spacer(Modifier.width(7.dp))
        Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
    }
    content()
}

@Composable
private fun ModeCard(label: String, description: String, selected: Boolean, onClick: () -> Unit, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier) {
    val container = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val borderColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = .70f) else MaterialTheme.colorScheme.outlineVariant
    Card(
        modifier = modifier.height(112.dp).clickable(onClick = onClick),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = container),
        border = BorderStroke(if (selected) 2.dp else 1.dp, borderColor)
    ) {
        Column(Modifier.fillMaxSize().padding(13.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Surface(shape = MaterialTheme.shapes.large, color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = .14f) else MaterialTheme.colorScheme.surfaceVariant) {
                Icon(icon, contentDescription = null, tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(8.dp).size(25.dp))
            }
            Spacer(Modifier.height(6.dp))
            Text(label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
    }
}

@Composable
private fun ChoiceGrid(options: List<Pair<String, Boolean>>, columns: Int = 2, onChoice: (Int) -> Unit) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.chunked(columns).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEachIndexed { index, (label, selected) ->
                    ChoiceTile(label, selected, { onChoice(options.indexOf(row[index])) }, Modifier.weight(1f))
                }
                repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun ChoiceTile(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val container = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val border = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = .72f) else MaterialTheme.colorScheme.outlineVariant
    Surface(
        modifier = modifier.height(46.dp).clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        color = container,
        border = BorderStroke(if (selected) 1.5.dp else 1.dp, border)
    ) {
        Row(Modifier.fillMaxSize().padding(horizontal = 10.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            if (selected) {
                Icon(Icons.Outlined.DoneAll, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(5.dp))
            }
            Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, textAlign = TextAlign.Center, maxLines = 1)
        }
    }
}

@Composable private fun FlashCard(state: ReviewUiState, vm: ReviewViewModel) { val card = state.card ?: return; val progress = if (state.total <= 0) 0f else state.answered.toFloat() / state.total.toFloat(); Text("${state.remaining} کارت باقی‌مانده از ${state.total}", style = MaterialTheme.typography.labelMedium); LinearProgressIndicator(progress = { progress.coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth()); Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) { Column(Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(card.sourceText, style = MaterialTheme.typography.headlineMedium); if (card.isFlipped) { Spacer(Modifier.height(14.dp)); Text(card.targetText, style = MaterialTheme.typography.headlineSmall) }; if (card.hintRevealed && !card.hintText.isNullOrBlank()) { Spacer(Modifier.height(8.dp)); Text(card.hintText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary) }; if (card.noteVisible && !card.sourceNotes.isNullOrBlank()) { Spacer(Modifier.height(8.dp)); Text(card.sourceNotes, style = MaterialTheme.typography.bodySmall) } } }; if (!card.isFlipped) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { if (!card.sourceNotes.isNullOrBlank()) OutlinedButton(onClick = vm::toggleNote, modifier = Modifier.weight(1f)) { Text(if (card.noteVisible) "مخفی کردن یادداشت" else "نمایش یادداشت") }; OutlinedButton(onClick = vm::revealHint, enabled = !card.hintRevealed, modifier = Modifier.weight(1f)) { Text("راهنما") } }; Button(onClick = vm::flipCard, modifier = Modifier.fillMaxWidth().height(48.dp)) { Text("نمایش پاسخ") } } else { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedButton(onClick = { vm.submitAnswer(false) }, enabled = state.canSubmitAnswer, modifier = Modifier.weight(1f)) { Text("غلط") }; Button(onClick = { vm.submitAnswer(true) }, enabled = state.canSubmitAnswer, modifier = Modifier.weight(1f)) { Text("صحیح") } } } }
@Composable private fun QuizCard(state: ReviewUiState, vm: ReviewViewModel) { val quiz = state.quizCard ?: return; val feedback = state.answerFeedback; val answered = feedback != null; val progress = if (state.total <= 0) 0f else state.answered.toFloat() / state.total.toFloat(); val selected = quiz.selectedOption; val correct = quiz.correctAnswerText; val card = state.card; Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) { Text("${state.correct} ✓", color = QuizCorrect, style = MaterialTheme.typography.titleMedium); Spacer(Modifier.width(22.dp)); Text("${state.answered} / ${state.total}", style = MaterialTheme.typography.titleMedium); Spacer(Modifier.width(22.dp)); Text("${state.wrong} ✕", color = QuizWrong, style = MaterialTheme.typography.titleMedium) }; LinearProgressIndicator(progress = { progress.coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth()); Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) { Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 26.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(quiz.promptText, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center); if (card?.hintRevealed == true && !card.hintText.isNullOrBlank()) { Spacer(Modifier.height(10.dp)); Text(card.hintText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center) }; if (card?.noteVisible == true && !card.sourceNotes.isNullOrBlank()) { Spacer(Modifier.height(8.dp)); Text(card.sourceNotes, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center) } } }; Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { if (!card?.sourceNotes.isNullOrBlank()) OutlinedButton(onClick = vm::toggleNote, enabled = !answered, modifier = Modifier.weight(1f)) { Text(if (card?.noteVisible == true) "مخفی کردن یادداشت" else "نمایش یادداشت") }; OutlinedButton(onClick = vm::revealHint, enabled = !answered && card != null && !card.hintRevealed, modifier = Modifier.weight(1f)) { Text("💡 راهنما") } }; Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) { quiz.options.forEach { option -> val isCorrect = option == correct; val isWrongSelection = answered && option == selected && !isCorrect; val isSelected = !answered && option == selected; val container = when { answered && isCorrect -> QuizCorrectContainer; isWrongSelection -> QuizWrongContainer; isSelected -> QuizSelectedContainer; else -> MaterialTheme.colorScheme.surface }; val content = when { answered && isCorrect -> QuizCorrect; isWrongSelection -> QuizWrong; isSelected -> QuizSelected; else -> MaterialTheme.colorScheme.onSurface }; val border = when { answered && isCorrect -> QuizCorrect; isWrongSelection -> QuizWrong; isSelected -> QuizSelected; else -> MaterialTheme.colorScheme.outline }; OutlinedButton(onClick = { vm.selectQuizOption(option) }, enabled = !answered && !state.isSubmitting, modifier = Modifier.fillMaxWidth().height(72.dp), shape = MaterialTheme.shapes.large, border = BorderStroke(if ((answered && (isCorrect || isWrongSelection)) || isSelected) 2.dp else 1.dp, border), colors = ButtonDefaults.outlinedButtonColors(containerColor = container, contentColor = content)) { Text(option, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center) } } }; if (!answered) Button(onClick = vm::submitQuizAnswer, enabled = selected != null && !state.isSubmitting, modifier = Modifier.fillMaxWidth().height(52.dp), shape = MaterialTheme.shapes.large) { Text("ثبت پاسخ", style = MaterialTheme.typography.titleMedium) } else Text(if (feedback?.isCorrect == true) "✓ پاسخ صحیح — چند لحظه صبر کنید…" else "✕ پاسخ غلط — پاسخ صحیح سبز شده است؛ چند لحظه صبر کنید…", color = if (feedback?.isCorrect == true) QuizCorrect else QuizWrong, style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth()) }
@Composable private fun QuizUnavailableCard() { Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) { Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text("آزمون چهارگزینه‌ای آماده نشد", style = MaterialTheme.typography.titleLarge); Text("حالت آزمون حفظ شده و به فلش‌کارت تبدیل نمی‌شود.", color = MaterialTheme.colorScheme.onSurfaceVariant) } } }
@Composable private fun FeedbackCard(state: ReviewUiState) { val f = state.answerFeedback ?: return; Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) { Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(7.dp)) { Text(if (f.isCorrect) "✓ پاسخ صحیح" else "✕ پاسخ نادرست", style = MaterialTheme.typography.headlineSmall, color = if (f.isCorrect) QuizCorrect else QuizWrong); Text("مرحله: ${f.stageLabel}"); Text("سختی: ${f.difficultyLabel}"); if (!f.isCorrect && !f.correctAnswerText.isNullOrBlank()) Text("پاسخ صحیح: ${f.correctAnswerText}"); Text("نتیجه: ${state.correct} صحیح، ${state.wrong} غلط") } } }
@Composable private fun FinishCard(state: ReviewUiState, onFinished: () -> Unit) { Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) { Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("مرور تمام شد!", style = MaterialTheme.typography.headlineSmall); Text("${state.answered} کارت پاسخ داده شد"); Text("صحیح: ${state.correct} • غلط: ${state.wrong}"); Text("دقت این جلسه: ${if (state.answered == 0) 0 else state.correct * 100 / state.answered}٪"); Button(onClick = onFinished, modifier = Modifier.fillMaxWidth()) { Text("بازگشت به خانه") } } } }
private fun difficultyLabel(difficulty: VocabularyDifficulty) = when (difficulty) { VocabularyDifficulty.EASY -> "آسان"; VocabularyDifficulty.MEDIUM -> "متوسط"; VocabularyDifficulty.HARD -> "سخت"; VocabularyDifficulty.VERY_HARD -> "خیلی سخت" }
private fun quizDifficultyLabel(difficulty: QuizDifficulty) = when (difficulty) { QuizDifficulty.EASY -> "ابتدایی"; QuizDifficulty.MEDIUM -> "متوسط"; QuizDifficulty.HARD -> "حرفه‌ای" }
