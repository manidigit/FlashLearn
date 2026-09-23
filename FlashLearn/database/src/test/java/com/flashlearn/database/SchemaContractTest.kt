package com.flashlearn.database

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class SchemaContractTest {
    private fun exportedSchema(): String {
        val schemaRoot = File("schemas")
        assertTrue("Room schema directory was not generated", schemaRoot.isDirectory)
        val schemaFile = schemaRoot.walkTopDown().filter { it.isFile && it.extension == "json" }.firstOrNull { it.name == "6.json" }
        assertTrue("Room schema version 6 was not generated", schemaFile != null)
        return schemaFile!!.readText()
    }
    @Test fun expectedEntityCountIsSeventeen() { assertEquals(17, Regex("\"tableName\":").findAll(exportedSchema()).count()) }
    @Test fun schemaVersionIsSixAfterVocabularyNormalization() { assertTrue(Regex("\"version\":\\s*6").containsMatchIn(exportedSchema())) }
    @Test fun contentSupportsMultipleTranslationsAndReviewMetadata() {
        val schema=exportedSchema(); assertTrue(schema.contains("\"tableName\": \"contents\"")); assertTrue(schema.contains("\"fieldPath\": \"translationIndex\"")); assertTrue(schema.contains("\"fieldPath\": \"grammarNote\"")); assertTrue(schema.contains("\"fieldPath\": \"possibleCorrection\""))
    }
    @Test fun vocabularyGraphTablesExist() {
        val schema=exportedSchema(); assertTrue(schema.contains("\"tableName\": \"vocabulary_relations\"")); assertTrue(schema.contains("\"tableName\": \"vocabulary_variants\"")); assertTrue(schema.contains("\"fieldPath\": \"relationType\"")); assertTrue(schema.contains("\"fieldPath\": \"variantType\""))
    }
    @Test fun reviewQueueAndLanguageTablesExist() {
        val schema=exportedSchema(); assertTrue(schema.contains("\"tableName\": \"review_queue\"")); assertTrue(schema.contains("\"tableName\": \"languages\"")); assertTrue(schema.contains("\"tableName\": \"language_pairs\"")); assertTrue(schema.contains("\"fieldPath\": \"confidence\""))
    }
}
