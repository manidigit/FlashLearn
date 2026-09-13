package com.flashlearn.domain.parser

import java.text.Normalizer
import java.util.Locale

enum class DetectedLanguage { SPANISH, PERSIAN, MIXED, UNKNOWN }
enum class ParsedLineType {
    ENTRY_HEADER, TRANSLATION, BREAKDOWN, NOTE, GRAMMAR_NOTE, DERIVATIVE,
    RELATION, VARIANT, COMMENT, NUMBER, SEPARATOR, UNKNOWN
}
enum class ParseWarningType { INCOMPLETE_ENTRY, ORPHAN_LINE, UNKNOWN_FORMAT }

data class ParseWarning(val warningType: ParseWarningType, val lineNumber: Int, val rawText: String, val message: String, val confidence: Double)
data class ParseLogEntry(val lineNumber: Int, val rawText: String, val lineType: ParsedLineType, val action: String)
data class BreakdownPart(val text: String)
data class ParsedRelationship(val label: String, val text: String)
data class ParsedVariant(val text: String)
data class ParseResult(val entries: List<ParsedEntry>, val warnings: List<ParseWarning>, val importLog: List<ParseLogEntry> = emptyList())
data class ParsedEntry(
    val sourceText: String, val translationText: String?, val notes: String?, val language: DetectedLanguage,
    val entryType: EntryKind, val rawLines: List<String>, val breakdown: List<BreakdownPart> = emptyList(),
    val relationships: List<ParsedRelationship> = emptyList(), val variants: List<ParsedVariant> = emptyList(), val confidence: Double = 0.0
)
enum class EntryKind { WORD, PHRASE, SENTENCE, IDIOM, COLLOCATION, STRUCTURE }

class VocabularyParser {
    fun parse(raw: String): List<ParsedEntry> = parseDetailed(raw).entries

    fun parseDetailed(raw: String): ParseResult {
        val sourceLines = normalize(raw).lineSequence().toList()
        val result = mutableListOf<ParsedEntry>()
        val warnings = mutableListOf<ParseWarning>()
        val importLog = mutableListOf<ParseLogEntry>()
        var pending: MutableEntry? = null
        fun flushPending() {
            pending?.let { entry ->
                val built = entry.build()
                if (built.translationText.isNullOrBlank()) warnings += ParseWarning(ParseWarningType.INCOMPLETE_ENTRY, entry.lineNumber, entry.rawLines.firstOrNull().orEmpty(), "مدخل بدون ترجمه تشخیص داده شد.", 0.98)
                result += built
            }
            pending = null
        }
        sourceLines.forEachIndexed { index, rawLine ->
            val lineNumber = index + 1
            val line0 = rawLine.trim()
            if (line0.isEmpty()) return@forEachIndexed
            val line = stripLeadingNumbering(line0)
            val type = classifyLine(line)
            importLog += ParseLogEntry(lineNumber, line0, type, "classified")
            when (type) {
                ParsedLineType.SEPARATOR, ParsedLineType.NUMBER -> importLog[importLog.lastIndex] = importLog.last().copy(action = "ignored")
                ParsedLineType.ENTRY_HEADER -> {
                    val pair = splitPair(line)
                    if (pending?.translation != null && pair == null) {
                        pending!!.rawLines += line0; pending!!.notes = joinNote(pending!!.notes, stripDecorativePrefix(line)); importLog[importLog.lastIndex] = importLog.last().copy(action = "attached_as_note")
                    } else {
                        flushPending(); val source = pair?.first?.trim() ?: line; val translation = pair?.second?.trim()?.takeIf { it.isNotBlank() }
                        pending = MutableEntry(source, translation, detectLanguage(line), classifyEntry(source), lineNumber, if (pair != null) 1.0 else 0.85, mutableListOf(line0))
                        importLog[importLog.lastIndex] = importLog.last().copy(action = "started_entry")
                    }
                }
                ParsedLineType.TRANSLATION -> {
                    if (pending == null) { warnings += ParseWarning(ParseWarningType.ORPHAN_LINE, lineNumber, line0, "ترجمه فارسی بدون مدخل قبلی نادیده گرفته نشد و به‌عنوان هشدار ثبت شد.", 0.96); importLog[importLog.lastIndex] = importLog.last().copy(action = "warning_orphan") }
                    else { pending!!.rawLines += line0; if (pending!!.translation == null) { pending!!.translation = line; pending!!.confidence = maxOf(pending!!.confidence, 0.95); importLog[importLog.lastIndex] = importLog.last().copy(action = "attached_translation") } else { pending!!.notes = joinNote(pending!!.notes, line); importLog[importLog.lastIndex] = importLog.last().copy(action = "attached_as_note") } }
                }
                ParsedLineType.BREAKDOWN -> attachStructuredLine(pending, line0, line, importLog, "breakdown") { text -> pending!!.breakdown += BreakdownPart(text) }
                ParsedLineType.RELATION -> attachStructuredLine(pending, line0, line, importLog, "relationship") { text -> val pair = splitLabel(text); pending!!.relationships += ParsedRelationship(pair.first, pair.second) }
                ParsedLineType.VARIANT, ParsedLineType.DERIVATIVE -> attachStructuredLine(pending, line0, line, importLog, "variant") { text -> pending!!.variants += ParsedVariant(text) }
                ParsedLineType.NOTE, ParsedLineType.GRAMMAR_NOTE, ParsedLineType.COMMENT -> {
                    if (pending == null) { warnings += ParseWarning(ParseWarningType.ORPHAN_LINE, lineNumber, line0, "خط توضیحی بدون مدخل قبلی پیدا شد.", 0.90); importLog[importLog.lastIndex] = importLog.last().copy(action = "warning_orphan") }
                    else { pending!!.rawLines += line0; pending!!.notes = joinNote(pending!!.notes, line); importLog[importLog.lastIndex] = importLog.last().copy(action = "attached_as_note") }
                }
                ParsedLineType.UNKNOWN -> {
                    if (pending != null) { pending!!.rawLines += line0; pending!!.notes = joinNote(pending!!.notes, stripDecorativePrefix(line)); importLog[importLog.lastIndex] = importLog.last().copy(action = "attached_as_note") }
                    else { warnings += ParseWarning(ParseWarningType.UNKNOWN_FORMAT, lineNumber, line0, "قالب این خط با الگوهای فعلی Parser تطبیق نداشت.", 0.70); importLog[importLog.lastIndex] = importLog.last().copy(action = "warning_unknown") }
                }
            }
        }
        flushPending()
        return ParseResult(mergeExactDuplicates(result), warnings, importLog)
    }

    private fun attachStructuredLine(pending: MutableEntry?, raw: String, normalized: String, log: MutableList<ParseLogEntry>, action: String, attach: (String) -> Unit) {
        if (pending == null) { log[log.lastIndex] = log.last().copy(action = "warning_orphan"); return }
        pending.rawLines += raw
        val cleaned = stripDecorativePrefix(normalized)
        val text = cleaned.substringAfter(':', cleaned).trim()
        attach(text); pending.notes = joinNote(pending.notes, normalized)
        log[log.lastIndex] = log.last().copy(action = "attached_$action")
    }

    private fun classifyLine(s: String): ParsedLineType {
        if (s.isBlank()) return ParsedLineType.UNKNOWN
        if (isSeparator(s)) return ParsedLineType.SEPARATOR
        if (s.all { it.isDigit() }) return ParsedLineType.NUMBER
        // Specific structured labels must win over the generic NOTE classifier.
        if (isBreakdownLine(s)) return ParsedLineType.BREAKDOWN
        if (isDerivativeLine(s)) return ParsedLineType.DERIVATIVE
        if (isVariantLine(s)) return ParsedLineType.VARIANT
        if (isRelationLine(s)) return ParsedLineType.RELATION
        if (isExplicitNoteLine(s)) {
            val lower = s.trim().lowercase(Locale.ROOT)
            return if (lower.startsWith("گرامر:") || lower.startsWith("grammar:") || lower.startsWith("gramática:")) ParsedLineType.GRAMMAR_NOTE else ParsedLineType.NOTE
        }
        if (isCommentLine(s)) return ParsedLineType.COMMENT
        val pair = splitPair(s)
        if (pair != null && isLikelySpanish(pair.first) && isLikelyPersian(pair.second)) return ParsedLineType.ENTRY_HEADER
        if (isLikelyPersian(s)) return ParsedLineType.TRANSLATION
        if (isLikelyEntryHeader(s)) return ParsedLineType.ENTRY_HEADER
        return ParsedLineType.UNKNOWN
    }

    private fun mergeExactDuplicates(entries: List<ParsedEntry>): List<ParsedEntry> {
        val merged = LinkedHashMap<String, ParsedEntry>()
        entries.forEach { entry ->
            val key = normalizeKey(entry.sourceText) + "\u0000" + normalizeKey(entry.translationText.orEmpty())
            val previous = merged[key]
            if (previous == null) merged[key] = entry else {
                val notes = listOf(previous.notes, entry.notes).filterNot { it.isNullOrBlank() }.joinToString("\n").ifBlank { null }
                merged[key] = previous.copy(notes = notes, rawLines = (previous.rawLines + entry.rawLines).distinct(), breakdown = (previous.breakdown + entry.breakdown).distinctBy { it.text }, relationships = (previous.relationships + entry.relationships).distinctBy { it.label + "\u0000" + it.text }, variants = (previous.variants + entry.variants).distinctBy { it.text }, confidence = maxOf(previous.confidence, entry.confidence))
            }
        }
        return merged.values.toList()
    }
    private fun normalizeKey(s: String): String = Normalizer.normalize(s.trim(), Normalizer.Form.NFC).replace(Regex("\\s+"), " ").lowercase(Locale.ROOT)
    private fun normalize(raw: String): String = Normalizer.normalize(raw.replace("\r\n", "\n").replace('\r', '\n').replace("\u200B", ""), Normalizer.Form.NFC).replace('\t', ' ').replace(Regex("[ ]{2,}"), " ")
    private fun stripLeadingNumbering(s: String): String = s.replaceFirst(Regex("""^\s*[0-9۰-۹٠-٩]+\s*(?:[.)-]|:|[-—])\s*"""), "").trim().replaceFirst(Regex("""^\s*[-—*•#»«➜→]\s*"""), "").trim()
    private fun stripDecorativePrefix(s: String) = stripLeadingNumbering(s)
    private fun splitPair(s: String): Pair<String, String>? { for (sep in listOf("→", "➜", ":")) { val i = s.indexOf(sep); if (i > 0 && i < s.length - 1) return s.substring(0, i) to s.substring(i + sep.length) }; val dash = Regex("""\s+[-—]\s+""").find(s); return if (dash != null) s.substring(0, dash.range.first) to s.substring(dash.range.last + 1) else null }
    private fun splitLabel(s: String): Pair<String, String> { val i = s.indexOf(':'); return if (i > 0) s.substring(0, i).trim() to s.substring(i + 1).trim() else "related" to s.trim() }
    private fun detectLanguage(s: String): DetectedLanguage { val fa = s.count { it in '\u0600'..'\u06FF' }; val es = s.count { it.isLetter() && it.lowercaseChar() in 'a'..'z' } + s.count { it in "áéíóúüñÁÉÍÓÚÜÑ¿¡" } * 2; return when { fa > 0 && es > 0 -> DetectedLanguage.MIXED; fa > 0 -> DetectedLanguage.PERSIAN; es > 0 -> DetectedLanguage.SPANISH; else -> DetectedLanguage.UNKNOWN } }
    private fun isLikelySpanish(s: String) = detectLanguage(s) == DetectedLanguage.SPANISH || (detectLanguage(s) == DetectedLanguage.MIXED && s.count { it in '\u0600'..'\u06FF' } < s.length / 3)
    private fun isLikelyPersian(s: String) = detectLanguage(s) == DetectedLanguage.PERSIAN || (detectLanguage(s) == DetectedLanguage.MIXED && s.count { it in '\u0600'..'\u06FF' } > s.length / 3)
    private fun isExplicitNoteLine(s: String): Boolean { val normalized = s.trim().lowercase(Locale.ROOT); val prefixes = listOf("مثال:", "نکته:", "گرامر:", "تجزیه:", "یادداشت:", "example:", "examples:", "note:", "grammar:", "breakdown:", "usage:", "ejemplo:", "nota:", "gramática:", "uso:"); if (prefixes.any { normalized.startsWith(it) }) return true; return Regex("""^(example|examples|note|grammar|breakdown|usage|ejemplo|nota|gramática|uso)\s+[^:]{1,30}:""").containsMatchIn(normalized) }
    private fun isBreakdownLine(s: String): Boolean { val lower = s.trim().lowercase(Locale.ROOT); return lower.startsWith("تجزیه:") || lower.startsWith("breakdown:") || lower.startsWith("desglose:") }
    private fun isDerivativeLine(s: String): Boolean { val lower = s.trim().lowercase(Locale.ROOT); return lower.startsWith("مشتق:") || lower.startsWith("derivative:") || lower.startsWith("derivada:") }
    private fun isVariantLine(s: String): Boolean { val lower = s.trim().lowercase(Locale.ROOT); return lower.startsWith("variant:") || lower.startsWith("alternative:") || lower.startsWith("variante:") || lower.startsWith("صورت دیگر:") || lower.startsWith("نسخه:") || lower.startsWith("شکل دیگر:") }
    private fun isRelationLine(s: String): Boolean { val lower = s.trim().lowercase(Locale.ROOT); return lower.startsWith("مرتبط:") || lower.startsWith("related:") || lower.startsWith("relacionado:") }
    private fun isCommentLine(s: String): Boolean = s.trim().startsWith("//") || s.trim().startsWith("# ")
    private fun isLikelyEntryHeader(s: String): Boolean { if (s.length < 2 || isLikelyPersian(s)) return false; if (s.endsWith(":") && s.length < 40) return false; return isLikelySpanish(s) || s.any { it in "áéíóúüñÁÉÍÓÚÜÑ¿¡" } }
    private fun isSeparator(s: String) = s.isNotEmpty() && s.all { it in "-—_*•#»«➜→ " }
    private fun classifyEntry(s: String): EntryKind { val words = s.trim().split(Regex("""\s+""")).size; val lower = s.lowercase(Locale.ROOT); return when { words == 1 -> EntryKind.WORD; lower.contains("a no ser que") || lower.contains("tener miedo de") -> EntryKind.STRUCTURE; lower.contains("estar en las nubes") || lower.contains("no hay mal que") -> EntryKind.IDIOM; lower.contains(" no ") || lower.startsWith("no ") || s.endsWith(".") || s.endsWith("?") || s.endsWith("!") -> EntryKind.SENTENCE; else -> EntryKind.PHRASE } }
    private fun joinNote(old: String?, next: String) = if (old.isNullOrBlank()) next else "$old\n$next"
    private data class MutableEntry(val source: String, var translation: String?, val language: DetectedLanguage, val kind: EntryKind, val lineNumber: Int, var confidence: Double, val rawLines: MutableList<String>, var notes: String? = null, var breakdown: MutableList<BreakdownPart> = mutableListOf(), var relationships: MutableList<ParsedRelationship> = mutableListOf(), var variants: MutableList<ParsedVariant> = mutableListOf()) {
        fun build() = ParsedEntry(source, translation, notes, language, kind, rawLines.toList(), breakdown.toList(), relationships.toList(), variants.toList(), confidence)
    }
}
