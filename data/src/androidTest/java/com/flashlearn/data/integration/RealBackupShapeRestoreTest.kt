package com.flashlearn.data.integration

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.flashlearn.data.backup.RoomBackupRepository
import com.flashlearn.data.repository.*
import com.flashlearn.database.CategoryEntity
import com.flashlearn.database.ReviewHistoryEntity
import com.flashlearn.database.ReviewSessionEntity
import com.flashlearn.database.RoomFlashLearnDatabase
import com.flashlearn.domain.usecase.CreateConceptCommand
import com.flashlearn.domain.usecase.CreateConceptUseCase
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class RealBackupShapeRestoreTest {
    private lateinit var sourceDb: RoomFlashLearnDatabase
    private lateinit var sourceBackup: RoomBackupRepository
    private lateinit var createConcept: CreateConceptUseCase

    @Before fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        sourceDb = Room.inMemoryDatabaseBuilder(context, RoomFlashLearnDatabase::class.java)
            .allowMainThreadQueries().build()
        sourceBackup = RoomBackupRepository(sourceDb, context)
        val conceptRepo = RoomConceptRepository(sourceDb.conceptDao())
        val contentRepo = RoomContentRepository(sourceDb.contentDao())
        val learningRepo = RoomLearningStateRepository(sourceDb.learningStateDao())
        val difficultyRepo = RoomDifficultyStateRepository(sourceDb.difficultyStateDao())
        val tagRepo = RoomConceptTagRepository(sourceDb.conceptTagDao())
        createConcept = CreateConceptUseCase(
            conceptRepo, contentRepo, learningRepo, difficultyRepo, tagRepo,
            FlashLearnDatabaseImpl(sourceDb)
        )
    }

    @After fun tearDown() = sourceDb.close()

    @Test fun restoresActualSchema2FullBackupShapeIntoEmptyDatabase() = runBlocking {
        val categoryId = UUID.randomUUID()
        sourceDb.categoryDao().insert(CategoryEntity(categoryId, "Real backup category"))
        val conceptId = createConcept(CreateConceptCommand(
            "buenos días", "صبح بخیر", categoryId = categoryId
        ))
        val sessionId = UUID.randomUUID()
        val started = Instant.parse("2026-09-16T05:33:00Z")
        val reviewed = Instant.parse("2026-09-16T05:33:20Z")
        sourceDb.reviewSessionDao().insert(ReviewSessionEntity(sessionId, started, reviewed, "RANDOM"))
        sourceDb.reviewHistoryDao().insert(ReviewHistoryEntity(
            UUID.randomUUID(), sessionId, UUID.randomUUID(), conceptId, reviewed, true, "RANDOM"
        ))

        // This is the exact section profile of the supplied production backup:
        // schemaVersion=2, backupType=FULL, with the historical 12 sections and
        // without the six sections introduced later (conceptTags/settings/achievements/
        // parserMetadata/reviewQueue/conceptReferences).
        val json = JSONObject(sourceBackup.exportFull())
        listOf("conceptTags", "settings", "achievements", "parserMetadata", "reviewQueue", "conceptReferences")
            .forEach(json::remove)

        val context = ApplicationProvider.getApplicationContext<Context>()
        val targetDb = Room.inMemoryDatabaseBuilder(context, RoomFlashLearnDatabase::class.java)
            .allowMainThreadQueries().build()
        try {
            val targetBackup = RoomBackupRepository(targetDb, context)
            val result = targetBackup.restoreFull(json.toString())

            assertTrue(result.issues.toString(), result.issues.isEmpty())
            assertNotNull(targetDb.categoryDao().getById(categoryId))
            assertNotNull(targetDb.conceptDao().getById(conceptId))
            assertEquals("RANDOM", targetDb.reviewSessionDao().getById(sessionId)!!.reviewType)
            assertEquals(1, targetDb.reviewHistoryDao().getAll().size)
            assertEquals("RANDOM", targetDb.reviewHistoryDao().getAll().first().reviewType)
        } finally {
            targetDb.close()
        }
    }
}
