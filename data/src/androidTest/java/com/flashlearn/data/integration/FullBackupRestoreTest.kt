package com.flashlearn.data.integration

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.flashlearn.data.backup.RoomBackupRepository
import com.flashlearn.data.repository.*
import com.flashlearn.database.RoomFlashLearnDatabase
import com.flashlearn.domain.usecase.*
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.json.JSONObject

@RunWith(AndroidJUnit4::class)
class FullBackupRestoreTest {
    private lateinit var db: RoomFlashLearnDatabase
    private lateinit var backup: RoomBackupRepository
    private lateinit var createConcept: CreateConceptUseCase

    @Before fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, RoomFlashLearnDatabase::class.java).allowMainThreadQueries().build()
        backup = RoomBackupRepository(db)
        val conceptRepo = RoomConceptRepository(db.conceptDao())
        val contentRepo = RoomContentRepository(db.contentDao())
        val learningRepo = RoomLearningStateRepository(db.learningStateDao())
        val difficultyRepo = RoomDifficultyStateRepository(db.difficultyStateDao())
        val tagRepo = RoomConceptTagRepository(db.conceptTagDao())
        createConcept = CreateConceptUseCase(conceptRepo, contentRepo, learningRepo, difficultyRepo, tagRepo, FlashLearnDatabaseImpl(db))
    }

    @After fun tearDown() = db.close()

    @Test fun fullRestoreRejectsDuplicateIdsBeforeMutation() = runBlocking {
        val original = createConcept(CreateConceptCommand("hola", "سلام"))
        val json = JSONObject(backup.exportFull())
        val concepts = json.getJSONArray("concepts")
        concepts.put(JSONObject(concepts.getJSONObject(0).toString()))
        val result = backup.restoreFull(json.toString())

        assertFalse(result.issues.isEmpty())
        assertTrue(result.issues.any { it.contains("DUPLICATE_UUID:concepts") })
        assertNotNull(db.conceptDao().getById(original))
        assertEquals(1, db.conceptDao().getAll().size)
    }

    @Test fun fullRestoreRejectsDuplicateReviewAttemptInSameSessionBeforeMutation() = runBlocking {
        createConcept(CreateConceptCommand("hola", "سلام"))
        val json = JSONObject(backup.exportFull())
        val sessions = json.getJSONArray("reviewSessions")
        val session = JSONObject()
            .put("id", java.util.UUID.randomUUID().toString())
            .put("startedAt", java.time.Instant.now().toString())
            .put("endedAt", JSONObject.NULL)
            .put("reviewType", "DAILY")
        sessions.put(session)
        val history = json.getJSONArray("reviewHistory")
        val attemptId = java.util.UUID.randomUUID().toString()
        val first = JSONObject()
            .put("id", java.util.UUID.randomUUID().toString())
            .put("sessionId", session.getString("id"))
            .put("reviewAttemptId", attemptId)
            .put("conceptId", json.getJSONArray("concepts").getJSONObject(0).getString("id"))
            .put("reviewedAt", java.time.Instant.now().toString())
            .put("isCorrect", true)
            .put("reviewType", "DAILY")
        history.put(first)
        history.put(JSONObject(first.toString()).put("id", java.util.UUID.randomUUID().toString()))

        val result = backup.restoreFull(json.toString())

        assertFalse(result.issues.isEmpty())
        assertTrue(result.issues.any { it.contains("DUPLICATE_ATTEMPT:reviewHistory") })
    }

    @Test fun fullRestoreReplacesExistingDatabaseSnapshot() = runBlocking {
        val original = createConcept(CreateConceptCommand("hola", "سلام"))
        val json = backup.exportFull()
        val extra = createConcept(CreateConceptCommand("adios", "خداحافظ"))

        val result = backup.restoreFull(json)

        assertTrue(result.issues.isEmpty())
        assertNotNull(db.conceptDao().getById(original))
        assertNull(db.conceptDao().getById(extra))
        assertEquals(1, db.conceptDao().getAll().size)
        assertEquals(2, db.contentDao().getAll().size)
    }
}
