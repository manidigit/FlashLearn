package com.flashlearn.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class DatabaseMigrationTest {
    private lateinit var context: Context
    private lateinit var dbFile: File
    private var db: RoomFlashLearnDatabase? = null

    @Before fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        dbFile = context.getDatabasePath("migration-test.db")
        dbFile.parentFile?.mkdirs()
        dbFile.delete()
    }

    @After fun tearDown() {
        db?.close()
        context.deleteDatabase("migration-test.db")
    }

    @Test fun migratesSchema1Through5WithoutDataLoss() = runBlocking {
        createSchemaVersion1()
        val conceptId = UUID.randomUUID()
        val sessionId = UUID.randomUUID()
        val contentId = UUID.randomUUID()
        val learningId = UUID.randomUUID()
        val difficultyId = UUID.randomUUID()
        val tagId = UUID.randomUUID()
        val now = System.currentTimeMillis()

        SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READWRITE).use { sqlite ->
            sqlite.execSQL("INSERT INTO concepts(id,entryType,categoryId,favorite,active,createdAt,updatedAt) VALUES(?,?,?,?,?,?,?)",
                arrayOf(conceptId.toString(), "WORD", null, 1, 1, now, now))
            sqlite.execSQL("INSERT INTO contents(id,conceptId,languageCode,text,canonicalKey,notes,pronunciation,example) VALUES(?,?,?,?,?,?,?,?)",
                arrayOf(contentId.toString(), conceptId.toString(), "es", "hola", "hola", null, null, null))
            sqlite.execSQL("INSERT INTO learning_states(id,conceptId,stage,nextReviewAt,monthlyWrongCount,hasPathFailure,totalCorrect,totalWrong,lastReviewedAt) VALUES(?,?,?,?,?,?,?,?,?)",
                arrayOf(learningId.toString(), conceptId.toString(), "DAILY", now, 0, 0, 2, 1, now))
            sqlite.execSQL("INSERT INTO difficulty_states(id,conceptId,current,consecutiveCorrect,consecutiveWrong,hasReachedVeryHard) VALUES(?,?,?,?,?,?)",
                arrayOf(difficultyId.toString(), conceptId.toString(), "EASY", 2, 0, 0))
            sqlite.execSQL("INSERT INTO tags(id,name) VALUES(?,?)", arrayOf(tagId.toString(), "legacy"))
            sqlite.execSQL("INSERT INTO concept_tags(conceptId,tagId) VALUES(?,?)", arrayOf(conceptId.toString(), tagId.toString()))
            sqlite.execSQL("INSERT INTO review_sessions(id,startedAt,endedAt) VALUES(?,?,?)", arrayOf(sessionId.toString(), now, null))
            sqlite.execSQL("INSERT INTO settings(key,value,updatedAt) VALUES(?,?,?)", arrayOf("theme", "dark", now))
            sqlite.execSQL("PRAGMA user_version=1")
        }

        db = Room.databaseBuilder(context, RoomFlashLearnDatabase::class.java, "migration-test.db")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
            .allowMainThreadQueries()
            .build()

        assertEquals(5, db!!.openHelper.readableDatabase.version)
        assertTableColumns(db!!.openHelper.readableDatabase, "concepts", setOf("id", "entryType", "categoryId", "favorite", "active", "createdAt", "updatedAt"))
        assertTableColumns(db!!.openHelper.readableDatabase, "contents", setOf("id", "conceptId", "languageCode", "text", "canonicalKey", "notes", "pronunciation", "example"))
        assertTableColumns(db!!.openHelper.readableDatabase, "learning_states", setOf("id", "conceptId", "stage", "nextReviewAt", "monthlyWrongCount", "hasPathFailure", "totalCorrect", "totalWrong", "lastReviewedAt"))
        assertTableColumns(db!!.openHelper.readableDatabase, "difficulty_states", setOf("id", "conceptId", "current", "consecutiveCorrect", "consecutiveWrong", "hasReachedVeryHard"))
        assertTableColumns(db!!.openHelper.readableDatabase, "tags", setOf("id", "name"))
        assertTableColumns(db!!.openHelper.readableDatabase, "concept_tags", setOf("conceptId", "tagId"))
        assertTableColumns(db!!.openHelper.readableDatabase, "review_sessions", setOf("id", "startedAt", "endedAt", "reviewType"))
        assertTableColumns(db!!.openHelper.readableDatabase, "review_history", setOf("id", "sessionId", "reviewAttemptId", "conceptId", "reviewedAt", "isCorrect", "reviewType"))
        assertTableColumns(db!!.openHelper.readableDatabase, "settings", setOf("key", "value", "updatedAt"))
        assertTableColumns(db!!.openHelper.readableDatabase, "categories", setOf("id", "name"))
        assertTableColumns(db!!.openHelper.readableDatabase, "parser_metadata", setOf("conceptId", "breakdownJson", "relationshipsJson", "variantsJson", "confidence"))
        assertTableColumns(db!!.openHelper.readableDatabase, "achievements", setOf("achievementId", "unlocked"))
        assertIndex(db!!.openHelper.readableDatabase, "contents", "index_contents_conceptId_languageCode", unique = true)
        assertIndex(db!!.openHelper.readableDatabase, "contents", "index_contents_languageCode_canonicalKey", unique = false)
        assertIndex(db!!.openHelper.readableDatabase, "learning_states", "index_learning_states_conceptId", unique = true)
        assertIndex(db!!.openHelper.readableDatabase, "learning_states", "index_learning_states_stage_nextReviewAt", unique = false)
        assertIndex(db!!.openHelper.readableDatabase, "difficulty_states", "index_difficulty_states_conceptId", unique = true)
        assertIndex(db!!.openHelper.readableDatabase, "concept_tags", "index_concept_tags_tagId_conceptId", unique = false)
        assertIndex(db!!.openHelper.readableDatabase, "review_history", "index_review_history_sessionId_reviewAttemptId", unique = true)
        assertIndex(db!!.openHelper.readableDatabase, "categories", "index_categories_name", unique = true)
        db!!.openHelper.readableDatabase.query("PRAGMA table_info(review_sessions)", emptyArray<Any?>()).use { cursor ->
            val names = mutableSetOf<String>()
            val nameIndex = cursor.getColumnIndexOrThrow("name")
            while (cursor.moveToNext()) names += cursor.getString(nameIndex)
            assertTrue(names.contains("reviewType"))
        }
        db!!.openHelper.readableDatabase.query("PRAGMA table_info(parser_metadata)", emptyArray<Any?>()).use { cursor ->
            assertTrue(cursor.count == 5)
        }
        db!!.openHelper.readableDatabase.query("PRAGMA table_info(achievements)", emptyArray<Any?>()).use { cursor ->
            assertTrue(cursor.count == 2)
        }
        assertNotNull(db!!.conceptDao().getById(conceptId))
        assertEquals("DAILY", db!!.reviewSessionDao().getById(sessionId)?.reviewType)
        assertEquals("legacy", db!!.tagDao().getAll().single().name)
        assertEquals("theme", db!!.settingsDao().getAll().single().key)
        assertTrue(db!!.categoryDao().getAll().isEmpty())
        assertTrue(db!!.parserMetadataDao().getAll().isEmpty())
        assertTrue(db!!.achievementDao().getAll().isEmpty())
    }

    private fun assertTableColumns(db: SupportSQLiteDatabase, table: String, expected: Set<String>) {
        db.query("PRAGMA table_info(`$table`)", emptyArray<Any?>()).use { cursor ->
            val index = cursor.getColumnIndexOrThrow("name")
            val actual = mutableSetOf<String>()
            while (cursor.moveToNext()) actual += cursor.getString(index)
            assertEquals("Unexpected columns in $table", expected, actual)
        }
    }

    private fun assertIndex(db: SupportSQLiteDatabase, table: String, indexName: String, unique: Boolean) {
        db.query("PRAGMA index_list(`$table`)", emptyArray<Any?>()).use { cursor ->
            val nameIndex = cursor.getColumnIndexOrThrow("name")
            val uniqueIndex = cursor.getColumnIndexOrThrow("unique")
            var found = false
            while (cursor.moveToNext()) {
                if (cursor.getString(nameIndex) == indexName) {
                    found = true
                    assertEquals(if (unique) 1 else 0, cursor.getInt(uniqueIndex))
                }
            }
            assertTrue("Missing index $indexName on $table", found)
        }
    }

    private fun createSchemaVersion1() {
        val sqlite = SQLiteDatabase.openOrCreateDatabase(dbFile, null)
        sqlite.execSQL("CREATE TABLE concepts (id TEXT NOT NULL PRIMARY KEY, entryType TEXT NOT NULL, categoryId TEXT, favorite INTEGER NOT NULL, active INTEGER NOT NULL, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL)")
        sqlite.execSQL("CREATE TABLE contents (id TEXT NOT NULL PRIMARY KEY, conceptId TEXT NOT NULL, languageCode TEXT NOT NULL, text TEXT NOT NULL, canonicalKey TEXT NOT NULL, notes TEXT, pronunciation TEXT, example TEXT)")
        sqlite.execSQL("CREATE UNIQUE INDEX index_contents_conceptId_languageCode ON contents(conceptId,languageCode)")
        sqlite.execSQL("CREATE INDEX index_contents_languageCode_canonicalKey ON contents(languageCode,canonicalKey)")
        sqlite.execSQL("CREATE TABLE learning_states (id TEXT NOT NULL PRIMARY KEY, conceptId TEXT NOT NULL, stage TEXT NOT NULL, nextReviewAt INTEGER, monthlyWrongCount INTEGER NOT NULL, hasPathFailure INTEGER NOT NULL, totalCorrect INTEGER NOT NULL, totalWrong INTEGER NOT NULL, lastReviewedAt INTEGER)")
        sqlite.execSQL("CREATE UNIQUE INDEX index_learning_states_conceptId ON learning_states(conceptId)")
        sqlite.execSQL("CREATE INDEX index_learning_states_stage_nextReviewAt ON learning_states(stage,nextReviewAt)")
        sqlite.execSQL("CREATE TABLE difficulty_states (id TEXT NOT NULL PRIMARY KEY, conceptId TEXT NOT NULL, current TEXT NOT NULL, consecutiveCorrect INTEGER NOT NULL, consecutiveWrong INTEGER NOT NULL, hasReachedVeryHard INTEGER NOT NULL)")
        sqlite.execSQL("CREATE UNIQUE INDEX index_difficulty_states_conceptId ON difficulty_states(conceptId)")
        sqlite.execSQL("CREATE TABLE tags (id TEXT NOT NULL PRIMARY KEY, name TEXT NOT NULL)")
        sqlite.execSQL("CREATE TABLE concept_tags (conceptId TEXT NOT NULL, tagId TEXT NOT NULL, PRIMARY KEY(conceptId,tagId))")
        sqlite.execSQL("CREATE INDEX index_concept_tags_tagId_conceptId ON concept_tags(tagId,conceptId)")
        sqlite.execSQL("CREATE TABLE review_sessions (id TEXT NOT NULL PRIMARY KEY, startedAt INTEGER NOT NULL, endedAt INTEGER)")
        sqlite.execSQL("CREATE TABLE review_history (id TEXT NOT NULL PRIMARY KEY, sessionId TEXT NOT NULL, reviewAttemptId TEXT NOT NULL, conceptId TEXT NOT NULL, reviewedAt INTEGER NOT NULL, isCorrect INTEGER NOT NULL, reviewType TEXT NOT NULL)")
        sqlite.execSQL("CREATE UNIQUE INDEX index_review_history_sessionId_reviewAttemptId ON review_history(sessionId,reviewAttemptId)")
        sqlite.execSQL("CREATE TABLE settings (key TEXT NOT NULL PRIMARY KEY, value TEXT NOT NULL, updatedAt INTEGER NOT NULL)")
        sqlite.execSQL("PRAGMA user_version=1")
        sqlite.close()
    }
}
