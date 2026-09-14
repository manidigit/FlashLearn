package com.flashlearn.app.ui.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import com.flashlearn.domain.model.ReviewType
import com.flashlearn.domain.model.VocabularyDifficulty
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ReviewScreen(viewModel: ReviewViewModel, onFinished: () -> Unit) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        state.error?.let { Text("خطا: $it", color = MaterialTheme.colorScheme.error, modifier = Modifier.fillMaxWidth()) }

        if (!state.isSelectingMode && !state.isFinished) {
            OutlinedButton(onClick = { viewModel.exitReview(onFinished) }, enabled = !state.isSubmitting, modifier = Modifier.fillMaxWidth()) { Text("خروج از مرور") }
        }

        when {
            state.isSelectingMode -> {
                Text("مرور: ${reviewTypeLabel(state.selectedReviewType)}", style = MaterialTheme.typography.headlineSmall)
                Text("حالت مرور", style = MaterialTheme.typography.titleMedium)
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { viewModel.chooseMode(ReviewMode.FLASHCARD) }, modifier = Modifier.fillMaxWidth()) { Text("فلش‌کارت") }
                    OutlinedButton(onClick = { viewModel.chooseMode(ReviewMode.QUIZ) }, modifier = Modifier.fillMaxWidth()) { Text("آزمون چهارگزینه‌ای") }
                }
                Text("حالت انتخاب‌شده: ${if (state.selectedMode == ReviewMode.QUIZ) "آزمون" else "فلش‌کارت"}")

                Text("نوع مرور", style = MaterialTheme.typography.titleMedium)
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ReviewType.entries.forEach { type -> OutlinedButton(onClick = { viewModel.chooseReviewType(type) }) { Text(reviewTypeLabel(type)) } }
                }
                Text("نوع انتخاب‌شده: ${reviewTypeLabel(state.selectedReviewType)}")

                Text("فیلتر سختی", style = MaterialTheme.typography.titleMedium)
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { viewModel.chooseDifficulty(null) }) { Text("همه") }
                    VocabularyDifficulty.entries.forEach { difficulty -> OutlinedButton(onClick = { viewModel.chooseDifficulty(difficulty) }) { Text(difficultyLabel(difficulty)) } }
                }
                Text("سختی انتخاب‌شده: ${state.selectedDifficulty?.let(::difficultyLabel) ?: "همه"}")

                if (state.categories.isNotEmpty()) {
                    Text("دسته‌بندی", style = MaterialTheme.typography.titleMedium)
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { viewModel.chooseCategory(null) }) { Text("همه") }
                        state.categories.forEach { category -> OutlinedButton(onClick = { viewModel.chooseCategory(category.id) }) { Text(category.name) } }
                    }
                    Text("دسته انتخاب‌شده: ${state.categories.firstOrNull { it.id == state.selectedCategoryId }?.name ?: "همه"}")
                }
                Button(onClick = { viewModel.startNewSession() }, modifier = Modifier.fillMaxWidth()) { Text("شروع مرور") }
            }

            state.isLoading -> CircularProgressIndicator()

            state.isFinished -> {
                if (state.total == 0) {
                    Text("کارتی برای این مرور پیدا نشد", style = MaterialTheme.typography.headlineSmall)
                    Text("فیلتر سختی یا نوع مرور را تغییر بده، یا ابتدا چند واژه به کتابخانه اضافه کن.", style = MaterialTheme.typography.bodyLarge)
                } else {
                    Text("مرور تمام شد!", style = MaterialTheme.typography.headlineSmall)
                    Text("${state.answered} کارت پاسخ داده شد.")
                    Text("صحیح: ${state.correct}  •  غلط: ${state.wrong}")
                    val accuracy = if (state.answered == 0) 0 else (state.correct * 100 / state.answered)
                    Text("دقت این جلسه: $accuracy٪")
                }
                Button(onClick = onFinished, modifier = Modifier.fillMaxWidth()) { Text("بازگشت به خانه") }
            }

            state.answerFeedback != null -> {
                val feedback = state.answerFeedback!!
                Text(if (feedback.isCorrect) "✓ پاسخ صحیح" else "✕ پاسخ نادرست", style = MaterialTheme.typography.headlineSmall, color = if (feedback.isCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                Text("مرحله فعلی: ${feedback.stageLabel}")
                Text("سختی فعلی: ${feedback.difficultyLabel}")
                if (!feedback.isCorrect && !feedback.correctAnswerText.isNullOrBlank()) Text("پاسخ صحیح: ${feedback.correctAnswerText}")
                Text("پاسخ ثبت شد و پیشرفتت ذخیره شد.")
                Text("نتیجه تا اینجا: ${state.correct} صحیح، ${state.wrong} غلط")
                if (!feedback.isCorrect) Text("سؤال بعدی تا چند لحظه دیگر…")
            }

            state.quizCard != null && state.answerFeedback == null -> {
                val quiz = state.quizCard!!
                val card = state.card
                val progress = if (state.total <= 0) 0f else state.answered.toFloat() / state.total.toFloat()
                Text("${state.remaining} سؤال باقی‌مانده از ${state.total}")
                LinearProgressIndicator(progress = { progress.coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth())
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(quiz.promptText, style = MaterialTheme.typography.headlineSmall)
                        if (card?.hintRevealed == true) { Spacer(Modifier.height(8.dp)); Text("راهنما: حرف اول پاسخ «${card.targetText.take(1)}»") }
                        if (card?.noteVisible == true && !card.sourceNotes.isNullOrBlank()) { Spacer(Modifier.height(8.dp)); Text("یادداشت: ${card.sourceNotes}") }
                        Spacer(Modifier.height(12.dp))
                        quiz.options.forEachIndexed { index, option ->
                            val selected = quiz.selectedOption == option
                            if (selected) Button(onClick = { viewModel.selectQuizOption(option) }, enabled = !state.isSubmitting, modifier = Modifier.fillMaxWidth()) { Text("${index + 1}. $option") }
                            else OutlinedButton(onClick = { viewModel.selectQuizOption(option) }, enabled = !state.isSubmitting, modifier = Modifier.fillMaxWidth()) { Text("${index + 1}. $option") }
                        }
                        Spacer(Modifier.height(8.dp))
                        if (card != null) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (!card.sourceNotes.isNullOrBlank()) OutlinedButton(onClick = { viewModel.toggleNote() }, enabled = !state.isSubmitting, modifier = Modifier.weight(1f)) { Text(if (card.noteVisible) "مخفی کردن یادداشت" else "نمایش یادداشت") }
                                OutlinedButton(onClick = { viewModel.revealHint() }, enabled = !card.hintRevealed && !state.isSubmitting, modifier = Modifier.weight(1f)) { Text("راهنما") }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = viewModel::submitQuizAnswer, enabled = quiz.selectedOption != null && !state.isSubmitting, modifier = Modifier.fillMaxWidth()) { Text(if (state.isSubmitting) "در حال ثبت…" else "ثبت پاسخ") }
                    }
                }
            }

            state.card != null -> {
                val card = state.card!!
                val progress = if (state.total <= 0) 0f else state.answered.toFloat() / state.total.toFloat()
                val liveAccuracy = if (state.answered == 0) 0 else state.correct * 100 / state.answered
                Text("${state.remaining} کارت باقی‌مانده از ${state.total}")
                LinearProgressIndicator(progress = { progress.coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth())
                if (state.answered > 0) Text("دقت جلسه: $liveAccuracy٪  •  ${state.correct} صحیح / ${state.wrong} غلط", style = MaterialTheme.typography.labelMedium)
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth().padding(16.dp)) {
                        Text(card.sourceText, style = MaterialTheme.typography.headlineSmall)
                        if (card.noteVisible && !card.sourceNotes.isNullOrBlank()) { Spacer(Modifier.height(8.dp)); Text("یادداشت: ${card.sourceNotes}") }
                        if (!card.isFlipped && card.hintRevealed) { Spacer(Modifier.height(8.dp)); Text("راهنما: حرف اول پاسخ «${card.targetText.take(1)}»") }
                        if (card.isFlipped) { Spacer(Modifier.height(16.dp)); Text(card.targetText, style = MaterialTheme.typography.headlineSmall) }
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!card.sourceNotes.isNullOrBlank()) OutlinedButton(onClick = { viewModel.toggleNote() }, modifier = Modifier.weight(1f)) { Text(if (card.noteVisible) "مخفی کردن یادداشت" else "یادداشت") }
                    if (!card.isFlipped) OutlinedButton(onClick = { viewModel.revealHint() }, enabled = !card.hintRevealed, modifier = Modifier.weight(1f)) { Text("راهنما") }
                }
                if (!card.isFlipped) Button(onClick = { viewModel.flipCard() }, modifier = Modifier.fillMaxWidth()) { Text("نمایش پاسخ") }
                else Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { viewModel.submitAnswer(false) }, enabled = state.canSubmitAnswer, modifier = Modifier.weight(1f)) { Text("غلط") }
                    Button(onClick = { viewModel.submitAnswer(true) }, enabled = state.canSubmitAnswer, modifier = Modifier.weight(1f)) { Text(if (state.isSubmitting) "در حال ثبت…" else "صحیح") }
                }
            }
        }
    }
}

private fun reviewTypeLabel(type: ReviewType): String = when (type) {
    ReviewType.DAILY -> "روزانه"
    ReviewType.WEEKLY -> "هفتگی"
    ReviewType.MONTHLY -> "ماهانه"
    ReviewType.LEARNED -> "یادگرفته‌شده"
    ReviewType.RANDOM -> "تصادفی"
}

private fun difficultyLabel(difficulty: VocabularyDifficulty): String = when (difficulty) {
    VocabularyDifficulty.EASY -> "آسان"
    VocabularyDifficulty.MEDIUM -> "متوسط"
    VocabularyDifficulty.HARD -> "سخت"
    VocabularyDifficulty.VERY_HARD -> "خیلی سخت"
}
