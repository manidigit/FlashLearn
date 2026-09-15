package com.flashlearn.core.di

import com.flashlearn.data.repository.*
import com.flashlearn.data.backup.DataExportRepositoryImpl
import com.flashlearn.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
 @Binds @Singleton abstract fun bindConceptRepository(i:RoomConceptRepository):ConceptRepository
 @Binds @Singleton abstract fun bindContentRepository(i:RoomContentRepository):ContentRepository
 @Binds @Singleton abstract fun bindLearningStateRepository(i:RoomLearningStateRepository):LearningStateRepository
 @Binds @Singleton abstract fun bindDifficultyStateRepository(i:RoomDifficultyStateRepository):DifficultyStateRepository
 @Binds @Singleton abstract fun bindConceptTagRepository(i:RoomConceptTagRepository):ConceptTagRepository
 @Binds @Singleton abstract fun bindTagRepository(i:RoomTagRepository):TagRepository
 @Binds @Singleton abstract fun bindReviewHistoryRepository(i:RoomReviewHistoryRepository):ReviewHistoryRepository
 @Binds @Singleton abstract fun bindReviewSessionRepository(i:RoomReviewSessionRepository):ReviewSessionRepository
 @Binds @Singleton abstract fun bindSettingsRepository(i:RoomSettingsRepository):SettingsRepository
 @Binds @Singleton abstract fun bindCategoryRepository(i:RoomCategoryRepository):CategoryRepository
 @Binds @Singleton abstract fun bindParserMetadataRepository(i:RoomParserMetadataRepository):ParserMetadataRepository
 @Binds @Singleton abstract fun bindAchievementRepository(i:RoomAchievementRepository):AchievementRepository
 @Binds @Singleton abstract fun bindBackupRepository(i:com.flashlearn.data.backup.RoomBackupRepository):BackupRepository
 @Binds @Singleton abstract fun bindFlashLearnDatabase(i:FlashLearnDatabaseImpl):FlashLearnDatabase
 @Binds @Singleton abstract fun bindDataVersionRepository(i:RoomDataVersionRepository):DataVersionRepository
 @Binds @Singleton abstract fun bindVocabularyRelationRepository(i:RoomVocabularyRelationRepository):VocabularyRelationRepository
 @Binds @Singleton abstract fun bindVocabularyVariantRepository(i:RoomVocabularyVariantRepository):VocabularyVariantRepository
 @Binds @Singleton abstract fun bindReviewQueueRepository(i:RoomReviewQueueRepository):ReviewQueueRepository
 @Binds @Singleton abstract fun bindLanguageRepository(i:RoomLanguageRepository):LanguageRepository
 @Binds @Singleton abstract fun bindDataExportRepository(i:DataExportRepositoryImpl):DataExportRepository
}
