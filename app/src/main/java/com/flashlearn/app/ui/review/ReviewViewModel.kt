package com.flashlearn.app.ui.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flashlearn.app.ui.LanguagePair
import com.flashlearn.domain.model.Content
import com.flashlearn.domain.model.ReviewType
import com.flashlearn.domain.model.VocabularyDifficulty
import com.flashlearn.domain.model.QuizDifficulty
import com.flashlearn.domain.model.Category
import com.flashlearn.domain.model.DifficultyState
import com.flashlearn.domain.repository.CategoryRepository
import com.flashlearn.domain.repository.ContentRepository
import com.flashlearn.domain.repository.ConceptRepository
import com.flashlearn.domain.repository.DifficultyStateRepository
import com.flashlearn.domain.usecase.EndReviewSessionUseCase
import com.flashlearn.domain.usecase.ReviewHelpUseCase
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ReviewMode { FLASHCARD, QUIZ }
data class QuizCardUiState(val promptText: String, val options: List<String>, val selectedOption: String? = null, val correctAnswerText: String)
data class ReviewCardUiState(val sourceText: String, val sourceNotes: String?, val targetText: String, val isFlipped: Boolean = false, val hintRevealed: Boolean = false, val hintText: String? = null, val noteVisible: Boolean = false)
data class ReviewAnswerFeedbackUiState(val isCorrect: Boolean, val stageLabel: String, val difficultyLabel: String, val correctAnswerText: String? = null, val answered: Int, val correct: Int, val wrong: Int)
data class ReviewUiState(
    val isLoading: Boolean = false, val isSelectingMode: Boolean = true,
    val selectedMode: ReviewMode = ReviewMode.FLASHCARD, val selectedReviewType: ReviewType = ReviewType.DAILY,
    val selectedDifficulty: VocabularyDifficulty? = null, val categories: List<Category> = emptyList(), val selectedCategoryId: UUID? = null,
    val card: ReviewCardUiState? = null, val quizCard: QuizCardUiState? = null, val remaining: Int = 0, val total: Int = 0,
    val isFinished: Boolean = false, val isSubmitting: Boolean = false, val answered: Int = 0, val correct: Int = 0, val wrong: Int = 0,
    val answerFeedback: ReviewAnswerFeedbackUiState? = null, val error: String? = null
) { val canSubmitAnswer: Boolean get() = !isSubmitting && answerFeedback == null && !isFinished && (card?.isFlipped == true || quizCard?.selectedOption != null) }

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
    private val reviewHelp = ReviewHelpUseCase()
    private var sessionId: UUID? = null
    private var queue: List<UUID> = emptyList()
    private var index = 0
    private var sessionGeneration = 0L
    private var isAdvancing = false
    private var activeLanguagePair = LanguagePair()
    private var quizDifficulty = QuizDifficulty.MEDIUM
    private var sessionContents: Map<UUID, List<Content>> = emptyMap()
    private var sessionDifficulties: Map<UUID, DifficultyState> = emptyMap()

    fun setLanguagePair(pair: LanguagePair) {
        if (pair == activeLanguagePair) return
        activeLanguagePair = pair
        if (sessionId != null || queue.isNotEmpty()) { sessionGeneration++; sessionId = null; queue = emptyList(); index = 0; sessionContents = emptyMap(); sessionDifficulties = emptyMap() }
    }
    fun setQuizDifficulty(value: QuizDifficulty) { quizDifficulty = value }
    fun chooseMode(mode: ReviewMode) { _state.value = _state.value.copy(selectedMode = mode) }
    init { viewModelScope.launch { runCatching { categoryRepository.getAll().sortedBy { it.name } }.onSuccess { _state.value = _state.value.copy(categories = it) } } }
    fun prepareReviewType(reviewType: ReviewType) { _state.value = _state.value.copy(selectedReviewType = reviewType, isSelectingMode = true, isFinished = false, error = null) }
    fun startNewSession(reviewType: ReviewType = _state.value.selectedReviewType, difficulty: VocabularyDifficulty? = _state.value.selectedDifficulty, categoryId: UUID? = _state.value.selectedCategoryId) {
        val generation = ++sessionGeneration
        viewModelScope.launch {
            if (generation != sessionGeneration) return@launch
            val now = Instant.now(); val pair = activeLanguagePair
            _state.value = ReviewUiState(isLoading = true, isSelectingMode = false, selectedReviewType = reviewType, selectedMode = _state.value.selectedMode, selectedDifficulty = difficulty, categories = _state.value.categories, selectedCategoryId = categoryId)
            try {
                val previousSessionId = sessionId
                if (previousSessionId != null) { endReviewSession(previousSessionId); if (generation != sessionGeneration) return@launch; sessionId = null }
                val candidates = selectReviewQueue(ReviewSelectionFilters(reviewType = reviewType, difficulty = difficulty, categoryId = categoryId, now = now))
                if (generation != sessionGeneration) return@launch
                val candidateIds = candidates.map { it.concept.id }.distinct()
                val contents = contentRepository.findForConcepts(candidateIds)
                val byConcept = contents.groupBy { it.conceptId }
                val validQueue = candidates.asSequence().filter { candidate -> val cc = byConcept[candidate.concept.id].orEmpty(); cc.any { it.languageCode == pair.source.code } && cc.any { it.languageCode == pair.target.code } }.map { it.concept.id }.distinct().toList()
                sessionContents = byConcept; sessionDifficulties = candidates.associate { it.concept.id to it.difficulty }; queue = validQueue; index = 0
                if (queue.isEmpty()) { sessionId = null; sessionContents = emptyMap(); sessionDifficulties = emptyMap(); _state.value = _state.value.copy(isLoading = false, isFinished = true, total = 0, remaining = 0) }
                else { val startedSession = startReviewSession(reviewType, now); if (generation != sessionGeneration) { runCatching { endReviewSession(startedSession) }; return@launch }; sessionId = startedSession; loadCurrentCard(generation, pair) }
            } catch (e: Exception) { if (generation != sessionGeneration) return@launch; sessionId = null; queue = emptyList(); index = 0; sessionContents = emptyMap(); sessionDifficulties = emptyMap(); _state.value = _state.value.copy(isLoading = false, isSelectingMode = true, card = null, quizCard = null, answerFeedback = null, remaining = 0, total = 0, error = e.message ?: "خطا در آماده‌سازی مرور") }
        }
    }
    private suspend fun loadCurrentCard(generation: Long, pair: LanguagePair = activeLanguagePair) {
        if (generation != sessionGeneration) return
        val conceptId = queue.getOrNull(index) ?: return
        val cc = sessionContents[conceptId].orEmpty()
        val source = cc.firstOrNull { it.languageCode == pair.source.code }; val target = cc.firstOrNull { it.languageCode == pair.target.code }
        if (source == null || target == null) { if (generation == sessionGeneration) advanceToNext(generation); return }
        if (generation != sessionGeneration || sessionId == null) return
        val baseCard = ReviewCardUiState(source.text, source.notes, target.text)
        if (_state.value.selectedMode == ReviewMode.QUIZ) {
            val concept = conceptRepository.get(conceptId)
            if (concept == null) { _state.value = _state.value.copy(isLoading = false, error = "واژه برای آزمون پیدا نشد"); return }
            when (val result = generateQuizQuestion(concept, QuizLanguagePair(pair.source.code, pair.target.code), sessionDifficulties[conceptId], quizDifficulty)) {
                is QuizQuestionResult.QuizQuestion -> _state.value = _state.value.copy(isLoading = false, isFinished = false, card = baseCard, quizCard = QuizCardUiState(result.promptText, result.options, correctAnswerText = result.correctAnswerText), remaining = queue.size - index, total = queue.size)
                QuizQuestionResult.FlashcardFallback -> _state.value = _state.value.copy(isLoading = false, isFinished = false, card = null, quizCard = null, error = "برای این سؤال چهار گزینهٔ معتبر پیدا نشد؛ حالت آزمون حفظ شد.", remaining = queue.size - index, total = queue.size)
            }
        } else _state.value = _state.value.copy(isLoading = false, isFinished = false, card = baseCard, quizCard = null, remaining = queue.size - index, total = queue.size)
    }
    fun chooseReviewType(reviewType: ReviewType) { _state.value = _state.value.copy(selectedReviewType = reviewType) }
    fun chooseDifficulty(difficulty: VocabularyDifficulty?) { _state.value = _state.value.copy(selectedDifficulty = difficulty) }
    fun chooseCategory(categoryId: UUID?) { _state.value = _state.value.copy(selectedCategoryId = categoryId) }
    fun selectQuizOption(option: String) { val quiz = _state.value.quizCard ?: return; if (_state.value.isSubmitting || _state.value.answerFeedback != null || option !in quiz.options) return; _state.value = _state.value.copy(quizCard = quiz.copy(selectedOption = option), error = null) }
    fun submitQuizAnswer() { val quiz = _state.value.quizCard ?: return; if (_state.value.isSubmitting || _state.value.answerFeedback != null) return; val selected = quiz.selectedOption ?: return; if (selected !in quiz.options) return; submitAnswer(selected == quiz.correctAnswerText) }
    fun flipCard() { _state.value.card?.let { _state.value = _state.value.copy(card = it.copy(isFlipped = true)) } }
    fun revealHint() {
        val card = _state.value.card ?: return
        val hint = reviewHelp.hintFor(card.sourceText)
        _state.value = _state.value.copy(card = card.copy(hintRevealed = true, hintText = hint))
    }
    fun toggleNote() {
        val card = _state.value.card ?: return
        if (reviewHelp.noteFor(card.sourceNotes) == null) return
        _state.value = _state.value.copy(card = card.copy(noteVisible = !card.noteVisible))
    }
    fun submitAnswer(isCorrect: Boolean) {
        val currentState = _state.value; if (!currentState.canSubmitAnswer) return
        val session = sessionId ?: return; val conceptId = queue.getOrNull(index) ?: return; val reviewType = currentState.selectedReviewType; val generation = sessionGeneration
        viewModelScope.launch {
            if (generation != sessionGeneration || sessionId != session) return@launch
            _state.value = _state.value.copy(isSubmitting = true, error = null)
            runCatching { submitReviewAnswer(SubmitReviewAnswerRequest(conceptId = conceptId, sessionId = session, reviewAttemptId = UUID.randomUUID(), reviewType = reviewType, isCorrect = isCorrect, reviewedAt = Instant.now())) }
                .onSuccess { result ->
                    if (generation != sessionGeneration || sessionId != session) return@onSuccess
                    val answered = _state.value.answered + 1; val correct = _state.value.correct + if (isCorrect) 1 else 0; val wrong = _state.value.wrong + if (isCorrect) 0 else 1
                    _state.value = _state.value.copy(isSubmitting = false, answerFeedback = ReviewAnswerFeedbackUiState(isCorrect, stageLabel(result.learningState.stage), difficultyLabel(result.difficultyState.current), if (!isCorrect && currentState.selectedMode == ReviewMode.QUIZ) currentState.quizCard?.correctAnswerText else null, answered, correct, wrong), answered = answered, correct = correct, wrong = wrong)
                    delay(2_000)
                    if (generation == sessionGeneration && sessionId == session && _state.value.answerFeedback != null) advanceToNext(generation)
                }
                .onFailure { if (generation == sessionGeneration && sessionId == session) _state.value = _state.value.copy(isSubmitting = false, error = it.message ?: "خطا در ثبت پاسخ") }
        }
    }
    fun nextCard() { if (_state.value.isSubmitting || _state.value.answerFeedback == null || isAdvancing) return; val generation = sessionGeneration; isAdvancing = true; viewModelScope.launch { try { if (generation == sessionGeneration) advanceToNext(generation) } finally { isAdvancing = false } } }
    fun exitReview(onCompleted: () -> Unit = {}) {
        val exitGeneration = ++sessionGeneration; val activeSessionId = sessionId
        if (activeSessionId == null) { queue = emptyList(); index = 0; sessionContents = emptyMap(); sessionDifficulties = emptyMap(); onCompleted(); return }
        viewModelScope.launch {
            var ended = false
            try { endReviewSession(activeSessionId); ended = true } catch (e: Exception) { if (exitGeneration == sessionGeneration) _state.value = _state.value.copy(error = e.message ?: "خطا در پایان مرور") }
            if (exitGeneration != sessionGeneration || !ended) return@launch
            sessionId = null; queue = emptyList(); index = 0; sessionContents = emptyMap(); sessionDifficulties = emptyMap(); _state.value = _state.value.copy(card = null, quizCard = null, isSubmitting = false, isFinished = true, remaining = 0); onCompleted()
        }
    }
    private suspend fun advanceToNext(generation: Long) {
        if (generation != sessionGeneration) return
        index += 1
        if (index >= queue.size) {
            val activeSessionId = sessionId
            try { activeSessionId?.let { endReviewSession(it) } } catch (e: Exception) { if (generation == sessionGeneration) _state.value = _state.value.copy(isLoading = false, isSubmitting = false, error = e.message ?: "خطا در پایان مرور"); return }
            if (generation != sessionGeneration) return
            sessionId = null; queue = emptyList(); index = 0; sessionContents = emptyMap(); sessionDifficulties = emptyMap(); _state.value = _state.value.copy(isLoading = false, card = null, isFinished = true, isSubmitting = false, answerFeedback = null, quizCard = null, remaining = 0)
        } else { if (generation != sessionGeneration) return; _state.value = _state.value.copy(isSubmitting = false, answerFeedback = null, error = null); loadCurrentCard(generation, activeLanguagePair) }
    }
}
private fun difficultyLabel(value: VocabularyDifficulty): String = when (value) { VocabularyDifficulty.EASY -> "آسان"; VocabularyDifficulty.MEDIUM -> "متوسط"; VocabularyDifficulty.HARD -> "سخت"; VocabularyDifficulty.VERY_HARD -> "خیلی سخت" }
private fun stageLabel(stage: com.flashlearn.domain.model.Stage): String = when (stage) { com.flashlearn.domain.model.Stage.DAILY -> "روزانه"; com.flashlearn.domain.model.Stage.WEEKLY -> "هفتگی"; com.flashlearn.domain.model.Stage.MONTHLY -> "ماهانه"; com.flashlearn.domain.model.Stage.LEARNED -> "یادگرفته‌شده" }
