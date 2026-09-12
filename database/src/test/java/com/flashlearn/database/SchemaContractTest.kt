package com.flashlearn.database

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class SchemaContractTest {
    private fun exportedSchema(): String {
        val schemaRoot = File("schemas")
        assertTrue("Room schema directory was not generated", schemaRoot.isDirectory)
        val schemaFile = schemaRoot.walkTopDown()
            .filter { it.isFile && it.extension == "json" }
            .firstOrNull { it.name == "3.json" }
        assertTrue("Room schema version 3 was not generated", schemaFile != null)
        return schemaFile!!.readText()
    }

    @Test
    fun expectedEntityCountIsTen() {
        val schema = exportedSchema()
        assertEquals(10, Regex("\"tableName\":").findAll(schema).count())
    }

    @Test
    fun schemaVersionIsThreeAfterCategoryAddition() {
        val schema = exportedSchema()
        assertTrue(Regex("\"version\":\\s*3").containsMatchIn(schema))
    }

    @Test
    fun reviewSessionContainsReviewType() {
        val schema = exportedSchema()
        assertTrue(schema.contains("\"tableName\": \"review_sessions\""))
        assertTrue(schema.contains("\"fieldPath\": \"reviewType\""))
    }

    @Test
    fun categoriesTableExistsWithUniqueNameIndex() {
        val schema = exportedSchema()
        assertTrue(schema.contains("\"tableName\": \"categories\""))
        assertTrue(schema.contains("\"name\": \"index_categories_name\""))
    }
}
