package com.flashlearn.data.integration

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.flashlearn.data.backup.VocabularyBackupRepository
import com.flashlearn.database.RoomFlashLearnDatabase
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class VocabularyBackupRestoreTest {
    private lateinit var db: RoomFlashLearnDatabase
    private lateinit var restore: VocabularyBackupRepository

    @Before fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, RoomFlashLearnDatabase::class.java).allowMainThreadQueries().build()
        restore = VocabularyBackupRepository(db)
    }

    @After fun tearDown() = db.close()

    @Test fun emptyLegacyTranslationsDoNotAbortRestore() = runBlocking {
        val id = UUID.randomUUID()
        val json = JSONObject()
            .put("schemaVersion", 1)
            .put("languages", JSONArray().put(JSONObject().put("code", "es")).put(JSONObject().put("code", "fa")))
            .put("categories", JSONArray())
            .put("concepts", JSONArray().put(JSONObject()
                .put("uuid", id.toString()).put("contentType", "WORD")
                .put("contents", JSONArray()
                    .put(JSONObject().put("languageCode", "es").put("text", "hola"))
                    .put(JSONObject().put("languageCode", "fa").put("text", "سلام"))
                    .put(JSONObject().put("languageCode", "fa").put("text", "")))))
            .put("backupMode", "VOCABULARY")

        val result = restore.restore(json.toString())
        assertTrue(result.issues.toString(), result.issues.isEmpty())
        assertEquals(1, db.conceptDao().getAll().size)
        assertEquals("سلام", db.contentDao().getByConceptIdAndLanguage(id, "fa")!!.text)
    }
}
