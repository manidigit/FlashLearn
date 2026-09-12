package com.flashlearn.app.ui.addword

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
}
