package com.flashlearn.data.integration

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.flashlearn.data.backup.RoomBackupRepository
import com.flashlearn.data.repository.*
import com.flashlearn.database.ReviewHistoryEntity
import com.flashlearn.database.ReviewSessionEntity
import com.flashlearn.database.RoomFlashLearnDatabase
import com.flashlearn.domain.usecase.CreateConceptCommand
import com.flashlearn.domain.usecase.CreateConceptUseCase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class RandomReviewTypeRestoreTest {
    private lateinit var db: RoomFlashLearnDatabase
    private lateinit var backup: RoomBackupRepository
    private lateinit var createConcept: CreateConceptUseCase

    @Before fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, RoomFlashLearnDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        backup = RoomBackupRepository(db, context)
        val conceptRepo = RoomConceptRepository(db.conceptDao())
        val contentRepo = RoomContentRepository(db.contentDao())
        val learningRepo = RoomLearningStateRepository(db.learningStateDao())
        val difficultyRepo = RoomDifficultyStateRepository(db.difficultyStateDao())
        val tagRepo = RoomConceptTagRepository(db.conceptTagDao())
        createConcept = CreateConceptUseCase(
            conceptRepo, contentRepo, learningRepo, difficultyRepo, tagRepo,
            com.flashlearn.data.repository.FlashLearnDatabaseImpl(db)
        )
    }

    @After fun tearDown() = db.close()

    @Test fun fullRestoreAcceptsRandomReviewType() = runBlocking {
        val conceptId = createConcept(CreateConceptCommand("random", "تصادفی"))
        val sessionId = UUID.randomUUID()
        val started = Instant.now().minusSeconds(30)
        val reviewed = started.plusSeconds(10)
        db.reviewSessionDao().insert(
            ReviewSessionEntity(sessionId, started, reviewed, "RANDOM")
        )
        db.reviewHistoryDao().insert(
            ReviewHistoryEntity(
                id = UUID.randomUUID(),
                sessionId = sessionId,
                reviewAttemptId = UUID.randomUUID(),
                conceptId = conceptId,
                reviewedAt = reviewed,
                isCorrect = true,
                reviewType = "RANDOM"
            )
        )

        val result = backup.restoreFull(backup.exportFull())

        assertTrue(result.issues.toString(), result.issues.isEmpty())
    }
}
