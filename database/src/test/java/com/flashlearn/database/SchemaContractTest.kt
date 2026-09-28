package com.flashlearn.database

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class SchemaContractTest {
    private fun exportedSchema(): String {
        val schemaRoot = listOf(File("schemas"), File("database/schemas")).firstOrNull { it.isDirectory }
        assertTrue("Room schema directory was not generated", schemaRoot != null)
        val schemaFile = schemaRoot!!.walkTopDown()
            .filter { it.isFile && it.extension == "json" }
            .firstOrNull { it.name == "7.json" }
        assertTrue("Room schema version 7 was not generated", schemaFile != null)
        return schemaFile!!.readText()
    }

    @Test
    fun expectedEntityCountIsSeventeen() {
        assertEquals(17, Regex("\"tableName\":").findAll(exportedSchema()).count())
    }

    @Test
    fun schemaVersionIsSevenAfterDatabaseOptimization() {
        assertTrue(Regex("\"version\":\\s*7").containsMatchIn(exportedSchema()))
    }

    @Test
    fun contentSupportsMultipleTranslationsAndReviewMetadata() {
        val schema = exportedSchema()
        assertTrue(schema.contains("\"tableName\": \"contents\""))
        assertTrue(schema.contains("\"fieldPath\": \"translationIndex\""))
        assertTrue(schema.contains("\"fieldPath\": \"grammarNote\""))
        assertTrue(schema.contains("\"fieldPath\": \"possibleCorrection\""))
    }

    @Test
    fun vocabularyGraphTablesExist() {
        val schema = exportedSchema()
        assertTrue(schema.contains("\"tableName\": \"vocabulary_relations\""))
        assertTrue(schema.contains("\"tableName\": \"vocabulary_variants\""))
        assertTrue(schema.contains("\"fieldPath\": \"relationType\""))
        assertTrue(schema.contains("\"fieldPath\": \"variantType\""))
    }

    @Test
    fun reviewQueueAndLanguageTablesExist() {
        val schema = exportedSchema()
        assertTrue(schema.contains("\"tableName\": \"review_queue\""))
        assertTrue(schema.contains("\"tableName\": \"languages\""))
        assertTrue(schema.contains("\"tableName\": \"language_pairs\""))
        assertTrue(schema.contains("\"fieldPath\": \"confidence\""))
    }
}
