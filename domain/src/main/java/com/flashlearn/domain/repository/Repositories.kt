package com.flashlearn.domain.repository

import com.flashlearn.domain.model.*
import com.flashlearn.domain.gamification.AchievementState
import java.io.File
import java.util.UUID

interface LearningStateRepository { suspend fun get(conceptId:UUID):LearningState?;suspend fun upsert(state:LearningState);suspend fun getAllByStage(stage:Stage):List<LearningState>;suspend fun getDueNonLearned(now:java.time.Instant):List<LearningState>;suspend fun getAll():List<LearningState> }
interface DifficultyStateRepository { suspend fun get(conceptId:UUID):DifficultyState?;suspend fun upsert(state:DifficultyState);suspend fun delete(conceptId:UUID);suspend fun getAll():List<DifficultyState> }
interface ContentRepository { suspend fun findByUuid(uuid:UUID):Content?;suspend fun find(conceptId:UUID,languageCode:String):Content?;suspend fun findAll(conceptId:UUID,languageCode:String):List<Content> = find(conceptId,languageCode)?.let{listOf(it)}?:emptyList();suspend fun findForConcepts(conceptIds:List<UUID>):List<Content> = getAll().filter{it.conceptId in conceptIds.toSet()};suspend fun upsert(content:Content);suspend fun insertTranslation(content:Content)=upsert(content);suspend fun getAll():List<Content> }
interface ConceptTagRepository { suspend fun insert(conceptTag:ConceptTag);suspend fun getTagsForConcept(conceptId:UUID):List<UUID>;suspend fun getConceptsForTag(tagId:UUID):List<UUID>;suspend fun getAll():List<ConceptTag> = emptyList() }
interface TagRepository { suspend fun getAll():List<Tag>;suspend fun insert(tag:Tag):UUID;suspend fun update(tag:Tag);suspend fun delete(id:UUID) }
interface ConceptRepository { suspend fun insert(concept:Concept):UUID;suspend fun get(conceptId:UUID):Concept?;suspend fun getAllActive():List<Concept>;suspend fun searchActive(query:String):List<Concept>;suspend fun update(concept:Concept);suspend fun softDelete(conceptId:UUID,now:java.time.Instant) }
interface ReviewHistoryRepository { suspend fun insert(entry:ReviewHistory);suspend fun existsByAttemptId(sessionId:UUID,reviewAttemptId:UUID):Boolean;suspend fun getAll():List<ReviewHistory> }
interface SettingsRepository { suspend fun getInt(key:String,default:Int):Int; suspend fun getString(key:String,default:String):String }
interface CategoryRepository { suspend fun getAll():List<Category>;suspend fun findByName(name:String):Category?;suspend fun insert(category:Category):UUID }
interface FlashLearnDatabase { suspend fun <T> withTransaction(block:suspend()->T):T }
interface ReviewSessionRepository { suspend fun insert(session:ReviewSession);suspend fun get(sessionId:UUID):ReviewSession?;suspend fun update(session:ReviewSession) }
interface ParserMetadataRepository { suspend fun get(conceptId:UUID):ParserMetadata?;suspend fun upsert(conceptId:UUID,metadata:ParserMetadata);suspend fun getAll():List<Pair<UUID,ParserMetadata>> }
interface AchievementRepository { suspend fun getAll():List<AchievementState>;suspend fun upsertAll(states:List<AchievementState>) }
interface DataVersionRepository { suspend fun getConceptDataVersion():Int;suspend fun getContentDataVersion():Int;suspend fun setConceptDataVersion(version:Int);suspend fun setContentDataVersion(version:Int) }
interface VocabularyRelationRepository { suspend fun insert(value:VocabularyRelation);suspend fun getForConcept(conceptId:UUID):List<VocabularyRelation>;suspend fun getAll():List<VocabularyRelation> }
interface VocabularyVariantRepository { suspend fun insert(value:VocabularyVariant);suspend fun getForConcept(conceptId:UUID):List<VocabularyVariant>;suspend fun getAll():List<VocabularyVariant> }
interface ReviewQueueRepository { suspend fun upsert(value:ReviewQueueItem);suspend fun getPending():List<ReviewQueueItem>;suspend fun getAll():List<ReviewQueueItem>;suspend fun update(value:ReviewQueueItem) }
interface LanguageRepository { suspend fun getAll():List<Language>;suspend fun getActive():List<Language>;suspend fun getPairs():List<LanguagePair>;suspend fun getActivePairs():List<LanguagePair>;suspend fun upsert(value:Language);suspend fun upsertPair(value:LanguagePair) }
enum class ExportFormat { CSV, JSON, XLSX, SQLITE }
interface DataExportRepository { suspend fun export(format:ExportFormat):File }
