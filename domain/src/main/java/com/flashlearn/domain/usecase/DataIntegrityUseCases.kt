package com.flashlearn.domain.usecase

import com.flashlearn.domain.model.Stage
import com.flashlearn.domain.repository.ConceptRepository
import com.flashlearn.domain.repository.DifficultyStateRepository
import com.flashlearn.domain.repository.LearningStateRepository
import com.flashlearn.domain.repository.ReviewHistoryRepository
import javax.inject.Inject

data class DataIntegrityIssue(val code: String, val message: String, val conceptId: String? = null)
data class DataIntegrityReport(val valid: Boolean, val issues: List<DataIntegrityIssue>) {
    val issueCount: Int get() = issues.size
}

class ValidateDataIntegrityUseCase @Inject constructor(
    private val concepts: ConceptRepository,
    private val learning: LearningStateRepository,
    private val difficulty: DifficultyStateRepository,
    private val history: ReviewHistoryRepository
) {
    suspend operator fun invoke(): DataIntegrityReport {
        val issues = mutableListOf<DataIntegrityIssue>()
        val activeConcepts = concepts.getAllActive()
        val conceptIds = activeConcepts.map { it.id }.toSet()
        val learningStates = learning.getAll()
        val difficultyStates = difficulty.getAll()
        val learningByConcept = learningStates.groupBy { it.conceptId }
        val difficultyByConcept = difficultyStates.groupBy { it.conceptId }

        activeConcepts.forEach { concept ->
            val ls = learningByConcept[concept.id].orEmpty()
            val ds = difficultyByConcept[concept.id].orEmpty()
            if (ls.size != 1) issues += DataIntegrityIssue(
                "LEARNING_STATE_CARDINALITY",
                "Active concept must have exactly one LearningState; found ${ls.size}",
                concept.id.toString()
            )
            if (ds.size != 1) issues += DataIntegrityIssue(
                "DIFFICULTY_STATE_CARDINALITY",
                "Active concept must have exactly one DifficultyState; found ${ds.size}",
                concept.id.toString()
            )
            ls.singleOrNull()?.let {
                if (it.stage == Stage.LEARNED && it.nextReviewAt != null) {
                    issues += DataIntegrityIssue("LEARNED_HAS_DUE_DATE", "LEARNED state must have null nextReviewAt", concept.id.toString())
                }
                if (it.monthlyWrongCount < 0 || it.totalCorrect < 0 || it.totalWrong < 0) {
                    issues += DataIntegrityIssue("NEGATIVE_COUNTER", "Learning counters cannot be negative", concept.id.toString())
                }
            }
            ds.singleOrNull()?.let {
                if (it.consecutiveCorrect > 0 && it.consecutiveWrong > 0) {
                    issues += DataIntegrityIssue("DIFFICULTY_COUNTER_CONFLICT", "Consecutive correct and wrong counters cannot both be positive", concept.id.toString())
                }
            }
        }

        val orphanLearning = learningStates.filter { it.conceptId !in conceptIds }
        orphanLearning.forEach {
            issues += DataIntegrityIssue("ORPHAN_LEARNING_STATE", "LearningState references a non-active concept", it.conceptId.toString())
        }
        val orphanDifficulty = difficultyStates.filter { it.conceptId !in conceptIds }
        orphanDifficulty.forEach {
            issues += DataIntegrityIssue("ORPHAN_DIFFICULTY_STATE", "DifficultyState references a non-active concept", it.conceptId.toString())
        }

        history.getAll().forEach {
            if (it.conceptId !in conceptIds) {
                issues += DataIntegrityIssue("ORPHAN_REVIEW_HISTORY", "ReviewHistory references a non-active concept", it.conceptId.toString())
            }
        }
        return DataIntegrityReport(issues.isEmpty(), issues)
    }
}
