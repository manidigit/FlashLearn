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
        val screen = java.io.File("src/main/java/com/flashlearn/app/ui/backup/BackupScreen.kt").readText()
        assertTrue(screen.contains("listOf(ExportFormat.JSON, ExportFormat.XLSX)"))
        assertFalse(screen.contains("ExportFormat.CSV"))
        assertFalse(screen.contains("ExportFormat.SQLITE"))
    }
}
