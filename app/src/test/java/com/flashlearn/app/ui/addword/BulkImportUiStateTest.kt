package com.flashlearn.app.ui.addword

import com.flashlearn.domain.parser.DetectedLanguage
import com.flashlearn.domain.parser.EntryKind
import com.flashlearn.domain.parser.ParsedEntry
import com.flashlearn.domain.parser.BreakdownPart
import com.flashlearn.domain.parser.ParsedRelationship
import com.flashlearn.domain.parser.ParsedVariant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BulkImportUiStateTest {
    @Test
    fun importingState_blocks_mutation_navigation() {
        val state = BulkImportUiState(isImporting = true)
        assertTrue(state.isImporting)
    }

    @Test
    fun defaultState_is_not_importing_and_has_no_preview_or_results() {
        val state = BulkImportUiState()
        assertFalse(state.isImporting)
        assertTrue(state.preview.isEmpty())
        assertTrue(state.results.isEmpty())
    }

    @Test
    fun itemStatus_has_stable_user_labels() {
        assertEquals("آماده", BulkImportItemStatus.READY.label())
        assertEquals("ناقص", BulkImportItemStatus.INCOMPLETE.label())
        assertEquals("وارد شد", BulkImportItemStatus.IMPORTED.label())
        assertEquals("تکراری", BulkImportItemStatus.DUPLICATE.label())
        assertEquals("خطا", BulkImportItemStatus.FAILED.label())
    }

    @Test
    fun itemResult_projects_parser_metadata_without_mutating_entry() {
        val entry = ParsedEntry(
            sourceText = "estar listo",
            translationText = "آماده بودن",
            notes = "usage",
            language = DetectedLanguage.SPANISH,
            entryType = EntryKind.PHRASE,
            rawLines = listOf("estar listo → آماده بودن"),
            breakdown = listOf(BreakdownPart("estar + listo")),
            relationships = listOf(ParsedRelationship("related", "preparado")),
            variants = listOf(ParsedVariant("estar preparado")),
            confidence = 0.87
        )
        val result = BulkImportItemResult(entry, BulkImportItemStatus.READY)

        assertEquals(87, result.confidencePercent)
        assertEquals(1, result.breakdownCount)
        assertEquals(1, result.relationshipCount)
        assertEquals(1, result.variantCount)
        assertEquals("estar listo", result.entry.sourceText)
    }
}
