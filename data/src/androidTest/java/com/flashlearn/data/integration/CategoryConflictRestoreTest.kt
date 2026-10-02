package com.flashlearn.data.integration

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.flashlearn.data.backup.RoomBackupRepository
import com.flashlearn.database.CategoryEntity
import com.flashlearn.database.RoomFlashLearnDatabase
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CategoryConflictRestoreTest {
    private lateinit var db: RoomFlashLearnDatabase
    private lateinit var backup: RoomBackupRepository

    @Before fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, RoomFlashLearnDatabase::class.java).allowMainThreadQueries().build()
        backup = RoomBackupRepository(db, context)
    }

    @After fun tearDown() = db.close()

    @Test fun fullRestoreReusesExistingCategoryWhenBackupUuidDiffers() = runBlocking {
        val existingCategoryId = UUID.randomUUID()
        val incomingCategoryId = UUID.randomUUID()
        val conceptId = UUID.randomUUID()
        val contentId = UUID.randomUUID()
        val now = Instant.now().toString()
        db.categoryDao().insert(CategoryEntity(existingCategoryId, "اخلاق"))

        val root = JSONObject()
            .put("schemaVersion", 2)
            .put("exportedAt", now)
            .put("backupType", "FULL")
        val sections = listOf("concepts", "contents", "learningStates", "difficultyStates", "tags", "conceptTags", "reviewSessions", "reviewHistory", "settings", "categories", "achievements", "parserMetadata", "relations", "variants", "reviewQueue", "languages", "languagePairs")
        sections.forEach { root.put(it, JSONArray()) }
        root.getJSONArray("categories").put(JSONObject().put("id", incomingCategoryId).put("name", "اخلاق"))
        root.getJSONArray("concepts").put(JSONObject().put("id", conceptId).put("entryType", "WORD").put("categoryId", incomingCategoryId).put("favorite", false).put("active", true).put("createdAt", now).put("updatedAt", now))
        root.getJSONArray("contents").put(JSONObject().put("id", contentId).put("conceptId", conceptId).put("languageCode", "fa").put("text", "مهربانی").put("canonicalKey", "مهربانی").put("translationIndex", 0))

        val result = backup.restoreFull(root.toString())

        assertTrue(result.issues.toString(), result.issues.isEmpty())
        assertEquals(1, db.categoryDao().getAll().size)
        assertEquals(existingCategoryId, db.categoryDao().getAll().single().id)
        assertEquals(existingCategoryId, db.conceptDao().getById(conceptId)?.categoryId)
        assertEquals(1, db.contentDao().getAll().size)
    }
}
