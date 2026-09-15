package com.flashlearn.data.repository

import com.flashlearn.database.*
import com.flashlearn.domain.model.*
import com.flashlearn.domain.repository.*
import java.util.UUID
import org.json.JSONArray
import javax.inject.Inject

object Mappers {
    fun concept(e: ConceptEntity)=Concept(e.id,EntryType.valueOf(e.entryType),e.categoryId,e.favorite,e.active,e.createdAt,e.updatedAt)
    fun concept(e: Concept)=ConceptEntity(e.id,e.entryType.name,e.categoryId,e.favorite,e.active,e.createdAt,e.updatedAt)
    fun content(e: ContentEntity)=Content(e.id,e.conceptId,e.languageCode,e.text,e.canonicalKey,e.notes,e.pronunciation,e.example,e.translationIndex,e.grammarNote,e.possibleCorrection)
    fun content(e: Content)=ContentEntity(e.id,e.conceptId,e.languageCode,e.text,e.canonicalKey,e.notes,e.pronunciation,e.example,e.translationIndex,e.grammarNote,e.possibleCorrection)
    fun learning(e: LearningStateEntity)=LearningState(e.id,e.conceptId,Stage.valueOf(e.stage),e.nextReviewAt,e.monthlyWrongCount,e.hasPathFailure,e.totalCorrect,e.totalWrong,e.lastReviewedAt)
    fun learning(e: LearningState)=LearningStateEntity(e.id,e.conceptId,e.stage.name,e.nextReviewAt,e.monthlyWrongCount,e.hasPathFailure,e.totalCorrect,e.totalWrong,e.lastReviewedAt)
    fun difficulty(e: DifficultyStateEntity)=DifficultyState(e.id,e.conceptId,VocabularyDifficulty.valueOf(e.current),e.consecutiveCorrect,e.consecutiveWrong,e.hasReachedVeryHard)
    fun difficulty(e: DifficultyState)=DifficultyStateEntity(e.id,e.conceptId,e.current.name,e.consecutiveCorrect,e.consecutiveWrong,e.hasReachedVeryHard)
    fun tag(e: ConceptTag)=ConceptTagEntity(e.conceptId,e.tagId)
    fun tag(e: ConceptTagEntity)=ConceptTag(e.conceptId,e.tagId)
    fun history(e: ReviewHistory)=ReviewHistoryEntity(e.id,e.sessionId,e.reviewAttemptId,e.conceptId,e.reviewedAt,e.isCorrect,e.reviewType.name)
    fun history(e: ReviewHistoryEntity)=ReviewHistory(e.id,e.sessionId,e.reviewAttemptId,e.conceptId,e.reviewedAt,e.isCorrect,ReviewType.valueOf(e.reviewType))
    fun session(e: ReviewSession)=ReviewSessionEntity(e.id,e.startedAt,e.endedAt,e.reviewType.name)
    fun session(e: ReviewSessionEntity)=ReviewSession(e.id,e.startedAt,e.endedAt,ReviewType.valueOf(e.reviewType))
    fun category(e: CategoryEntity)=Category(e.id,e.name)
    fun category(e: Category)=CategoryEntity(e.id,e.name)
    fun parserMetadata(e: ParserMetadataEntity): ParserMetadata = ParserMetadata(
        breakdown = JSONArray(e.breakdownJson).let { a -> (0 until a.length()).map { a.getString(it) } },
        relationships = JSONArray(e.relationshipsJson).let { a -> (0 until a.length()).map { a.getString(it) } },
        variants = JSONArray(e.variantsJson).let { a -> (0 until a.length()).map { a.getString(it) } },
        confidence = e.confidence
    )
    fun parserMetadata(conceptId: UUID, value: ParserMetadata): ParserMetadataEntity = ParserMetadataEntity(
        conceptId,
        JSONArray().apply { value.breakdown.forEach(::put) }.toString(),
        JSONArray().apply { value.relationships.forEach(::put) }.toString(),
        JSONArray().apply { value.variants.forEach(::put) }.toString(),
        value.confidence.coerceIn(0.0, 1.0)
    )
    fun relation(e: VocabularyRelationEntity)=VocabularyRelation(UUID.fromString(e.id.toString()),e.sourceConceptId,e.targetConceptId,VocabularyRelationType.valueOf(e.relationType),e.unresolvedText)
    fun relation(e: VocabularyRelation)=VocabularyRelationEntity(e.id,e.sourceConceptId,e.targetConceptId,e.relationType.name,e.unresolvedText)
    fun variant(e: VocabularyVariantEntity)=VocabularyVariant(e.id,e.conceptId,e.text,VocabularyVariantType.valueOf(e.variantType))
    fun variant(e: VocabularyVariant)=VocabularyVariantEntity(e.id,e.conceptId,e.text,e.variantType.name)
    fun reviewQueue(e: ReviewQueueEntity)=ReviewQueueItem(e.id,e.conceptId,e.sourceText,e.targetText,e.confidence,e.possibleCorrection,ReviewQueueStatus.valueOf(e.status),e.lineNumber,e.warning)
    fun reviewQueue(e: ReviewQueueItem)=ReviewQueueEntity(e.id,e.conceptId,e.sourceText,e.targetText,e.confidence.coerceIn(0.0,1.0),e.possibleCorrection,e.status.name,e.lineNumber,e.warning)
    fun language(e: LanguageEntity)=Language(e.code,e.name,e.active)
    fun language(e: Language)=LanguageEntity(e.code,e.name,e.active)
    fun languagePair(e: LanguagePairEntity)=LanguagePair(e.sourceLanguageCode,e.targetLanguageCode,e.active)
    fun languagePair(e: LanguagePair)=LanguagePairEntity(e.sourceLanguageCode,e.targetLanguageCode,e.active)
}
class RoomConceptRepository @Inject constructor(private val dao: ConceptDao): ConceptRepository {
    override suspend fun insert(concept: Concept): UUID { dao.insert(Mappers.concept(concept)); return concept.id }
    override suspend fun get(conceptId: UUID)=dao.getById(conceptId)?.let(Mappers::concept)
    override suspend fun getAllActive()=dao.getAllActive().map(Mappers::concept)
    override suspend fun searchActive(query: String)=dao.searchActive(query).map(Mappers::concept)
    override suspend fun update(concept: Concept)=dao.update(Mappers.concept(concept))
    override suspend fun softDelete(conceptId: UUID, now: java.time.Instant)=dao.softDelete(conceptId, now)
}
class RoomContentRepository @Inject constructor(private val dao: ContentDao): ContentRepository {
    override suspend fun findByUuid(uuid: UUID)=dao.getById(uuid)?.let(Mappers::content)
    override suspend fun find(conceptId: UUID, languageCode: String)=dao.getByConceptIdAndLanguage(conceptId,languageCode)?.let(Mappers::content)
    override suspend fun findAll(conceptId: UUID, languageCode: String)=dao.getAllByConceptIdAndLanguage(conceptId,languageCode).map(Mappers::content)
    override suspend fun findForConcepts(conceptIds: List<UUID>): List<Content> {
        if (conceptIds.isEmpty()) return emptyList()
        return conceptIds.distinct().chunked(500).flatMap { ids -> dao.getForConcepts(ids).map(Mappers::content) }
    }
    override suspend fun getAll()=dao.getAll().map(Mappers::content)
    override suspend fun upsert(content: Content) {
        val existing=dao.getById(content.id)
        if(existing==null) dao.insert(Mappers.content(content)) else dao.update(Mappers.content(content))
    }
    override suspend fun insertTranslation(content: Content) = dao.insert(Mappers.content(content))
}
class RoomLearningStateRepository @Inject constructor(private val dao: LearningStateDao): LearningStateRepository {
    override suspend fun get(conceptId: UUID)=dao.getByConceptId(conceptId)?.let(Mappers::learning)
    override suspend fun upsert(state: LearningState)=dao.upsert(Mappers.learning(state))
    override suspend fun getAllByStage(stage: Stage)=dao.getAllByStage(stage.name).map(Mappers::learning)
    override suspend fun getDueNonLearned(now: java.time.Instant)=dao.getAllDueNonLearned(now).map(Mappers::learning)
    override suspend fun getAll()=dao.getAll().map(Mappers::learning)
}
class RoomDifficultyStateRepository @Inject constructor(private val dao: DifficultyStateDao): DifficultyStateRepository {
    override suspend fun get(conceptId: UUID)=dao.getByConceptId(conceptId)?.let(Mappers::difficulty)
    override suspend fun upsert(state: DifficultyState)=dao.upsert(Mappers.difficulty(state))
    override suspend fun delete(conceptId: UUID)=dao.deleteByConceptId(conceptId)
    override suspend fun getAll()=dao.getAll().map(Mappers::difficulty)
}
class RoomParserMetadataRepository @Inject constructor(private val dao: ParserMetadataDao): ParserMetadataRepository {
    override suspend fun get(conceptId: UUID)=dao.getByConceptId(conceptId)?.let(Mappers::parserMetadata)
    override suspend fun upsert(conceptId: UUID, metadata: ParserMetadata)=dao.upsert(Mappers.parserMetadata(conceptId, metadata))
    override suspend fun getAll(): List<Pair<UUID, ParserMetadata>> = dao.getAll().map { it.conceptId to Mappers.parserMetadata(it) }
}
class RoomConceptTagRepository @Inject constructor(private val dao: ConceptTagDao): ConceptTagRepository {
    override suspend fun insert(conceptTag: ConceptTag)=dao.insert(Mappers.tag(conceptTag))
    override suspend fun getTagsForConcept(conceptId: UUID)=dao.getTagIdsForConcept(conceptId)
    override suspend fun getConceptsForTag(tagId: UUID)=dao.getConceptIdsForTag(tagId)
    override suspend fun getAll(): List<ConceptTag> = dao.getAll().map(Mappers::tag)
}
class RoomReviewHistoryRepository @Inject constructor(private val dao: ReviewHistoryDao): ReviewHistoryRepository {
    override suspend fun insert(entry: ReviewHistory)=dao.insert(Mappers.history(entry))
    override suspend fun existsByAttemptId(sessionId: UUID, reviewAttemptId: UUID)=dao.existsByAttemptId(sessionId,reviewAttemptId)
    override suspend fun getAll(): List<ReviewHistory> = dao.getAll().map(Mappers::history)
}
class RoomSettingsRepository @Inject constructor(private val dao: SettingsDao): SettingsRepository {
    override suspend fun getInt(key: String, default: Int): Int=dao.getByKey(key)?.value?.toIntOrNull()?:default
    suspend fun setInt(key: String, value: Int) { dao.put(SettingsEntity(key, value.toString(), java.time.Instant.now())) }
}
class RoomDataVersionRepository @Inject constructor(private val dao: SettingsDao): DataVersionRepository {
    override suspend fun getConceptDataVersion(): Int = dao.getByKey(KEY_CONCEPT_DATA_VERSION)?.value?.toIntOrNull() ?: 0
    override suspend fun getContentDataVersion(): Int = dao.getByKey(KEY_CONTENT_DATA_VERSION)?.value?.toIntOrNull() ?: 0
    override suspend fun setConceptDataVersion(version: Int) { dao.put(SettingsEntity(KEY_CONCEPT_DATA_VERSION, version.toString(), java.time.Instant.now())) }
    override suspend fun setContentDataVersion(version: Int) { dao.put(SettingsEntity(KEY_CONTENT_DATA_VERSION, version.toString(), java.time.Instant.now())) }
    private companion object { const val KEY_CONCEPT_DATA_VERSION = "data_version.concept"; const val KEY_CONTENT_DATA_VERSION = "data_version.content" }
}
class RoomReviewSessionRepository @Inject constructor(private val dao: ReviewSessionDao): ReviewSessionRepository {
    override suspend fun insert(session: ReviewSession) = dao.insert(Mappers.session(session))
    override suspend fun get(sessionId: UUID) = dao.getById(sessionId)?.let(Mappers::session)
    override suspend fun update(session: ReviewSession) = dao.update(Mappers.session(session))
}
class RoomCategoryRepository @Inject constructor(private val dao: CategoryDao): CategoryRepository {
    override suspend fun getAll() = dao.getAll().map(Mappers::category)
    override suspend fun findByName(name: String) = dao.findByName(name)?.let(Mappers::category)
    override suspend fun insert(category: Category): UUID { dao.insert(Mappers.category(category)); return category.id }
}
class RoomVocabularyRelationRepository @Inject constructor(private val dao: VocabularyRelationDao): VocabularyRelationRepository {
    override suspend fun insert(value: VocabularyRelation)=dao.insert(Mappers.relation(value))
    override suspend fun getForConcept(conceptId: UUID)=dao.getForConcept(conceptId).map(Mappers::relation)
    override suspend fun getAll()=dao.getAll().map(Mappers::relation)
}
class RoomVocabularyVariantRepository @Inject constructor(private val dao: VocabularyVariantDao): VocabularyVariantRepository {
    override suspend fun insert(value: VocabularyVariant)=dao.insert(Mappers.variant(value))
    override suspend fun getForConcept(conceptId: UUID)=dao.getForConcept(conceptId).map(Mappers::variant)
    override suspend fun getAll()=dao.getAll().map(Mappers::variant)
}
class RoomReviewQueueRepository @Inject constructor(private val dao: ReviewQueueDao): ReviewQueueRepository {
    override suspend fun upsert(value: ReviewQueueItem)=dao.upsert(Mappers.reviewQueue(value))
    override suspend fun getPending()=dao.getPending().map(Mappers::reviewQueue)
    override suspend fun getAll()=dao.getAll().map(Mappers::reviewQueue)
    override suspend fun update(value: ReviewQueueItem)=dao.update(Mappers.reviewQueue(value))
}
class RoomLanguageRepository @Inject constructor(private val dao: LanguageDao, private val pairDao: LanguagePairDao): LanguageRepository {
    override suspend fun getAll()=dao.getAll().map(Mappers::language)
    override suspend fun getActive()=dao.getActive().map(Mappers::language)
    override suspend fun getPairs()=pairDao.getAll().map(Mappers::languagePair)
    override suspend fun getActivePairs()=pairDao.getActive().map(Mappers::languagePair)
    override suspend fun upsert(value: Language)=dao.upsert(Mappers.language(value))
    override suspend fun upsertPair(value: LanguagePair)=pairDao.upsert(Mappers.languagePair(value))
}
