package com.flashlearn.domain.usecase

import com.flashlearn.domain.model.*
import com.flashlearn.domain.repository.*
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class RemoveExactDuplicateConceptsUseCase @Inject constructor(
    private val conceptRepository: ConceptRepository,
    private val contentRepository: ContentRepository,
    private val database: FlashLearnDatabase,
    private val learningRepository: com.flashlearn.domain.repository.LearningStateRepository,
    private val difficultyRepository: com.flashlearn.domain.repository.DifficultyStateRepository,
    private val conceptTagRepository: com.flashlearn.domain.repository.ConceptTagRepository = EmptyConceptTagRepository
) {
    constructor(conceptRepository: ConceptRepository, contentRepository: ContentRepository, database: FlashLearnDatabase) : this(conceptRepository, contentRepository, database, EmptyLearningStateRepository, EmptyDifficultyStateRepository, EmptyConceptTagRepository) {
    }
    suspend operator fun invoke(sourceLanguage: String = "es", targetLanguage: String = "fa"): Int = database.withTransaction {
        require(sourceLanguage.isNotBlank() && targetLanguage.isNotBlank() && sourceLanguage != targetLanguage) { "زبان‌های مبدأ و مقصد باید متفاوت باشند" }
        val activeConcepts = conceptRepository.getAllActive()
        if (activeConcepts.size < 2) return@withTransaction 0
        val contentsByConcept = contentRepository.getAll().groupBy(Content::conceptId)
        val groups: Collection<List<Concept>> = activeConcepts.mapNotNull { concept ->
            contentsByConcept[concept.id].orEmpty().firstOrNull { it.languageCode == sourceLanguage && it.text.isNotBlank() }?.let { computeCanonicalKey(it.text) to concept }
        }.groupBy({ it.first }, { it.second }).values.filter { it.size > 1 }
        var removed = 0
        for (group in groups) {
            val ordered = group.sortedWith(compareBy<Concept> { it.createdAt }.thenBy { it.id.toString() })
            val survivor = ordered.first()
            var survivorLearning = learningRepository.get(survivor.id)
            var survivorDifficulty = difficultyRepository.get(survivor.id)
            val survivorTags = conceptTagRepository.getTagsForConcept(survivor.id).toSet()
            val survivorTargets = contentRepository.findAll(survivor.id, targetLanguage).sortedBy { it.translationIndex }
            val existingTargetKeys = survivorTargets.map { computeCanonicalKey(it.text) }.filter(String::isNotBlank).toMutableSet()
            var nextIndex = (survivorTargets.maxOfOrNull { it.translationIndex } ?: -1) + 1
            ordered.drop(1).forEach { duplicate ->
                contentsByConcept[duplicate.id].orEmpty().asSequence().filter { it.languageCode == targetLanguage && it.text.isNotBlank() }.sortedBy { it.translationIndex }.forEach { content ->
                    val targetKey = computeCanonicalKey(content.text)
                    if (targetKey.isNotBlank() && existingTargetKeys.add(targetKey)) contentRepository.insertTranslation(Content(UUID.randomUUID(), survivor.id, targetLanguage, content.text.trim(), targetKey, notes = content.notes, translationIndex = nextIndex++, grammarNote = content.grammarNote, possibleCorrection = content.possibleCorrection))
                }
                learningRepository.get(duplicate.id)?.let { duplicateLearning ->
                    val mergedLearning = mergeLearningStates(survivorLearning, duplicateLearning, survivor.id)
                    learningRepository.upsert(mergedLearning)
                    survivorLearning = mergedLearning
                }
                difficultyRepository.get(duplicate.id)?.let { duplicateDifficulty ->
                    val mergedDifficulty = mergeDifficultyStates(survivorDifficulty, duplicateDifficulty, survivor.id)
                    difficultyRepository.upsert(mergedDifficulty)
                    survivorDifficulty = mergedDifficulty
                }
                conceptRepository.update(survivor.copy(favorite = survivor.favorite || duplicate.favorite, updatedAt = Instant.now()))
                conceptTagRepository.getTagsForConcept(duplicate.id).filter { it !in survivorTags }.forEach { conceptTagRepository.insert(com.flashlearn.domain.model.ConceptTag(survivor.id, it)) }
                conceptRepository.softDelete(duplicate.id, Instant.now()); removed++
            }
        }
        removed
    }
}

private fun mergeLearningStates(
    survivor: LearningState?,
    duplicate: LearningState,
    survivorConceptId: UUID
): LearningState {
    if (survivor == null) {
        return duplicate.copy(conceptId = survivorConceptId)
    }

    val rank = mapOf(
        Stage.DAILY to 0,
        Stage.WEEKLY to 1,
        Stage.MONTHLY to 2,
        Stage.LEARNED to 3
    )
    val preferred = if (rank.getValue(duplicate.stage) > rank.getValue(survivor.stage)) {
        duplicate
    } else {
        survivor
    }

    return preferred.copy(
        id = survivor.id,
        conceptId = survivorConceptId,
        totalCorrect = survivor.totalCorrect + duplicate.totalCorrect,
        totalWrong = survivor.totalWrong + duplicate.totalWrong,
        lastReviewedAt = listOfNotNull(
            survivor.lastReviewedAt,
            duplicate.lastReviewedAt
        ).maxOrNull()
    )
}

private fun mergeDifficultyStates(
    survivor: DifficultyState?,
    duplicate: DifficultyState,
    survivorConceptId: UUID
): DifficultyState {
    if (survivor == null) {
        return duplicate.copy(conceptId = survivorConceptId)
    }

    return if (duplicate.current.ordinal > survivor.current.ordinal) {
        duplicate.copy(
            id = survivor.id,
            conceptId = survivorConceptId
        )
    } else {
        survivor
    }
}

private object EmptyLearningStateRepository : LearningStateRepository { override suspend fun get(conceptId:UUID):LearningState?=null; override suspend fun upsert(state:LearningState)=Unit; override suspend fun getAllByStage(stage:Stage)=emptyList<LearningState>(); override suspend fun getDueNonLearned(now:Instant)=emptyList<LearningState>(); override suspend fun getAll()=emptyList<LearningState>() }
private object EmptyDifficultyStateRepository : DifficultyStateRepository { override suspend fun get(conceptId:UUID):DifficultyState?=null; override suspend fun upsert(state:DifficultyState)=Unit; override suspend fun delete(conceptId:UUID)=Unit; override suspend fun getAll()=emptyList<DifficultyState>() }
private object EmptyConceptTagRepository : ConceptTagRepository { override suspend fun insert(conceptTag:ConceptTag)=Unit; override suspend fun getTagsForConcept(conceptId:UUID)=emptyList<UUID>(); override suspend fun getConceptsForTag(tagId:UUID)=emptyList<UUID>(); override suspend fun getAll()=emptyList<ConceptTag>() }


data class ExactDuplicateGroup(val sourceText: String, val count: Int)

class FindExactDuplicateConceptsUseCase @Inject constructor(
    private val conceptRepository: ConceptRepository,
    private val contentRepository: ContentRepository
) {
    suspend operator fun invoke(sourceLanguage: String = "es", targetLanguage: String = "fa"): List<ExactDuplicateGroup> {
        require(sourceLanguage.isNotBlank() && targetLanguage.isNotBlank() && sourceLanguage != targetLanguage) { "زبان‌های مبدأ و مقصد باید متفاوت باشند" }
        val activeConcepts = conceptRepository.getAllActive()
        if (activeConcepts.size < 2) return emptyList()
        val contentsByConcept = contentRepository.getAll().groupBy(Content::conceptId)
        return activeConcepts.mapNotNull { concept ->
            contentsByConcept[concept.id].orEmpty().firstOrNull { it.languageCode == sourceLanguage && it.text.isNotBlank() }
                ?.let { computeCanonicalKey(it.text) to it.text.trim() }
        }.filter { it.first.isNotBlank() }.groupBy({ it.first }, { it.second }).values.filter { it.size > 1 }
            .map { texts -> ExactDuplicateGroup(texts.first(), texts.size) }.sortedBy { it.sourceText.lowercase() }
    }
}
