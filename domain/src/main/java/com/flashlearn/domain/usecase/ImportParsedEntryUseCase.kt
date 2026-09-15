package com.flashlearn.domain.usecase

import com.flashlearn.domain.model.*
import com.flashlearn.domain.parser.EntryKind
import com.flashlearn.domain.parser.ParsedEntry
import com.flashlearn.domain.repository.*
import java.util.UUID
import javax.inject.Inject

/** Imports parsed vocabulary while preserving every translation as a separate Content row. */
class ImportParsedEntryUseCase @Inject constructor(
    private val createConcept: CreateConceptUseCase,
    private val conceptRepository: ConceptRepository,
    private val contentRepository: ContentRepository,
    private val parserMetadataRepository: ParserMetadataRepository,
    private val relationRepository: VocabularyRelationRepository,
    private val variantRepository: VocabularyVariantRepository,
    private val reviewQueueRepository: ReviewQueueRepository,
    private val database: FlashLearnDatabase
) {
    companion object { const val LOW_CONFIDENCE_THRESHOLD = 0.80; val REVIEW_SENTINEL: UUID = UUID(0L, 0L) }

    suspend operator fun invoke(entry: ParsedEntry, sourceLanguage: String = "es", targetLanguage: String = "fa", mode: ImportMode = ImportMode.MERGE): UUID = database.withTransaction {
        require(sourceLanguage.isNotBlank() && targetLanguage.isNotBlank() && sourceLanguage != targetLanguage) { "زبان‌های مبدأ و مقصد باید متفاوت باشند" }
        val source = entry.sourceText.trim()
        val translations = entry.translationText.orEmpty().split(Regex("\\s*/\\s*|\\s*؛\\s*|\\s*;\\s*"))
            .map(String::trim).filter(String::isNotBlank).distinctBy(::computeCanonicalKey)
        val correction = entry.rawLines.firstOrNull { it.trim().lowercase().startsWith("correction:") || it.trim().lowercase().startsWith("possiblecorrection:") || it.trim().startsWith("اصلاح:") }
            ?.substringAfter(':')?.trim()?.takeIf { it.isNotBlank() }
        if (source.isBlank() || translations.isEmpty()) throw IllegalArgumentException("مدخل ناقص")

        if (entry.confidence < LOW_CONFIDENCE_THRESHOLD) {
            reviewQueueRepository.upsert(ReviewQueueItem(UUID.randomUUID(), null, source, translations.joinToString(" / "), entry.confidence, correction, ReviewQueueStatus.PENDING, null, "اعتماد پایین؛ نیازمند بررسی دستی"))
            return@withTransaction REVIEW_SENTINEL
        }

        val sourceKey = computeCanonicalKey(source)
        val activeIds = conceptRepository.getAllActive().map { it.id }.toSet()
        val existingSource = contentRepository.getAll().firstOrNull { it.conceptId in activeIds && it.languageCode == sourceLanguage && it.canonicalKey == sourceKey }
        val conceptId = when {
            existingSource == null || mode == ImportMode.ADD_NEW -> createConcept.createInTransaction(
                CreateConceptCommand(source, translations.first(), sourceLanguage, targetLanguage, notes = extractPlainNotes(entry), entryType = entry.entryType.toDomainEntryType())
            )
            else -> existingSource.conceptId
        }

        val existingTranslations = contentRepository.findAll(conceptId, targetLanguage)
        val incomingKeys = translations.map(::computeCanonicalKey).toSet()
        if (existingSource != null && mode == ImportMode.MERGE && existingTranslations.any { it.canonicalKey in incomingKeys }) {
            throw DuplicateConceptException("این واژه با همین ترجمه قبلاً در کتابخانه وجود دارد")
        }
        var nextIndex = (existingTranslations.maxOfOrNull { it.translationIndex } ?: -1) + 1
        val translationsToInsert = if (existingSource == null || mode == ImportMode.ADD_NEW) translations.drop(1) else translations
        translationsToInsert.forEach { text ->
            val key = computeCanonicalKey(text)
            val existing = existingTranslations.firstOrNull { it.canonicalKey == key }
            if (existing == null) {
                contentRepository.insertTranslation(Content(UUID.randomUUID(), conceptId, targetLanguage, text, key, grammarNote = extractGrammarNote(entry), possibleCorrection = correction, translationIndex = nextIndex++))
            } else if (mode == ImportMode.UPDATE) {
                contentRepository.upsert(existing.copy(grammarNote = extractGrammarNote(entry), possibleCorrection = correction))
            }
        }
        val sourceContent = contentRepository.find(conceptId, sourceLanguage)
        if (sourceContent != null && (sourceContent.possibleCorrection != correction || sourceContent.grammarNote != extractGrammarNote(entry))) {
            contentRepository.upsert(sourceContent.copy(possibleCorrection = correction, grammarNote = extractGrammarNote(entry), notes = extractPlainNotes(entry)))
        }

        entry.relationships.forEach { relationship ->
            val label = relationship.label.trim().uppercase()
            val type = when {
                label.contains("DERIVED") || label.contains("مشتق") -> VocabularyRelationType.DERIVED_FROM
                label.contains("USED") || label.contains("استفاده") -> VocabularyRelationType.USED_IN
                label.contains("SYNON") || label.contains("مترادف") -> VocabularyRelationType.SYNONYM
                else -> null
            }
            if (type != null) {
                val targetKey = computeCanonicalKey(relationship.text)
                val target = contentRepository.getAll().firstOrNull { it.languageCode == sourceLanguage && it.canonicalKey == targetKey }
                relationRepository.insert(VocabularyRelation(UUID.randomUUID(), conceptId, target?.conceptId, type, if (target == null) relationship.text else null))
            }
        }
        entry.variants.forEach { variant ->
            val raw = variant.text.trim()
            val type = when {
                entry.rawLines.any { it.contains("masculine:", true) && it.contains(raw, true) } || raw.startsWith("masculine:", true) || raw.startsWith("مذکر:") -> VocabularyVariantType.MASCULINE
                entry.rawLines.any { it.contains("feminine:", true) && it.contains(raw, true) } || raw.startsWith("feminine:", true) || raw.startsWith("مونث:") || raw.startsWith("مؤنث:") -> VocabularyVariantType.FEMININE
                else -> VocabularyVariantType.ALTERNATIVE
            }
            variantRepository.insert(VocabularyVariant(UUID.randomUUID(), conceptId, raw.substringAfter(':', raw).trim(), type))
        }
        parserMetadataRepository.upsert(conceptId, ParserMetadata(entry.breakdown.map { it.text }, entry.relationships.map { "${it.label}: ${it.text}" }, entry.variants.map { it.text }, entry.confidence.coerceIn(0.0,1.0)))
        conceptId
    }

    private fun extractGrammarNote(entry: ParsedEntry): String? = entry.rawLines
        .filter { it.trim().startsWith("گرامر:") || it.trim().startsWith("grammar:", true) || it.trim().startsWith("gramática:", true) }
        .joinToString("\n") { it.substringAfter(':').trim() }.ifBlank { null }

    private fun extractPlainNotes(entry: ParsedEntry): String? = entry.notes?.lines()
        ?.filterNot { it.trim().startsWith("گرامر:") || it.trim().startsWith("grammar:", true) || it.trim().startsWith("gramática:", true) }
        ?.joinToString("\n")?.ifBlank { null }

    private fun EntryKind.toDomainEntryType() = when (this) {
        EntryKind.WORD -> EntryType.WORD
        EntryKind.PHRASE -> EntryType.PHRASE
        EntryKind.SENTENCE -> EntryType.SENTENCE
        EntryKind.IDIOM -> EntryType.IDIOM
        EntryKind.COLLOCATION -> EntryType.COLLOCATION
        EntryKind.STRUCTURE -> EntryType.STRUCTURE
    }
}
