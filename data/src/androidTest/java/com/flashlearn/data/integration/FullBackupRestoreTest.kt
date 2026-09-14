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
import com.flashlearn.database.ParserMetadataEntity
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class FullBackupRestoreTest {
    private lateinit var db: RoomFlashLearnDatabase
    private lateinit var backup: RoomBackupRepository
    private lateinit var createConcept: CreateConceptUseCase

    @Before fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, RoomFlashLearnDatabase::class.java).allowMainThreadQueries().build()
        backup = RoomBackupRepository(db, context)
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


    @Test fun fullBackupRoundTripsEveryCurrentEntityAndRelationship() = runBlocking {
        val categoryId = java.util.UUID.randomUUID()
        val tagId = java.util.UUID.randomUUID()
        db.categoryDao().insert(com.flashlearn.database.CategoryEntity(categoryId, "Travel"))
        db.tagDao().insert(com.flashlearn.database.TagEntity(tagId, "important"))
        val conceptId = createConcept(CreateConceptCommand(
            sourceText = "buenos días", targetText = "صبح بخیر", categoryId = categoryId,
            notes = "greeting note", pronunciation = "bwenos", example = "Buenos días, Ana.",
            entryType = com.flashlearn.domain.model.EntryType.PHRASE, tags = listOf(tagId)
        ))
        val learning = db.learningStateDao().getByConceptId(conceptId)!!.copy(
            stage = "WEEKLY", monthlyWrongCount = 2, hasPathFailure = true,
            totalCorrect = 7, totalWrong = 3, lastReviewedAt = java.time.Instant.now()
        )
        db.learningStateDao().upsert(learning)
        val difficulty = db.difficultyStateDao().getByConceptId(conceptId)!!.copy(
            current = "HARD", consecutiveCorrect = 0, consecutiveWrong = 2, hasReachedVeryHard = true
        )
        db.difficultyStateDao().upsert(difficulty)
        val sessionId = java.util.UUID.randomUUID()
        val started = java.time.Instant.now().minusSeconds(60)
        val reviewed = started.plusSeconds(20)
        db.reviewSessionDao().insert(com.flashlearn.database.ReviewSessionEntity(sessionId, started, reviewed.plusSeconds(20), "WEEKLY"))
        db.reviewHistoryDao().insert(com.flashlearn.database.ReviewHistoryEntity(
            id = java.util.UUID.randomUUID(), sessionId = sessionId, reviewAttemptId = java.util.UUID.randomUUID(),
            conceptId = conceptId, reviewedAt = reviewed, isCorrect = false, reviewType = "WEEKLY"
        ))
        db.settingsDao().put(com.flashlearn.database.SettingsEntity("review_mode", "quiz", reviewed))
        db.achievementDao().upsert(com.flashlearn.database.AchievementEntity("ROUND_TRIP", true))
        db.parserMetadataDao().upsert(com.flashlearn.database.ParserMetadataEntity(
            conceptId, "{\"lemma\":\"buenos días\"}", "[]", "[\"buenos dias\"]", 0.93
        ))

        val before = JSONObject(backup.exportFull())
        val result = backup.restoreFull(before.toString())
        assertTrue(result.issues.toString(), result.issues.isEmpty())

        val after = JSONObject(backup.exportFull())
        listOf("concepts", "contents", "learningStates", "difficultyStates", "tags", "conceptTags", "reviewSessions", "reviewHistory", "settings", "categories", "achievements", "parserMetadata").forEach { section ->
            assertEquals("Round-trip mismatch in $section", before.getJSONArray(section).toString(), after.getJSONArray(section).toString())
        }
    }

    @Test fun fullBackupRoundTripsAchievementStates() = runBlocking {
        db.achievementDao().upsertAll(listOf(
            com.flashlearn.database.AchievementEntity("FIRST_TEN_WORDS", true),
            com.flashlearn.database.AchievementEntity("MEMORY_BUILDER", false)
        ))

        val json = JSONObject(backup.exportFull())
        val achievements = json.getJSONArray("achievements")
        assertEquals(2, achievements.length())
        assertTrue((0 until achievements.length()).any {
            achievements.getJSONObject(it).getString("achievementId") == "FIRST_TEN_WORDS" &&
                achievements.getJSONObject(it).getBoolean("unlocked")
        })

        db.achievementDao().deleteAll()
        val result = backup.restoreFull(json.toString())

        assertTrue(result.issues.isEmpty())
        val restored = db.achievementDao().getAll().associate { it.achievementId to it.unlocked }
        assertEquals(true, restored["FIRST_TEN_WORDS"])
        assertEquals(false, restored["MEMORY_BUILDER"])
    }

    @Test fun fullBackupRoundTripsParserMetadataAndPreservesUnrelatedMetadata() = runBlocking {
        val original = createConcept(CreateConceptCommand("hola", "سلام"))
        db.parserMetadataDao().upsert(
            ParserMetadataEntity(
                conceptId = original,
                breakdownJson = "{\"lemma\":\"hola\"}",
                relationshipsJson = "[{\"type\":\"translation\"}]",
                variantsJson = "[{\"text\":\"holaaa\"}]",
                confidence = 0.87
            )
        )

        val json = JSONObject(backup.exportFull())
        val metadata = json.getJSONArray("parserMetadata")
        assertEquals(1, metadata.length())
        assertEquals(original.toString(), metadata.getJSONObject(0).getString("conceptId"))
        assertEquals(0.87, metadata.getJSONObject(0).getDouble("confidence"), 0.000001)

        val replacement = createConcept(CreateConceptCommand("adios", "خداحافظ"))
        db.parserMetadataDao().upsert(ParserMetadataEntity(replacement, "stale", "stale", "stale", 0.5))

        val result = backup.restoreFull(json.toString())

        assertTrue(result.issues.isEmpty())
        val restored = db.parserMetadataDao().getByConceptId(original)
        assertNotNull(restored)
        assertEquals("{\"lemma\":\"hola\"}", restored!!.breakdownJson)
        assertEquals(0.87, restored.confidence, 0.000001)
        val stale = db.parserMetadataDao().getByConceptId(replacement)
        assertNotNull(stale)
        assertEquals("stale", stale!!.breakdownJson)
    }

    @Test fun fullRestoreRejectsInvalidParserMetadataBeforeMutation() = runBlocking {
        val original = createConcept(CreateConceptCommand("hola", "سلام"))
        db.parserMetadataDao().upsert(ParserMetadataEntity(original, "good", "good", "good", 0.8))
        val json = JSONObject(backup.exportFull())
        json.getJSONArray("parserMetadata").getJSONObject(0).put("confidence", 2.0)

        val result = backup.restoreFull(json.toString())

        assertTrue(result.issues.any { it.contains("INVALID_VALUE:parserMetadata_confidence") })
        assertEquals("good", db.parserMetadataDao().getByConceptId(original)?.breakdownJson)
    }

    @Test fun fullBackupContainsEveryCurrentTableSection() = runBlocking {
        val json = JSONObject(backup.exportFull())
        listOf("concepts", "contents", "learningStates", "difficultyStates", "tags", "conceptTags",
            "reviewSessions", "reviewHistory", "settings", "categories", "achievements", "parserMetadata")
            .forEach { assertTrue("Missing backup section: $it", json.has(it) && json.get(it) is org.json.JSONArray) }
    }

    @Test fun fullRestoreRecalculatesCanonicalKeyFromContentText() = runBlocking {
        createConcept(CreateConceptCommand("hola", "سلام"))
        val json = JSONObject(backup.exportFull())
        val content = json.getJSONArray("contents").getJSONObject(0)
        val text = content.getString("text")
        content.put("canonicalKey", "tampered-stale-key")

        val result = backup.restoreFull(json.toString())

        assertTrue(result.issues.toString(), result.issues.isEmpty())
        val restored = db.contentDao().getById(UUID.fromString(content.getString("id")))
        assertNotNull(restored)
        assertEquals(computeCanonicalKey(text), restored!!.canonicalKey)
        assertNotEquals("tampered-stale-key", restored.canonicalKey)
    }

    @Test fun fullRestoreRejectsMissingCanonicalKeyBeforeMutation() = runBlocking {
        val original = createConcept(CreateConceptCommand("hola", "سلام"))
        val json = JSONObject(backup.exportFull())
        json.getJSONArray("contents").getJSONObject(0).remove("canonicalKey")

        val result = backup.restoreFull(json.toString())

        assertTrue(result.issues.any { it.contains("INVALID_VALUE:canonicalKey") })
        assertNotNull(db.conceptDao().getById(original))
        assertEquals(2, db.contentDao().getAll().size)
    }

    @Test fun fullRestoreRejectsNullCanonicalKeyBeforeMutation() = runBlocking {
        val original = createConcept(CreateConceptCommand("hola", "سلام"))
        val json = JSONObject(backup.exportFull())
        json.getJSONArray("contents").getJSONObject(0).put("canonicalKey", JSONObject.NULL)

        val result = backup.restoreFull(json.toString())

        assertTrue(result.issues.any { it.contains("INVALID_VALUE:canonicalKey") })
        assertNotNull(db.conceptDao().getById(original))
        assertEquals(2, db.contentDao().getAll().size)
    }

    @Test fun restoreRejectsMissingSectionBeforeMutation() = runBlocking {
        val original = createConcept(CreateConceptCommand("hola", "سلام"))
        val json = JSONObject(backup.exportFull())
        json.remove("parserMetadata")

        val result = backup.restoreFull(json.toString())

        assertTrue(result.issues.any { it.contains("MISSING_SECTION:parserMetadata") })
        assertNotNull(db.conceptDao().getById(original))
        assertEquals(1, db.conceptDao().getAll().size)
    }

    @Test fun restoreRejectsInvalidLearningAndSessionValuesBeforeMutation() = runBlocking {
        val original = createConcept(CreateConceptCommand("hola", "سلام"))
        val json = JSONObject(backup.exportFull())
        json.getJSONArray("learningStates").getJSONObject(0).put("stage", "NOT_A_STAGE")

        val result = backup.restoreFull(json.toString())

        assertTrue(result.issues.any { it.contains("INVALID_VALUE:stage") })
        assertNotNull(db.conceptDao().getById(original))
    }

    @Test fun restoreRejectsSessionEndBeforeSessionStartBeforeMutation() = runBlocking {
        val original = createConcept(CreateConceptCommand("hola", "سلام"))
        val json = JSONObject(backup.exportFull())
        val session = JSONObject()
            .put("id", java.util.UUID.randomUUID().toString())
            .put("startedAt", "2026-01-02T00:00:00Z")
            .put("endedAt", "2026-01-01T00:00:00Z")
            .put("reviewType", "DAILY")
        json.getJSONArray("reviewSessions").put(session)

        val result = backup.restoreFull(json.toString())

        assertTrue(result.issues.any { it.contains("INVALID_VALUE:reviewSession_time") })
        assertNotNull(db.conceptDao().getById(original))
    }

    @Test fun restoreRejectsHistoryReviewTypeMismatchBeforeMutation() = runBlocking {
        val original = createConcept(CreateConceptCommand("hola", "سلام"))
        val json = JSONObject(backup.exportFull())
        val session = JSONObject()
            .put("id", java.util.UUID.randomUUID().toString())
            .put("startedAt", java.time.Instant.now().toString())
            .put("endedAt", JSONObject.NULL)
            .put("reviewType", "DAILY")
        json.getJSONArray("reviewSessions").put(session)
        json.getJSONArray("reviewHistory").put(JSONObject()
            .put("id", java.util.UUID.randomUUID().toString())
            .put("sessionId", session.getString("id"))
            .put("reviewAttemptId", java.util.UUID.randomUUID().toString())
            .put("conceptId", original.toString())
            .put("reviewedAt", java.time.Instant.now().toString())
            .put("isCorrect", true)
            .put("reviewType", "WEEKLY"))

        val result = backup.restoreFull(json.toString())

        assertTrue(result.issues.any { it.contains("INVALID_VALUE:history_session_reviewType") })
        assertNotNull(db.conceptDao().getById(original))
    }

    @Test fun fullRestoreMergesWithoutDeletingExistingDatabaseRecords() = runBlocking {
        val original = createConcept(CreateConceptCommand("hola", "سلام"))
        val json = backup.exportFull()
        val extra = createConcept(CreateConceptCommand("adios", "خداحافظ"))

        val result = backup.restoreFull(json)

        assertTrue(result.issues.isEmpty())
        assertNotNull(db.conceptDao().getById(original))
        assertNotNull(db.conceptDao().getById(extra))
        assertTrue(result.mergedCount > 0)
        assertTrue(result.newCount == 0)
        assertEquals(2, db.conceptDao().getAll().size)
        assertEquals(4, db.contentDao().getAll().size)
    }

    @Test fun fullRestoreUpdatesContentByConceptAndLanguageWhenUuidIsMissing() = runBlocking {
        val original = createConcept(CreateConceptCommand("hola", "سلام"))
        val json = JSONObject(backup.exportFull())
        val incoming = json.getJSONArray("contents").getJSONObject(0)
        val existingId = incoming.getString("id")
        incoming.put("id", UUID.randomUUID().toString())
        incoming.put("text", "hola nuevo")
        incoming.put("canonicalKey", "stale")

        val result = backup.restoreFull(json.toString())

        assertTrue(result.issues.isEmpty())
        val contents = db.contentDao().getAllByConceptId(original)
        assertEquals(2, contents.size)
        assertTrue(contents.any { it.id.toString() == existingId && it.text == "hola nuevo" })
        assertTrue(result.mergedCount >= 1)
    }

    @Test fun fullBackupRoundTripPreservesAllCurrentEntitiesAndRelationships() = runBlocking {
        val concept = createConcept(CreateConceptCommand("hola", "سلام"))
        val categoryId = java.util.UUID.randomUUID()
        val tagId = java.util.UUID.randomUUID()
        val sessionId = java.util.UUID.randomUUID()
        val attemptId = java.util.UUID.randomUUID()
        val now = java.time.Instant.now()

        db.categoryDao().insert(com.flashlearn.database.CategoryEntity(categoryId, "greetings"))
        db.conceptDao().update(db.conceptDao().getById(concept)!!.copy(categoryId = categoryId, favorite = true))
        db.tagDao().insert(com.flashlearn.database.TagEntity(tagId, "common"))
        db.conceptTagDao().insert(com.flashlearn.database.ConceptTagEntity(concept, tagId))
        db.learningStateDao().upsert(db.learningStateDao().getByConceptId(concept)!!.copy(totalCorrect = 7, totalWrong = 2, monthlyWrongCount = 1))
        db.difficultyStateDao().upsert(db.difficultyStateDao().getByConceptId(concept)!!.copy(current = "HARD", consecutiveCorrect = 2, consecutiveWrong = 0, hasReachedVeryHard = true))
        db.reviewSessionDao().insert(com.flashlearn.database.ReviewSessionEntity(sessionId, now.minusSeconds(60), now, "DAILY"))
        db.reviewHistoryDao().insert(com.flashlearn.database.ReviewHistoryEntity(UUID.randomUUID(), sessionId, attemptId, concept, now, true, "DAILY"))
        db.settingsDao().put(com.flashlearn.database.SettingsEntity("theme", "dark", now))
        db.parserMetadataDao().upsert(com.flashlearn.database.ParserMetadataEntity(concept, "b", "r", "v", 0.91))
        db.achievementDao().upsertAll(listOf(com.flashlearn.database.AchievementEntity("FIRST_TEN_WORDS", true)))

        val exported = backup.exportFull()
        db.conceptTagDao().deleteAll(); db.reviewHistoryDao().deleteAll(); db.reviewSessionDao().deleteAll()
        db.learningStateDao().deleteAll(); db.difficultyStateDao().deleteAll(); db.parserMetadataDao().deleteAll()
        db.contentDao().deleteAll(); db.conceptDao().deleteAll(); db.tagDao().deleteAll(); db.categoryDao().deleteAll()
        db.settingsDao().deleteAll(); db.achievementDao().deleteAll()

        val result = backup.restoreFull(exported)
        assertTrue(result.issues.toString(), result.issues.isEmpty())
        assertEquals(1, db.conceptDao().getAll().size)
        assertEquals(categoryId, db.conceptDao().getAll().single().categoryId)
        assertTrue(db.conceptDao().getAll().single().favorite)
        assertEquals(1, db.conceptTagDao().getAll().size)
        assertEquals(7, db.learningStateDao().getByConceptId(concept)!!.totalCorrect)
        assertEquals("HARD", db.difficultyStateDao().getByConceptId(concept)!!.current)
        assertEquals(1, db.reviewSessionDao().getAll().size)
        assertEquals(1, db.reviewHistoryDao().getAll().size)
        assertEquals("dark", db.settingsDao().getAll().single().value)
        assertEquals(0.91, db.parserMetadataDao().getByConceptId(concept)!!.confidence, 0.000001)
        assertTrue(db.achievementDao().getAll().single().unlocked)
    }

    @Test fun restoreCreatesAutomaticPreRestoreBackup() = runBlocking {
        val original = createConcept(CreateConceptCommand("hola", "سلام"))
        val before = backup.exportFull()
        val json = JSONObject(before)
        json.getJSONArray("contents").getJSONObject(0).put("canonicalKey", "tampered")

        val result = backup.restoreFull(json.toString())

        assertTrue(result.issues.toString(), result.issues.isEmpty())
        val backupFile = java.io.File(
            androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext.filesDir,
            "flashlearn-pre-restore-backup.json"
        )
        assertTrue(backupFile.exists())
        val saved = JSONObject(backupFile.readText())
        val expected = JSONObject(before)
        assertEquals(expected.getInt("schemaVersion"), saved.getInt("schemaVersion"))
        assertEquals(expected.getString("backupType"), saved.getString("backupType"))
        assertEquals(expected.getJSONArray("concepts").toString(), saved.getJSONArray("concepts").toString())
        assertEquals(expected.getJSONArray("contents").toString(), saved.getJSONArray("contents").toString())
        assertEquals(expected.getJSONArray("learningStates").toString(), saved.getJSONArray("learningStates").toString())
        assertEquals(expected.getJSONArray("difficultyStates").toString(), saved.getJSONArray("difficultyStates").toString())
        assertEquals(expected.getJSONArray("tags").toString(), saved.getJSONArray("tags").toString())
        assertEquals(expected.getJSONArray("conceptTags").toString(), saved.getJSONArray("conceptTags").toString())
        assertEquals(expected.getJSONArray("reviewSessions").toString(), saved.getJSONArray("reviewSessions").toString())
        assertEquals(expected.getJSONArray("reviewHistory").toString(), saved.getJSONArray("reviewHistory").toString())
        assertEquals(expected.getJSONArray("settings").toString(), saved.getJSONArray("settings").toString())
        assertEquals(expected.getJSONArray("categories").toString(), saved.getJSONArray("categories").toString())
        assertEquals(expected.getJSONArray("achievements").toString(), saved.getJSONArray("achievements").toString())
        assertEquals(expected.getJSONArray("parserMetadata").toString(), saved.getJSONArray("parserMetadata").toString())
        assertNotNull(db.conceptDao().getById(original))
    }

}
