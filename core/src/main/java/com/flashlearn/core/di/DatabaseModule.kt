package com.flashlearn.core.di

import android.content.Context
import androidx.room.Room
import com.flashlearn.database.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton
    fun provideRoomDatabase(@ApplicationContext context: Context): RoomFlashLearnDatabase =
        Room.databaseBuilder(context, RoomFlashLearnDatabase::class.java, "flashlearn.db")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
            .build()
    @Provides fun categoryDao(db: RoomFlashLearnDatabase)=db.categoryDao()
    @Provides fun conceptDao(db: RoomFlashLearnDatabase)=db.conceptDao()
    @Provides fun contentDao(db: RoomFlashLearnDatabase)=db.contentDao()
    @Provides fun learningStateDao(db: RoomFlashLearnDatabase)=db.learningStateDao()
    @Provides fun difficultyStateDao(db: RoomFlashLearnDatabase)=db.difficultyStateDao()
    @Provides fun tagDao(db: RoomFlashLearnDatabase)=db.tagDao()
    @Provides fun conceptTagDao(db: RoomFlashLearnDatabase)=db.conceptTagDao()
    @Provides fun reviewSessionDao(db: RoomFlashLearnDatabase)=db.reviewSessionDao()
    @Provides fun reviewHistoryDao(db: RoomFlashLearnDatabase)=db.reviewHistoryDao()
    @Provides fun settingsDao(db: RoomFlashLearnDatabase)=db.settingsDao()
    @Provides fun parserMetadataDao(db: RoomFlashLearnDatabase)=db.parserMetadataDao()
    @Provides fun achievementDao(db: RoomFlashLearnDatabase)=db.achievementDao()
}
