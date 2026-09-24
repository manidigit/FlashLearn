package com.flashlearn.domain.parser

import java.text.Normalizer
import java.util.Locale

enum class DetectedLanguage { SPANISH, PERSIAN, MIXED, UNKNOWN }
enum class ParsedLineType { ENTRY_HEADER, TRANSLATION, BREAKDOWN, NOTE, GRAMMAR_NOTE, DERIVATIVE, RELATION, VARIANT, COMMENT, NUMBER, SEPARATOR, UNKNOWN }
enum class ParseWarningType { INCOMPLETE_ENTRY, ORPHAN_LINE, ORPHAN_SOURCE, ORPHAN_TRANSLATION, UNKNOWN_FORMAT }

data class ParseWarning(val warningType: ParseWarningType, val lineNumber: Int, val rawText: String, val message: String, val confidence: Double)
data class ParseLogEntry(val lineNumber: Int, val rawText: String, val lineType: ParsedLineType, val action: String)
data class BreakdownPart(val text: String, val sourcePart: String? = null, val translationPart: String? = null, val orderIndex: Int = 0)
data class ParsedRelationship(val label: String, val text: String)
data class ParsedVariant(val text: String)
data class ParseResult(val entries: List<ParsedEntry>, val warnings: List<ParseWarning>, val importLog: List<ParseLogEntry> = emptyList())
data class ParsedEntry(
    val sourceText: String,
    val translationText: String?,
    val notes: String?,
    val grammarNote: String? = null,
    val language: DetectedLanguage,
    val entryType: EntryKind,
    val rawLines: List<String>,
    val breakdown: List<BreakdownPart> = emptyList(),
    val relationships: List<ParsedRelationship> = emptyList(),
    val variants: List<ParsedVariant> = emptyList(),
    val confidence: Double = 0.0,
    val evidence: List<String> = emptyList()
)
enum class EntryKind { WORD, PHRASE, SENTENCE, IDIOM, COLLOCATION, STRUCTURE }

class VocabularyParser(private val markers: ParserMarkers = ParserMarkers.DEFAULT) {
    fun parse(raw: String): List<ParsedEntry> = parseDetailed(raw).entries

    fun parseDetailed(raw: String): ParseResult {
        val lines = normalize(raw).lineSequence().toList()
        val entries = mutableListOf<ParsedEntry>()
        val warnings = mutableListOf<ParseWarning>()
        val log = mutableListOf<ParseLogEntry>()
        var pending: MutableEntry? = null
        var orphanTranslation: String? = null
        var orphanWarning: ParseWarning? = null

        fun flush() {
            pending?.let { entry ->
                val built = entry.build()
                if (built.translationText.isNullOrBlank()) {
                    warnings += ParseWarning(ParseWarningType.ORPHAN_SOURCE, entry.lineNumber, entry.rawLines.firstOrNull().orEmpty(), "Source بدون ترجمه پیدا شد و حذف نشد.", built.confidence)
                }
                entries += built
            }
            pending = null
        }

        lines.forEachIndexed { index, rawLine ->
            val lineNumber = index + 1
            val rawTrimmed = rawLine.trim()
            if (rawTrimmed.isEmpty()) return@forEachIndexed
            // Preserve separator runs before decorative-prefix stripping; otherwise `---` becomes `--`.
            val line = if (isSeparator(rawTrimmed)) rawTrimmed else stripLeadingNumbering(rawTrimmed)
            val type = classifyLine(line)
            log += ParseLogEntry(lineNumber, rawTrimmed, type, "classified")

            when (type) {
                ParsedLineType.SEPARATOR, ParsedLineType.NUMBER -> log[log.lastIndex] = log.last().copy(action = "ignored")
                ParsedLineType.ENTRY_HEADER -> {
                    val pair = splitPair(line)
                    val numbered = hasLeadingNumber(rawTrimmed)
                    val nextPersian = index + 1 < lines.size && isLikelyPersian(stripLeadingNumbering(lines[index + 1].trim()))
                    if (pending != null && pending!!.translation == null && pair == null && !numbered && nextPersian && isLikelySpanish(line)) {
                        pending!!.source = "${pending!!.source} ${stripDecorativePrefix(line)}"
                        pending!!.rawLines += rawTrimmed
                        pending!!.evidence += "multilineSource"
                        pending!!.confidence = maxOf(pending!!.confidence, 0.90)
                        log[log.lastIndex] = log.last().copy(action = "extended_source")
                        return@forEachIndexed
                    }
                    flush()
                    if (orphanTranslation != null) warnings.remove(orphanWarning)
                    val source = pair?.first?.trim().orEmpty().ifBlank { line }
                    val inlineTranslation = pair?.second?.trim()?.takeIf { it.isNotBlank() }
                    val translation = inlineTranslation ?: orphanTranslation
                    val evidence = mutableListOf<String>()
                    if (numbered) evidence += "numbered"
                    if (isLikelySpanish(source)) evidence += "spanishDetected"
                    if (translation != null) evidence += "adjacentPersianTranslation"
                    if (pair != null) evidence += "translationMarker"
                    pending = MutableEntry(source, translation, detectLanguage(line), classifyEntry(source), lineNumber, when { pair != null -> 1.0; translation != null -> 0.95; else -> 0.60 }, mutableListOf(rawTrimmed), evidence = evidence)
                    orphanTranslation = null
                    orphanWarning = null
                    log[log.lastIndex] = log.last().copy(action = "started_entry")
                }
                ParsedLineType.TRANSLATION -> {
                    if (pending == null) {
                        if (index + 1 < lines.size && isLikelyEntryHeader(stripLeadingNumbering(lines[index + 1].trim()))) {
                            orphanTranslation = line
                            orphanWarning = ParseWarning(ParseWarningType.ORPHAN_TRANSLATION, lineNumber, rawTrimmed, "ترجمه قبل از Source پیدا شد و برای جفت‌سازی خط بعد نگه داشته شد.", 0.85)
                            warnings += orphanWarning!!
                            log[log.lastIndex] = log.last().copy(action = "held_orphan_translation")
                        } else {
                            warnings += ParseWarning(ParseWarningType.ORPHAN_TRANSLATION, lineNumber, rawTrimmed, "ترجمه فارسی بدون Source پیدا شد و حذف نشد.", 0.50)
                            log[log.lastIndex] = log.last().copy(action = "warning_orphan")
                        }
                    } else if (pending!!.translation == null) {
                        pending!!.translation = line
                        pending!!.rawLines += rawTrimmed
                        pending!!.confidence = maxOf(pending!!.confidence, 0.95)
                        pending!!.evidence += "adjacentPersianTranslation"
                        log[log.lastIndex] = log.last().copy(action = "attached_translation")
                    } else {
                        pending!!.translation = joinTranslation(pending!!.translation, line)
                        pending!!.rawLines += rawTrimmed
                        pending!!.confidence = maxOf(pending!!.confidence, 0.95)
                        pending!!.evidence += "additionalTranslation"
                        log[log.lastIndex] = log.last().copy(action = "attached_translation")
                    }
                }
                ParsedLineType.BREAKDOWN -> attachStructured(pending, rawTrimmed, line, log, "breakdown") { text ->
                    val parts = text.split(Regex("\\s*=\\s*|\\s*:\\s*"), limit = 2)
                    pending!!.breakdown += BreakdownPart(text, parts.firstOrNull()?.trim(), parts.getOrNull(1)?.trim(), pending!!.breakdown.size)
                }
                ParsedLineType.DERIVATIVE -> attachStructured(pending, rawTrimmed, line, log, "relationship") { text -> pending!!.relationships += ParsedRelationship("DERIVED_FROM", text) }
                ParsedLineType.RELATION -> attachStructured(pending, rawTrimmed, line, log, "relationship") { text -> val pair = splitLabel(text); pending!!.relationships += ParsedRelationship(pair.first, pair.second) }
                ParsedLineType.VARIANT -> attachStructured(pending, rawTrimmed, line, log, "variant") { text -> pending!!.variants += ParsedVariant(text) }
                ParsedLineType.GRAMMAR_NOTE -> {
                    if (pending == null) {
                        warnings += ParseWarning(ParseWarningType.ORPHAN_LINE, lineNumber, rawTrimmed, "نکته گرامری بدون مدخل قبلی پیدا شد.", 0.90)
                        log[log.lastIndex] = log.last().copy(action = "warning_orphan")
                    } else {
                        pending!!.rawLines += rawTrimmed
                        pending!!.grammarNote = joinNote(pending!!.grammarNote, markerBody(line))
                        pending!!.evidence += "grammarNoteMarker"
                        log[log.lastIndex] = log.last().copy(action = "attached_grammar_note")
                    }
                }
                ParsedLineType.NOTE, ParsedLineType.COMMENT -> {
                    if (pending == null) {
                        warnings += ParseWarning(ParseWarningType.ORPHAN_LINE, lineNumber, rawTrimmed, "خط توضیحی بدون مدخل قبلی پیدا شد.", 0.90)
                        log[log.lastIndex] = log.last().copy(action = "warning_orphan")
                    } else {
                        pending!!.rawLines += rawTrimmed
                        pending!!.notes = joinNote(pending!!.notes, noteBody(line))
                        pending!!.evidence += "noteMarker"
                        log[log.lastIndex] = log.last().copy(action = "attached_note")
                    }
                }
                ParsedLineType.UNKNOWN -> when {
                    pending != null && pending!!.translation == null && isLikelySpanish(line) -> {
                        pending!!.source = "${pending!!.source} ${stripDecorativePrefix(line)}"
                        pending!!.rawLines += rawTrimmed
                        pending!!.confidence = maxOf(pending!!.confidence, 0.90)
                        pending!!.evidence += "multilineSource"
                        log[log.lastIndex] = log.last().copy(action = "extended_source")
                    }
                    pending != null -> {
                        pending!!.rawLines += rawTrimmed
                        pending!!.notes = joinNote(pending!!.notes, stripDecorativePrefix(line))
                        log[log.lastIndex] = log.last().copy(action = "attached_note")
                    }
                    else -> {
                        warnings += ParseWarning(ParseWarningType.UNKNOWN_FORMAT, lineNumber, rawTrimmed, "قالب این خط با الگوهای فعلی Parser تطبیق نداشت.", 0.70)
                        log[log.lastIndex] = log.last().copy(action = "warning_unknown")
                    }
                }
            }
        }
        flush()
        return ParseResult(mergeExactDuplicates(entries), warnings, log)
    }

    private fun attachStructured(pending: MutableEntry?, raw: String, normalized: String, log: MutableList<ParseLogEntry>, action: String, attach: (String) -> Unit) {
        if (pending == null) { log[log.lastIndex] = log.last().copy(action = "warning_orphan"); return }
        pending.rawLines += raw
        attach(markerBody(normalized))
        log[log.lastIndex] = log.last().copy(action = "attached_$action")
    }

    private fun classifyLine(s: String): ParsedLineType {
        if (s.isBlank()) return ParsedLineType.UNKNOWN
        if (isSeparator(s)) return ParsedLineType.SEPARATOR
        if (s.all { it.isDigit() }) return ParsedLineType.NUMBER
        if (isBreakdownLine(s)) return ParsedLineType.BREAKDOWN
        if (isDerivativeLine(s)) return ParsedLineType.DERIVATIVE
        if (isVariantLine(s)) return ParsedLineType.VARIANT
        if (isRelationLine(s)) return ParsedLineType.RELATION
        explicitMarkerType(s)?.let { return it }
        if (isCommentLine(s)) return ParsedLineType.COMMENT
        val pair = splitPair(s)
        if (pair != null && hasLatinScript(pair.first) && hasPersianScript(pair.second)) return ParsedLineType.ENTRY_HEADER
        if (isLikelyPersian(s)) return ParsedLineType.TRANSLATION
        if (isLikelyEntryHeader(s)) return ParsedLineType.ENTRY_HEADER
        return ParsedLineType.UNKNOWN
    }

    private fun explicitMarkerType(s: String): ParsedLineType? {
        val lower = s.trim().lowercase(Locale.ROOT)
        if (markers.grammar.any { lower.startsWith(it.lowercase(Locale.ROOT)) }) return ParsedLineType.GRAMMAR_NOTE
        if (markers.notes.any { lower.startsWith(it.lowercase(Locale.ROOT)) }) return ParsedLineType.NOTE
        if (Regex("^(example|examples|note|notes|usage|ejemplo|ejemplos|nota|uso)\\s+[^:]{1,60}:").containsMatchIn(lower)) return ParsedLineType.NOTE
        return null
    }

    private fun noteBody(s: String): String {
        val lower = s.trim().lowercase(Locale.ROOT)
        return if (Regex("^(example|examples|note|notes|usage|ejemplo|ejemplos|nota|uso)\\s+[^:]{1,60}:").containsMatchIn(lower)) s.trim() else markerBody(s)
    }

    private fun mergeExactDuplicates(entries: List<ParsedEntry>): List<ParsedEntry> {
        val merged = LinkedHashMap<String, ParsedEntry>()
        entries.forEach { entry ->
            val key = normalizeKey(entry.sourceText) + "\u0000" + normalizeKey(entry.translationText.orEmpty())
            val previous = merged[key]
            if (previous == null) merged[key] = entry else merged[key] = previous.copy(
                notes = listOf(previous.notes, entry.notes).filterNot { it.isNullOrBlank() }.joinToString("\n").ifBlank { null },
                grammarNote = listOf(previous.grammarNote, entry.grammarNote).filterNot { it.isNullOrBlank() }.joinToString("\n").ifBlank { null },
                rawLines = (previous.rawLines + entry.rawLines).distinct(),
                breakdown = (previous.breakdown + entry.breakdown).distinctBy { it.text },
                relationships = (previous.relationships + entry.relationships).distinctBy { it.label + "\u0000" + it.text },
                variants = (previous.variants + entry.variants).distinctBy { it.text },
                confidence = maxOf(previous.confidence, entry.confidence),
                evidence = (previous.evidence + entry.evidence).distinct()
            )
        }
        return merged.values.toList()
    }

    private fun normalizeKey(s: String): String = Normalizer.normalize(s.trim(), Normalizer.Form.NFC).replace(Regex("\\s+"), " ").lowercase(Locale.ROOT)
    private fun normalize(raw: String): String = Normalizer.normalize(raw.replace("\r\n", "\n").replace('\r', '\n').replace("\u200B", ""), Normalizer.Form.NFC).replace('\t', ' ').replace(Regex("[ ]{2,}"), " ")
    private fun hasLeadingNumber(s: String) = Regex("""^\s*[0-9۰-۹٠-٩]+\s*(?:[.)-]|:|[-—])\s*""").containsMatchIn(s)
    private fun stripLeadingNumbering(s: String): String = s.replaceFirst(Regex("""^\s*[0-9۰-۹٠-٩]+\s*(?:[.)-]|:|[-—])\s*"""), "").trim().replaceFirst(Regex("""^\s*[-—*•#»«➜→]\s*"""), "").trim()
    private fun stripDecorativePrefix(s: String) = stripLeadingNumbering(s)

    private fun splitPair(s: String): Pair<String, String>? {
        for (sep in listOf("→", "➜")) {
            val i = s.indexOf(sep)
            if (i > 0 && i < s.length - 1) return s.substring(0, i) to s.substring(i + sep.length)
        }
        val dash = Regex("""\s+[-—]\s+""").find(s)
        if (dash != null) return s.substring(0, dash.range.first) to s.substring(dash.range.last + 1)
        val colon = s.indexOf(':')
        if (colon > 0 && colon < s.length - 1) {
            val left = s.substring(0, colon).trim()
            val right = s.substring(colon + 1).trim()
            if (hasLatinScript(left) && hasPersianScript(right)) return left to right
        }
        return null
    }

    private fun splitLabel(s: String): Pair<String, String> { val i = s.indexOf(':'); return if (i > 0) s.substring(0, i).trim() to s.substring(i + 1).trim() else "related" to s.trim() }
    private fun markerBody(s: String): String = s.trim().substringAfter(':', s.trim()).trim()
    private fun joinNote(old: String?, new: String): String = listOf(old, new).filterNot { it.isNullOrBlank() }.joinToString("\n").ifBlank { "" }
    private fun joinTranslation(old: String?, new: String): String = listOf(old, new).filterNot { it.isNullOrBlank() }.joinToString(" / ")

    private fun isLikelyEntryHeader(s: String): Boolean = isLikelySpanish(s) && !isSeparator(s)
    private fun isLikelySpanish(s: String): Boolean = detectLanguage(s) == DetectedLanguage.SPANISH
    private fun isLikelyPersian(s: String): Boolean = detectLanguage(s) == DetectedLanguage.PERSIAN

    private fun hasLatinScript(s: String): Boolean = s.any { it in 'a'..'z' || it in 'A'..'Z' }
    private fun hasPersianScript(s: String): Boolean = s.any {
        it in '\u0600'..'\u06FF' || it in '\u0750'..'\u077F' || it in '\u08A0'..'\u08FF'
    }

    private fun detectLanguage(s: String): DetectedLanguage {
        var spanish = 0
        var persian = 0
        for (c in s) {
            when {
                c in 'a'..'z' || c in 'A'..'Z' -> spanish++
                c in '\u0600'..'\u06FF' || c in '\u0750'..'\u077F' || c in '\u08A0'..'\u08FF' -> persian++
            }
        }
        return when {
            spanish > 0 && persian > 0 -> DetectedLanguage.MIXED
            spanish > persian && spanish > 0 -> DetectedLanguage.SPANISH
            persian > spanish && persian > 0 -> DetectedLanguage.PERSIAN
            else -> DetectedLanguage.UNKNOWN
        }
    }

    private fun isBreakdownLine(s: String): Boolean = markers.breakdown.any { s.lowercase(Locale.ROOT).startsWith(it.lowercase(Locale.ROOT)) }
    private fun isDerivativeLine(s: String): Boolean = markers.derivative.any { s.lowercase(Locale.ROOT).startsWith(it.lowercase(Locale.ROOT)) }
    private fun isVariantLine(s: String): Boolean = markers.variant.any { s.lowercase(Locale.ROOT).startsWith(it.lowercase(Locale.ROOT)) }
    private fun isRelationLine(s: String): Boolean = markers.relation.any { s.lowercase(Locale.ROOT).startsWith(it.lowercase(Locale.ROOT)) }
    private fun isCommentLine(s: String): Boolean = s.startsWith("#") || s.startsWith("//")
    private fun isSeparator(s: String): Boolean = s.matches(Regex("[-—_=*•]{3,}"))

    private class MutableEntry(
        var source: String,
        var translation: String?,
        val language: DetectedLanguage,
        val kind: EntryKind,
        val lineNumber: Int,
        var confidence: Double,
        val rawLines: MutableList<String>,
        val breakdown: MutableList<BreakdownPart> = mutableListOf(),
        val relationships: MutableList<ParsedRelationship> = mutableListOf(),
        val variants: MutableList<ParsedVariant> = mutableListOf(),
        val evidence: MutableList<String> = mutableListOf(),
        var notes: String? = null,
        var grammarNote: String? = null
    ) {
        fun build() = ParsedEntry(source.trim(), translation?.trim(), notes?.trim()?.ifBlank { null }, grammarNote?.trim()?.ifBlank { null }, language, kind, rawLines.toList(), breakdown.toList(), relationships.toList(), variants.toList(), confidence.coerceIn(0.0, 1.0), evidence.distinct())
    }

    private fun classifyEntry(source: String): EntryKind {
        val lower = source.lowercase(Locale.ROOT)
        return when {
            Regex("\\b(tener miedo|estar en las nubes|dar a luz|hacer falta)\\b").containsMatchIn(lower) -> if (lower.contains("estar en las nubes")) EntryKind.IDIOM else EntryKind.STRUCTURE
            lower.split(Regex("\\s+")).size >= 6 -> EntryKind.SENTENCE
            lower.contains(" ") -> EntryKind.PHRASE
            else -> EntryKind.WORD
        }
    }
}
