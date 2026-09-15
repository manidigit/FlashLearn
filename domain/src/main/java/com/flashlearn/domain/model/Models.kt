package com.flashlearn.domain.model
import java.time.Instant
import java.util.UUID

enum class Stage { DAILY, WEEKLY, MONTHLY, LEARNED }
enum class VocabularyDifficulty { EASY, MEDIUM, HARD, VERY_HARD }
enum class ReviewType { DAILY, WEEKLY, MONTHLY, LEARNED, RANDOM }
enum class EntryType { WORD, PHRASE, SENTENCE, IDIOM, COLLOCATION, STRUCTURE }
enum class ImportMode { ADD_NEW, SKIP_DUPLICATE, MERGE, UPDATE }
enum class VocabularyRelationType { DERIVED_FROM, USED_IN, SYNONYM }
enum class VocabularyVariantType { MASCULINE, FEMININE, ALTERNATIVE }
enum class ReviewQueueStatus { PENDING, APPROVED, REJECTED }

data class Tag(val id: UUID, val name: String)
data class LearningState(val id: UUID,val conceptId: UUID,val stage: Stage,val nextReviewAt: Instant?,val monthlyWrongCount: Int,val hasPathFailure: Boolean,val totalCorrect: Int,val totalWrong: Int,val lastReviewedAt: Instant?)
data class DifficultyState(val id: UUID,val conceptId: UUID,val current: VocabularyDifficulty,val consecutiveCorrect: Int,val consecutiveWrong: Int,val hasReachedVeryHard: Boolean){init{require(consecutiveCorrect>=0&&consecutiveWrong>=0);require(!(consecutiveCorrect>0&&consecutiveWrong>0))}}
data class Content(val id: UUID,val conceptId: UUID,val languageCode: String,val text: String,val canonicalKey: String,val notes: String?=null,val pronunciation: String?=null,val example: String?=null,val translationIndex: Int=0,val grammarNote: String?=null,val possibleCorrection: String?=null)
data class ConceptTag(val conceptId: UUID,val tagId: UUID)
data class Category(val id: UUID,val name: String)
data class Concept(val id: UUID,val entryType: EntryType,val categoryId: UUID?,val favorite: Boolean,val active: Boolean,val createdAt: Instant,val updatedAt: Instant)
data class VocabularyRelation(val id: UUID,val sourceConceptId: UUID,val targetConceptId: UUID?,val relationType: VocabularyRelationType,val unresolvedText: String?=null)
data class VocabularyVariant(val id: UUID,val conceptId: UUID,val text: String,val variantType: VocabularyVariantType)
data class ReviewQueueItem(val id: UUID,val conceptId: UUID?,val sourceText: String,val targetText: String?,val confidence: Double,val possibleCorrection: String?,val status: ReviewQueueStatus,val lineNumber: Int?,val warning: String?)
data class Language(val code: String,val name: String,val active: Boolean=true)
data class LanguagePair(val sourceLanguageCode: String,val targetLanguageCode: String,val active: Boolean=true)
data class ReviewSession(val id: UUID,val startedAt: Instant,val endedAt: Instant?,val reviewType: ReviewType)
data class ReviewHistory(val id: UUID,val sessionId: UUID,val reviewAttemptId: UUID,val conceptId: UUID,val reviewedAt: Instant,val isCorrect: Boolean,val reviewType: ReviewType)
data class TransitionResult(val newStage: Stage,val nextReviewAt: Instant?,val hasPathFailure: Boolean,val monthlyWrongCount: Int)
data class ProgressSummary(val activeConceptCount: Int,val learnedConceptCount: Int,val dueConceptCount: Int,val dailyDueConceptCount: Int,val weeklyDueConceptCount: Int,val monthlyDueConceptCount: Int,val totalCorrect: Int,val totalWrong: Int,val accuracyPercent: Int)
data class ParserMetadata(val breakdown: List<String>=emptyList(),val relationships: List<String>=emptyList(),val variants: List<String>=emptyList(),val confidence: Double=0.0)
