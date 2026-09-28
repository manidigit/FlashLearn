package com.flashlearn.domain.usecase

import com.flashlearn.domain.model.Content
import com.flashlearn.domain.model.Concept
import com.flashlearn.domain.repository.ConceptRepository
import com.flashlearn.domain.repository.ContentRepository
import com.flashlearn.domain.repository.FlashLearnDatabase
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
            val survivorLearning = learningRepository.get(survivor.id)
            val survivorDifficulty = difficultyRepository.get(survivor.id)
            val survivorTags = conceptTagRepository.getTagsForConcept(survivor.id).toSet()
            val survivorTargets = contentRepository.findAll(survivor.id, targetLanguage).sortedBy { it.translationIndex }
            val existingTargetKeys = survivorTargets.map { computeCanonicalKey(it.text) }.filter(String::isNotBlank).toMutableSet()
            var nextIndex = (survivorTargets.maxOfOrNull { it.translationIndex } ?: -1) + 1
            ordered.drop(1).forEach { duplicate ->
                contentsByConcept[duplicate.id].orEmpty().asSequence().filter { it.languageCode == targetLanguage && it.text.isNotBlank() }.sortedBy { it.translationIndex }.forEach { content ->
                    val targetKey = computeCanonicalKey(content.text)
                    if (targetKey.isNotBlank() && existingTargetKeys.add(targetKey)) contentRepository.insertTranslation(Content(UUID.randomUUID(), survivor.id, targetLanguage, content.text.trim(), targetKey, notes = content.notes, pronunciation = content.pronunciation, example = content.example, translationIndex = nextIndex++, grammarNote = content.grammarNote, possibleCorrection = content.possibleCorrection))
                }
                learningRepository.get(duplicate.id)?.let { duplicateLearning ->
                    if (survivorLearning != null) {
                        val rank = mapOf(com.flashlearn.domain.model.Stage.DAILY to 0, com.flashlearn.domain.model.Stage.WEEKLY to 1, com.flashlearn.domain.model.Stage.MONTHLY to 2, com.flashlearn.domain.model.Stage.LEARNED to 3)
                        val preferred = if (rank.getValue(duplicateLearning.stage) > rank.getValue(survivorLearning.stage)) duplicateLearning else survivorLearning
                        learningRepository.upsert(preferred.copy(id = survivorLearning.id, totalCorrect = survivorLearning.totalCorrect + duplicateLearning.totalCorrect, totalWrong = survivorLearning.totalWrong + duplicateLearning.totalWrong, lastReviewedAt = listOfNotNull(survivorLearning.lastReviewedAt, duplicateLearning.lastReviewedAt).maxOrNull()))
                    }
                }
                difficultyRepository.get(duplicate.id)?.let { duplicateDifficulty ->
                    if (survivorDifficulty != null && duplicateDifficulty.current.ordinal > survivorDifficulty.current.ordinal) difficultyRepository.upsert(duplicateDifficulty.copy(id = survivorDifficulty.id))
                }
                conceptRepository.update(survivor.copy(favorite = survivor.favorite || duplicate.favorite, updatedAt = Instant.now()))
                conceptTagRepository.getTagsForConcept(duplicate.id).filter { it !in survivorTags }.forEach { conceptTagRepository.insert(com.flashlearn.domain.model.ConceptTag(survivor.id, it)) }
                conceptRepository.softDelete(duplicate.id, Instant.now()); removed++
            }
        }
        removed
    }
}

private object EmptyLearningStateRepository : LearningStateRepository { override suspend fun get(conceptId:UUID):LearningState?=null; override suspend fun upsert(state:LearningState)=Unit; override suspend fun getAllByStage(stage:Stage)=emptyList<LearningState>(); override suspend fun getDueNonLearned(now:Instant)=emptyList<LearningState>(); override suspend fun getAll()=emptyList<LearningState>() }
private object EmptyDifficultyStateRepository : DifficultyStateRepository { override suspend fun get(conceptId:UUID):DifficultyState?=null; override suspend fun upsert(state:DifficultyState)=Unit; override suspend fun delete(conceptId:UUID)=Unit; override suspend fun getAll()=emptyList<DifficultyState>() }
private object EmptyConceptTagRepository : ConceptTagRepository { override suspend fun insert(conceptTag:ConceptTag)=Unit; override suspend fun getTagsForConcept(conceptId:UUID)=emptyList<UUID>(); override suspend fun getConceptsForTag(tagId:UUID)=emptyList<UUID>(); override suspend fun getAll()=emptyList<ConceptTag>() }
