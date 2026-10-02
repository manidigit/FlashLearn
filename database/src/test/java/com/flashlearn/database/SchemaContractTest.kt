package com.flashlearn.database

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class SchemaContractTest {
    private val source: String
        get() = File("src/main/java/com/flashlearn/database/RoomSchema.kt").readText()

    @Test fun expectedEntityCountIsSeventeen() {
        val entities = Regex("data class (\\w+Entity)").findAll(source).map { it.groupValues[1] }.toSet()
        assertEquals(17, entities.size)
    }

    @Test fun schemaVersionIsEightAfterVocabularyFieldCleanup() {
        assertTrue(Regex("version\\s*=\\s*8").containsMatchIn(source))
    }

    @Test fun contentSupportsMultipleTranslationsAndReviewMetadata() {
        assertTrue(source.contains("translationIndex"))
        assertTrue(source.contains("grammarNote"))
        assertTrue(source.contains("possibleCorrection"))
    }

    @Test fun vocabularyGraphTablesExist() {
        assertTrue(source.contains("VocabularyRelationEntity"))
        assertTrue(source.contains("VocabularyVariantEntity"))
        assertTrue(source.contains("relationType"))
        assertTrue(source.contains("variantType"))
    }

    @Test fun reviewQueueAndLanguageTablesExist() {
        assertTrue(source.contains("ReviewQueueEntity"))
        assertTrue(source.contains("LanguageEntity"))
        assertTrue(source.contains("LanguagePairEntity"))
        assertTrue(source.contains("confidence"))
    }
}
