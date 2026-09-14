package com.flashlearn.data.integration

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.flashlearn.data.backup.LegacyFullBackupRepository
import com.flashlearn.database.RoomFlashLearnDatabase
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class LegacyFullBackupRestoreTest {
    private lateinit var db: RoomFlashLearnDatabase
    private lateinit var restore: LegacyFullBackupRepository

    @Before fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, RoomFlashLearnDatabase::class.java).allowMainThreadQueries().build()
        restore = LegacyFullBackupRepository(db)
    }

    @After fun tearDown() = db.close()

    @Test fun restoresLegacyFullShapeIncludingProgressHistoryAndSettings() = runBlocking {
        val conceptId = UUID.randomUUID()
        val exported = System.currentTimeMillis()
        val review1 = exported - 2_000L
        val review2 = exported - 1_000L
        val json = JSONObject()
            .put("schemaVersion", 1)
            .put("exportedAt", exported)
            .put("languages", JSONArray().put(JSONObject().put("code", "fa")).put(JSONObject().put("code", "es")))
            .put("categories", JSONArray().put(JSONObject().put("name", "اعضای بدن").put("isCustom", true)))
            .put("concepts", JSONArray().put(
                JSONObject()
                    .put("uuid", conceptId.toString())
                    .put("contentType", "WORD")
                    .put("categoryName", "اعضای بدن")
                    .put("favorite", true)
                    .put("active", true)
                    .put("createdAt", exported - 10_000L)
                    .put("updatedAt", exported - 5_000L)
                    .put("notes", "legacy note")
                    .put("tags", JSONArray())
                    .put("contents", JSONArray()
                        .put(JSONObject().put("languageCode", "es").put("text", "pecho"))
                        .put(JSONObject().put("languageCode", "fa").put("text", "قفسه سینه"))
                        .put(JSONObject().put("languageCode", "fa").put("text", "سینه")))
            ))
            .put("learningStates", JSONArray().put(
                JSONObject()
                    .put("conceptUuid", conceptId.toString())
                    .put("stage", "WEEKLY")
                    .put("difficulty", "HARD")
                    .put("monthlyWrongCount", 2)
                    .put("totalCorrect", 7)
                    .put("totalWrong", 3)
                    .put("everFailed", true)
                    .put("consecutiveCorrect", 0)
                    .put("consecutiveIncorrect", 2)
                    .put("difficultyScore", 7)
            ))
            .put("reviewHistory", JSONArray()
                .put(JSONObject().put("conceptUuid", conceptId.toString()).put("sessionId", "legacy-session")
                    .put("reviewStage", "DAILY").put("reviewDate", review1).put("isCorrect", true))
                .put(JSONObject().put("conceptUuid", conceptId.toString()).put("sessionId", "legacy-session")
                    .put("reviewStage", "WEEKLY").put("reviewDate", review2).put("isCorrect", false)))
            .put("settings", JSONArray().put(JSONObject().put("key", "theme_mode").put("value", "light").put("updatedAt", exported)))
            .put("backupMode", "FULL")

        val result = restore.restore(json.toString())
        assertTrue(result.issues.toString(), result.issues.isEmpty())
        assertEquals(1, db.conceptDao().getAll().size)
        assertEquals("اعضای بدن", db.categoryDao().getAll().single().name)
        assertEquals("pecho", db.contentDao().getByConceptIdAndLanguage(conceptId, "es")!!.text)
        assertEquals("قفسه سینه / سینه", db.contentDao().getByConceptIdAndLanguage(conceptId, "fa")!!.text)
        assertEquals("WEEKLY", db.learningStateDao().getByConceptId(conceptId)!!.stage)
        assertEquals(7, db.learningStateDao().getByConceptId(conceptId)!!.totalCorrect)
        assertEquals("HARD", db.difficultyStateDao().getByConceptId(conceptId)!!.current)
        assertEquals(2, db.difficultyStateDao().getByConceptId(conceptId)!!.consecutiveWrong)
        assertEquals(2, db.reviewHistoryDao().getByConceptId(conceptId).size)
        assertEquals("light", db.settingsDao().getAll().single().value)
    }

    @Test fun legacyRestoreIsIdempotent() = runBlocking {
        val conceptId = UUID.randomUUID()
        val now = System.currentTimeMillis()
        val json = JSONObject()
            .put("schemaVersion", 1).put("exportedAt", now).put("languages", JSONArray())
            .put("categories", JSONArray()).put("concepts", JSONArray().put(JSONObject()
                .put("uuid", conceptId.toString()).put("contentType", "WORD").put("favorite", false).put("active", true)
                .put("createdAt", now).put("updatedAt", now).put("contents", JSONArray()
                    .put(JSONObject().put("languageCode", "es").put("text", "hola"))
                    .put(JSONObject().put("languageCode", "fa").put("text", "سلام"))))
            .put("learningStates", JSONArray().put(JSONObject().put("conceptUuid", conceptId.toString()).put("stage", "DAILY").put("difficulty", "EASY")))
            .put("reviewHistory", JSONArray()).put("settings", JSONArray()).put("backupMode", "FULL")

        assertTrue(restore.restore(json.toString()).issues.isEmpty())
        val firstHistory = db.reviewHistoryDao().getAll().size
        val firstConcepts = db.conceptDao().getAll().size
        assertTrue(restore.restore(json.toString()).issues.isEmpty())
        assertEquals(firstHistory, db.reviewHistoryDao().getAll().size)
        assertEquals(firstConcepts, db.conceptDao().getAll().size)
    }
}
