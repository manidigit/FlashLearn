package com.flashlearn.app.ui.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flashlearn.domain.model.Content
import com.flashlearn.domain.model.ReviewType
import com.flashlearn.domain.model.VocabularyDifficulty
import com.flashlearn.domain.repository.ContentRepository
import com.flashlearn.domain.usecase.EndReviewSessionUseCase
import com.flashlearn.domain.usecase.ReviewSelectionFilters
import com.flashlearn.domain.usecase.SelectReviewQueueUseCase
import com.flashlearn.domain.usecase.StartReviewSessionUseCase
import com.flashlearn.domain.usecase.SubmitReviewAnswerRequest
import com.flashlearn.domain.usecase.SubmitReviewAnswerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Mirrors the default language pair used by CreateConceptUseCase (domain module).
private const val SOURCE_LANGUAGE = "es"
private const val TARGET_LANGUAGE = "fa"

data class ReviewCardUiState(
    val sourceText: String,
    val sourceNotes: String?,
    val targetText: String,
    val isFlipped: Boolean = false,
    val hintRevealed: Boolean = false,
    val noteVisible: Boolean = false
)

data class ReviewAnswerFeedbackUiState(
    val isCorrect: Boolean,
    val stageLabel: String,
    val difficultyLabel: String,
    val answered: Int,
    val correct: Int,
    val wrong: Int
)

data class ReviewUiState(
    val isLoading: Boolean = false,
    val isSelectingMode: Boolean = true,
    val selectedReviewType: ReviewType = ReviewType.DAILY,
    val selectedDifficulty: com.flashlearn.domain.model.VocabularyDifficulty? = null,
    val card: ReviewCardUiState? = null,
    val remaining: Int = 0,
    val total: Int = 0,
    val isFinished: Boolean = false,
    val isSubmitting: Boolean = false,
    val answered: Int = 0,
    val correct: Int = 0,
    val wrong: Int = 0,
    val answerFeedback: ReviewAnswerFeedbackUiState? = null,
    val error: String? = null
) {
    /** True only while the current revealed card is ready for one answer submission. */
    val canSubmitAnswer: Boolean
        get() = card?.isFlipped == true && !isSubmitting && answerFeedback == null && !isFinished
}

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val selectReviewQueue: SelectReviewQueueUseCase,
    private val startReviewSession: StartReviewSessionUseCase,
    private val endReviewSession: EndReviewSessionUseCase,
    private val submitReviewAnswer: SubmitReviewAnswerUseCase,
    private val contentRepository: ContentRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ReviewUiState())
    val state: StateFlow<ReviewUiState> = _state.asStateFlow()

    private var sessionId: UUID? = null
    private var queue: List<UUID> = emptyList()
    private var index = 0
    private var sessionGeneration = 0L
    private var isAdvancing = false

    fun prepareReviewType(reviewType: ReviewType) {
        _state.value = _state.value.copy(selectedReviewType = reviewType, isSelectingMode = true, isFinished = false, error = null)
    }

    fun startNewSession(
        reviewType: ReviewType = _state.value.selectedReviewType,
        difficulty: com.flashlearn.domain.model.VocabularyDifficulty? = _state.value.selectedDifficulty
    ) {
        val generation = ++sessionGeneration
        viewModelScope.launch {
            if (generation != sessionGeneration) return@launch
            val now = Instant.now()
            _state.value = ReviewUiState(
                isLoading = true,
                isSelectingMode = false,
                selectedReviewType = reviewType,
                selectedDifficulty = difficulty
            )

            try {
                val candidates = selectReviewQueue(
                    ReviewSelectionFilters(
                        reviewType = reviewType,
                        difficulty = difficulty,
                        now = now
                    )
                )
                if (generation != sessionGeneration) return@launch
                queue = candidates.map { it.concept.id }
                index = 0

                if (queue.isEmpty()) {
                    sessionId = null
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isFinished = true,
                        total = 0,
                        remaining = 0
                    )
                } else {
                    if (generation != sessionGeneration) return@launch
                    val startedSession = startReviewSession(reviewType, now)
                    if (generation != sessionGeneration) {
                        runCatching { endReviewSession(startedSession) }
                        return@launch
                    }
                    sessionId = startedSession
                    loadCurrentCard(generation)
                }
            } catch (e: Exception) {
                if (generation != sessionGeneration) return@launch
                sessionId = null
                queue = emptyList()
                index = 0
                _state.value = _state.value.copy(
                    isLoading = false,
                    isSelectingMode = true,
                    card = null,
                    answerFeedback = null,
                    remaining = 0,
                    total = 0,
                    error = e.message ?: "خطا در آماده‌سازی مرور"
                )
            }
        }
    }

    private suspend fun loadCurrentCard(generation: Long) {
        if (generation != sessionGeneration) return
        val conceptId = queue.getOrNull(index) ?: return
        val source: Content? = contentRepository.find(conceptId, SOURCE_LANGUAGE)
        val target: Content? = contentRepository.find(conceptId, TARGET_LANGUAGE)

        if (source == null || target == null) {
            // Data integrity issue for this concept; skip it rather than crash the review flow.
            if (generation == sessionGeneration) advanceToNext(generation)
            return
        }
        if (generation != sessionGeneration || sessionId == null) return

        _state.value = _state.value.copy(
            isLoading = false,
            isFinished = false,
            card = ReviewCardUiState(
                sourceText = source.text,
                sourceNotes = source.notes,
                targetText = target.text
            ),
            remaining = queue.size - index,
            total = queue.size
        )
    }

    fun chooseMode(reviewType: ReviewType) {
        _state.value = _state.value.copy(selectedReviewType = reviewType)
    }

    fun chooseDifficulty(difficulty: com.flashlearn.domain.model.VocabularyDifficulty?) {
        _state.value = _state.value.copy(selectedDifficulty = difficulty)
    }

    fun flipCard() {
        val current = _state.value.card ?: return
        _state.value = _state.value.copy(card = current.copy(isFlipped = true))
    }

    fun revealHint() {
        val current = _state.value.card ?: return
        _state.value = _state.value.copy(card = current.copy(hintRevealed = true))
    }

    fun toggleNote() {
        val current = _state.value.card ?: return
        _state.value = _state.value.copy(card = current.copy(noteVisible = !current.noteVisible))
    }

    fun submitAnswer(isCorrect: Boolean) {
        val currentState = _state.value
        if (!currentState.canSubmitAnswer) return
        val session = sessionId ?: return
        val conceptId = queue.getOrNull(index) ?: return
        val reviewType = currentState.selectedReviewType
        val generation = sessionGeneration

        viewModelScope.launch {
            if (generation != sessionGeneration || sessionId != session) return@launch
            _state.value = _state.value.copy(isSubmitting = true, error = null)
            runCatching {
                submitReviewAnswer(
                    SubmitReviewAnswerRequest(
                        conceptId = conceptId,
                        sessionId = session,
                        reviewAttemptId = UUID.randomUUID(),
                        reviewType = reviewType,
                        isCorrect = isCorrect,
                        reviewedAt = Instant.now()
                    )
                )
            }.onSuccess { result ->
                if (generation != sessionGeneration || sessionId != session) return@onSuccess
                _state.value = _state.value.copy(
                    isSubmitting = false,
                    answerFeedback = ReviewAnswerFeedbackUiState(
                        isCorrect = isCorrect,
                        stageLabel = stageLabel(result.learningState.stage),
                        difficultyLabel = difficultyLabel(result.difficultyState.current),
                        answered = _state.value.answered + 1,
                        correct = _state.value.correct + if (isCorrect) 1 else 0,
                        wrong = _state.value.wrong + if (isCorrect) 0 else 1
                    ),
                    answered = _state.value.answered + 1,
                    correct = _state.value.correct + if (isCorrect) 1 else 0,
                    wrong = _state.value.wrong + if (isCorrect) 0 else 1
                )
            }.onFailure {
                if (generation != sessionGeneration || sessionId != session) return@onFailure
                _state.value = _state.value.copy(
                    isSubmitting = false,
                    error = it.message ?: "خطا در ثبت پاسخ"
                )
            }
        }
    }

    fun nextCard() {
        if (_state.value.isSubmitting || _state.value.answerFeedback == null || isAdvancing) return
        val generation = sessionGeneration
        isAdvancing = true
        viewModelScope.launch {
            try {
                if (generation == sessionGeneration) advanceToNext(generation)
            } finally {
                isAdvancing = false
            }
        }
    }

    /** Ends an active review session when the user intentionally leaves Review. */
    fun exitReview(onCompleted: () -> Unit = {}) {
        val exitGeneration = ++sessionGeneration
        val activeSessionId = sessionId
        if (activeSessionId == null) {
            queue = emptyList()
            index = 0
            onCompleted()
            return
        }

        viewModelScope.launch {
            try {
                runCatching { endReviewSession(activeSessionId) }
                    .onFailure {
                        if (exitGeneration == sessionGeneration) {
                            _state.value = _state.value.copy(
                                error = it.message ?: "خطا در پایان مرور"
                            )
                        }
                    }
            } finally {
                // A new session may have started while the old session was being ended.
                // In that case the old cleanup must not clear or overwrite the new session.
                if (exitGeneration != sessionGeneration) return@launch
                sessionId = null
                queue = emptyList()
                index = 0
                _state.value = _state.value.copy(
                    card = null,
                    isSubmitting = false,
                    isFinished = true,
                    remaining = 0
                )
                onCompleted()
            }
        }
    }

    private suspend fun advanceToNext(generation: Long) {
        if (generation != sessionGeneration) return
        index += 1
        if (index >= queue.size) {
            val activeSessionId = sessionId
            try {
                activeSessionId?.let { endReviewSession(it) }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message ?: "خطا در پایان مرور"
                )
            } finally {
                if (generation != sessionGeneration) return
                sessionId = null
                queue = emptyList()
                index = 0
            }
            if (generation != sessionGeneration) return
            _state.value = _state.value.copy(
                isLoading = false,
                card = null,
                isFinished = true,
                isSubmitting = false,
                answerFeedback = null,
                remaining = 0
            )
        } else {
            // Clear the previous answer feedback before presenting the next card.
            // ReviewScreen intentionally renders feedback before the card, so keeping
            // this value would make the UI stay on the previous result forever.
            if (generation != sessionGeneration) return
            _state.value = _state.value.copy(
                isSubmitting = false,
                answerFeedback = null,
                error = null
            )
            loadCurrentCard(generation)
        }
    }
}


private fun difficultyLabel(value: VocabularyDifficulty): String = when (value) {
    VocabularyDifficulty.EASY -> "آسان"
    VocabularyDifficulty.MEDIUM -> "متوسط"
    VocabularyDifficulty.HARD -> "سخت"
    VocabularyDifficulty.VERY_HARD -> "خیلی سخت"
}

private fun stageLabel(stage: com.flashlearn.domain.model.Stage): String = when (stage) {
    com.flashlearn.domain.model.Stage.DAILY -> "روزانه"
    com.flashlearn.domain.model.Stage.WEEKLY -> "هفتگی"
    com.flashlearn.domain.model.Stage.MONTHLY -> "ماهانه"
    com.flashlearn.domain.model.Stage.LEARNED -> "یادگرفته‌شده"
}
