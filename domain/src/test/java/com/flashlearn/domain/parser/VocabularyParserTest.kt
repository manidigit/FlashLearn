package com.flashlearn.domain.parser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VocabularyParserTest {
    @Test fun parses_arrow_pair_and_preserves_parenthetical_meaning() {
        val entries = VocabularyParser().parse("1. tener miedo de → ترسیدن از (حالت اضطراب)")
        assertEquals(1, entries.size)
        assertEquals("tener miedo de", entries[0].sourceText)
        assertEquals("ترسیدن از (حالت اضطراب)", entries[0].translationText)
    }

    @Test fun separates_following_persian_translation() {
        val entries = VocabularyParser().parse("el científico\nدانشمند")
        assertEquals(1, entries.size)
        assertEquals("دانشمند", entries[0].translationText)
    }

    @Test fun strips_numbering_bullets_and_spaced_dash() {
        val entries = VocabularyParser().parse("2) hablar — صحبت کردن\n• casa → خانه")
        assertEquals(2, entries.size)
        assertEquals("hablar", entries[0].sourceText)
        assertEquals("صحبت کردن", entries[0].translationText)
        assertEquals("casa", entries[1].sourceText)
        assertEquals("خانه", entries[1].translationText)
    }

    @Test fun preserves_following_non_persian_lines_as_notes() {
        val entries = VocabularyParser().parse("aprender\nیاد گرفتن\nمثال: aprender español cada día")
        assertEquals(1, entries.size)
        assertEquals("یاد گرفتن", entries[0].translationText)
        assertEquals("مثال: aprender español cada día", entries[0].notes)
    }

    @Test fun detects_mixed_analysis_without_creating_fake_entry() {
        val entries = VocabularyParser().parse("quiero: می‌خواهم (از querer)")
        assertEquals(1, entries.size)
        assertTrue(entries[0].sourceText == "quiero")
        assertEquals("می‌خواهم (از querer)", entries[0].translationText)
    }

    @Test fun merges_exact_duplicates_without_losing_notes() {
        val entries = VocabularyParser().parse(
            "hola → سلام\nExample one: hola amigo\nhola → سلام\nExample two: hola amigo"
        )
        assertEquals(1, entries.size)
        assertTrue(entries[0].notes!!.contains("Example one: hola amigo"))
        assertTrue(entries[0].notes!!.contains("Example two: hola amigo"))
    }

    @Test fun classifies_common_structures_and_idioms() {
        val entries = VocabularyParser().parse(
            "tener miedo de → ترسیدن از\nestar en las nubes → حواس‌پرت بودن"
        )
        assertEquals(2, entries.size)
        assertEquals(EntryKind.STRUCTURE, entries[0].entryType)
        assertEquals(EntryKind.IDIOM, entries[1].entryType)
    }
    @Test fun detailed_parse_classifies_breakdown_and_grammar_as_notes() {
        val result = VocabularyParser().parseDetailed(
            "hablar → صحبت کردن\nتجزیه: hablar = to speak\nGrammar: فعل بی‌قاعده"
        )
        assertEquals(1, result.entries.size)
        assertTrue(result.entries[0].notes!!.contains("تجزیه: hablar = to speak"))
        assertTrue(result.entries[0].notes!!.contains("Grammar: فعل بی‌قاعده"))
        assertTrue(result.warnings.isEmpty())
    }

    @Test fun detailed_parse_reports_orphan_translation_and_incomplete_header() {
        val result = VocabularyParser().parseDetailed(
            "سلام\naprender"
        )
        assertEquals(1, result.entries.size)
        assertEquals("aprender", result.entries[0].sourceText)
        assertTrue(result.entries[0].translationText == null)
        assertEquals(2, result.warnings.size)
        assertEquals(ParseWarningType.ORPHAN_LINE, result.warnings[0].warningType)
        assertEquals(ParseWarningType.INCOMPLETE_ENTRY, result.warnings[1].warningType)
    }

    @Test fun detailed_parse_preserves_unknown_following_lines_as_notes() {
        val result = VocabularyParser().parseDetailed(
            "casa → خانه\ntexto libre para contexto"
        )
        assertEquals(1, result.entries.size)
        assertEquals("texto libre para contexto", result.entries[0].notes)
        assertTrue(result.warnings.isEmpty())
    }

}

