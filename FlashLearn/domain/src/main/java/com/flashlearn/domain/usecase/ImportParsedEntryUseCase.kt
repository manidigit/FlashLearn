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
    suspend operator fun invoke(entry: ParsedEntry, sourceLanguage: String = "es", targetLanguage: String = "fa", mode: ImportMode = ImportMode.MERGE, lineNumber: Int? = null): UUID = database.withTransaction {
        require(sourceLanguage.isNotBlank() && targetLanguage.isNotBlank() && sourceLanguage != targetLanguage) { "زبان‌های مبدأ و مقصد باید متفاوت باشند" }
        val source = entry.sourceText.trim()
        val translations = entry.translationText.orEmpty().split(Regex("\\s*/\\s*|\\s*؛\\s*|\\s*;\\s*"))
            .map(String::trim).filter(String::isNotBlank).distinctBy(::computeCanonicalKey)
        val correction = entry.rawLines.firstOrNull { it.trim().lowercase().startsWith("correction:") || it.trim().lowercase().startsWith("possiblecorrection:") || it.trim().startsWith("اصلاح:") }?.substringAfter(':')?.trim()?.takeIf { it.isNotBlank() }
        if (source.isBlank() || translations.isEmpty()) throw IllegalArgumentException("مدخل ناقص")
        if (entry.confidence < LOW_CONFIDENCE_THRESHOLD) { reviewQueueRepository.upsert(ReviewQueueItem(UUID.randomUUID(), null, source, translations.joinToString(" / "), entry.confidence, correction, ReviewQueueStatus.PENDING, lineNumber, "اعتماد پایین؛ نیازمند بررسی دستی")); return@withTransaction REVIEW_SENTINEL }
        val sourceKey = computeCanonicalKey(source)
        val activeIds = conceptRepository.getAllActive().map { it.id }.toSet()
        val existingSource = contentRepository.getAll().firstOrNull { it.conceptId in activeIds && it.languageCode == sourceLanguage && computeCanonicalKey(it.text) == sourceKey }
        val conceptId = if (existingSource == null || mode == ImportMode.ADD_NEW) createConcept.createInTransaction(CreateConceptCommand(source, translations.first(), sourceLanguage, targetLanguage, notes = extractPlainNotes(entry), entryType = entry.entryType.toDomainEntryType(), mergeExistingSource = false)) else existingSource.conceptId
        val existingTranslations = contentRepository.findAll(conceptId, targetLanguage)
        val existingKeys = existingTranslations.map { computeCanonicalKey(it.text) }.toSet()
        var nextIndex = (existingTranslations.maxOfOrNull { it.translationIndex } ?: -1) + 1
        val newTranslations = if (existingSource == null || mode == ImportMode.ADD_NEW) translations.drop(1) else translations.filter { computeCanonicalKey(it) !in existingKeys }
        if (existingSource != null && mode == ImportMode.MERGE && newTranslations.isEmpty()) throw DuplicateConceptException("این واژه با همین معنی‌ها قبلاً در کتابخانه وجود دارد")
        newTranslations.forEach { text -> val key = computeCanonicalKey(text); contentRepository.insertTranslation(Content(UUID.randomUUID(), conceptId, targetLanguage, text, key, grammarNote = entry.grammarNote, possibleCorrection = correction, translationIndex = nextIndex++)) }
        if (existingSource != null && mode == ImportMode.UPDATE) translations.filter { computeCanonicalKey(it) in existingKeys }.forEach { text -> val key=computeCanonicalKey(text); val existing=existingTranslations.first{computeCanonicalKey(it.text)==key}; contentRepository.upsert(existing.copy(grammarNote=entry.grammarNote,possibleCorrection=correction,canonicalKey=key)) }
        val sourceContent=contentRepository.find(conceptId,sourceLanguage); if(sourceContent!=null&&(sourceContent.possibleCorrection!=correction||sourceContent.grammarNote!=entry.grammarNote||sourceContent.notes!=extractPlainNotes(entry))) contentRepository.upsert(sourceContent.copy(possibleCorrection=correction,grammarNote=entry.grammarNote,notes=extractPlainNotes(entry),canonicalKey=sourceKey))
        entry.relationships.forEach { relationship -> val label=relationship.label.trim().uppercase(); val type=when { label.contains("DERIVED")||label.contains("مشتق")->VocabularyRelationType.DERIVED_FROM; label.contains("USED")||label.contains("استفاده")->VocabularyRelationType.USED_IN; label.contains("SYNON")||label.contains("مترادف")->VocabularyRelationType.SYNONYM; else->null }; if(type!=null){val targetKey=computeCanonicalKey(relationship.text);val target=contentRepository.getAll().firstOrNull{it.languageCode==sourceLanguage&&computeCanonicalKey(it.text)==targetKey};relationRepository.insert(VocabularyRelation(UUID.randomUUID(),conceptId,target?.conceptId,type,if(target==null)relationship.text else null))} }
        entry.variants.forEach { variant -> val raw=variant.text.trim(); val type=when { entry.rawLines.any{it.contains("masculine:",true)&&it.contains(raw,true)}||raw.startsWith("masculine:",true)||raw.startsWith("مذکر:")->VocabularyVariantType.MASCULINE; entry.rawLines.any{it.contains("feminine:",true)&&it.contains(raw,true)}||raw.startsWith("feminine:",true)||raw.startsWith("مونث:")||raw.startsWith("مؤنث:")->VocabularyVariantType.FEMININE; else->VocabularyVariantType.ALTERNATIVE };variantRepository.insert(VocabularyVariant(UUID.randomUUID(),conceptId,raw.substringAfter(':',raw).trim(),type)) }
        parserMetadataRepository.upsert(conceptId,ParserMetadata(entry.breakdown.map{it.text},entry.relationships.map{"${it.label}: ${it.text}"},entry.variants.map{it.text},entry.confidence.coerceIn(0.0,1.0))); conceptId
    }
    private fun extractPlainNotes(entry: ParsedEntry): String? = entry.notes?.lines()?.filterNot { it.trim().startsWith("گرامر:") || it.trim().startsWith("grammar:", true) || it.trim().startsWith("gramática:", true) }?.joinToString("\n")?.ifBlank { null }
    private fun EntryKind.toDomainEntryType() = when (this) { EntryKind.WORD->EntryType.WORD; EntryKind.PHRASE->EntryType.PHRASE; EntryKind.SENTENCE->EntryType.SENTENCE; EntryKind.IDIOM->EntryType.IDIOM; EntryKind.COLLOCATION->EntryType.COLLOCATION; EntryKind.STRUCTURE->EntryType.STRUCTURE }
}
