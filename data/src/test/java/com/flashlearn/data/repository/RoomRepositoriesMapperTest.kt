package com.flashlearn.data.repository

import com.flashlearn.database.ConceptEntity
import com.flashlearn.database.DifficultyStateEntity
import com.flashlearn.database.LearningStateEntity
import com.flashlearn.domain.model.EntryType
import com.flashlearn.domain.model.Stage
import com.flashlearn.domain.model.VocabularyDifficulty
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.util.UUID

class RoomRepositoriesMapperTest {
    private val id = UUID.randomUUID()
    private val conceptId = UUID.randomUUID()
    private val now = Instant.parse("2026-01-01T00:00:00Z")

    @Test
    fun malformedConceptEnumFallsBackWithoutThrowing() {
        val mapped = Mappers.concept(
            ConceptEntity(id, "BROKEN", null, false, true, now, now)
        )
        assertEquals(EntryType.WORD, mapped.entryType)
    }

    @Test
    fun malformedLearningAndDifficultyEnumsFallBackWithoutThrowing() {
        val learning = Mappers.learning(
            LearningStateEntity(id, conceptId, "BROKEN", null, 0, false, 0, 0, null)
        )
        val difficulty = Mappers.difficulty(
            DifficultyStateEntity(UUID.randomUUID(), conceptId, "BROKEN", 0, 0, false)
        )
        assertEquals(Stage.DAILY, learning.stage)
        assertEquals(VocabularyDifficulty.MEDIUM, difficulty.current)
    }
}
