package com.flashlearn.domain.backup

import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class BackupTypeTest {
    @Test fun vocabularyBackupRejectsProgressPayload() {
        val result = BackupValidator.validate(
            BackupData(1, Instant.now(), BackupType.VOCABULARY,
                learningStates = listOf(com.flashlearn.domain.model.LearningState(
                    java.util.UUID.randomUUID(), java.util.UUID.randomUUID(),
                    com.flashlearn.domain.model.Stage.DAILY, null, 0, false, 0, 0, null
                ))),
            1
        )
        assertFalse(result.valid)
    }
}
