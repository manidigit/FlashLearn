package com.flashlearn.app.ui.review

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.RocketLaunch
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.flashlearn.domain.model.QuizDifficulty
import com.flashlearn.domain.model.ReviewType
import com.flashlearn.domain.model.VocabularyDifficulty
import com.flashlearn.app.ui.library.CategorySelectionScreen

private val QuizCorrect = Color(0xFF2E7D32)
private val QuizWrong = Color(0xFFC62828)
private val QuizSelected = Color(0xFF7C4DFF)
private val QuizCorrectContainer = Color(0xFFE8F5E9)
private val QuizWrongContainer = Color(0xFFFFEBEE)
private val QuizSelectedContainer = Color(0xFFF0E7FF)

@Composable
fun ReviewScreen(viewModel: ReviewViewModel, personalDifficulty: VocabularyDifficulty? = null, quizDifficulty: QuizDifficulty = QuizDifficulty.MEDIUM, onFinished: () -> Unit) {
    val state by viewModel.state.collectAsState()
    var showCategoryPicker by remember { mutableStateOf(false) }
    LaunchedEffect(quizDifficulty) { viewModel.setQuizDifficulty(quizDifficulty) }
    if (showCategoryPicker) {
        CategorySelectionScreen(categories = state.categories, selectedIds = state.selectedCategoryIds, counts = state.categoryWordCounts, allCount = state.allCategoryWordCount, onBack = { showCategoryPicker = false }, onApply = { ids -> viewModel.setCategories(ids); showCategoryPicker = false })
        return
    }
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        state.error?.takeIf { state.selectedMode != ReviewMode.QUIZ || state.quizCard == null }?.let { Text("خطا: $it", color = MaterialTheme.colorScheme.error, modifier = Modifier.fillMaxWidth()) }
        when {
            state.isSelectingMode -> ReviewSetup(state, viewModel, personalDifficulty, onFinished, onOpenCategories = { showCategoryPicker = true })
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
private fun ReviewSetup(state: ReviewUiState, vm: ReviewViewModel, personalDifficulty: VocabularyDifficulty?, onBack: () -> Unit = {}, onOpenCategories: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Box(Modifier.fillMaxWidth().height(58.dp)) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) { Icon(Icons.Outlined.ArrowBack, contentDescription = "بازگشت", tint = MaterialTheme.colorScheme.onSurface) }
            Text("مرور کلمات", modifier = Modifier.align(Alignment.Center), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }

        Text("نوع مرور", Modifier.fillMaxWidth(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
        Text("انتخاب یکی از حالت‌های تصادفی، یادگرفته یا مرور زمان‌بندی‌شده", Modifier.fillMaxWidth(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.End)
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ReviewModeCard("تصادفی", state.selectedReviewType == ReviewType.RANDOM, { vm.chooseReviewType(ReviewType.RANDOM) }, Icons.Outlined.Style, Modifier.weight(1f))
                ReviewModeCard("یادگرفته", state.selectedReviewType == ReviewType.LEARNED, { vm.chooseReviewType(ReviewType.LEARNED) }, Icons.Outlined.DoneAll, Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ReviewModeCard("روزانه", state.selectedReviewType == ReviewType.DAILY, { vm.chooseReviewType(ReviewType.DAILY) }, Icons.Outlined.FormatListNumbered, Modifier.weight(1f))
                ReviewModeCard("هفتگی", state.selectedReviewType == ReviewType.WEEKLY, { vm.chooseReviewType(ReviewType.WEEKLY) }, Icons.Outlined.Category, Modifier.weight(1f))
                ReviewModeCard("ماهانه", state.selectedReviewType == ReviewType.MONTHLY, { vm.chooseReviewType(ReviewType.MONTHLY) }, Icons.Outlined.AutoAwesome, Modifier.weight(1f))
            }
        }

        Text("حالت اجرا", Modifier.fillMaxWidth(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
        Text("یکی را انتخاب کن: تستی یا فلش‌کارت", Modifier.fillMaxWidth(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.End)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ReviewModeCard("تستی", state.selectedMode == ReviewMode.QUIZ, { vm.chooseMode(ReviewMode.QUIZ) }, Icons.Outlined.Quiz, Modifier.weight(1f))
            ReviewModeCard("فلش‌کارت", state.selectedMode == ReviewMode.FLASHCARD, { vm.chooseMode(ReviewMode.FLASHCARD) }, Icons.Outlined.Style, Modifier.weight(1f))
        }

        Text("سطح دشواری کلمات", Modifier.fillMaxWidth(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
        Text("چند انتخابی", Modifier.fillMaxWidth(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.End)
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DifficultyTile("آسان", VocabularyDifficulty.EASY in state.selectedDifficulties, VocabularyDifficulty.EASY, difficultyIcon(VocabularyDifficulty.EASY), Modifier.weight(1f), vm)
                DifficultyTile("متوسط", VocabularyDifficulty.MEDIUM in state.selectedDifficulties, VocabularyDifficulty.MEDIUM, difficultyIcon(VocabularyDifficulty.MEDIUM), Modifier.weight(1f), vm)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DifficultyTile("سخت", VocabularyDifficulty.HARD in state.selectedDifficulties, VocabularyDifficulty.HARD, difficultyIcon(VocabularyDifficulty.HARD), Modifier.weight(1f), vm)
                DifficultyTile("خیلی سخت", VocabularyDifficulty.VERY_HARD in state.selectedDifficulties, VocabularyDifficulty.VERY_HARD, difficultyIcon(VocabularyDifficulty.VERY_HARD), Modifier.weight(1f), vm)
            }
            Surface(modifier = Modifier.fillMaxWidth().height(48.dp).clickable { vm.toggleDifficulty(null) }, shape = MaterialTheme.shapes.large, color = if (state.selectedDifficulties.isEmpty()) QuizSelectedContainer else MaterialTheme.colorScheme.surface, border = BorderStroke(if (state.selectedDifficulties.isEmpty()) 2.dp else 1.dp, if (state.selectedDifficulties.isEmpty()) QuizSelected else MaterialTheme.colorScheme.outlineVariant)) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("همه سطوح", style = MaterialTheme.typography.labelLarge, fontWeight = if (state.selectedDifficulties.isEmpty()) FontWeight.Bold else FontWeight.Normal) }
            }
        }

        Text("دسته‌بندی لغات", Modifier.fillMaxWidth(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
        Text("چند انتخابی", Modifier.fillMaxWidth(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.End)
        CategoryFilterCard(state, onOpenCategories)

        if (state.selectedMode == ReviewMode.QUIZ) {
            Text("سطح دشواری آزمون تستی", Modifier.fillMaxWidth(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuizLevelTile("حرفه‌ای", state.selectedQuizDifficulty == QuizDifficulty.HARD, Icons.Outlined.EmojiEvents, Modifier.weight(1f)) { vm.chooseQuizDifficulty(QuizDifficulty.HARD) }
                QuizLevelTile("متوسط", state.selectedQuizDifficulty == QuizDifficulty.MEDIUM, Icons.Outlined.Tune, Modifier.weight(1f)) { vm.chooseQuizDifficulty(QuizDifficulty.MEDIUM) }
                QuizLevelTile("ابتدایی", state.selectedQuizDifficulty == QuizDifficulty.EASY, Icons.Outlined.School, Modifier.weight(1f)) { vm.chooseQuizDifficulty(QuizDifficulty.EASY) }
            }
        }

        Text("تعداد کلمات", Modifier.fillMaxWidth(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf(10, 20, 30, 50, 100).forEach { count -> CompactChoice(count.toString(), state.maximumReviewCards == count, Modifier.weight(1f)) { vm.setMaximumReviewCards(count) } } }
        personalDifficulty?.let { Text("سطح شخصی فعلی: ${difficultyLabel(it)}", Modifier.fillMaxWidth(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.End) }
        val filterSummary = buildList { add(if (state.selectedDifficulties.isEmpty()) "همه سطوح" else state.selectedDifficulties.sortedBy { it.ordinal }.joinToString("، ") { difficultyLabel(it) }); add(if (state.selectedCategoryIds.isEmpty()) "همه دسته‌ها" else "${state.selectedCategoryIds.size} دسته"); add(reviewTypeLabel(state.selectedReviewType)); add(if (state.selectedMode == ReviewMode.QUIZ) "تست" else "فلش‌کارت") }.joinToString("  •  ")
        Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.extraLarge, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .42f))) { Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text("${minOf(state.maximumReviewCards, state.availableReviewCount)} کلمه آماده مرور از ${state.availableReviewCount} کلمه فیلتر شده", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); Spacer(Modifier.height(5.dp)); Text(filterSummary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center) } }
        Button(enabled = state.availableReviewCount > 0, onClick = vm::startNewSession, modifier = Modifier.fillMaxWidth().height(58.dp), shape = MaterialTheme.shapes.extraLarge, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary), contentPadding = PaddingValues(horizontal = 22.dp)) { Text("شروع مرور", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); Spacer(Modifier.width(8.dp)); Icon(Icons.Outlined.PlayArrow, contentDescription = null) }
    }
}

@Composable private fun CategoryFilterCard(state: ReviewUiState, onClick: () -> Unit) {
    val selectedCount = state.selectedCategoryIds.size
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.extraLarge, colors = CardDefaults.cardColors(containerColor = if (selectedCount > 0) QuizSelectedContainer else MaterialTheme.colorScheme.surface), border = BorderStroke(if (selectedCount > 0) 2.dp else 1.dp, if (selectedCount > 0) QuizSelected.copy(alpha = .55f) else MaterialTheme.colorScheme.outlineVariant)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.ArrowBack, null, tint = QuizSelected, modifier = Modifier.size(22.dp)); Spacer(Modifier.weight(1f)); Column(horizontalAlignment = Alignment.End) { Text(if (selectedCount == 0) "همه دسته‌ها" else "$selectedCount دسته انتخاب شده", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); Text(if (selectedCount == 0) "انتخاب چند دسته برای مرور" else state.selectedCategoryIds.joinToString("، ") { id -> state.categories.firstOrNull { it.id == id }?.name ?: "" }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2) }; Spacer(Modifier.width(14.dp)); Surface(Modifier.size(48.dp), shape = MaterialTheme.shapes.large, color = QuizSelectedContainer) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Category, null, tint = QuizSelected, modifier = Modifier.size(28.dp)) } }
        }
    }
}

@Composable private fun ReviewModeCard(label: String, selected: Boolean, onClick: () -> Unit, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier) {
    Card(modifier.height(108.dp).clickable(onClick = onClick), shape = MaterialTheme.shapes.extraLarge, colors = CardDefaults.cardColors(containerColor = if (selected) QuizSelectedContainer else MaterialTheme.colorScheme.surface), border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) QuizSelected.copy(alpha = .75f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = .75f))) { Box(Modifier.fillMaxSize()) { if (selected) Surface(Modifier.align(Alignment.TopEnd).padding(8.dp).size(28.dp), shape = MaterialTheme.shapes.extraLarge, color = QuizSelected) { Icon(Icons.Outlined.DoneAll, null, tint = Color.White, modifier = Modifier.padding(6.dp)) }; Column(Modifier.fillMaxSize().padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { Icon(icon, null, tint = if (selected) QuizSelected else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(34.dp)); Spacer(Modifier.height(6.dp)); Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold) } } }
}

@Composable private fun DifficultyTile(label: String, selected: Boolean, difficulty: VocabularyDifficulty, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier, vm: ReviewViewModel) {
    val tint = when (difficulty) { VocabularyDifficulty.EASY -> Color(0xFF39B982); VocabularyDifficulty.MEDIUM -> Color(0xFFFFC84A); VocabularyDifficulty.HARD -> Color(0xFFFF7A3D); VocabularyDifficulty.VERY_HARD -> Color(0xFFE95C73) }
    Surface(modifier.height(128.dp).clickable { vm.toggleDifficulty(difficulty) }, shape = MaterialTheme.shapes.extraLarge, color = if (selected) tint.copy(alpha = .07f) else MaterialTheme.colorScheme.surface, border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) tint.copy(alpha = .55f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = .7f))) { Box(Modifier.fillMaxSize()) { if (selected) Icon(Icons.Outlined.DoneAll, null, tint = tint, modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).size(20.dp)); Column(Modifier.fillMaxSize().padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { Icon(icon, null, tint = tint, modifier = Modifier.size(38.dp)); Spacer(Modifier.height(8.dp)); Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, textAlign = TextAlign.Center) } } }
}

private fun difficultyIcon(difficulty: VocabularyDifficulty) = when (difficulty) { VocabularyDifficulty.EASY -> Icons.Outlined.Star; VocabularyDifficulty.MEDIUM -> Icons.Outlined.AutoAwesome; VocabularyDifficulty.HARD -> Icons.Outlined.LocalFireDepartment; VocabularyDifficulty.VERY_HARD -> Icons.Outlined.RocketLaunch }

@Composable private fun QuizLevelTile(label: String, selected: Boolean, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier, onClick: () -> Unit) { Surface(modifier.height(96.dp).clickable(onClick = onClick), shape = MaterialTheme.shapes.large, color = if (selected) QuizSelectedContainer else MaterialTheme.colorScheme.surface, border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) QuizSelected.copy(alpha = .7f) else MaterialTheme.colorScheme.outlineVariant)) { Box(Modifier.fillMaxSize()) { if (selected) Icon(Icons.Outlined.DoneAll, null, tint = QuizSelected, modifier = Modifier.align(Alignment.TopEnd).padding(7.dp).size(18.dp)); Column(Modifier.fillMaxSize().padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { Icon(icon, null, tint = if (selected) QuizSelected else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(30.dp)); Spacer(Modifier.height(5.dp)); Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) } } } }

@Composable private fun CompactChoice(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) { Surface(modifier.height(50.dp).clickable(onClick = onClick), shape = MaterialTheme.shapes.large, color = if (selected) QuizSelectedContainer else MaterialTheme.colorScheme.surface, border = BorderStroke(if (selected) 1.5.dp else 1.dp, if (selected) QuizSelected.copy(alpha = .55f) else MaterialTheme.colorScheme.outlineVariant)) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) } } }

@Composable private fun FlashCard(state: ReviewUiState, vm: ReviewViewModel) { val card = state.card ?: return; val progress = if (state.total <= 0) 0f else state.answered.toFloat() / state.total.toFloat(); Text("${state.remaining} کارت باقی‌مانده از ${state.total}", style = MaterialTheme.typography.labelMedium); LinearProgressIndicator(progress = { progress.coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth()); Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) { Column(Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(card.sourceText, style = MaterialTheme.typography.headlineMedium); if (card.isFlipped) { Spacer(Modifier.height(14.dp)); Text(card.targetText, style = MaterialTheme.typography.headlineSmall) }; if (card.hintRevealed && !card.hintText.isNullOrBlank()) { Spacer(Modifier.height(8.dp)); Text(card.hintText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary) }; if (card.noteVisible && !card.sourceNotes.isNullOrBlank()) { Spacer(Modifier.height(8.dp)); Text(card.sourceNotes, style = MaterialTheme.typography.bodySmall) } } }; if (!card.isFlipped) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { if (!card.sourceNotes.isNullOrBlank()) OutlinedButton(onClick = vm::toggleNote, modifier = Modifier.weight(1f)) { Text(if (card.noteVisible) "مخفی کردن یادداشت" else "نمایش یادداشت") }; OutlinedButton(onClick = vm::revealHint, enabled = !card.hintRevealed, modifier = Modifier.weight(1f)) { Text("راهنما") } }; Button(onClick = vm::flipCard, modifier = Modifier.fillMaxWidth().height(48.dp)) { Text("نمایش پاسخ") } } else { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedButton(onClick = { vm.submitAnswer(false) }, enabled = state.canSubmitAnswer, modifier = Modifier.weight(1f)) { Text("غلط") }; Button(onClick = { vm.submitAnswer(true) }, enabled = state.canSubmitAnswer, modifier = Modifier.weight(1f)) { Text("صحیح") } } } }
@Composable private fun QuizCard(state: ReviewUiState, vm: ReviewViewModel) { val quiz = state.quizCard ?: return; val feedback = state.answerFeedback; val answered = feedback != null; val progress = if (state.total <= 0) 0f else state.answered.toFloat() / state.total.toFloat(); val selected = quiz.selectedOption; val correct = quiz.correctAnswerText; val card = state.card; Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) { Text("${state.correct} ✓", color = QuizCorrect, style = MaterialTheme.typography.titleMedium); Spacer(Modifier.width(22.dp)); Text("${state.answered} / ${state.total}", style = MaterialTheme.typography.titleMedium); Spacer(Modifier.width(22.dp)); Text("${state.wrong} ✕", color = QuizWrong, style = MaterialTheme.typography.titleMedium) }; LinearProgressIndicator(progress = { progress.coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth()); Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) { Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 26.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(quiz.promptText, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center); if (card?.hintRevealed == true && !card.hintText.isNullOrBlank()) { Spacer(Modifier.height(10.dp)); Text(card.hintText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center) }; if (card?.noteVisible == true && !card.sourceNotes.isNullOrBlank()) { Spacer(Modifier.height(8.dp)); Text(card.sourceNotes, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center) } } }; Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { if (!card?.sourceNotes.isNullOrBlank()) OutlinedButton(onClick = vm::toggleNote, enabled = !answered, modifier = Modifier.weight(1f)) { Text(if (card?.noteVisible == true) "مخفی کردن یادداشت" else "نمایش یادداشت") }; OutlinedButton(onClick = vm::revealHint, enabled = !answered && card != null && !card.hintRevealed, modifier = Modifier.weight(1f)) { Text("💡 راهنما") } }; Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) { quiz.options.forEach { option -> val isCorrect = option == correct; val isWrongSelection = answered && option == selected && !isCorrect; val isSelected = !answered && option == selected; val container = when { answered && isCorrect -> QuizCorrectContainer; isWrongSelection -> QuizWrongContainer; isSelected -> QuizSelectedContainer; else -> MaterialTheme.colorScheme.surface }; val content = when { answered && isCorrect -> QuizCorrect; isWrongSelection -> QuizWrong; isSelected -> QuizSelected; else -> MaterialTheme.colorScheme.onSurface }; val border = when { answered && isCorrect -> QuizCorrect; isWrongSelection -> QuizWrong; isSelected -> QuizSelected; else -> MaterialTheme.colorScheme.outline }; OutlinedButton(onClick = { vm.selectQuizOption(option) }, enabled = !answered && !state.isSubmitting, modifier = Modifier.fillMaxWidth().height(72.dp), shape = MaterialTheme.shapes.large, border = BorderStroke(if ((answered && (isCorrect || isWrongSelection)) || isSelected) 2.dp else 1.dp, border), colors = ButtonDefaults.outlinedButtonColors(containerColor = container, contentColor = content)) { Text(option, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center) } } }; if (!answered) Button(onClick = vm::submitQuizAnswer, enabled = selected != null && !state.isSubmitting, modifier = Modifier.fillMaxWidth().height(52.dp), shape = MaterialTheme.shapes.large) { Text("ثبت پاسخ", style = MaterialTheme.typography.titleMedium) } else Text(if (feedback?.isCorrect == true) "✓ پاسخ صحیح — چند لحظه صبر کنید…" else "✕ پاسخ غلط — پاسخ صحیح سبز شده است؛ چند لحظه صبر کنید…", color = if (feedback?.isCorrect == true) QuizCorrect else QuizWrong, style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth()) }
@Composable private fun QuizUnavailableCard() { Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) { Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text("آزمون چهارگزینه‌ای آماده نشد", style = MaterialTheme.typography.titleLarge); Text("حالت آزمون حفظ شده و به فلش‌کارت تبدیل نمی‌شود.", color = MaterialTheme.colorScheme.onSurfaceVariant) } } }
@Composable private fun FeedbackCard(state: ReviewUiState) { val f = state.answerFeedback ?: return; Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) { Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(7.dp)) { Text(if (f.isCorrect) "✓ پاسخ صحیح" else "✕ پاسخ نادرست", style = MaterialTheme.typography.headlineSmall, color = if (f.isCorrect) QuizCorrect else QuizWrong); Text("مرحله: ${f.stageLabel}"); Text("سختی: ${f.difficultyLabel}"); if (!f.isCorrect && !f.correctAnswerText.isNullOrBlank()) Text("پاسخ صحیح: ${f.correctAnswerText}"); Text("نتیجه: ${state.correct} صحیح، ${state.wrong} غلط") } } }
@Composable private fun FinishCard(state: ReviewUiState, onFinished: () -> Unit) { Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) { Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("مرور تمام شد!", style = MaterialTheme.typography.headlineSmall); Text("${state.answered} کارت پاسخ داده شد"); Text("صحیح: ${state.correct} • غلط: ${state.wrong}"); Text("دقت این جلسه: ${if (state.answered == 0) 0 else state.correct * 100 / state.answered}٪"); Button(onClick = onFinished, modifier = Modifier.fillMaxWidth()) { Text("بازگشت به خانه") } } } }
private fun difficultyLabel(difficulty: VocabularyDifficulty) = when (difficulty) { VocabularyDifficulty.EASY -> "آسان"; VocabularyDifficulty.MEDIUM -> "متوسط"; VocabularyDifficulty.HARD -> "سخت"; VocabularyDifficulty.VERY_HARD -> "خیلی سخت" }
private fun reviewTypeLabel(type: ReviewType) = when (type) { ReviewType.DAILY -> "روزانه"; ReviewType.WEEKLY -> "هفتگی"; ReviewType.MONTHLY -> "ماهانه"; ReviewType.LEARNED -> "یادگرفته"; ReviewType.RANDOM -> "تصادفی" }
private fun quizDifficultyLabel(difficulty: QuizDifficulty) = when (difficulty) { QuizDifficulty.EASY -> "ابتدایی"; QuizDifficulty.MEDIUM -> "متوسط"; QuizDifficulty.HARD -> "حرفه‌ای" }
