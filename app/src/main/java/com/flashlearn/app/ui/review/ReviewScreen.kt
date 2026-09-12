package com.flashlearn.app.ui.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
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
fun ReviewScreen(
    viewModel: ReviewViewModel,
    onFinished: () -> Unit
) {
    val state by viewModel.state.collectAsState()


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        state.error?.let {
            Text("خطا: $it", color = MaterialTheme.colorScheme.error)
        }

        if (!state.isSelectingMode && !state.isFinished) {
            OutlinedButton(
                onClick = {
                    viewModel.exitReview(onFinished)
                },
                enabled = !state.isSubmitting
            ) {
                Text("خروج از مرور")
            }
        }

        when {
            state.isSelectingMode -> {
                Text("مرور: ${reviewTypeLabel(state.selectedReviewType)}", style = MaterialTheme.typography.headlineSmall)
                Text("فیلتر سختی", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(onClick = { viewModel.chooseDifficulty(null) }) { Text("همه") }
                    VocabularyDifficulty.entries.forEach { difficulty ->
                        OutlinedButton(onClick = { viewModel.chooseDifficulty(difficulty) }) {
                            Text(difficultyLabel(difficulty))
                        }
                    }
                }
                Text("سختی انتخاب‌شده: ${state.selectedDifficulty?.let(::difficultyLabel) ?: "همه"}")
                Button(onClick = { viewModel.startNewSession() }) { Text("شروع مرور") }
            }

            state.isLoading -> CircularProgressIndicator()

            state.isFinished -> {
                if (state.total == 0) {
                    Text("کارتی برای این مرور پیدا نشد", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        "فیلتر سختی یا نوع مرور را تغییر بده، یا ابتدا چند واژه به کتابخانه اضافه کن.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    Text("مرور تمام شد!", style = MaterialTheme.typography.headlineSmall)
                    Text("${state.answered} کارت پاسخ داده شد.")
                    Text("صحیح: ${state.correct}  •  غلط: ${state.wrong}")
                    val accuracy = if (state.answered == 0) 0 else (state.correct * 100 / state.answered)
                    Text("دقت این جلسه: $accuracy٪")
                }
                Button(onClick = onFinished) { Text("بازگشت به خانه") }
            }

            state.answerFeedback != null -> {
                val feedback = state.answerFeedback!!
                Text(
                    if (feedback.isCorrect) "✓ پاسخ صحیح" else "✕ پاسخ نادرست",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text("مرحله فعلی: ${feedback.stageLabel}")
                Text("سختی فعلی: ${feedback.difficultyLabel}")
                Text("پاسخ ثبت شد و پیشرفتت ذخیره شد.")
                Text("نتیجه تا اینجا: ${state.correct} صحیح، ${state.wrong} غلط")
                Button(onClick = viewModel::nextCard, modifier = Modifier.fillMaxWidth()) {
                    Text(if (state.remaining <= 1) "پایان مرور" else "کارت بعدی")
                }
            }

            state.card != null -> {
                val card = state.card!!

                Text("${state.remaining} کارت باقی‌مانده از ${state.total}")

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(card.sourceText, style = MaterialTheme.typography.headlineSmall)

                        if (card.noteVisible && !card.sourceNotes.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("یادداشت: ${card.sourceNotes}")
                        }

                        if (!card.isFlipped && card.hintRevealed) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("راهنما: حرف اول پاسخ «${card.targetText.take(1)}»")
                        }

                        if (card.isFlipped) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(card.targetText, style = MaterialTheme.typography.headlineSmall)
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!card.sourceNotes.isNullOrBlank()) {
                        OutlinedButton(onClick = { viewModel.toggleNote() }) {
                            Text(if (card.noteVisible) "مخفی کردن یادداشت" else "نمایش یادداشت")
                        }
                    }
                    if (!card.isFlipped) {
                        OutlinedButton(
                            onClick = { viewModel.revealHint() },
                            enabled = !card.hintRevealed
                        ) {
                            Text("راهنما")
                        }
                    }
                }

                if (!card.isFlipped) {
                    Button(onClick = { viewModel.flipCard() }) {
                        Text("نمایش پاسخ")
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { viewModel.submitAnswer(false) },
                            enabled = state.canSubmitAnswer
                        ) { Text("غلط") }
                        Button(
                            onClick = { viewModel.submitAnswer(true) },
                            enabled = state.canSubmitAnswer
                        ) { Text(if (state.isSubmitting) "در حال ثبت…" else "صحیح") }
                    }
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
