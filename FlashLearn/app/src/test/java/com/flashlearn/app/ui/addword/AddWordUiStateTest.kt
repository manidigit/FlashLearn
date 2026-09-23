package com.flashlearn.app.ui.addword

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AddWordUiStateTest {
    @Test
    fun canSave_requiresBothSourceAndTarget() {
        assertFalse(AddWordUiState(sourceText = "hola").canSave)
        assertFalse(AddWordUiState(targetText = "سلام").canSave)
        assertTrue(AddWordUiState(sourceText = "hola", targetText = "سلام").canSave)
    }

    @Test
    fun canSave_isDisabledWhileSaving() {
        val state = AddWordUiState(sourceText = "hola", targetText = "سلام", isSaving = true)
        assertFalse(state.canSave)
    }
}
