package com.flashlearn.database

import androidx.room.*
import java.time.Instant
import java.util.UUID

class Converters {
    @TypeConverter fun fromUuid(value: UUID?): String? = value?.toString()
    @TypeConverter fun toUuid(value: String?): UUID? = value?.let(UUID::fromString)
    @TypeConverter fun fromInstant(value: Instant?): Long? = value?.toEpochMilli()
    @TypeConverter fun toInstant(value: Long?): Instant? = value?.let(Instant::ofEpochMilli)
}

@Entity(tableName = "concepts") data class ConceptEntity(@PrimaryKey val id: UUID, val entryType: String, val categoryId: UUID?, val favorite: Boolean, val active: Boolean, val createdAt: Instant, val updatedAt: Instant)
@Entity(tableName = "contents", indices = [Index(value = ["conceptId", "languageCode"], unique = true), Index(value = ["languageCode", "canonicalKey"])]) data class ContentEntity(@PrimaryKey val id: UUID, val conceptId: UUID, val languageCode: String, val text: String, val canonicalKey: String, val notes: String?, val pronunciation: String?, val example: String?)
@Entity(tableName = "learning_states", indices = [Index(value = ["conceptId"], unique = true), Index(value = ["stage", "nextReviewAt"])]) data class LearningStateEntity(@PrimaryKey val id: UUID, val conceptId: UUID, val stage: String, val nextReviewAt: Instant?, val monthlyWrongCount: Int, val hasPathFailure: Boolean, val totalCorrect: Int, val totalWrong: Int, val lastReviewedAt: Instant?)
@Entity(tableName = "difficulty_states", indices = [Index(value = ["conceptId"], unique = true)]) data class DifficultyStateEntity(@PrimaryKey val id: UUID, val conceptId: UUID, val current: String, val consecutiveCorrect: Int, val consecutiveWrong: Int, val hasReachedVeryHard: Boolean)
@Entity(tableName = "tags") data class TagEntity(@PrimaryKey val id: UUID, val name: String)
@Entity(tableName = "concept_tags", primaryKeys = ["conceptId", "tagId"], indices = [Index(value = ["tagId", "conceptId"])]) data class ConceptTagEntity(val conceptId: UUID, val tagId: UUID)
@Entity(tableName = "review_sessions") data class ReviewSessionEntity(@PrimaryKey val id: UUID, val startedAt: Instant, val endedAt: Instant?, val reviewType: String)
@Entity(tableName = "review_history", indices = [Index(value = ["sessionId", "reviewAttemptId"], unique = true)]) data class ReviewHistoryEntity(@PrimaryKey val id: UUID, val sessionId: UUID, val reviewAttemptId: UUID, val conceptId: UUID, val reviewedAt: Instant, val isCorrect: Boolean, val reviewType: String)
@Entity(tableName = "settings") data class SettingsEntity(@PrimaryKey val key: String, val value: String, val updatedAt: Instant)
@Entity(tableName = "categories", indices = [Index(value = ["name"], unique = true)]) data class CategoryEntity(@PrimaryKey val id: UUID, val name: String)
@Entity(tableName = "parser_metadata") data class ParserMetadataEntity(@PrimaryKey val conceptId: UUID, val breakdownJson: String, val relationshipsJson: String, val variantsJson: String, val confidence: Double)
@Entity(tableName = "achievements") data class AchievementEntity(@PrimaryKey val achievementId: String, val unlocked: Boolean)

@Dao interface ConceptDao {
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insert(entity: ConceptEntity)
    @Update suspend fun update(entity: ConceptEntity)
    @Query("SELECT * FROM concepts WHERE id = :id AND active = 1 LIMIT 1") suspend fun getById(id: UUID): ConceptEntity?
    @Query("SELECT * FROM concepts WHERE active = 1") suspend fun getAllActive(): List<ConceptEntity>
    @Query("SELECT DISTINCT c.* FROM concepts c LEFT JOIN contents x ON x.conceptId = c.id WHERE c.active = 1 AND (x.text LIKE :query OR x.canonicalKey LIKE :query) ORDER BY c.createdAt DESC") suspend fun searchActive(query: String): List<ConceptEntity>
    @Query("SELECT * FROM concepts") suspend fun getAll(): List<ConceptEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(entities: List<ConceptEntity>)
    @Query("UPDATE concepts SET active = 0, updatedAt = :now WHERE id = :id") suspend fun softDelete(id: UUID, now: Instant)
    @Query("DELETE FROM concepts") suspend fun deleteAll()
}
@Dao interface ContentDao {
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insert(entity: ContentEntity)
    @Update suspend fun update(entity: ContentEntity)
    @Query("SELECT * FROM contents WHERE id = :id LIMIT 1") suspend fun getById(id: UUID): ContentEntity?
    @Query("SELECT * FROM contents WHERE conceptId = :conceptId AND languageCode = :languageCode LIMIT 1") suspend fun getByConceptIdAndLanguage(conceptId: UUID, languageCode: String): ContentEntity?
    @Query("SELECT * FROM contents WHERE conceptId = :conceptId") suspend fun getAllByConceptId(conceptId: UUID): List<ContentEntity>
    @Query("SELECT * FROM contents WHERE languageCode = :languageCode AND canonicalKey = :canonicalKey") suspend fun findByCanonicalKey(languageCode: String, canonicalKey: String): List<ContentEntity>
    @Query("SELECT * FROM contents") suspend fun getAll(): List<ContentEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(entities: List<ContentEntity>)
    @Query("DELETE FROM contents") suspend fun deleteAll()
}
@Dao interface LearningStateDao {
    @Query("SELECT * FROM learning_states WHERE conceptId = :conceptId LIMIT 1") suspend fun getByConceptId(conceptId: UUID): LearningStateEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(entity: LearningStateEntity)
    @Query("SELECT * FROM learning_states WHERE stage = :stage ORDER BY nextReviewAt ASC, conceptId ASC") suspend fun getAllByStage(stage: String): List<LearningStateEntity>
    @Query("SELECT * FROM learning_states WHERE stage = :stage AND nextReviewAt IS NOT NULL AND nextReviewAt <= :now ORDER BY nextReviewAt ASC, conceptId ASC") suspend fun getDueByStage(stage: String, now: Instant): List<LearningStateEntity>
    @Query("SELECT * FROM learning_states WHERE stage IN ('DAILY','WEEKLY','MONTHLY') AND nextReviewAt IS NOT NULL AND nextReviewAt <= :now ORDER BY nextReviewAt ASC, conceptId ASC") suspend fun getAllDueNonLearned(now: Instant): List<LearningStateEntity>
    @Query("SELECT * FROM learning_states ORDER BY conceptId ASC") suspend fun getAll(): List<LearningStateEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertAll(entities: List<LearningStateEntity>)
    @Query("DELETE FROM learning_states") suspend fun deleteAll()
}
@Dao interface DifficultyStateDao {
    @Query("SELECT * FROM difficulty_states WHERE conceptId = :conceptId LIMIT 1") suspend fun getByConceptId(conceptId: UUID): DifficultyStateEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(entity: DifficultyStateEntity)
    @Query("DELETE FROM difficulty_states WHERE conceptId = :conceptId") suspend fun deleteByConceptId(conceptId: UUID)
    @Query("SELECT * FROM difficulty_states ORDER BY conceptId ASC") suspend fun getAll(): List<DifficultyStateEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertAll(entities: List<DifficultyStateEntity>)
    @Query("DELETE FROM difficulty_states") suspend fun deleteAll()
}
@Dao interface TagDao {
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insert(entity: TagEntity)
    @Query("SELECT * FROM tags WHERE id = :id LIMIT 1") suspend fun getById(id: UUID): TagEntity?
    @Query("SELECT * FROM tags") suspend fun getAll(): List<TagEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(entities: List<TagEntity>)
    @Query("DELETE FROM tags") suspend fun deleteAll()
}
@Dao interface ConceptTagDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insert(entity: ConceptTagEntity)
    @Query("SELECT tagId FROM concept_tags WHERE conceptId = :conceptId") suspend fun getTagIdsForConcept(conceptId: UUID): List<UUID>
    @Query("SELECT conceptId FROM concept_tags WHERE tagId = :tagId") suspend fun getConceptIdsForTag(tagId: UUID): List<UUID>
    @Query("SELECT * FROM concept_tags") suspend fun getAll(): List<ConceptTagEntity>
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertAll(entities: List<ConceptTagEntity>)
    @Query("DELETE FROM concept_tags") suspend fun deleteAll()
}
@Dao interface ReviewSessionDao {
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insert(entity: ReviewSessionEntity)
    @Update suspend fun update(entity: ReviewSessionEntity)
    @Query("SELECT * FROM review_sessions WHERE id = :id LIMIT 1") suspend fun getById(id: UUID): ReviewSessionEntity?
    @Query("SELECT * FROM review_sessions") suspend fun getAll(): List<ReviewSessionEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(entities: List<ReviewSessionEntity>)
    @Query("DELETE FROM review_sessions") suspend fun deleteAll()
}
@Dao interface ReviewHistoryDao {
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insert(entity: ReviewHistoryEntity)
    @Query("SELECT * FROM review_history WHERE conceptId = :conceptId") suspend fun getByConceptId(conceptId: UUID): List<ReviewHistoryEntity>
    @Query("SELECT * FROM review_history WHERE sessionId = :sessionId") suspend fun getBySessionId(sessionId: UUID): List<ReviewHistoryEntity>
    @Query("SELECT EXISTS(SELECT 1 FROM review_history WHERE sessionId = :sessionId AND reviewAttemptId = :attemptId)") suspend fun existsByAttemptId(sessionId: UUID, attemptId: UUID): Boolean
    @Query("SELECT DISTINCT conceptId FROM review_history") suspend fun getDistinctConceptIds(): List<UUID>
    @Query("SELECT * FROM review_history ORDER BY reviewedAt ASC, id ASC") suspend fun getAll(): List<ReviewHistoryEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(entities: List<ReviewHistoryEntity>)
    @Query("DELETE FROM review_history") suspend fun deleteAll()
}
@Dao interface SettingsDao {
    @Query("SELECT * FROM settings WHERE key = :key LIMIT 1") suspend fun getByKey(key: String): SettingsEntity?
    @Query("SELECT * FROM settings") suspend fun getAll(): List<SettingsEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun putAll(entities: List<SettingsEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun put(entity: SettingsEntity)
    @Query("DELETE FROM settings") suspend fun deleteAll()
}
@Dao interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insert(entity: CategoryEntity)
    @Query("SELECT * FROM categories ORDER BY name ASC") suspend fun getAll(): List<CategoryEntity>
    @Query("SELECT * FROM categories WHERE name = :name COLLATE NOCASE LIMIT 1") suspend fun findByName(name: String): CategoryEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(entities: List<CategoryEntity>)
    @Query("DELETE FROM categories") suspend fun deleteAll()
}
@Dao interface ParserMetadataDao {
    @Query("SELECT * FROM parser_metadata WHERE conceptId = :conceptId LIMIT 1") suspend fun getByConceptId(conceptId: UUID): ParserMetadataEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(entity: ParserMetadataEntity)
    @Query("SELECT * FROM parser_metadata ORDER BY conceptId ASC") suspend fun getAll(): List<ParserMetadataEntity>
    @Query("DELETE FROM parser_metadata") suspend fun deleteAll()
    @Query("DELETE FROM parser_metadata WHERE conceptId = :conceptId") suspend fun deleteByConceptId(conceptId: UUID)
}
@Dao interface AchievementDao {
    @Query("SELECT * FROM achievements ORDER BY achievementId ASC") suspend fun getAll(): List<AchievementEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertAll(entities: List<AchievementEntity>)
}

@Database(entities = [ConceptEntity::class, ContentEntity::class, LearningStateEntity::class, DifficultyStateEntity::class, TagEntity::class, ConceptTagEntity::class, ReviewSessionEntity::class, ReviewHistoryEntity::class, SettingsEntity::class, CategoryEntity::class, ParserMetadataEntity::class, AchievementEntity::class], version = 5, exportSchema = true)
@TypeConverters(Converters::class)
abstract class RoomFlashLearnDatabase : RoomDatabase() {
    abstract fun conceptDao(): ConceptDao
    abstract fun contentDao(): ContentDao
    abstract fun learningStateDao(): LearningStateDao
    abstract fun difficultyStateDao(): DifficultyStateDao
    abstract fun tagDao(): TagDao
    abstract fun conceptTagDao(): ConceptTagDao
    abstract fun reviewSessionDao(): ReviewSessionDao
    abstract fun reviewHistoryDao(): ReviewHistoryDao
    abstract fun settingsDao(): SettingsDao
    abstract fun categoryDao(): CategoryDao
    abstract fun parserMetadataDao(): ParserMetadataDao
    abstract fun achievementDao(): AchievementDao
}
