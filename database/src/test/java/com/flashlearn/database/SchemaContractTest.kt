package com.flashlearn.database

import androidx.room.Database
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SchemaContractTest {
    private val database: Database
        get() = RoomFlashLearnDatabase::class.java.getAnnotation(Database::class.java)

    private fun entityNames(): Set<String> = database.entities.map { it.simpleName }.toSet()
    private fun fieldNames(entity: Class<*>): Set<String> = entity.declaredFields.map { it.name }.toSet()

    @Test fun expectedEntityCountIsSeventeen() {
        assertEquals(17, database.entities.size)
    }

    @Test fun schemaVersionIsSevenAfterDatabaseOptimization() {
        assertEquals(7, database.version)
    }

    @Test fun contentSupportsMultipleTranslationsAndReviewMetadata() {
        val fields = fieldNames(ContentEntity::class.java)
        assertTrue(fields.contains("translationIndex"))
        assertTrue(fields.contains("grammarNote"))
        assertTrue(fields.contains("possibleCorrection"))
    }

    @Test fun vocabularyGraphTablesExist() {
        val names = entityNames()
        assertTrue(names.contains("VocabularyRelationEntity"))
        assertTrue(names.contains("VocabularyVariantEntity"))
        assertTrue(fieldNames(VocabularyRelationEntity::class.java).contains("relationType"))
        assertTrue(fieldNames(VocabularyVariantEntity::class.java).contains("variantType"))
    }

    @Test fun reviewQueueAndLanguageTablesExist() {
        val names = entityNames()
        assertTrue(names.contains("ReviewQueueEntity"))
        assertTrue(names.contains("LanguageEntity"))
        assertTrue(names.contains("LanguagePairEntity"))
        assertTrue(fieldNames(ReviewQueueEntity::class.java).contains("confidence"))
    }
}
