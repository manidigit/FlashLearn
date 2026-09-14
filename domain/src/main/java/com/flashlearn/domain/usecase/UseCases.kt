package com.flashlearn.domain.usecase

import com.flashlearn.domain.algorithm.calculateDifficulty
import com.flashlearn.domain.algorithm.calculateLearningTransition
import com.flashlearn.domain.model.*
import com.flashlearn.domain.repository.*
import java.time.Instant
import java.text.Normalizer
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

data class CreateConceptCommand(
    val sourceText: String, val targetText: String,
    val sourceLanguage: String = "es", val targetLanguage: String = "fa",
    val categoryId: UUID? = null, val notes: String? = null,
    val pronunciation: String? = null, val example: String? = null,
    val entryType: EntryType = EntryType.WORD, val tags: List<UUID> = emptyList()
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
    suspend operator fun invoke(command: CreateConceptCommand): UUID =
        database.withTransaction { createInTransaction(command) }

    /**
     * Creates a concept without opening a second transaction.
     * Callers that need to persist additional data atomically with the concept
     * must invoke this from their own database transaction.
     */
    internal suspend fun createInTransaction(command: CreateConceptCommand): UUID {
        val sourceKey = computeCanonicalKey(command.sourceText)
        val targetKey = computeCanonicalKey(command.targetText)
        require(sourceKey.isNotBlank() && targetKey.isNotBlank()) { "متن واژه نمی‌تواند خالی باشد" }

        val activeIds = conceptRepository.getAllActive().map { it.id }.toSet()
        if (activeIds.isNotEmpty()) {
            val duplicate = contentRepository.getAll().any {
                it.conceptId in activeIds &&
                    it.languageCode == command.sourceLanguage &&
                    it.canonicalKey == sourceKey &&
                    contentRepository.find(it.conceptId, command.targetLanguage)?.canonicalKey == targetKey
            }
            if (duplicate) {
                throw DuplicateConceptException("این واژه با همین ترجمه قبلاً در کتابخانه وجود دارد")
            }
        }

        val id = UUID.randomUUID()
        val now = Instant.now()
        conceptRepository.insert(Concept(id, command.entryType, command.categoryId, false, true, now, now))
        contentRepository.upsert(Content(UUID.randomUUID(), id, command.sourceLanguage, command.sourceText.trim(), sourceKey, command.notes, command.pronunciation, command.example))
        contentRepository.upsert(Content(UUID.randomUUID(), id, command.targetLanguage, command.targetText.trim(), targetKey))
        learningStateRepository.upsert(LearningState(UUID.randomUUID(), id, Stage.DAILY, now, 0, false, 0, 0, null))
        difficultyStateRepository.upsert(DifficultyState(UUID.randomUUID(), id, VocabularyDifficulty.EASY, 0, 0, false))
        command.tags.forEach { conceptTagRepository.insert(ConceptTag(id, it)) }
        return id
    }
}


data class UpdateConceptCommand(
    val conceptId: UUID, val sourceText: String, val targetText: String,
    val notes: String? = null, val pronunciation: String? = null, val example: String? = null,
    val entryType: EntryType? = null, val categoryId: UUID? = null, val preserveCategory: Boolean = true
)

class ToggleFavoriteUseCase @Inject constructor(
    private val conceptRepository: ConceptRepository, private val database: FlashLearnDatabase
) {
    suspend operator fun invoke(conceptId: UUID): Boolean = database.withTransaction {
        val concept = conceptRepository.get(conceptId) ?: error("Concept not found: $conceptId")
        val updated = concept.copy(favorite = !concept.favorite, updatedAt = Instant.now())
        conceptRepository.update(updated); updated.favorite
    }
}

class UpdateConceptUseCase @Inject constructor(
    private val conceptRepository: ConceptRepository, private val contentRepository: ContentRepository,
    private val database: FlashLearnDatabase
) {
    suspend operator fun invoke(command: UpdateConceptCommand): UUID = database.withTransaction {
        val concept = conceptRepository.get(command.conceptId) ?: error("Concept not found: ${command.conceptId}")
        val sourceKey = computeCanonicalKey(command.sourceText)
        val targetKey = computeCanonicalKey(command.targetText)
        require(sourceKey.isNotBlank() && targetKey.isNotBlank()) { "متن واژه نمی‌تواند خالی باشد" }

        val duplicate = conceptRepository.getAllActive().asSequence()
            .filter { it.id != command.conceptId }
            .any { other ->
                contentRepository.find(other.id, "es")?.canonicalKey == sourceKey &&
                    contentRepository.find(other.id, "fa")?.canonicalKey == targetKey
            }
        if (duplicate) {
            throw DuplicateConceptException("این واژه با همین ترجمه قبلاً در کتابخانه وجود دارد")
        }

        val now = Instant.now()
        conceptRepository.update(concept.copy(entryType = command.entryType ?: concept.entryType, categoryId = if (command.preserveCategory) concept.categoryId else command.categoryId, updatedAt = now))
        val source = contentRepository.find(command.conceptId, "es")
        val target = contentRepository.find(command.conceptId, "fa")
        contentRepository.upsert(Content(source?.id ?: UUID.randomUUID(), command.conceptId, "es", command.sourceText, computeCanonicalKey(command.sourceText), command.notes, command.pronunciation, command.example))
        contentRepository.upsert(Content(target?.id ?: UUID.randomUUID(), command.conceptId, "fa", command.targetText, computeCanonicalKey(command.targetText)))
        command.conceptId
    }
}

class DeleteConceptUseCase @Inject constructor(
    private val conceptRepository: ConceptRepository, private val database: FlashLearnDatabase
) {
    suspend operator fun invoke(conceptId: UUID) = database.withTransaction {
        if (conceptRepository.get(conceptId) == null) error("Concept not found: $conceptId")
        conceptRepository.softDelete(conceptId, Instant.now())
    }
}
/** Seeds a small first-run vocabulary only when the local library is empty. */
class EnsureStarterDataUseCase @Inject constructor(
    private val conceptRepository: ConceptRepository,
    private val categoryRepository: CategoryRepository,
    private val createConcept: CreateConceptUseCase,
    private val database: FlashLearnDatabase
) {
    suspend operator fun invoke(): Int = database.withTransaction {
        if (conceptRepository.getAllActive().isNotEmpty()) return@withTransaction 0

        val categoryNames = listOf("مکالمه روزمره", "سفر", "غذا", "افعال پایه")
        val categories = categoryNames.associateWith { name ->
            categoryRepository.findByName(name) ?: run {
                val category = Category(UUID.randomUUID(), name)
                categoryRepository.insert(category)
                category
            }
        }

        val starter = listOf(
            Triple("hola", "سلام", "مکالمه روزمره"),
            Triple("gracias", "ممنون / متشکرم", "مکالمه روزمره"),
            Triple("por favor", "لطفاً", "مکالمه روزمره"),
            Triple("adiós", "خداحافظ", "مکالمه روزمره"),
            Triple("buenos días", "صبح بخیر", "مکالمه روزمره"),
            Triple("¿cómo estás?", "حالت چطور است؟", "مکالمه روزمره"),
            Triple("agua", "آب", "غذا"),
            Triple("comida", "غذا", "غذا"),
            Triple("café", "قهوه", "غذا"),
            Triple("casa", "خانه", "مکالمه روزمره"),
            Triple("viaje", "سفر", "سفر"),
            Triple("hotel", "هتل", "سفر"),
            Triple("ir", "رفتن", "افعال پایه"),
            Triple("venir", "آمدن", "افعال پایه"),
            Triple("comer", "خوردن", "افعال پایه"),
            Triple("beber", "نوشیدن", "افعال پایه"),
            Triple("hablar", "صحبت کردن", "افعال پایه"),
            Triple("aprender", "یاد گرفتن", "افعال پایه"),
            Triple("entender", "فهمیدن", "افعال پایه"),
            Triple("ayuda", "کمک", "سفر")
        )

        starter.forEach { (source, target, categoryName) ->
            createConcept(
                CreateConceptCommand(
                    sourceText = source,
                    targetText = target,
                    categoryId = categories.getValue(categoryName).id,
                    notes = "واژهٔ نمونهٔ اولیه FlashLearn"
                )
            )
        }
        starter.size
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
        val learning = learningStateRepository.get(request.conceptId)
            ?: error("DATA_INTEGRITY_ERROR: LearningState not found for concept ${request.conceptId}")
        val difficulty = difficultyStateRepository.get(request.conceptId)
            ?: error("DATA_INTEGRITY_ERROR: DifficultyState not found for concept ${request.conceptId}")

        // Idempotency/duplicate validation precedes due validation so a replay is always
        // classified as a duplicate, even if the first attempt advanced nextReviewAt.
        if (reviewHistoryRepository.existsByAttemptId(request.sessionId, request.reviewAttemptId)) {
            error("Duplicate review attempt: ${request.reviewAttemptId}")
        }

        if (request.reviewType != ReviewType.LEARNED) {
            val dueAt = learning.nextReviewAt
            if (dueAt != null && dueAt > request.reviewedAt) {
                error("Concept is not due yet (nextReviewAt = $dueAt)")
            }
        }

        val transition = calculateLearningTransition(learning, request.isCorrect, request.reviewedAt)
        val threshold = settingsRepository.getInt("threshold_difficulty", default = 3)
        val newDifficulty = calculateDifficulty(
            state = difficulty,
            isCorrect = request.isCorrect,
            reviewType = request.reviewType,
            monthlyWrongCountBefore = learning.monthlyWrongCount,
            threshold = threshold
        )
        val updatedLearning = learning.copy(
            stage = transition.newStage,
            nextReviewAt = transition.nextReviewAt,
            hasPathFailure = transition.hasPathFailure,
            monthlyWrongCount = transition.monthlyWrongCount,
            totalCorrect = if (request.isCorrect) learning.totalCorrect + 1 else learning.totalCorrect,
            totalWrong = if (!request.isCorrect) learning.totalWrong + 1 else learning.totalWrong,
            lastReviewedAt = request.reviewedAt
        )
        learningStateRepository.upsert(updatedLearning)
        difficultyStateRepository.upsert(newDifficulty)
        reviewHistoryRepository.insert(ReviewHistory(UUID.randomUUID(), request.sessionId, request.reviewAttemptId, request.conceptId, request.reviewedAt, request.isCorrect, request.reviewType))
        SubmitReviewAnswerResult(updatedLearning, newDifficulty, transition)
    }
}
