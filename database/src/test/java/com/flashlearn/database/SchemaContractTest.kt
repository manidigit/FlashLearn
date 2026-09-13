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
            .firstOrNull { it.name == "4.json" }
        assertTrue("Room schema version 4 was not generated", schemaFile != null)
        return schemaFile!!.readText()
    }

    @Test
    fun expectedEntityCountIsEleven() {
        val schema = exportedSchema()
        assertEquals(11, Regex("\"tableName\":").findAll(schema).count())
    }

    @Test
    fun schemaVersionIsFourAfterParserMetadataAddition() {
        val schema = exportedSchema()
        assertTrue(Regex("\"version\":\\s*4").containsMatchIn(schema))
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

    @Test
    fun parserMetadataTableExistsWithExpectedFields() {
        val schema = exportedSchema()
        assertTrue(schema.contains("\"tableName\": \"parser_metadata\""))
        assertTrue(schema.contains("\"fieldPath\": \"breakdownJson\""))
        assertTrue(schema.contains("\"fieldPath\": \"relationshipsJson\""))
        assertTrue(schema.contains("\"fieldPath\": \"variantsJson\""))
        assertTrue(schema.contains("\"fieldPath\": \"confidence\""))
    }
}
