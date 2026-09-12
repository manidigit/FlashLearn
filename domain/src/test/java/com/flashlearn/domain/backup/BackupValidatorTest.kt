package com.flashlearn.domain.backup

import com.flashlearn.domain.model.*
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant
import java.util.UUID

class BackupValidatorTest {
    private val concept = Concept(UUID.randomUUID(), EntryType.WORD, null, false, true, Instant.now(), Instant.now())

    @Test fun rejectsDuplicateConceptUuids() {
        val data = BackupData(1, Instant.now(), BackupType.FULL, concepts=listOf(concept, concept))
        assertFalse(BackupValidator.validate(data, 1).valid)
    }

    @Test fun rejectsBrokenContentReference() {
        val data = BackupData(1, Instant.now(), BackupType.FULL,
            concepts=listOf(concept),
            contents=listOf(Content(UUID.randomUUID(), UUID.randomUUID(), "es", "hola", "hola")))
        assertFalse(BackupValidator.validate(data, 1).valid)
    }


    @Test fun rejectsConceptWithMissingCategory() {
        val missing = UUID.randomUUID()
        val broken = concept.copy(categoryId = missing)
        val data = BackupData(1, Instant.now(), BackupType.FULL, concepts=listOf(broken))
        assertFalse(BackupValidator.validate(data, 1).valid)
    }

    @Test fun rejectsHistoryWithMissingSession() {
        val history = ReviewHistory(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), concept.id, Instant.now(), true, ReviewType.DAILY)
        val data = BackupData(1, Instant.now(), BackupType.FULL, concepts=listOf(concept), reviewHistory=listOf(history))
        assertFalse(BackupValidator.validate(data, 1).valid)
    }


    @Test fun rejectsDuplicateReviewAttempts() {
        val attemptId = UUID.randomUUID()
        val sessionId = UUID.randomUUID()
        val h1 = ReviewHistory(UUID.randomUUID(), sessionId, attemptId, concept.id, Instant.now(), true, ReviewType.DAILY)
        val h2 = ReviewHistory(UUID.randomUUID(), sessionId, attemptId, concept.id, Instant.now(), false, ReviewType.DAILY)
        val data = BackupData(1, Instant.now(), BackupType.FULL, concepts=listOf(concept), reviewSessions=listOf(ReviewSession(sessionId, Instant.now(), null, ReviewType.DAILY)), reviewHistory=listOf(h1, h2))
        assertFalse(BackupValidator.validate(data, 1).valid)
    }

    @Test fun acceptsMinimalFullBackup() {
        val data = BackupData(1, Instant.now(), BackupType.FULL, concepts=listOf(concept))
        assertTrue(BackupValidator.validate(data, 1).valid)
    }
}
