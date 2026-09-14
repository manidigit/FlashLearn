package com.flashlearn.domain.usecase

import com.flashlearn.domain.model.Content
import com.flashlearn.domain.model.Concept
import com.flashlearn.domain.model.DifficultyState
import com.flashlearn.domain.repository.ContentRepository
import com.flashlearn.domain.repository.ConceptRepository
import com.flashlearn.domain.repository.DifficultyStateRepository
import java.text.Normalizer
import java.util.Locale
import javax.inject.Inject

private fun normalizeQuizText(text: String): String =
    Normalizer.normalize(
        text.trim().replace(Regex("\\s+"), " "),
        Normalizer.Form.NFC
    ).lowercase(Locale.ROOT)

sealed interface QuizQuestionResult {
    data class QuizQuestion(
        val promptText: String,
        val correctAnswerText: String,
        val options: List<String>
    ) : QuizQuestionResult {
        init {
            require(promptText.isNotBlank()) { "Quiz prompt must not be blank" }
            require(correctAnswerText.isNotBlank()) { "Quiz correct answer must not be blank" }
            require(options.size == 4) { "QuizQuestion must contain exactly 4 options" }
            require(options.all { it.isNotBlank() }) { "Quiz options must not be blank" }
            require(options.map(::normalizeQuizText).distinct().size == 4) {
                "Quiz options must be unique after normalization"
            }
            require(options.count { normalizeQuizText(it) == normalizeQuizText(correctAnswerText) } == 1) {
                "QuizQuestion must contain exactly one correct option"
            }
        }
    }

    data object FlashcardFallback : QuizQuestionResult
}

data class QuizLanguagePair(val sourceLanguage: String, val targetLanguage: String)

/** Read-only implementation of the frozen GenerateQuizQuestion algorithm. */
class GenerateQuizQuestionUseCase @Inject constructor(
    private val contentRepository: ContentRepository,
    private val conceptRepository: ConceptRepository,
    private val difficultyStateRepository: DifficultyStateRepository
) {
    suspend operator fun invoke(
        concept: Concept,
        activeLanguagePair: QuizLanguagePair,
        difficultyState: DifficultyState
    ): QuizQuestionResult {
        if (!concept.active || activeLanguagePair.sourceLanguage.isBlank() || activeLanguagePair.targetLanguage.isBlank()) {
            return QuizQuestionResult.FlashcardFallback
        }

        val allContents = contentRepository.getAll()
        val prompt = allContents.firstOrNull {
            it.conceptId == concept.id && it.languageCode == activeLanguagePair.sourceLanguage && it.text.isNotBlank()
        } ?: return QuizQuestionResult.FlashcardFallback
        val correct = allContents.firstOrNull {
            it.conceptId == concept.id && it.languageCode == activeLanguagePair.targetLanguage && it.text.isNotBlank()
        } ?: return QuizQuestionResult.FlashcardFallback

        val activeConcepts = conceptRepository.getAllActive()
            .asSequence()
            .filter { it.id != concept.id }
            .filter { other ->
                allContents.any { it.conceptId == other.id && it.languageCode == activeLanguagePair.sourceLanguage && it.text.isNotBlank() } &&
                    allContents.any { it.conceptId == other.id && it.languageCode == activeLanguagePair.targetLanguage && it.text.isNotBlank() }
            }
            .toList()
        val activeIds = activeConcepts.map { it.id }.toSet()
        val normalizedCorrect = normalizeQuizText(correct.text)

        fun unique(values: List<Content>): List<Content> = values
            .filter { it.text.isNotBlank() }
            .distinctBy { normalizeQuizText(it.text) }
            .filter { normalizeQuizText(it.text) != normalizedCorrect }

        // The frozen V1 algorithm uses VocabularyDifficulty only as the first
        // candidate pool, then broadens to adjacent levels and finally the full
        // eligible bank. It never changes DifficultyState or scheduling.
        suspend fun findCandidates(filter: com.flashlearn.domain.model.VocabularyDifficulty?): List<Content> {
            val out = mutableListOf<Content>()
            for (content in allContents) {
                if (content.languageCode != activeLanguagePair.targetLanguage || content.conceptId !in activeIds) continue
                if (normalizeQuizText(content.text) == normalizedCorrect) continue
                if (filter != null) {
                    val state = difficultyStateRepository.get(content.conceptId) ?: continue
                    if (state.current != filter) continue
                }
                out += content
            }
            return unique(out)
        }

        var candidates = findCandidates(difficultyState.current).toMutableList()
        if (candidates.size < 3) {
            for (difficulty in adjacentDifficulties(difficultyState.current)) {
                candidates += unique(findCandidates(difficulty))
                candidates = unique(candidates).toMutableList()
                if (candidates.size >= 3) break
            }
        }
        if (candidates.size < 3) {
            candidates += findCandidates(null)
            candidates = unique(candidates).toMutableList()
        }
        if (candidates.size < 3) return QuizQuestionResult.FlashcardFallback

        val wrongOptions = candidates.shuffled().take(3).map { it.text }
        val options = (listOf(correct.text) + wrongOptions).shuffled()
        return QuizQuestionResult.QuizQuestion(
            promptText = prompt.text,
            correctAnswerText = correct.text,
            options = options
        )
    }

    private fun adjacentDifficulties(value: com.flashlearn.domain.model.VocabularyDifficulty): List<com.flashlearn.domain.model.VocabularyDifficulty> = when (value) {
        com.flashlearn.domain.model.VocabularyDifficulty.EASY -> listOf(com.flashlearn.domain.model.VocabularyDifficulty.MEDIUM)
        com.flashlearn.domain.model.VocabularyDifficulty.MEDIUM -> listOf(com.flashlearn.domain.model.VocabularyDifficulty.HARD, com.flashlearn.domain.model.VocabularyDifficulty.EASY)
        com.flashlearn.domain.model.VocabularyDifficulty.HARD -> listOf(com.flashlearn.domain.model.VocabularyDifficulty.VERY_HARD, com.flashlearn.domain.model.VocabularyDifficulty.MEDIUM)
        com.flashlearn.domain.model.VocabularyDifficulty.VERY_HARD -> listOf(com.flashlearn.domain.model.VocabularyDifficulty.HARD)
    }
}
