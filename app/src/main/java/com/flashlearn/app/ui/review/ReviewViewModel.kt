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
import com.flashlearn.domain.settings.SettingsKeys
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
import com.flashlearn.domain.usecase.ReviewCandidate
import com.flashlearn.domain.usecase.CountReviewQueueUseCase
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
    val isLoading: Boolean = false,
    val isSelectingMode: Boolean = true,
    val selectedMode: ReviewMode = ReviewMode.QUIZ,
    val selectedReviewType: ReviewType = ReviewType.RANDOM,
    val selectedDifficulty: VocabularyDifficulty? = null,
    val selectedDifficulties: Set<VocabularyDifficulty> = emptySet(),
    val categories: List<Category> = emptyList(),
    val categoryWordCounts: Map<UUID, Int> = emptyMap(),
    val allCategoryWordCount: Int = 0,
    val selectedCategoryId: UUID? = null,
    val selectedCategoryIds: Set<UUID> = emptySet(),
    val selectedQuizDifficulty: QuizDifficulty = QuizDifficulty.MEDIUM,
    val maximumReviewCards: Int = SettingsKeys.DEFAULT_MAXIMUM_REVIEW_CARDS,
    val availableReviewCount: Int = 0,
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
    val canSubmitAnswer: Boolean get() = !isSubmitting && answerFeedback == null && !isFinished && (card?.isFlipped == true || quizCard?.selectedOption != null)
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
    private val countReviewQueue: CountReviewQueueUseCase,
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
    private var maximumReviewCards = SettingsKeys.DEFAULT_MAXIMUM_REVIEW_CARDS
    private var sessionContents: Map<UUID, List<Content>> = emptyMap()
    private var sessionDifficulties: Map<UUID, DifficultyState> = emptyMap()
    private val usedQuizDistractorTexts = mutableSetOf<String>()

    init {
        viewModelScope.launch {
            runCatching {
                val categories = categoryRepository.getAll().sortedBy { it.name }
                val concepts = conceptRepository.getAllActive()
                val counts = concepts.mapNotNull { concept -> concept.categoryId?.let { it to 1 } }
                    .groupingBy { it.first }.fold(0) { acc, item -> acc + item.second }
                Triple(categories, counts, concepts.size)
            }.onSuccess { (categories, counts, total) ->
                _state.value = _state.value.copy(categories = categories, categoryWordCounts = counts, allCategoryWordCount = total)
                refreshAvailableReviewCount()
            }
        }
    }

    fun setLanguagePair(pair: LanguagePair) {
        if (pair == activeLanguagePair) return
        activeLanguagePair = pair
        if (sessionId != null || queue.isNotEmpty()) { sessionGeneration++; sessionId = null; queue = emptyList(); index = 0; sessionContents = emptyMap(); sessionDifficulties = emptyMap() }
    }

    fun setQuizDifficulty(value: QuizDifficulty) { quizDifficulty = value; _state.value = _state.value.copy(selectedQuizDifficulty = value) }
    fun chooseQuizDifficulty(value: QuizDifficulty) = setQuizDifficulty(value)
    fun setMaximumReviewCards(value: Int) { maximumReviewCards = value.coerceIn(SettingsKeys.MINIMUM_REVIEW_CARDS, SettingsKeys.MAXIMUM_REVIEW_CARDS_LIMIT); _state.value = _state.value.copy(maximumReviewCards = maximumReviewCards) }
    fun chooseMode(mode: ReviewMode) { _state.value = _state.value.copy(selectedMode = mode) }

    fun prepareReviewType(reviewType: ReviewType) {
        _state.value = _state.value.copy(selectedReviewType = normalizeReviewType(reviewType), isSelectingMode = true, isFinished = false, error = null)
    }

    fun chooseReviewType(reviewType: ReviewType) {
        _state.value = _state.value.copy(selectedReviewType = normalizeReviewType(reviewType))
        refreshAvailableReviewCount()
    }

    private fun normalizeReviewType(reviewType: ReviewType): ReviewType = when (reviewType) {
        ReviewType.DAILY -> ReviewType.DAILY
        ReviewType.WEEKLY -> ReviewType.WEEKLY
        ReviewType.MONTHLY -> ReviewType.MONTHLY
        ReviewType.LEARNED -> ReviewType.LEARNED
        ReviewType.RANDOM -> ReviewType.RANDOM
    }

    fun toggleDifficulty(difficulty: VocabularyDifficulty?) {
        val current = _state.value.selectedDifficulties.toMutableSet()
        if (difficulty == null) current.clear() else if (!current.add(difficulty)) current.remove(difficulty)
        _state.value = _state.value.copy(selectedDifficulties = current, selectedDifficulty = current.singleOrNull())
        refreshAvailableReviewCount()
    }

    fun toggleCategory(categoryId: UUID?) {
        val current = _state.value.selectedCategoryIds.toMutableSet()
        if (categoryId == null) current.clear() else if (!current.add(categoryId)) current.remove(categoryId)
        _state.value = _state.value.copy(selectedCategoryIds = current, selectedCategoryId = current.singleOrNull())
        refreshAvailableReviewCount()
    }

    fun setCategories(categoryIds: Set<UUID>) {
        _state.value = _state.value.copy(selectedCategoryIds = categoryIds, selectedCategoryId = categoryIds.singleOrNull())
        refreshAvailableReviewCount()
    }

    // Compatibility with existing callers.
    fun chooseDifficulty(difficulty: VocabularyDifficulty?) { _state.value = if (difficulty == null) _state.value.copy(selectedDifficulty = null, selectedDifficulties = emptySet()) else _state.value.copy(selectedDifficulty = difficulty, selectedDifficulties = setOf(difficulty)); refreshAvailableReviewCount() }
    fun chooseCategory(categoryId: UUID?) { _state.value = if (categoryId == null) _state.value.copy(selectedCategoryId = null, selectedCategoryIds = emptySet()) else _state.value.copy(selectedCategoryId = categoryId, selectedCategoryIds = setOf(categoryId)); refreshAvailableReviewCount() }

    private fun refreshAvailableReviewCount() {
        val snapshot = _state.value
        val reviewType = snapshot.selectedReviewType
        val difficulties = snapshot.selectedDifficulties
        val categories = snapshot.selectedCategoryIds
        viewModelScope.launch {
            val count = runCatching {
                val difficultyOptions = if (difficulties.isEmpty()) listOf<VocabularyDifficulty?>(null) else difficulties.map { it }
                val categoryOptions = if (categories.isEmpty()) listOf<UUID?>(null) else categories.map { it }
                var total = 0
                val now = Instant.now()
                for (d in difficultyOptions) for (c in categoryOptions) {
                    total += countReviewQueue(ReviewSelectionFilters(reviewType = reviewType, difficulty = d, categoryId = c, now = now))
                }
                total
            }.getOrDefault(0)
            val current = _state.value
            if (current.selectedReviewType == reviewType && current.selectedDifficulties == difficulties && current.selectedCategoryIds == categories) {
                _state.value = current.copy(availableReviewCount = count)
            }
        }
    }

    fun startNewSession(reviewType: ReviewType = _state.value.selectedReviewType, difficulty: VocabularyDifficulty? = null, categoryId: UUID? = null) {
        val generation = ++sessionGeneration
        val currentState = _state.value
        usedQuizDistractorTexts.clear()
        val normalizedReviewType = normalizeReviewType(reviewType)
        val difficulties = if (difficulty != null) setOf(difficulty) else currentState.selectedDifficulties
        val categories = if (categoryId != null) setOf(categoryId) else currentState.selectedCategoryIds
        val maxCards = maximumReviewCards
        viewModelScope.launch {
            if (generation != sessionGeneration) return@launch
            val now = Instant.now(); val pair = activeLanguagePair
            _state.value = currentState.copy(isLoading = true, isSelectingMode = false, selectedReviewType = normalizedReviewType, selectedDifficulties = difficulties, selectedDifficulty = difficulties.singleOrNull(), selectedCategoryIds = categories, selectedCategoryId = categories.singleOrNull(), maximumReviewCards = maxCards, selectedQuizDifficulty = quizDifficulty, card = null, quizCard = null, answerFeedback = null, error = null, answered = 0, correct = 0, wrong = 0)
            try {
                sessionId?.let { endReviewSession(it); sessionId = null }
                val candidates = selectCandidates(normalizedReviewType, difficulties, categories, now, maxCards)
                if (generation != sessionGeneration) return@launch
                val candidateIds = candidates.map { it.concept.id }.distinct()
                val contents = contentRepository.findForConcepts(candidateIds)
                val byConcept = contents.groupBy { it.conceptId }
                val validCandidates = candidates.asSequence().filter { candidate -> val cc = byConcept[candidate.concept.id].orEmpty(); cc.any { it.languageCode == pair.source.code } && cc.any { it.languageCode == pair.target.code } }.distinctBy { it.concept.id }.take(maxCards).toList()
                sessionContents = byConcept
                sessionDifficulties = validCandidates.associate { it.concept.id to it.difficulty }
                if (_state.value.selectedMode == ReviewMode.QUIZ) {
                    // Refresh once per review session so newly added/edited vocabulary is
                    // available to the distractor generator without refreshing per card.
                    generateQuizQuestion.refreshBank()
                }
                queue = validCandidates.map { it.concept.id }.distinct(); index = 0
                if (queue.isEmpty()) {
                    sessionId = null; sessionContents = emptyMap(); sessionDifficulties = emptyMap(); _state.value = _state.value.copy(isLoading = false, isFinished = true, total = 0, remaining = 0)
                } else {
                    val startedSession = startReviewSession(normalizedReviewType, now)
                    if (generation != sessionGeneration) { runCatching { endReviewSession(startedSession) }; return@launch }
                    sessionId = startedSession; loadCurrentCard(generation, pair)
                }
            } catch (e: Exception) {
                if (generation != sessionGeneration) return@launch
                sessionId = null; queue = emptyList(); index = 0; sessionContents = emptyMap(); sessionDifficulties = emptyMap(); usedQuizDistractorTexts.clear(); _state.value = _state.value.copy(isLoading = false, isSelectingMode = true, card = null, quizCard = null, answerFeedback = null, remaining = 0, total = 0, error = e.message ?: "خطا در آماده‌سازی مرور")
            }
        }
    }

    private suspend fun selectCandidates(reviewType: ReviewType, difficulties: Set<VocabularyDifficulty>, categories: Set<UUID>, now: Instant, maxCards: Int): List<ReviewCandidate> {
        val difficultyOptions = if (difficulties.isEmpty()) listOf<VocabularyDifficulty?>(null) else difficulties.map { it }
        val categoryOptions = if (categories.isEmpty()) listOf<UUID?>(null) else categories.map { it }
        val results = mutableListOf<ReviewCandidate>()
        for (d in difficultyOptions) for (c in categoryOptions) results += selectReviewQueue(ReviewSelectionFilters(reviewType = reviewType, difficulty = d, categoryId = c, now = now, maxCards = maxCards))
        val unique = results.distinctBy { it.concept.id }
        return if (reviewType == ReviewType.RANDOM || reviewType == ReviewType.LEARNED) unique.shuffled().take(maxCards) else unique.take(maxCards)
    }

    private suspend fun loadCurrentCard(generation: Long, pair: LanguagePair = activeLanguagePair) {
        if (generation != sessionGeneration) return
        val conceptId = queue.getOrNull(index) ?: return
        val cc = sessionContents[conceptId].orEmpty()
        val source = cc.firstOrNull { it.languageCode == pair.source.code }
        val target = cc.firstOrNull { it.languageCode == pair.target.code }
        if (source == null || target == null) { if (generation == sessionGeneration) advanceToNext(generation); return }
        if (generation != sessionGeneration || sessionId == null) return
        val baseCard = ReviewCardUiState(source.text, source.notes, target.text)
        if (_state.value.selectedMode == ReviewMode.QUIZ) {
            val concept = conceptRepository.get(conceptId) ?: run { _state.value = _state.value.copy(isLoading = false, error = "واژه برای آزمون پیدا نشد"); return }
            when (val result = generateQuizQuestion(
                concept,
                QuizLanguagePair(pair.source.code, pair.target.code),
                sessionDifficulties[conceptId],
                quizDifficulty,
                usedQuizDistractorTexts
            )) {
                is QuizQuestionResult.QuizQuestion -> {
                    usedQuizDistractorTexts += result.options
                        .filterNot { it.equals(result.correctAnswerText, ignoreCase = false) }
                        .map { it.trim() }
                    _state.value = _state.value.copy(isLoading = false, isFinished = false, card = baseCard, quizCard = QuizCardUiState(result.promptText, result.options, correctAnswerText = result.correctAnswerText), remaining = queue.size - index, total = queue.size)
                }
                QuizQuestionResult.FlashcardFallback -> _state.value = _state.value.copy(isLoading = false, isFinished = false, card = null, quizCard = null, error = "برای این سؤال چهار گزینهٔ معتبر پیدا نشد؛ حالت آزمون حفظ شد.", remaining = queue.size - index, total = queue.size)
            }
        } else _state.value = _state.value.copy(isLoading = false, isFinished = false, card = baseCard, quizCard = null, remaining = queue.size - index, total = queue.size)
    }

    fun selectQuizOption(option: String) { val quiz = _state.value.quizCard ?: return; if (_state.value.isSubmitting || _state.value.answerFeedback != null || option !in quiz.options) return; _state.value = _state.value.copy(quizCard = quiz.copy(selectedOption = option), error = null) }
    fun submitQuizAnswer() { val quiz = _state.value.quizCard ?: return; if (_state.value.isSubmitting || _state.value.answerFeedback != null) return; val selected = quiz.selectedOption ?: return; if (selected !in quiz.options) return; submitAnswer(selected == quiz.correctAnswerText) }
    fun flipCard() { _state.value.card?.let { _state.value = _state.value.copy(card = it.copy(isFlipped = true)) } }
    fun revealHint() { val card = _state.value.card ?: return; _state.value = _state.value.copy(card = card.copy(hintRevealed = true, hintText = reviewHelp.hintFor(card.sourceText))) }
    fun toggleNote() { val card = _state.value.card ?: return; if (reviewHelp.noteFor(card.sourceNotes) == null) return; _state.value = _state.value.copy(card = card.copy(noteVisible = !card.noteVisible)) }

    fun submitAnswer(isCorrect: Boolean) {
        val currentState = _state.value; if (!currentState.canSubmitAnswer) return
        val session = sessionId ?: return; val conceptId = queue.getOrNull(index) ?: return; val reviewType = currentState.selectedReviewType; val generation = sessionGeneration
        viewModelScope.launch {
            if (generation != sessionGeneration || sessionId != session) return@launch
            _state.value = _state.value.copy(isSubmitting = true, error = null)
            runCatching { submitReviewAnswer(SubmitReviewAnswerRequest(conceptId, session, UUID.randomUUID(), reviewType, isCorrect, Instant.now())) }
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
        viewModelScope.launch { var ended = false; try { endReviewSession(activeSessionId); ended = true } catch (e: Exception) { if (exitGeneration == sessionGeneration) _state.value = _state.value.copy(error = e.message ?: "خطا در پایان مرور") }; if (exitGeneration != sessionGeneration || !ended) return@launch; sessionId = null; queue = emptyList(); index = 0; sessionContents = emptyMap(); sessionDifficulties = emptyMap(); usedQuizDistractorTexts.clear(); _state.value = _state.value.copy(card = null, quizCard = null, isSubmitting = false, isFinished = true, remaining = 0); onCompleted() }
    }
    private suspend fun advanceToNext(generation: Long) {
        if (generation != sessionGeneration) return
        index += 1
        if (index >= queue.size) {
            val activeSessionId = sessionId
            try { activeSessionId?.let { endReviewSession(it) } } catch (e: Exception) { if (generation == sessionGeneration) _state.value = _state.value.copy(isLoading = false, isSubmitting = false, error = e.message ?: "خطا در پایان مرور"); return }
            if (generation != sessionGeneration) return
            sessionId = null; queue = emptyList(); index = 0; sessionContents = emptyMap(); sessionDifficulties = emptyMap(); usedQuizDistractorTexts.clear(); _state.value = _state.value.copy(isLoading = false, card = null, isFinished = true, isSubmitting = false, answerFeedback = null, quizCard = null, remaining = 0)
        } else { if (generation != sessionGeneration) return; _state.value = _state.value.copy(isSubmitting = false, answerFeedback = null, error = null); loadCurrentCard(generation, activeLanguagePair) }
    }
}

private fun difficultyLabel(value: VocabularyDifficulty) = when (value) { VocabularyDifficulty.EASY -> "آسان"; VocabularyDifficulty.MEDIUM -> "متوسط"; VocabularyDifficulty.HARD -> "سخت"; VocabularyDifficulty.VERY_HARD -> "خیلی سخت" }
private fun stageLabel(stage: com.flashlearn.domain.model.Stage) = when (stage) { com.flashlearn.domain.model.Stage.DAILY -> "روزانه"; com.flashlearn.domain.model.Stage.WEEKLY -> "هفتگی"; com.flashlearn.domain.model.Stage.MONTHLY -> "ماهانه"; com.flashlearn.domain.model.Stage.LEARNED -> "یادگرفته‌شده" }
