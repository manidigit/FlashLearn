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
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf(5, 10, 20, 30).forEach { count -> CompactChoice(count.toString(), state.cardLimit == count, Modifier.weight(1f)) { vm.setCardLimit(count) } }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = { vm.startReview() }, modifier = Modifier.weight(1f)) { Icon(Icons.Outlined.PlayArrow, null); Spacer(Modifier.width(8.dp)); Text("شروع مرور") }
        }
    }
}

@Composable private fun ReviewModeCard(label: String, selected: Boolean, onClick: () -> Unit, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier) { Surface(modifier.height(104.dp).clickable(onClick = onClick), shape = MaterialTheme.shapes.extraLarge, color = if (selected) QuizSelectedContainer else MaterialTheme.colorScheme.surface, border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) QuizSelected.copy(alpha = .7f) else MaterialTheme.colorScheme.outlineVariant)) { Column(Modifier.fillMaxSize().padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { Icon(icon, null, tint = if (selected) QuizSelected else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(30.dp)); Spacer(Modifier.height(6.dp)); Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) } } }

@Composable private fun DifficultyTile(label: String, selected: Boolean, difficulty: VocabularyDifficulty, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier, vm: ReviewViewModel) {
    val tint = when (difficulty) { VocabularyDifficulty.EASY -> Color(0xFF39B982); VocabularyDifficulty.MEDIUM -> Color(0xFFFFC84A); VocabularyDifficulty.HARD -> Color(0xFFFF7A3D); VocabularyDifficulty.VERY_HARD -> Color(0xFFE95C73) }
    Surface(modifier.height(128.dp).clickable { vm.toggleDifficulty(difficulty) }, shape = MaterialTheme.shapes.extraLarge, color = if (selected) tint.copy(alpha = .07f) else MaterialTheme.colorScheme.surface, border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) tint.copy(alpha = .55f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = .7f))) { Box(Modifier.fillMaxSize()) { if (selected) Icon(Icons.Outlined.DoneAll, null, tint = tint, modifier = Modifier.align(Alignment.TopEnd).padding(7.dp).size(18.dp)); Column(Modifier.fillMaxSize().padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { Icon(icon, null, tint = tint, modifier = Modifier.size(32.dp)); Spacer(Modifier.height(5.dp)); Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) } } }
}

private fun difficultyIcon(difficulty: VocabularyDifficulty) = when (difficulty) { VocabularyDifficulty.EASY -> Icons.Outlined.Star; VocabularyDifficulty.MEDIUM -> Icons.Outlined.AutoAwesome; VocabularyDifficulty.HARD -> Icons.Outlined.LocalFireDepartment; VocabularyDifficulty.VERY_HARD -> Icons.Outlined.RocketLaunch }

@Composable private fun QuizLevelTile(label: String, selected: Boolean, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier, onClick: () -> Unit) { Surface(modifier.height(96.dp).clickable(onClick = onClick), shape = MaterialTheme.shapes.large, color = if (selected) QuizSelectedContainer else MaterialTheme.colorScheme.surface, border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) QuizSelected.copy(alpha = .7f) else MaterialTheme.colorScheme.outlineVariant)) { Box(Modifier.fillMaxSize()) { if (selected) Icon(Icons.Outlined.DoneAll, null, tint = QuizSelected, modifier = Modifier.align(Alignment.TopEnd).padding(7.dp).size(18.dp)); Column(Modifier.fillMaxSize().padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { Icon(icon, null, tint = if (selected) QuizSelected else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(30.dp)); Spacer(Modifier.height(5.dp)); Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) } } } }

@Composable private fun CompactChoice(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) { Surface(modifier.height(50.dp).clickable(onClick = onClick), shape = MaterialTheme.shapes.large, color = if (selected) QuizSelectedContainer else MaterialTheme.colorScheme.surface, border = BorderStroke(if (selected) 1.5.dp else 1.dp, if (selected) QuizSelected.copy(alpha = .55f) else MaterialTheme.colorScheme.outlineVariant)) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) } } }

@Composable private fun FlashCard(state: ReviewUiState, vm: ReviewViewModel) { val card = state.card ?: return; val progress = if (state.total <= 0) 0f else state.answered.toFloat() / state.total.toFloat(); Text("${state.remaining} کارت باقی‌مانده از ${state.total}", style = MaterialTheme.typography.labelMedium); LinearProgressIndicator(progress = { progress.coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth()); Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) { Column(Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(card.sourceText, style = MaterialTheme.typography.headlineMedium); if (card.isFlipped) { Spacer(Modifier.height(14.dp)); Text(card.targetText, style = MaterialTheme.typography.headlineSmall) }; if (card.hintRevealed && !card.hintText.isNullOrBlank()) { Spacer(Modifier.height(8.dp)); Text(card.hintText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary) }; if (card.noteVisible && !card.sourceNotes.isNullOrBlank()) { Spacer(Modifier.height(8.dp)); Text(card.sourceNotes, style = MaterialTheme.typography.bodySmall) } } }; if (!card.isFlipped) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { if (!card.sourceNotes.isNullOrBlank()) OutlinedButton(onClick = vm::toggleNote, modifier = Modifier.weight(1f)) { Text(if (card.noteVisible) "پنهان کردن یادداشت" else "نمایش یادداشت") }; OutlinedButton(onClick = vm::revealHint, modifier = Modifier.weight(1f)) { Text("راهنما") } } }; Button(onClick = vm::flipCard, modifier = Modifier.fillMaxWidth()) { Text("${if (card.isFlipped) "ادامه" else "نمایش پاسخ"}") } }

@Composable private fun QuizCard(state: ReviewUiState, vm: ReviewViewModel) { val q = state.quizCard ?: return; val submitted = state.quizSubmitted; val selected = state.selectedQuizAnswer; val correct = q.correctAnswer; Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) { Text("${state.remaining} کارت باقی‌مانده از ${state.total}", style = MaterialTheme.typography.labelMedium); LinearProgressIndicator(progress = { (state.answered.toFloat() / state.total.coerceAtLeast(1)).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth()); Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.extraLarge) { Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.End) { Text(q.prompt, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End); Spacer(Modifier.height(14.dp)); q.options.forEachIndexed { index, option -> QuizAnswerButton(index, option, selected == index, submitted, correct == index, Modifier.fillMaxWidth()) { vm.selectQuizAnswer(index) } }; if (submitted) { Spacer(Modifier.height(8.dp)); Text(if (selected == correct) "پاسخ صحیح" else "پاسخ نادرست — پاسخ صحیح: ${q.options.getOrNull(correct) ?: "-"}", color = if (selected == correct) QuizCorrect else QuizWrong, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End) } } } }

@Composable private fun QuizAnswerButton(index: Int, label: String, selected: Boolean, submitted: Boolean, correct: Boolean, modifier: Modifier, onClick: () -> Unit) { val isWrongSelected = submitted && selected && !correct; val isCorrect = submitted && correct; val container = when { isCorrect -> QuizCorrectContainer; isWrongSelected -> QuizWrongContainer; selected -> QuizSelectedContainer; else -> MaterialTheme.colorScheme.surface }; val borderColor = when { isCorrect -> QuizCorrect; isWrongSelected -> QuizWrong; selected -> QuizSelected; else -> MaterialTheme.colorScheme.outlineVariant }; val textColor = when { isCorrect -> QuizCorrect; isWrongSelected -> QuizWrong; selected -> QuizSelected; else -> MaterialTheme.colorScheme.onSurface }; Surface(modifier.height(68.dp).clickable(enabled = !submitted, onClick = onClick), shape = MaterialTheme.shapes.large, color = container, border = BorderStroke(if (isCorrect || isWrongSelected || selected) 2.dp else 1.dp, borderColor)) { Row(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(34.dp), contentAlignment = Alignment.Center) { Text((index + 1).toString(), fontWeight = FontWeight.Bold, color = textColor) }; Spacer(Modifier.width(12.dp)); Text(label, modifier = Modifier.weight(1f), color = textColor, fontWeight = if (isCorrect || isWrongSelected || selected) FontWeight.Bold else FontWeight.Normal, textAlign = TextAlign.End); if (isCorrect) Icon(Icons.Outlined.DoneAll, null, tint = QuizCorrect, modifier = Modifier.size(22.dp)); else if (isWrongSelected) Text("✕", color = QuizWrong, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium); else if (selected) Text("✓", color = QuizSelected, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium) } }
}

@Composable private fun FeedbackCard(state: ReviewUiState) { val feedback = state.answerFeedback ?: return; Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) { Column(Modifier.fillMaxWidth().padding(18.dp), horizontalAlignment = Alignment.End) { Text(feedback, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.End) } } }

@Composable private fun QuizUnavailableCard() { Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) { Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Outlined.Quiz, null, modifier = Modifier.size(42.dp)); Spacer(Modifier.height(10.dp)); Text("برای این مرور سؤال تستی در دسترس نیست.", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center) } } }

@Composable private fun FinishCard(state: ReviewUiState, onFinished: () -> Unit) { Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.extraLarge) { Column(Modifier.fillMaxWidth().padding(26.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) { Icon(Icons.Outlined.EmojiEvents, null, modifier = Modifier.size(52.dp), tint = QuizSelected); Text("مرور تمام شد", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold); Text("${state.answered} کارت مرور شد.", style = MaterialTheme.typography.bodyLarge); Button(onClick = onFinished) { Text("بازگشت") } } } }

@Composable private fun CategoryFilterCard(state: ReviewUiState, onOpen: () -> Unit) { val count = state.selectedCategoryIds.size; Surface(Modifier.fillMaxWidth().clickable(onClick = onOpen), shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) { Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.Category, null); Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) { Text(if (count == 0) "همه دسته‌ها" else "$count دسته انتخاب شده", fontWeight = FontWeight.Bold); Text("برای انتخاب و اعمال دسته‌ها لمس کن", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } } } }
