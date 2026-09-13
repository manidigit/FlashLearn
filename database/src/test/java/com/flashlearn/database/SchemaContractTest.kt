package com.flashlearn.database

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class SchemaContractTest {
    private fun exportedSchema(): String {
        val schemaRoot = File("schemas")
        assertTrue("Room schema directory was not generated", schemaRoot.isDirectory)
        val schemaFile = schemaRoot.walkTopDown().filter { it.isFile && it.extension == "json" }
            .firstOrNull { it.name == "5.json" }
        assertTrue("Room schema version 5 was not generated", schemaFile != null)
        return schemaFile!!.readText()
    }
    @Test fun expectedEntityCountIsTwelve() {
        assertEquals(12, Regex("\"tableName\":").findAll(exportedSchema()).count())
    }
    @Test fun schemaVersionIsFiveAfterAchievementPersistenceAddition() {
        assertTrue(Regex("\"version\":\\s*5").containsMatchIn(exportedSchema()))
    }
    @Test fun reviewSessionContainsReviewType() {
        val schema = exportedSchema()
        assertTrue(schema.contains("\"tableName\": \"review_sessions\""))
        assertTrue(schema.contains("\"fieldPath\": \"reviewType\""))
    }
    @Test fun categoriesTableExistsWithUniqueNameIndex() {
        val schema = exportedSchema()
        assertTrue(schema.contains("\"tableName\": \"categories\""))
        assertTrue(schema.contains("\"name\": \"index_categories_name\""))
    }
    @Test fun parserMetadataTableExistsWithExpectedFields() {
        val schema = exportedSchema()
        assertTrue(schema.contains("\"tableName\": \"parser_metadata\""))
        assertTrue(schema.contains("\"fieldPath\": \"breakdownJson\""))
        assertTrue(schema.contains("\"fieldPath\": \"relationshipsJson\""))
        assertTrue(schema.contains("\"fieldPath\": \"variantsJson\""))
        assertTrue(schema.contains("\"fieldPath\": \"confidence\""))
    }
    @Test fun achievementsTableExistsWithUnlockState() {
        val schema = exportedSchema()
        assertTrue(schema.contains("\"tableName\": \"achievements\""))
        assertTrue(schema.contains("\"fieldPath\": \"achievementId\""))
        assertTrue(schema.contains("\"fieldPath\": \"unlocked\""))
    }
}
