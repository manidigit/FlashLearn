package com.flashlearn.app.ui

import com.flashlearn.domain.repository.ExportFormat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupExportFormatContractTest {
    @Test
    fun onlySupportedBackupExportFormatsAreExposed() {
        assertEquals(setOf(ExportFormat.JSON, ExportFormat.XLSX), ExportFormat.entries.toSet())
        assertFalse(ExportFormat.entries.any { it.name.equals("CSV", ignoreCase = true) })
        assertFalse(ExportFormat.entries.any { it.name.equals("SQLITE", ignoreCase = true) })
        assertTrue(ExportFormat.entries.isNotEmpty())
    }
}
