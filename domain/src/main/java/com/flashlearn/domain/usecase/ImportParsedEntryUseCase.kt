package com.flashlearn.domain.model.EntryType
package com.flashlearn.domain.model.ParserMetadata
package com.flashlearn.domain.parser.EntryKind
package com.flashlearn.domain.parser.ParsedEntry
package com.flashlearn.domain.repository.FlashLearnDatabase
package com.flashlearn.domain.repository.ParserMetadataRepository
import java.util.UUID
import javax.inject.Inject

/**
 * Imports one parsed vocabulary entry as a single atomic unit.
 *
 * The Concept/Content/Learning/Difficulty records and parser metadata must either
 * all persist or all roll back. This prevents a successful concept from being left
 * behind when metadata persistence fails.
 */
class ImportParsedEntryUseCase @Inject constructor(
    private val createConcept: CreateConceptUseCase,
    private val parserMetadataRepository: ParserMetadataRepository,
    private val database: FlashLearnDatabase
) {
    suspend operator fun invoke(entry: ParsedEntry): UUID = database.withTransaction {
        val source = entry.sourceText.trim()
        val translation = entry.translationText?.trim()
        require(source.isNotBlank() && !translation.isNullOrBlank()) { "مدخل ناقص" }
        val targetText = requireNotNull(translation)

        val conceptId = createConcept.createInTransaction(
            CreateConceptCommand(
                sourceText = source,
                targetText = targetText,
                notes = entry.notes,
                entryType = entry.entryType.toDomainEntryType()
            )
        )
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

    private fun EntryKind.toDomainEntryType() = when (this) {
        EntryKind.WORD -> EntryType.WORD
        EntryKind.PHRASE -> EntryType.PHRASE
        EntryKind.SENTENCE -> EntryType.SENTENCE
        EntryKind.IDIOM -> EntryType.IDIOM
        EntryKind.COLLOCATION -> EntryType.COLLOCATION
        EntryKind.STRUCTURE -> EntryType.STRUCTURE
    }
}
