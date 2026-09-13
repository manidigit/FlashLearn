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

    @Test fun v500_extracts_breakdown_relationship_variant_and_confidence() {
        val result = VocabularyParser().parseDetailed(
            "hablar → صحبت کردن\nتجزیه: hablar = hab + lar\nمرتبط: خانواده: habla\nVariant: platicar"
        )
        val entry = result.entries.single()
        assertEquals(1, entry.breakdown.size)
        assertEquals("hablar = hab + lar", entry.breakdown[0].text)
        assertEquals(1, entry.relationships.size)
        assertEquals("خانواده", entry.relationships[0].label)
        assertEquals("habla", entry.relationships[0].text)
        assertEquals(1, entry.variants.size)
        assertEquals("platicar", entry.variants[0].text)
        assertTrue(entry.confidence >= 0.95)
    }

    @Test fun v500_exposes_deterministic_import_log_actions() {
        val result = VocabularyParser().parseDetailed(
            "1. casa → خانه\nمثال: casa grande\n---\nسلام"
        )
        assertEquals(4, result.importLog.size)
        assertEquals(ParsedLineType.ENTRY_HEADER, result.importLog[0].lineType)
        assertEquals("started_entry", result.importLog[0].action)
        assertEquals("attached_as_note", result.importLog[1].action)
        assertEquals("ignored", result.importLog[2].action)
        assertEquals("warning_unknown", result.importLog[3].action)
    }
}
