package com.flashlearn.core.di

import com.flashlearn.data.repository.*
import com.flashlearn.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton abstract fun bindConceptRepository(impl: RoomConceptRepository): ConceptRepository
    @Binds @Singleton abstract fun bindContentRepository(impl: RoomContentRepository): ContentRepository
    @Binds @Singleton abstract fun bindLearningStateRepository(impl: RoomLearningStateRepository): LearningStateRepository
    @Binds @Singleton abstract fun bindDifficultyStateRepository(impl: RoomDifficultyStateRepository): DifficultyStateRepository
    @Binds @Singleton abstract fun bindConceptTagRepository(impl: RoomConceptTagRepository): ConceptTagRepository
    @Binds @Singleton abstract fun bindReviewHistoryRepository(impl: RoomReviewHistoryRepository): ReviewHistoryRepository
    @Binds @Singleton abstract fun bindReviewSessionRepository(impl: RoomReviewSessionRepository): ReviewSessionRepository
    @Binds @Singleton abstract fun bindSettingsRepository(impl: RoomSettingsRepository): SettingsRepository
    @Binds @Singleton abstract fun bindCategoryRepository(impl: RoomCategoryRepository): CategoryRepository
    @Binds @Singleton abstract fun bindParserMetadataRepository(impl: RoomParserMetadataRepository): ParserMetadataRepository
    @Binds @Singleton abstract fun bindAchievementRepository(impl: RoomAchievementRepository): AchievementRepository
    @Binds @Singleton abstract fun bindBackupRepository(impl: com.flashlearn.data.backup.RoomBackupRepository): BackupRepository
    @Binds @Singleton abstract fun bindFlashLearnDatabase(impl: FlashLearnDatabaseImpl): FlashLearnDatabase
    @Binds @Singleton abstract fun bindDataVersionRepository(impl: RoomDataVersionRepository): DataVersionRepository
    @Binds @Singleton abstract fun bindVocabularyRelationRepository(impl: RoomVocabularyRelationRepository): VocabularyRelationRepository
    @Binds @Singleton abstract fun bindVocabularyVariantRepository(impl: RoomVocabularyVariantRepository): VocabularyVariantRepository
    @Binds @Singleton abstract fun bindReviewQueueRepository(impl: RoomReviewQueueRepository): ReviewQueueRepository
    @Binds @Singleton abstract fun bindLanguageRepository(impl: RoomLanguageRepository): LanguageRepository
}
