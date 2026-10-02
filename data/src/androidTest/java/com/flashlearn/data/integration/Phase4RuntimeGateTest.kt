package com.flashlearn.data.integration

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.flashlearn.data.repository.*
import com.flashlearn.database.RoomFlashLearnDatabase
import com.flashlearn.domain.model.*
import com.flashlearn.domain.repository.*
import com.flashlearn.domain.usecase.*
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class Phase4RuntimeGateTest {
    private lateinit var db: RoomFlashLearnDatabase
    private lateinit var conceptRepo: RoomConceptRepository
    private lateinit var contentRepo: RoomContentRepository
    private lateinit var learningRepo: RoomLearningStateRepository
    private lateinit var difficultyRepo: RoomDifficultyStateRepository
    private lateinit var tagRepo: RoomConceptTagRepository
    private lateinit var historyRepo: RoomReviewHistoryRepository
    private lateinit var settingsRepo: RoomSettingsRepository
    private lateinit var tx: FlashLearnDatabase

    @Before fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, RoomFlashLearnDatabase::class.java).allowMainThreadQueries().build()
        conceptRepo = RoomConceptRepository(db.conceptDao()); contentRepo = RoomContentRepository(db.contentDao())
        learningRepo = RoomLearningStateRepository(db.learningStateDao()); difficultyRepo = RoomDifficultyStateRepository(db.difficultyStateDao())
        tagRepo = RoomConceptTagRepository(db.conceptTagDao()); historyRepo = RoomReviewHistoryRepository(db.reviewHistoryDao())
        settingsRepo = RoomSettingsRepository(db.settingsDao()); tx = FlashLearnDatabaseImpl(db)
    }
    @After fun tearDown() { db.close() }

    private suspend fun createConcept(): UUID = CreateConceptUseCase(conceptRepo, contentRepo, learningRepo, difficultyRepo, tagRepo, tx)(CreateConceptCommand("hola", "سلام"))

    private suspend fun submit(conceptId: UUID, sessionId: UUID = UUID.randomUUID(), attemptId: UUID = UUID.randomUUID(), correct: Boolean = true, type: ReviewType = ReviewType.DAILY, reviewedAt: Instant = Instant.now()): SubmitReviewAnswerResult =
        SubmitReviewAnswerUseCase(learningRepo, difficultyRepo, historyRepo, settingsRepo, tx)(SubmitReviewAnswerRequest(conceptId, sessionId, attemptId, type, correct, reviewedAt))

    @Test fun gate1_initialization_createsRequiredState() = runBlocking {
        val id = createConcept()
        assertNotNull(conceptRepo.get(id)); assertNotNull(contentRepo.find(id, "es")); assertNotNull(contentRepo.find(id, "fa"))
        assertEquals(Stage.DAILY, learningRepo.get(id)!!.stage); assertEquals(VocabularyDifficulty.EASY, difficultyRepo.get(id)!!.current)
    }

    @Test fun gate2_duplicateAttemptRejected() = runBlocking {
        val id = createConcept(); val session = UUID.randomUUID(); val attempt = UUID.randomUUID(); val t = Instant.now()
        submit(id, session, attempt, reviewedAt=t)
        try { submit(id, session, attempt, reviewedAt=t.plusSeconds(86400)); fail("Second identical attempt must be rejected") }
        catch (e: IllegalStateException) { assertTrue(e.message!!.contains("Duplicate review attempt")) }
        assertTrue(historyRepo.existsByAttemptId(session, attempt)); assertEquals(1, db.reviewHistoryDao().getByConceptId(id).size)
    }

    @Test fun gate3_missingStateProducesDataIntegrityError() = runBlocking {
        val id = createConcept(); difficultyRepo.delete(id)
        try { submit(id); fail("Missing DifficultyState must fail") }
        catch (e: IllegalStateException) { assertTrue(e.message!!.contains("DATA_INTEGRITY_ERROR")) }
    }

    @Test fun gate4_transactionRollback_onHistoryFailure() = runBlocking {
        val id = createConcept(); val beforeLearning = learningRepo.get(id)!!; val beforeDifficulty = difficultyRepo.get(id)!!
        val failingHistory = object : ReviewHistoryRepository {
            override suspend fun insert(entry: ReviewHistory) { throw IllegalStateException("FORCED_HISTORY_FAILURE") }
            override suspend fun existsByAttemptId(sessionId: UUID, reviewAttemptId: UUID) = false
            override suspend fun getAll(): List<ReviewHistory> = emptyList()
        }
        try { SubmitReviewAnswerUseCase(learningRepo, difficultyRepo, failingHistory, settingsRepo, tx)(SubmitReviewAnswerRequest(id, UUID.randomUUID(), UUID.randomUUID(), ReviewType.DAILY, true, Instant.now())); fail("Forced failure must propagate") }
        catch (e: IllegalStateException) { assertEquals("FORCED_HISTORY_FAILURE", e.message) }
        assertEquals(beforeLearning, learningRepo.get(id)); assertEquals(beforeDifficulty, difficultyRepo.get(id)); assertEquals(0, db.reviewHistoryDao().getByConceptId(id).size)
    }

    @Test fun gate5_thresholdDefaultIsThree_forConsecutiveWrongs() = runBlocking {
        val id = createConcept(); val t = Instant.now(); val session = UUID.randomUUID()
        submit(id, session, UUID.randomUUID(), correct=false, reviewedAt=t)
        submit(id, session, UUID.randomUUID(), correct=false, reviewedAt=t.plusSeconds(172800))
        assertEquals(VocabularyDifficulty.EASY, difficultyRepo.get(id)!!.current)
        submit(id, session, UUID.randomUUID(), correct=false, reviewedAt=t.plusSeconds(259200))
        assertEquals(VocabularyDifficulty.MEDIUM, difficultyRepo.get(id)!!.current)
    }
}
