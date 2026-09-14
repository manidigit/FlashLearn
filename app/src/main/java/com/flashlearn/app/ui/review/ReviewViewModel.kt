package com.flashlearn.app.ui.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flashlearn.domain.model.Content
import com.flashlearn.domain.model.ReviewType
import com.flashlearn.domain.model.VocabularyDifficulty
import com.flashlearn.domain.model.Category
import com.flashlearn.domain.repository.CategoryRepository
import com.flashlearn.domain.repository.ContentRepository
import com.flashlearn.domain.repository.ConceptRepository
import com.flashlearn.domain.repository.DifficultyStateRepository
import com.flashlearn.domain.usecase.EndReviewSessionUseCase
import com.flashlearn.domain.usecase.ReviewSelectionFilters
import com.flashlearn.domain.usecase.GenerateQuizQuestionUseCase
import com.flashlearn.domain.usecase.QuizLanguagePair
import com.flashlearn.domain.usecase.QuizQuestionResult
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

enum class ReviewMode { FLASHCARD, QUIZ }

data class QuizCardUiState(
    val promptText: String,
    val options: List<String>,
    val selectedOption: String? = null,
    val correctAnswerText: String
)

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
    val correctAnswerText: String? = null,
    val answered: Int,
    val correct: Int,
    val wrong: Int
)

data class ReviewUiState(
    val isLoading: Boolean = false,
    val isSelectingMode: Boolean = true,
    val selectedMode: ReviewMode = ReviewMode.FLASHCARD,
    val selectedReviewType: ReviewType = ReviewType.DAILY,
    val selectedDifficulty: com.flashlearn.domain.model.VocabularyDifficulty? = null,
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: UUID? = null,
    val card: ReviewCardUiState? = null,
    val quizCard: QuizCardUiState? = null,
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
        get() = !isSubmitting && answerFeedback == null && !isFinished &&
            (card?.isFlipped == true || quizCard?.selectedOption != null)
}

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val selectReviewQueue: SelectReviewQueueUseCase,
    private val startReviewSession: StartReviewSessionUseCase,
    private val endReviewSession: EndReviewSessionUseCase,
    private val submitReviewAnswer: SubmitReviewAnswerUseCase,
    private val contentRepository: ContentRepository,
    private val conceptRepository: ConceptRepository,
    private val difficultyStateRepository: DifficultyStateRepository,
    private val categoryRepository: CategoryRepository,
    private val generateQuizQuestion: GenerateQuizQuestionUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ReviewUiState())
    val state: StateFlow<ReviewUiState> = _state.asStateFlow()

    private var sessionId: UUID? = null
    private var queue: List<UUID> = emptyList()
    private var index = 0
    private var sessionGeneration = 0L
    private var isAdvancing = false

    fun chooseMode(mode: ReviewMode) {
        _state.value = _state.value.copy(selectedMode = mode)
    }

    init {
        viewModelScope.launch {
            runCatching { categoryRepository.getAll().sortedBy { it.name } }
                .onSuccess { categories -> _state.value = _state.value.copy(categories = categories) }
                .onFailure { /* Category filtering is optional; review remains usable if it fails. */ }
        }
    }

    fun prepareReviewType(reviewType: ReviewType) {
        _state.value = _state.value.copy(selectedReviewType = reviewType, isSelectingMode = true, isFinished = false, error = null)
    }

    fun startNewSession(
        reviewType: ReviewType = _state.value.selectedReviewType,
        difficulty: com.flashlearn.domain.model.VocabularyDifficulty? = _state.value.selectedDifficulty,
        categoryId: UUID? = _state.value.selectedCategoryId
    ) {
        val generation = ++sessionGeneration
        viewModelScope.launch {
            if (generation != sessionGeneration) return@launch
            val now = Instant.now()
            _state.value = ReviewUiState(
                isLoading = true,
                isSelectingMode = false,
                selectedReviewType = reviewType,
                selectedMode = _state.value.selectedMode,
                selectedDifficulty = difficulty,
                categories = _state.value.categories,
                selectedCategoryId = categoryId
            )

            try {
                // Close any previous session before replacing its queue. This prevents
                // abandoned sessions when the user starts a new review without first
                // leaving the previous one.
                val previousSessionId = sessionId
                if (previousSessionId != null) {
                    // Do not silently continue if the previous session cannot be closed:
                    // doing so would leave session history inconsistent.
                    endReviewSession(previousSessionId)
                    if (generation != sessionGeneration) return@launch
                    sessionId = null
                }

                val candidates = selectReviewQueue(
                    ReviewSelectionFilters(
                        reviewType = reviewType,
                        difficulty = difficulty,
                        categoryId = categoryId,
                        now = now
                    )
                )
                if (generation != sessionGeneration) return@launch

                // A review card requires both language contents. Filter broken/incomplete
                // concepts before the session starts so progress counts never include
                // cards that will later be skipped by loadCurrentCard().
                val validQueue = mutableListOf<UUID>()
                for (candidate in candidates) {
                    if (generation != sessionGeneration) return@launch
                    val hasSource = contentRepository.find(candidate.concept.id, SOURCE_LANGUAGE) != null
                    val hasTarget = contentRepository.find(candidate.concept.id, TARGET_LANGUAGE) != null
                    if (hasSource && hasTarget) validQueue += candidate.concept.id
                }
                queue = validQueue
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

        val quizCard = if (_state.value.selectedMode == ReviewMode.QUIZ) {
            val concept = candidatesConcept(conceptId)
            val difficultyState = concept?.let { difficultyStateRepositoryForQuiz(it.id) }
            if (concept != null && difficultyState != null) {
                when (val result = generateQuizQuestion(concept, QuizLanguagePair(SOURCE_LANGUAGE, TARGET_LANGUAGE), difficultyState)) {
                    is QuizQuestionResult.QuizQuestion -> ReviewCardUiState(source.text, source.notes, target.text) to QuizCardUiState(result.promptText, result.options, correctAnswerText = result.correctAnswerText)
                    QuizQuestionResult.FlashcardFallback -> ReviewCardUiState(source.text, source.notes, target.text) to null
                }
            } else ReviewCardUiState(source.text, source.notes, target.text) to null
        } else ReviewCardUiState(source.text, source.notes, target.text) to null
        _state.value = _state.value.copy(
            isLoading = false, isFinished = false,
            card = quizCard.first, quizCard = quizCard.second,
            remaining = queue.size - index, total = queue.size
        )
    }

    fun chooseReviewType(reviewType: ReviewType) {
        _state.value = _state.value.copy(selectedReviewType = reviewType)
    }

    fun chooseDifficulty(difficulty: com.flashlearn.domain.model.VocabularyDifficulty?) {
        _state.value = _state.value.copy(selectedDifficulty = difficulty)
    }

    fun chooseCategory(categoryId: UUID?) {
        _state.value = _state.value.copy(selectedCategoryId = categoryId)
    }

    private suspend fun candidatesConcept(conceptId: UUID) = conceptRepository.get(conceptId)
    private suspend fun difficultyStateRepositoryForQuiz(conceptId: UUID) = difficultyStateRepository.get(conceptId)

    fun selectQuizOption(option: String) {
        val quiz = _state.value.quizCard ?: return
        if (_state.value.isSubmitting || _state.value.answerFeedback != null) return
        if (option !in quiz.options) return
        _state.value = _state.value.copy(quizCard = quiz.copy(selectedOption = option))
    }

    fun submitQuizAnswer() {
        val quiz = _state.value.quizCard ?: return
        if (_state.value.isSubmitting || _state.value.answerFeedback != null) return
        val selected = quiz.selectedOption ?: return
        if (selected !in quiz.options) return
        submitAnswer(selected == quiz.correctAnswerText)
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
                        correctAnswerText = if (!isCorrect && currentState.selectedMode == ReviewMode.QUIZ) {
                            currentState.quizCard?.correctAnswerText
                        } else null,
                        answered = _state.value.answered + 1,
                        correct = _state.value.correct + if (isCorrect) 1 else 0,
                        wrong = _state.value.wrong + if (isCorrect) 0 else 1
                    ),
                    answered = _state.value.answered + 1,
                    correct = _state.value.correct + if (isCorrect) 1 else 0,
                    wrong = _state.value.wrong + if (isCorrect) 0 else 1
                )
                // Quiz UX contract: correct answers advance immediately; wrong answers
                // remain visible for two seconds before the next question.
                if (isCorrect) {
                    if (generation == sessionGeneration && sessionId == session) {
                        advanceToNext(generation)
                    }
                } else {
                    kotlinx.coroutines.delay(2_000)
                    if (generation == sessionGeneration && sessionId == session && _state.value.answerFeedback != null) {
                        advanceToNext(generation)
                    }
                }
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
            var ended = false
            try {
                endReviewSession(activeSessionId)
                ended = true
            } catch (e: Exception) {
                if (exitGeneration == sessionGeneration) {
                    _state.value = _state.value.copy(
                        error = e.message ?: "خطا در پایان مرور"
                    )
                }
            }

            // A new session may have started while the old session was being ended.
            // In that case the old cleanup must not clear or overwrite the new session.
            if (exitGeneration != sessionGeneration) return@launch
            if (!ended) return@launch

            sessionId = null
            queue = emptyList()
            index = 0
            _state.value = _state.value.copy(
                card = null,
                quizCard = null,
                isSubmitting = false,
                isFinished = true,
                remaining = 0
            )
            onCompleted()
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
                // Do not discard the active session when persistence of endedAt fails.
                // The session can be retried instead of being left orphaned while the
                // UI incorrectly reports a completed review.
                if (generation == sessionGeneration) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isSubmitting = false,
                        error = e.message ?: "خطا در پایان مرور"
                    )
                }
                return
            }
            if (generation != sessionGeneration) return
            sessionId = null
            queue = emptyList()
            index = 0
            _state.value = _state.value.copy(
                isLoading = false,
                card = null,
                isFinished = true,
                isSubmitting = false,
                answerFeedback = null,
                quizCard = null,
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
