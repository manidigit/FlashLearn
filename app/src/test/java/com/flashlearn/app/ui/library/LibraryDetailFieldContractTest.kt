package com.flashlearn.app.ui.library

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class LibraryDetailFieldContractTest {
    private fun source(path: String): String = File(path).readText()

    @Test
    fun editVocabularyDoesNotExposeObsoletePronunciationOrExampleFields() {
        val detail = source("src/main/java/com/flashlearn/app/ui/library/LibraryDetailScreen.kt")
        assertFalse(detail.contains("detail_pronunciation"))
        assertFalse(detail.contains("detail_example"))
        assertFalse(detail.contains("var pronunciation"))
        assertFalse(detail.contains("var example"))
    }

    @Test
    fun addWordAndEditVocabularyExposeTheSameCoreEditableFields() {
        val add = source("src/main/java/com/flashlearn/app/ui/addword/AddWordScreen.kt")
        val detail = source("src/main/java/com/flashlearn/app/ui/library/LibraryDetailScreen.kt")
        listOf("addword_word_or_phrase", "addword_translation", "addword_category", "addword_entry_type", "addword_notes").forEach {
            assertTrue("Add Word missing $it", add.contains(it))
        }
        listOf("detail_word_or_phrase", "detail_translation", "detail_category", "detail_entry_type", "detail_notes").forEach {
            assertTrue("Edit Vocabulary missing $it", detail.contains(it))
        }
    }
}
