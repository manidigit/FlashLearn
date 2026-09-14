package com.flashlearn.domain.usecase

import com.flashlearn.domain.model.Content
import com.flashlearn.domain.model.EntryType
import com.flashlearn.domain.model.ParserMetadata
import com.flashlearn.domain.parser.EntryKind
import com.flashlearn.domain.parser.ParsedEntry
import com.flashlearn.domain.repository.ContentRepository
import com.flashlearn.domain.repository.ConceptRepository
import com.flashlearn.domain.repository.FlashLearnDatabase
import com.flashlearn.domain.repository.ParserMetadataRepository
import java.util.UUID
import javax.inject.Inject

/** Imports one parsed vocabulary entry as a single atomic unit. */
class ImportParsedEntryUseCase @Inject constructor(
    private val createConcept: CreateConceptUseCase,
    private val conceptRepository: ConceptRepository,
    private val contentRepository: ContentRepository,
    private val parserMetadataRepository: ParserMetadataRepository,
    private val database: FlashLearnDatabase
) {
    suspend operator fun invoke(
        entry: ParsedEntry,
        sourceLanguage: String = "es",
        targetLanguage: String = "fa"
    ): UUID = database.withTransaction {
        require(sourceLanguage.isNotBlank() && targetLanguage.isNotBlank() && sourceLanguage != targetLanguage) {
            "زبان‌های مبدأ و مقصد باید متفاوت باشند"
        }
        val source = entry.sourceText.trim()
        val translation = entry.translationText?.trim()
        require(source.isNotBlank() && !translation.isNullOrBlank()) { "مدخل ناقص" }
        val targetText = requireNotNull(translation)
        val sourceKey = computeCanonicalKey(source)
        val targetKey = computeCanonicalKey(targetText)

        val activeIds = conceptRepository.getAllActive().map { it.id }.toSet()
        val existingSource = contentRepository.getAll().firstOrNull {
            it.conceptId in activeIds &&
                it.languageCode == sourceLanguage &&
                it.canonicalKey == sourceKey
        }

        val conceptId = if (existingSource == null) {
            createConcept.createInTransaction(
                CreateConceptCommand(
                    sourceText = source,
                    targetText = targetText,
                    sourceLanguage = sourceLanguage,
                    targetLanguage = targetLanguage,
                    notes = entry.notes,
                    entryType = entry.entryType.toDomainEntryType()
                )
            )
        } else {
            val existingTarget = contentRepository.find(existingSource.conceptId, targetLanguage)
            if (existingTarget?.canonicalKey == targetKey) {
                throw DuplicateConceptException("این واژه با همین ترجمه قبلاً در کتابخانه وجود دارد")
            }
            if (existingTarget == null) {
                contentRepository.upsert(
                    Content(UUID.randomUUID(), existingSource.conceptId, targetLanguage, targetText, targetKey)
                )
            } else {
                val mergedText = mergeTranslationText(existingTarget.text, targetText)
                if (mergedText != existingTarget.text) {
                    contentRepository.upsert(
                        existingTarget.copy(
                            text = mergedText,
                            canonicalKey = computeCanonicalKey(mergedText)
                        )
                    )
                }
            }
            existingSource.conceptId
        }

        parserMetadataRepository.upsert(
            conceptId,
            ParserMetadata(
                breakdown = entry.breakdown.map { it.text },
                relationships = entry.relationships.map { "${it.label}: ${it.text}" },
                variants = entry.variants.map { it.text },
                confidence = entry.confidence.coerceIn(0.0, 1.0)
            )
        )
        conceptId
    }

    private fun mergeTranslationText(existing: String, incoming: String): String {
        val parts = existing.split(" / ")
            .map { it.trim() }
            .filter { it.isNotBlank() }
        if (parts.any { computeCanonicalKey(it) == computeCanonicalKey(incoming) }) return existing
        return (parts + incoming.trim()).joinToString(" / ")
    }

    private fun EntryKind.toDomainEntryType() = when (this) {
        EntryKind.WORD -> EntryType.WORD
        EntryKind.PHRASE -> EntryType.PHRASE
        EntryKind.SENTENCE -> EntryType.SENTENCE
        EntryKind.IDIOM -> EntryType.IDIOM
        EntryKind.COLLOCATION -> EntryType.COLLOCATION
        EntryKind.STRUCTURE -> EntryType.STRUCTURE
    }
}
