package com.flashlearn.app.ui.review

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
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
import android.speech.tts.TextToSpeech
import java.util.Locale
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.flashlearn.app.R

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.flashlearn.domain.model.QuizDifficulty
import com.flashlearn.domain.model.ReviewType
import com.flashlearn.domain.model.VocabularyDifficulty
import com.flashlearn.app.ui.library.CategorySelectionScreen
import com.flashlearn.app.ui.components.FlashLearnScreenHeader
import com.flashlearn.app.ui.components.FlashLearnScreenHeaderVariant
import com.flashlearn.app.ui.theme.LocalFlashLearnThemeTokens


private val QuizSelected: Color
    @Composable get() = LocalFlashLearnThemeTokens.current.primary

private val QuizSelectedContainer: Color
    @Composable get() = LocalFlashLearnThemeTokens.current.primary.copy(alpha = .10f)

private val QuizCorrect: Color
    @Composable get() = LocalFlashLearnThemeTokens.current.success

private val QuizCorrectContainer: Color
    @Composable get() = LocalFlashLearnThemeTokens.current.success.copy(alpha = .10f)

private val QuizWrong: Color
    @Composable get() = LocalFlashLearnThemeTokens.current.error

private val QuizWrongContainer: Color
    @Composable get() = LocalFlashLearnThemeTokens.current.error.copy(alpha = .10f)

@Composable
fun ReviewScreen(viewModel: ReviewViewModel, personalDifficulty: VocabularyDifficulty? = null, quizDifficulty: QuizDifficulty = QuizDifficulty.MEDIUM, onFinished: () -> Unit) {
    val state by viewModel.state.collectAsState()
    var showCategoryPicker by remember { mutableStateOf(false) }
    LaunchedEffect(quizDifficulty) { viewModel.setQuizDifficulty(quizDifficulty) }
    if (showCategoryPicker) {
        CategorySelectionScreen(categories = state.categories, selectedIds = state.selectedCategoryIds, counts = state.categoryWordCounts, allCount = state.allCategoryWordCount, onBack = { showCategoryPicker = false }, onApply = { ids -> viewModel.setCategories(ids); showCategoryPicker = false })
        return
    }
    val tokens = LocalFlashLearnThemeTokens.current
    val leadingTextAlign = TextAlign.Start
    Surface(modifier = Modifier.fillMaxSize(), color = tokens.reviewBackground) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = tokens.screenPadding, vertical = tokens.tinyGap),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(tokens.reviewSectionGap)
    ) {
            state.error?.takeIf { state.selectedMode != ReviewMode.QUIZ || state.quizCard == null }?.let { Text(stringResource(R.string.review_error, it), color = MaterialTheme.colorScheme.error, modifier = Modifier.fillMaxWidth(), textAlign = leadingTextAlign) }
            when {
                state.isSelectingMode -> ReviewSetup(state, viewModel, personalDifficulty, onFinished, onOpenCategories = { showCategoryPicker = true })
                state.isLoading -> CircularProgressIndicator()
                state.isFinished -> FinishCard(state, onFinished)
                state.selectedMode == ReviewMode.QUIZ && state.quizCard != null -> QuizCard(state, viewModel)
                state.answerFeedback != null -> FeedbackCard(state, viewModel)
                state.selectedMode == ReviewMode.QUIZ -> QuizUnavailableCard(viewModel)
                state.card != null -> FlashCard(state, viewModel)
            }
    }
    }
}

@Composable
private fun ReviewSetup(
    state: ReviewUiState,
    vm: ReviewViewModel,
    personalDifficulty: VocabularyDifficulty?,
    onBack: () -> Unit = {},
    onOpenCategories: () -> Unit
) {
    val tokens = LocalFlashLearnThemeTokens.current
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(tokens.reviewSectionGap)) {
        ReviewHeader(onBack)
        ReviewSectionTitle(1, stringResource(R.string.review_answer_mode))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.reviewItemGap)) {
            ReviewChoiceCard(stringResource(R.string.review_quiz), state.selectedMode == ReviewMode.QUIZ, Icons.Outlined.Quiz, { vm.chooseMode(ReviewMode.QUIZ) }, Modifier.weight(1f))
            ReviewChoiceCard(stringResource(R.string.review_flashcard), state.selectedMode == ReviewMode.FLASHCARD, Icons.Outlined.Style, { vm.chooseMode(ReviewMode.FLASHCARD) }, Modifier.weight(1f))
        }
        ReviewSectionTitle(2, stringResource(R.string.review_word_categories))
        CategoryFilterCard(state, onOpenCategories)
        ReviewSectionTitle(3, stringResource(R.string.review_special))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.reviewItemGap)) {
            ReviewChoiceCard(stringResource(R.string.review_random), state.selectedReviewType == ReviewType.RANDOM, Icons.Outlined.Style, { vm.chooseReviewType(ReviewType.RANDOM) }, Modifier.weight(1f))
            ReviewChoiceCard(stringResource(R.string.review_learned), state.selectedReviewType == ReviewType.LEARNED, Icons.Outlined.DoneAll, { vm.chooseReviewType(ReviewType.LEARNED) }, Modifier.weight(1f))
        }
        ReviewSectionTitle(4, stringResource(R.string.review_schedule))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.reviewItemGap)) {
            ReviewChoiceCard(stringResource(R.string.review_daily), state.selectedReviewType == ReviewType.DAILY, Icons.Outlined.FormatListNumbered, { vm.chooseReviewType(ReviewType.DAILY) }, Modifier.weight(1f))
            ReviewChoiceCard(stringResource(R.string.review_weekly), state.selectedReviewType == ReviewType.WEEKLY, Icons.Outlined.Category, { vm.chooseReviewType(ReviewType.WEEKLY) }, Modifier.weight(1f))
            ReviewChoiceCard(stringResource(R.string.review_monthly), state.selectedReviewType == ReviewType.MONTHLY, Icons.Outlined.AutoAwesome, { vm.chooseReviewType(ReviewType.MONTHLY) }, Modifier.weight(1f))
        }
        ReviewSectionTitle(5, stringResource(R.string.review_difficulty_words))
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(tokens.reviewItemGap)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.reviewItemGap)) {
                DifficultyTile(stringResource(R.string.review_easy), VocabularyDifficulty.EASY in state.selectedDifficulties, VocabularyDifficulty.EASY, difficultyIcon(VocabularyDifficulty.EASY), Modifier.weight(1f), vm)
                DifficultyTile(stringResource(R.string.review_medium), VocabularyDifficulty.MEDIUM in state.selectedDifficulties, VocabularyDifficulty.MEDIUM, difficultyIcon(VocabularyDifficulty.MEDIUM), Modifier.weight(1f), vm)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.reviewItemGap)) {
                DifficultyTile(stringResource(R.string.review_hard), VocabularyDifficulty.HARD in state.selectedDifficulties, VocabularyDifficulty.HARD, difficultyIcon(VocabularyDifficulty.HARD), Modifier.weight(1f), vm)
                DifficultyTile(stringResource(R.string.review_very_hard), VocabularyDifficulty.VERY_HARD in state.selectedDifficulties, VocabularyDifficulty.VERY_HARD, difficultyIcon(VocabularyDifficulty.VERY_HARD), Modifier.weight(1f), vm)
            }
        }
        if (state.selectedMode == ReviewMode.QUIZ) {
            ReviewSectionTitle(6, stringResource(R.string.review_quiz_difficulty))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.reviewItemGap)) {
                QuizLevelTile(stringResource(R.string.review_beginner), state.selectedQuizDifficulty == QuizDifficulty.EASY, Icons.Outlined.School, Modifier.weight(1f)) { vm.chooseQuizDifficulty(QuizDifficulty.EASY) }
                QuizLevelTile(stringResource(R.string.review_medium), state.selectedQuizDifficulty == QuizDifficulty.MEDIUM, Icons.Outlined.Tune, Modifier.weight(1f)) { vm.chooseQuizDifficulty(QuizDifficulty.MEDIUM) }
                QuizLevelTile(stringResource(R.string.review_professional), state.selectedQuizDifficulty == QuizDifficulty.HARD, Icons.Outlined.EmojiEvents, Modifier.weight(1f)) { vm.chooseQuizDifficulty(QuizDifficulty.HARD) }
            }
        }
        ReviewSectionTitle(if (state.selectedMode == ReviewMode.QUIZ) 7 else 6, stringResource(R.string.review_word_count))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.reviewItemGap)) {
            listOf(10, 20, 30, 50, 100).forEach { count ->
                CompactChoice(count.toString(), state.maximumReviewCards == count, Modifier.weight(1f)) { vm.setMaximumReviewCards(count) }
            }
        }
        personalDifficulty?.let {
            Text(stringResource(R.string.review_personal_level, difficultyLabel(it)), Modifier.fillMaxWidth(), style = MaterialTheme.typography.bodySmall, color = tokens.reviewMutedText, textAlign = TextAlign.Start)
        }
        val difficultyLabels = mapOf(
            VocabularyDifficulty.EASY to stringResource(R.string.review_easy),
            VocabularyDifficulty.MEDIUM to stringResource(R.string.review_medium),
            VocabularyDifficulty.HARD to stringResource(R.string.review_hard),
            VocabularyDifficulty.VERY_HARD to stringResource(R.string.review_very_hard)
        )
        val reviewTypeText = reviewTypeLabel(state.selectedReviewType)
        val filterSummary = listOf(
            if (state.selectedDifficulties.isEmpty()) stringResource(R.string.review_all_levels) else state.selectedDifficulties.sortedBy { it.ordinal }.joinToString("، ") { difficultyLabels[it].orEmpty() },
            if (state.selectedCategoryIds.isEmpty()) stringResource(R.string.category_all) else stringResource(R.string.review_categories_count, state.selectedCategoryIds.size),
            reviewTypeText,
            if (state.selectedMode == ReviewMode.QUIZ) stringResource(R.string.review_test) else stringResource(R.string.review_flashcard_mode)
        ).joinToString("  •  ")
        Surface(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, color = tokens.reviewSurface, border = BorderStroke(tokens.borderThin, tokens.reviewBorder)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = tokens.reviewCardPadding, vertical = tokens.reviewCompactPadding), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Tune, null, tint = tokens.reviewAccent, modifier = Modifier.size(tokens.reviewIconMedium))
                Column(Modifier.weight(1f).padding(horizontal = tokens.reviewItemGap), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.review_available, minOf(state.maximumReviewCards, state.availableReviewCount), state.availableReviewCount), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = tokens.reviewText, textAlign = TextAlign.Center)
                    Text(filterSummary, style = MaterialTheme.typography.bodySmall, color = tokens.reviewMutedText, textAlign = TextAlign.Center)
                }
            }
        }
        Button(
            enabled = state.availableReviewCount > 0,
            onClick = vm::startNewSession,
            modifier = Modifier.fillMaxWidth().height(tokens.reviewButtonHeight),
            shape = MaterialTheme.shapes.large,
            colors = ButtonDefaults.buttonColors(containerColor = tokens.reviewButton, contentColor = tokens.reviewButtonContent),
            contentPadding = PaddingValues(horizontal = tokens.reviewContentPadding)
        ) {
            Surface(Modifier.size(tokens.reviewPlayCircle), shape = CircleShape, color = tokens.reviewButtonContent.copy(alpha = .96f)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.PlayArrow, null, tint = tokens.reviewButton, modifier = Modifier.size(tokens.reviewPlayIcon))
                }
            }
            Spacer(Modifier.width(tokens.reviewItemGap))
            Text(stringResource(R.string.review_start), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ReviewHeader(onBack: () -> Unit) {
    FlashLearnScreenHeader(
        title = stringResource(R.string.review_title_full),
        onBack = onBack,
        variant = FlashLearnScreenHeaderVariant.REVIEW
    )
}

@Composable
private fun ReviewSectionTitle(number: Int, title: String) {
    val tokens = LocalFlashLearnThemeTokens.current
    Text("$number. $title", Modifier.fillMaxWidth(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = tokens.reviewText, textAlign = TextAlign.Start)
}

@Composable
private fun ReviewChoiceCard(label: String, selected: Boolean, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit, modifier: Modifier) {
    val tokens = LocalFlashLearnThemeTokens.current
    Card(onClick = onClick, modifier = modifier.height(tokens.reviewChoiceHeight), shape = MaterialTheme.shapes.large, colors = CardDefaults.cardColors(containerColor = if (selected) tokens.reviewSurfaceSelected else tokens.reviewSurface), border = BorderStroke(if (selected) tokens.borderStrong else tokens.borderThin, if (selected) tokens.reviewAccent else tokens.reviewBorder), elevation = CardDefaults.cardElevation(defaultElevation = tokens.reviewCardElevation)) {
        Box(Modifier.fillMaxSize().padding(horizontal = tokens.reviewCardPadding)) {
            Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold, color = tokens.reviewText)
                Icon(icon, null, tint = tokens.reviewAccent, modifier = Modifier.size(tokens.reviewIconLarge))
            }
            if (selected) Surface(Modifier.align(Alignment.TopStart).padding(tokens.reviewSelectedBadgeInset), shape = CircleShape, color = tokens.reviewAccent) {
                Icon(Icons.Outlined.DoneAll, null, tint = tokens.reviewButtonContent, modifier = Modifier.padding(tokens.reviewSelectedBadgePadding).size(tokens.reviewSelectedBadgeIcon))
            }
        }
    }
}

@Composable
private fun CategoryFilterCard(state: ReviewUiState, onClick: () -> Unit) {
    val tokens = LocalFlashLearnThemeTokens.current
    val selectedCount = state.selectedCategoryIds.size
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth().height(tokens.reviewCategoryHeight), shape = MaterialTheme.shapes.large, colors = CardDefaults.cardColors(containerColor = if (selectedCount > 0) tokens.reviewSurfaceSelected else tokens.reviewSurface), border = BorderStroke(if (selectedCount > 0) tokens.borderStrong else tokens.borderThin, if (selectedCount > 0) tokens.reviewAccent else tokens.reviewBorder), elevation = CardDefaults.cardElevation(defaultElevation = tokens.reviewCardElevation)) {
        Row(Modifier.fillMaxSize().padding(horizontal = tokens.reviewCardPadding), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(tokens.reviewItemGap)) {
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                Text(if (selectedCount == 0) stringResource(R.string.category_all) else stringResource(R.string.review_categories_selected, selectedCount), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = tokens.reviewText, textAlign = TextAlign.Start)
                Text(if (selectedCount == 0) stringResource(R.string.review_choose_categories) else state.selectedCategoryIds.joinToString("، ") { id -> state.categories.firstOrNull { it.id == id }?.name ?: "" }, style = MaterialTheme.typography.bodySmall, color = tokens.reviewMutedText, maxLines = 2, textAlign = TextAlign.Start)
            }
            Surface(Modifier.size(tokens.reviewCategoryIconTile), shape = MaterialTheme.shapes.medium, color = tokens.reviewSurfaceSelected) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Category, null, tint = tokens.reviewAccent, modifier = Modifier.size(tokens.reviewIconLarge)) }
            }
            Icon(Icons.AutoMirrored.Outlined.KeyboardArrowLeft, null, tint = tokens.reviewAccent, modifier = Modifier.size(tokens.reviewIconMedium))
        }
    }
}

@Composable
private fun DifficultyTile(label: String, selected: Boolean, difficulty: VocabularyDifficulty, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier, vm: ReviewViewModel) {
    val tokens = LocalFlashLearnThemeTokens.current
    Surface(modifier.height(tokens.reviewDifficultyHeight).clickable { vm.toggleDifficulty(difficulty) }, shape = MaterialTheme.shapes.large, color = if (selected) tokens.reviewSurfaceSelected else tokens.reviewSurface, border = BorderStroke(if (selected) tokens.borderStrong else tokens.borderThin, if (selected) tokens.reviewAccent else tokens.reviewBorder), shadowElevation = tokens.reviewCardElevation) {
        Box(Modifier.fillMaxSize().padding(horizontal = tokens.reviewCardPadding)) {
            if (selected) Surface(Modifier.align(Alignment.TopStart).padding(tokens.reviewSelectedBadgeInset), shape = CircleShape, color = tokens.reviewAccent) {
                Icon(Icons.Outlined.DoneAll, null, tint = tokens.reviewButtonContent, modifier = Modifier.padding(tokens.reviewSelectedBadgePadding).size(tokens.reviewSelectedBadgeIcon))
            }
            Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold, color = tokens.reviewText)
                Icon(icon, null, tint = tokens.reviewAccent, modifier = Modifier.size(tokens.reviewIconLarge))
            }
        }
    }
}

private fun difficultyIcon(difficulty: VocabularyDifficulty) = when (difficulty) {
    VocabularyDifficulty.EASY -> Icons.Outlined.Star
    VocabularyDifficulty.MEDIUM -> Icons.Outlined.AutoAwesome
    VocabularyDifficulty.HARD -> Icons.Outlined.LocalFireDepartment
    VocabularyDifficulty.VERY_HARD -> Icons.Outlined.RocketLaunch
}

@Composable
private fun QuizLevelTile(label: String, selected: Boolean, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier, onClick: () -> Unit) {
    val tokens = LocalFlashLearnThemeTokens.current
    Surface(modifier.height(tokens.reviewQuizHeight).clickable(onClick = onClick), shape = MaterialTheme.shapes.large, color = if (selected) tokens.reviewSurfaceSelected else tokens.reviewSurface, border = BorderStroke(if (selected) tokens.borderStrong else tokens.borderThin, if (selected) tokens.reviewAccent else tokens.reviewBorder), shadowElevation = tokens.reviewCardElevation) {
        Box(Modifier.fillMaxSize().padding(horizontal = tokens.reviewCardPadding)) {
            if (selected) Surface(Modifier.align(Alignment.TopStart).padding(tokens.reviewSelectedBadgeInset), shape = CircleShape, color = tokens.reviewAccent) {
                Icon(Icons.Outlined.DoneAll, null, tint = tokens.reviewButtonContent, modifier = Modifier.padding(tokens.reviewSelectedBadgePadding).size(tokens.reviewSelectedBadgeIcon))
            }
            Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold, color = tokens.reviewText)
                Icon(icon, null, tint = tokens.reviewAccent, modifier = Modifier.size(tokens.reviewIconLarge))
            }
        }
    }
}

@Composable
private fun ReviewFullChoice(label: String, selected: Boolean, onClick: () -> Unit) {
    val tokens = LocalFlashLearnThemeTokens.current
    Surface(modifier = Modifier.fillMaxWidth().height(tokens.reviewFullChoiceHeight).clickable(onClick = onClick), shape = MaterialTheme.shapes.large, color = if (selected) tokens.reviewSurfaceSelected else tokens.reviewSurface, border = BorderStroke(if (selected) tokens.borderStrong else tokens.borderThin, if (selected) tokens.reviewAccent else tokens.reviewBorder), shadowElevation = tokens.reviewCardElevation) {
        Box(contentAlignment = Alignment.Center) { Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold, color = tokens.reviewText) }
    }
}

@Composable
private fun CompactChoice(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val tokens = LocalFlashLearnThemeTokens.current
    Surface(modifier.height(tokens.reviewCountHeight).clickable(onClick = onClick), shape = MaterialTheme.shapes.extraLarge, color = if (selected) tokens.reviewAccent else tokens.reviewSurface, border = BorderStroke(tokens.borderThin, if (selected) tokens.reviewAccent else tokens.reviewBorder), shadowElevation = tokens.reviewCardElevation) {
        Box(contentAlignment = Alignment.Center) { Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (selected) tokens.reviewButtonContent else tokens.reviewText) }
    }
}

@Composable private fun FlashCard(state: ReviewUiState, vm: ReviewViewModel) {
    val tokens = LocalFlashLearnThemeTokens.current
    val card = state.card ?: return
    val leadingTextAlign = TextAlign.Start
    val progress = if (state.total <= 0) 0f else state.answered.toFloat() / state.total.toFloat(); Text(stringResource(R.string.review_remaining_cards, state.remaining, state.total), style = MaterialTheme.typography.labelMedium, textAlign = leadingTextAlign, modifier = Modifier.fillMaxWidth()); LinearProgressIndicator(progress = { progress.coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth()); Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) { Column(Modifier.fillMaxWidth().padding(tokens.cardPadding), horizontalAlignment = Alignment.CenterHorizontally) { Text(card.sourceText, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center); if (card.isFlipped) { Spacer(Modifier.height(tokens.sectionGap)); Text(card.targetText, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center) }; if (card.hintRevealed && !card.hintText.isNullOrBlank()) { Spacer(Modifier.height(tokens.compactGap)); Text(card.hintText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center) }; if (card.noteVisible && !card.sourceNotes.isNullOrBlank()) { Spacer(Modifier.height(tokens.compactGap)); Text(card.sourceNotes, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center) } } }; if (!card.isFlipped) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.compactGap)) { if (!card.sourceNotes.isNullOrBlank()) OutlinedButton(onClick = vm::toggleNote, modifier = Modifier.weight(1f)) { Text(if (card.noteVisible) stringResource(R.string.review_hide_note) else stringResource(R.string.review_show_note)) }; OutlinedButton(onClick = vm::revealHint, enabled = !card.hintRevealed, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.review_hint)) } }; Button(onClick = vm::flipCard, modifier = Modifier.fillMaxWidth().height(tokens.controlHeight)) { Text(stringResource(R.string.review_show_answer)) } } else { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.compactGap)) { OutlinedButton(onClick = { vm.submitAnswer(false) }, enabled = state.canSubmitAnswer, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.review_wrong)) }; Button(onClick = { vm.submitAnswer(true) }, enabled = state.canSubmitAnswer, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.review_correct)) } } } }

@Composable private fun QuizCard(state: ReviewUiState, vm: ReviewViewModel) {
    val tokens = LocalFlashLearnThemeTokens.current
    val quiz = state.quizCard ?: return
    val feedback = state.answerFeedback
    val answered = feedback != null
    val progress = if (state.total <= 0) 0f else state.answered.toFloat() / state.total.toFloat()
    val selected = quiz.selectedOption
    val correct = quiz.correctAnswerText
    val card = state.card
    val context = androidx.compose.ui.platform.LocalContext.current
    var ttsReady by remember { mutableStateOf(false) }
    val tts = remember(context, state.sourceLanguage) {
        lateinit var engine: TextToSpeech
        engine = TextToSpeech(context) { status ->
            val sourceLocale = Locale.forLanguageTag(state.sourceLanguage)
            ttsReady = status == TextToSpeech.SUCCESS &&
                engine.isLanguageAvailable(sourceLocale) >= TextToSpeech.LANG_AVAILABLE
            if (ttsReady) {
                engine.language = sourceLocale
                engine.setSpeechRate(0.92f)
            }
        }
        engine
    }
    DisposableEffect(tts) {
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = MaterialTheme.shapes.large, color = QuizWrongContainer.copy(alpha = .9f)) { Text("${state.wrong} ✕", color = QuizWrong, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = tokens.contentGap, vertical = tokens.tinyGap)) }
        Spacer(Modifier.width(tokens.compactGap))
        Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .65f)) { Text("${state.answered} / ${state.total}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = tokens.contentGap, vertical = tokens.tinyGap)) }
        Spacer(Modifier.width(tokens.compactGap))
        Surface(shape = MaterialTheme.shapes.large, color = QuizCorrectContainer.copy(alpha = .9f)) { Text("${state.correct} ✓", color = QuizCorrect, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = tokens.contentGap, vertical = tokens.tinyGap)) }
    }
    LinearProgressIndicator(progress = { progress.coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth())
    Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = tokens.screenPadding, vertical = tokens.dp(26f)), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(quiz.promptText, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
            OutlinedButton(
                onClick = {
                    val sourceLocale = Locale.forLanguageTag(state.sourceLanguage)
                    if (tts.isLanguageAvailable(sourceLocale) >= TextToSpeech.LANG_AVAILABLE) {
                        tts.language = sourceLocale
                        tts.speak(quiz.promptText, TextToSpeech.QUEUE_FLUSH, null, "flashlearn_quiz_prompt")
                    }
                },
                enabled = !answered && ttsReady
            ) { Text(stringResource(R.string.review_play_question)) }
            if (card?.hintRevealed == true && !card.hintText.isNullOrBlank()) { Spacer(Modifier.height(tokens.compactGap)); Text(card.hintText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center) }
            if (card?.noteVisible == true && !card.sourceNotes.isNullOrBlank()) { Spacer(Modifier.height(tokens.compactGap)); Text(card.sourceNotes, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center) }
        }
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.compactGap)) {
        if (!card?.sourceNotes.isNullOrBlank()) OutlinedButton(onClick = vm::toggleNote, enabled = !answered, modifier = Modifier.weight(1f)) { Text(if (card?.noteVisible == true) stringResource(R.string.review_hide_note) else stringResource(R.string.review_show_note)) }
        OutlinedButton(onClick = vm::revealHint, enabled = !answered && card != null && !card.hintRevealed, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.review_hint)) }
    }
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(tokens.contentGap)) {
        quiz.options.forEach { option ->
            val isCorrect = option == correct
            val isWrongSelection = answered && option == selected && !isCorrect
            val isSelected = !answered && option == selected
            val container = when { answered && isCorrect -> QuizCorrectContainer; isWrongSelection -> QuizWrongContainer; isSelected -> QuizSelectedContainer; else -> MaterialTheme.colorScheme.surface }
            val content = when { answered && isCorrect -> QuizCorrect; isWrongSelection -> QuizWrong; isSelected -> QuizSelected; else -> MaterialTheme.colorScheme.onSurface }
            val border = when { answered && isCorrect -> QuizCorrect; isWrongSelection -> QuizWrong; isSelected -> QuizSelected; else -> MaterialTheme.colorScheme.outline }
            val emphasized = (answered && (isCorrect || isWrongSelection)) || isSelected
            OutlinedButton(onClick = { vm.selectQuizOption(option) }, enabled = !answered && !state.isSubmitting, modifier = Modifier.fillMaxWidth().heightIn(min = tokens.dp(72f)), shape = MaterialTheme.shapes.large, border = BorderStroke(if (emphasized) tokens.dp(3f) else tokens.borderThin, border), colors = ButtonDefaults.outlinedButtonColors(containerColor = container, contentColor = content), contentPadding = PaddingValues(horizontal = tokens.contentGap + tokens.compactGap, vertical = tokens.compactGap)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    when { answered && isCorrect -> Text("✓", color = QuizCorrect, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold); isWrongSelection -> Text("✕", color = QuizWrong, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold); isSelected -> Text("●", color = QuizSelected, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold) }
                    if ((answered && isCorrect) || isWrongSelection || isSelected) Spacer(Modifier.width(tokens.compactGap))
                    Text(option, style = MaterialTheme.typography.titleMedium, fontWeight = if (emphasized) FontWeight.Bold else FontWeight.Medium, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                }
            }
        }
    }
    if (!answered) {
        Button(onClick = vm::submitQuizAnswer, enabled = selected != null && !state.isSubmitting, modifier = Modifier.fillMaxWidth().height(tokens.buttonHeight), shape = MaterialTheme.shapes.large) { Text(stringResource(R.string.review_register_answer), style = MaterialTheme.typography.titleMedium) }
    } else {
        Surface(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, color = if (feedback?.isCorrect == true) QuizCorrectContainer else QuizWrongContainer) {
            Column(Modifier.fillMaxWidth().padding(horizontal = tokens.contentGap, vertical = tokens.compactGap), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(tokens.compactGap)) { Text(if (feedback?.isCorrect == true) stringResource(R.string.review_feedback_correct) else stringResource(R.string.review_feedback_wrong), color = if (feedback?.isCorrect == true) QuizCorrect else QuizWrong, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center);  }
        }
    }
}

@Composable private fun QuizUnavailableCard(vm: ReviewViewModel) {
    val tokens = LocalFlashLearnThemeTokens.current
    Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) { Column(Modifier.padding(tokens.cardPadding), horizontalAlignment = Alignment.CenterHorizontally) { Text(stringResource(R.string.review_quiz_not_ready), style = MaterialTheme.typography.titleLarge); Text(stringResource(R.string.review_quiz_preserved), color = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.height(tokens.compactGap)); Button(onClick = vm::skipCurrentCard) { Text(stringResource(R.string.review_skip_card)) } } } }
@Composable private fun FeedbackCard(state: ReviewUiState, vm: ReviewViewModel) {
    val tokens = LocalFlashLearnThemeTokens.current
    val f = state.answerFeedback ?: return
    Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) { Column(Modifier.padding(tokens.cardPadding), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(tokens.tinyGap)) { Text(if (f.isCorrect) stringResource(R.string.review_feedback_correct) else stringResource(R.string.review_feedback_incorrect), style = MaterialTheme.typography.headlineSmall, color = if (f.isCorrect) QuizCorrect else QuizWrong); Text(stringResource(R.string.review_stage, f.stageLabel)); Text(stringResource(R.string.review_difficulty, f.difficultyLabel)); if (!f.isCorrect && !f.correctAnswerText.isNullOrBlank()) Text(stringResource(R.string.review_correct_answer, f.correctAnswerText)); Text(stringResource(R.string.review_result, state.correct, state.wrong)); Button(onClick = vm::nextCard, enabled = !state.isSubmitting, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.review_next_card)) } } } }
@Composable private fun FinishCard(state: ReviewUiState, onFinished: () -> Unit) {
    val tokens = LocalFlashLearnThemeTokens.current
    Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) { Column(Modifier.padding(tokens.cardPadding), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(tokens.compactGap)) { Text(stringResource(R.string.review_finished), style = MaterialTheme.typography.headlineSmall); Text(stringResource(R.string.review_answered, state.answered)); Text(stringResource(R.string.review_result, state.correct, state.wrong)); Text(stringResource(R.string.review_accuracy_session, if (state.answered == 0) 0 else state.correct * 100 / state.answered)); Button(onClick = onFinished, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.review_back_home)) } } } }
@Composable
private fun difficultyLabel(difficulty: VocabularyDifficulty) = when (difficulty) { VocabularyDifficulty.EASY -> stringResource(R.string.review_easy); VocabularyDifficulty.MEDIUM -> stringResource(R.string.review_medium); VocabularyDifficulty.HARD -> stringResource(R.string.review_hard); VocabularyDifficulty.VERY_HARD -> stringResource(R.string.review_very_hard) }
@Composable
private fun reviewTypeLabel(type: ReviewType) = when (type) { ReviewType.DAILY -> stringResource(R.string.review_daily); ReviewType.WEEKLY -> stringResource(R.string.review_weekly); ReviewType.MONTHLY -> stringResource(R.string.review_monthly); ReviewType.LEARNED -> stringResource(R.string.review_learned); ReviewType.RANDOM -> stringResource(R.string.review_random) }
