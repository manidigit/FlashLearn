package com.flashlearn.domain.parser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VocabularyParserMarkerTest {
    @Test
    fun barePersianMarkerWordsRemainTranslations() {
        val parser = VocabularyParser()
        listOf("نکته", "توضیحات", "احتمال اشتباه", "توجه", "مثال").forEach { markerWord ->
            val entry = parser.parse("hablar\n$markerWord").single()
            assertEquals(markerWord, entry.translationText)
            assertNull(entry.notes)
        }
    }

    @Test
    fun explicitExampleMarkerRemainsNote() {
        val entry = VocabularyParser().parse("hablar\nسلام\nمثال: hablar con amigos").single()
        assertEquals("سلام", entry.translationText)
        assertEquals("hablar con amigos", entry.notes)
    }

    @Test
    fun markerWordAsPhraseDoesNotBecomeNote() {
        val entry = VocabularyParser().parse("hablar\nتوجه کردن").single()
        assertEquals("توجه کردن", entry.translationText)
        assertNull(entry.notes)
    }
}
