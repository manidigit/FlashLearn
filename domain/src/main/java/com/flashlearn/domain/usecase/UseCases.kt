package com.flashlearn.domain.usecase

import com.flashlearn.domain.algorithm.calculateDifficulty
import com.flashlearn.domain.algorithm.calculateLearningTransition
import com.flashlearn.domain.model.*
import com.flashlearn.domain.repository.*
import com.flashlearn.domain.settings.SettingsKeys
import java.time.Instant
import java.time.ZoneId
import java.text.Normalizer
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

data class CreateConceptCommand(
    val sourceText: String, val targetText: String,
    val sourceLanguage: String = "es", val targetLanguage: String = "fa",
    val categoryId: UUID? = null, val notes: String? = null,
    val pronunciation: String? = null, val example: String? = null,
    val entryType: EntryType = EntryType.WORD, val tags: List<UUID> = emptyList(),
    val mergeExistingSource: Boolean = true
)

data class SubmitReviewAnswerRequest(
    val conceptId: UUID, val sessionId: UUID, val reviewAttemptId: UUID,
    val reviewType: ReviewType, val isCorrect: Boolean, val reviewedAt: Instant
)

data class SubmitReviewAnswerResult(
    val learningState: LearningState,
    val difficultyState: DifficultyState,
    val transition: TransitionResult
)

fun computeCanonicalKey(text: String): String =
    Normalizer.normalize(text.trim(), Normalizer.Form.NFC)
        .replace(Regex("\\s+"), " ")
        .lowercase(Locale.ROOT)

class DuplicateConceptException(message: String) : IllegalArgumentException(message)

class CreateConceptUseCase @Inject constructor(
    private val conceptRepository: ConceptRepository,
    private val contentRepository: ContentRepository,
    private val learningStateRepository: LearningStateRepository,
    private val difficultyStateRepository: DifficultyStateRepository,
    private val conceptTagRepository: ConceptTagRepository,
    private val database: FlashLearnDatabase
) {
    suspend operator fun invoke(command: CreateConceptCommand): UUID = database.withTransaction { createInTransaction(command) }
    internal suspend fun createInTransaction(command: CreateConceptCommand): UUID {
        val sourceKey = computeCanonicalKey(command.sourceText); val targetKey = computeCanonicalKey(command.targetText)
        require(sourceKey.isNotBlank() && targetKey.isNotBlank()) { "متن واژه نمی‌تواند خالی باشد" }
        val activeConcepts = conceptRepository.getAllActive()
        if (command.mergeExistingSource) {
            val existing = activeConcepts.firstOrNull { concept ->
                contentRepository.find(concept.id, command.sourceLanguage)?.let { computeCanonicalKey(it.text) } == sourceKey
            }
            if (existing != null) {
                val translations = contentRepository.findAll(existing.id, command.targetLanguage)
                if (translations.any { computeCanonicalKey(it.text) == targetKey }) throw DuplicateConceptException("این واژه با همین ترجمه قبلاً در کتابخانه وجود دارد")
                val nextIndex = (translations.maxOfOrNull { it.translationIndex } ?: -1) + 1
                contentRepository.insertTranslation(Content(UUID.randomUUID(), existing.id, command.targetLanguage, command.targetText.trim(), targetKey, translationIndex = nextIndex))
                return existing.id
            }
        }
        val id = UUID.randomUUID(); val now = Instant.now()
        conceptRepository.insert(Concept(id, command.entryType, command.categoryId, false, true, now, now))
        contentRepository.upsert(Content(UUID.randomUUID(), id, command.sourceLanguage, command.sourceText.trim(), sourceKey, command.notes, command.pronunciation, command.example))
        contentRepository.upsert(Content(UUID.randomUUID(), id, command.targetLanguage, command.targetText.trim(), targetKey, translationIndex = 0))
        learningStateRepository.upsert(LearningState(UUID.randomUUID(), id, Stage.DAILY, now, 0, false, 0, 0, null))
        difficultyStateRepository.upsert(DifficultyState(UUID.randomUUID(), id, VocabularyDifficulty.EASY, 0, 0, false))
        command.tags.forEach { conceptTagRepository.insert(ConceptTag(id, it)) }; return id
    }
}

data class UpdateConceptCommand(
    val conceptId: UUID, val sourceText: String, val targetText: String,
    val notes: String? = null, val pronunciation: String? = null, val example: String? = null,
    val entryType: EntryType? = null, val categoryId: UUID? = null, val preserveCategory: Boolean = true,
    val sourceLanguage: String = "es", val targetLanguage: String = "fa"
)

class ToggleFavoriteUseCase @Inject constructor(private val conceptRepository: ConceptRepository, private val database: FlashLearnDatabase) {
    suspend operator fun invoke(conceptId: UUID): Boolean = database.withTransaction { val concept = conceptRepository.get(conceptId) ?: error("Concept not found: $conceptId"); val updated = concept.copy(favorite = !concept.favorite, updatedAt = Instant.now()); conceptRepository.update(updated); updated.favorite }
}

class UpdateConceptUseCase @Inject constructor(private val conceptRepository: ConceptRepository, private val contentRepository: ContentRepository, private val database: FlashLearnDatabase) {
    suspend operator fun invoke(command: UpdateConceptCommand): UUID = database.withTransaction {
        val concept = conceptRepository.get(command.conceptId) ?: error("Concept not found: ${command.conceptId}")
        require(command.sourceLanguage.isNotBlank() && command.targetLanguage.isNotBlank() && command.sourceLanguage != command.targetLanguage) { "زبان‌های مبدأ و مقصد باید متفاوت باشند" }
        val sourceKey = computeCanonicalKey(command.sourceText)
        val requestedTranslations = command.targetText.split(Regex("\\s*/\\s*|\\s*؛\\s*|\\s*;\\s*"))
            .map(String::trim).filter(String::isNotBlank).distinctBy(::computeCanonicalKey)
        require(sourceKey.isNotBlank() && requestedTranslations.isNotEmpty()) { "متن واژه نمی‌تواند خالی باشد" }
        val activeConcepts = conceptRepository.getAllActive().filter { it.id != command.conceptId }
        val requestedKeys = requestedTranslations.map(::computeCanonicalKey).toSet()
        val duplicate = activeConcepts.any { other ->
            contentRepository.find(other.id, command.sourceLanguage)?.let { computeCanonicalKey(it.text) } == sourceKey &&
                contentRepository.findAll(other.id, command.targetLanguage).any { computeCanonicalKey(it.text) in requestedKeys }
        }
        if (duplicate) throw DuplicateConceptException("این واژه با همین ترجمه قبلاً در کتابخانه وجود دارد")
        val now = Instant.now()
        conceptRepository.update(concept.copy(entryType = command.entryType ?: concept.entryType, categoryId = if (command.preserveCategory) concept.categoryId else command.categoryId, updatedAt = now))
        val source = contentRepository.find(command.conceptId, command.sourceLanguage)
        contentRepository.upsert(Content(source?.id ?: UUID.randomUUID(), command.conceptId, command.sourceLanguage, command.sourceText.trim(), sourceKey, command.notes, command.pronunciation, command.example, translationIndex = source?.translationIndex ?: 0, grammarNote = source?.grammarNote, possibleCorrection = source?.possibleCorrection))
        val existingTranslations = contentRepository.findAll(command.conceptId, command.targetLanguage)
        val firstRequested = requestedTranslations.first(); val firstKey = computeCanonicalKey(firstRequested); val firstExisting = existingTranslations.firstOrNull()
        contentRepository.upsert(Content(firstExisting?.id ?: UUID.randomUUID(), command.conceptId, command.targetLanguage, firstRequested, firstKey, translationIndex = firstExisting?.translationIndex ?: 0, grammarNote = firstExisting?.grammarNote, possibleCorrection = firstExisting?.possibleCorrection))
        val existingKeysAfterFirst = existingTranslations.drop(1).map { computeCanonicalKey(it.text) }.toMutableSet()
        var nextIndex = maxOf(existingTranslations.maxOfOrNull { it.translationIndex } ?: 0, firstExisting?.translationIndex ?: 0) + 1
        requestedTranslations.drop(1).forEach { text -> val key = computeCanonicalKey(text); if (existingKeysAfterFirst.add(key) && key != firstKey) contentRepository.insertTranslation(Content(UUID.randomUUID(), command.conceptId, command.targetLanguage, text, key, translationIndex = nextIndex++)) }
        command.conceptId
    }
}

class DeleteConceptUseCase @Inject constructor(private val conceptRepository: ConceptRepository, private val database: FlashLearnDatabase) {
    suspend operator fun invoke(conceptId: UUID) = database.withTransaction { if (conceptRepository.get(conceptId) == null) error("Concept not found: $conceptId"); conceptRepository.softDelete(conceptId, Instant.now()) }
}

class EnsureStarterDataUseCase @Inject constructor(private val conceptRepository: ConceptRepository, private val categoryRepository: CategoryRepository, private val createConcept: CreateConceptUseCase, private val database: FlashLearnDatabase) {
    suspend operator fun invoke(): Int = database.withTransaction {
        if (conceptRepository.getAllActive().isNotEmpty()) return@withTransaction 0
        val categoryNames = listOf("مکالمه روزمره", "سفر", "غذا", "افعال پایه")
        val categories = categoryNames.associateWith { name -> categoryRepository.findByName(name) ?: run { val category = Category(UUID.randomUUID(), name); categoryRepository.insert(category); category } }
        val starter = listOf(Triple("hola", "سلام", "مکالمه روزمره"), Triple("gracias", "ممنون / متشکرم", "مکالمه روزمره"), Triple("por favor", "لطفاً", "مکالمه روزمره"), Triple("adiós", "خداحافظ", "مکالمه روزمره"), Triple("buenos días", "صبح بخیر", "مکالمه روزمره"), Triple("¿cómo estás?", "حالت چطور است؟", "مکالمه روزمره"), Triple("agua", "آب", "غذا"), Triple("comida", "غذا", "غذا"), Triple("café", "قهوه", "غذا"), Triple("casa", "خانه", "مکالمه روزمره"), Triple("viaje", "سفر", "سفر"), Triple("hotel", "هتل", "سفر"), Triple("ir", "رفتن", "افعال پایه"), Triple("venir", "آمدن", "افعال پایه"), Triple("comer", "خوردن", "افعال پایه"), Triple("beber", "نوشیدن", "افعال پایه"), Triple("hablar", "صحبت کردن", "افعال پایه"), Triple("aprender", "یاد گرفتن", "افعال پایه"), Triple("entender", "فهمیدن", "افعال پایه"), Triple("ayuda", "کمک", "سفر"))
        starter.forEach { (source, target, categoryName) -> createConcept(CreateConceptCommand(sourceText = source, targetText = target, categoryId = categories.getValue(categoryName).id, notes = "واژهٔ نمونهٔ اولیه FlashLearn")) }; starter.size
    }
}

class SubmitReviewAnswerUseCase @Inject constructor(
    private val learningStateRepository: LearningStateRepository,
    private val difficultyStateRepository: DifficultyStateRepository,
    private val reviewHistoryRepository: ReviewHistoryRepository,
    private val settingsRepository: SettingsRepository,
    private val database: FlashLearnDatabase
) {
    suspend operator fun invoke(request: SubmitReviewAnswerRequest): SubmitReviewAnswerResult = database.withTransaction {
        val learning = learningStateRepository.get(request.conceptId) ?: error("DATA_INTEGRITY_ERROR: LearningState not found for concept ${request.conceptId}")
        val difficulty = difficultyStateRepository.get(request.conceptId) ?: error("DATA_INTEGRITY_ERROR: DifficultyState not found for concept ${request.conceptId}")
        if (reviewHistoryRepository.existsByAttemptId(request.sessionId, request.reviewAttemptId)) error("Duplicate review attempt: ${request.reviewAttemptId}")
        val reviewedLocalDate = request.reviewedAt.atZone(ZoneId.systemDefault()).toLocalDate()
        val lastReviewedLocalDate = learning.lastReviewedAt?.atZone(ZoneId.systemDefault())?.toLocalDate()
        if (lastReviewedLocalDate == reviewedLocalDate) error("Concept has already been practiced today: ${request.conceptId}")
        if (request.reviewType != ReviewType.LEARNED) {
            val dueAt = learning.nextReviewAt
            if (dueAt != null && dueAt > request.reviewedAt) error("Concept is not due yet (nextReviewAt = $dueAt")
        }
        val transition = calculateLearningTransition(learning, request.isCorrect, request.reviewedAt)
        val threshold = settingsRepository.getInt("threshold_difficulty", default = 3)
        val newDifficulty = calculateDifficulty(state = difficulty, isCorrect = request.isCorrect, reviewType = request.reviewType, monthlyWrongCountBefore = learning.monthlyWrongCount, threshold = threshold)
        val updatedLearning = learning.copy(stage = transition.newStage, nextReviewAt = transition.nextReviewAt, hasPathFailure = transition.hasPathFailure, monthlyWrongCount = transition.monthlyWrongCount, totalCorrect = if (request.isCorrect) learning.totalCorrect + 1 else learning.totalCorrect, totalWrong = if (!request.isCorrect) learning.totalWrong + 1 else learning.totalWrong, lastReviewedAt = request.reviewedAt)
        learningStateRepository.upsert(updatedLearning); difficultyStateRepository.upsert(newDifficulty)
        reviewHistoryRepository.insert(ReviewHistory(UUID.randomUUID(), request.sessionId, request.reviewAttemptId, request.conceptId, request.reviewedAt, request.isCorrect, request.reviewType))
        SubmitReviewAnswerResult(updatedLearning, newDifficulty, transition)
    }
}
