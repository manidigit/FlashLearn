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

private fun normalizeQuizText(text: String): String = Normalizer.normalize(text.trim().replace(Regex("\\s+"), " "), Normalizer.Form.NFC).lowercase(Locale.ROOT)

enum class QuizChallenge { A, B, C }

object QuizChallengeProvider {
    @Volatile var current: QuizChallenge = QuizChallenge.B
}

sealed interface QuizQuestionResult {
    data class QuizQuestion(val promptText: String, val correctAnswerText: String, val options: List<String>) : QuizQuestionResult {
        init {
            require(promptText.isNotBlank())
            require(correctAnswerText.isNotBlank())
            require(options.size == 4)
            require(options.all { it.isNotBlank() })
            require(options.map(::normalizeQuizText).distinct().size == 4)
            require(options.count { normalizeQuizText(it) == normalizeQuizText(correctAnswerText) } == 1)
        }
    }
    data object FlashcardFallback : QuizQuestionResult
}

data class QuizLanguagePair(val sourceLanguage: String, val targetLanguage: String)

class GenerateQuizQuestionUseCase @Inject constructor(
    private val contentRepository: ContentRepository,
    private val conceptRepository: ConceptRepository,
    private val difficultyStateRepository: DifficultyStateRepository
) {
    suspend operator fun invoke(
        concept: Concept,
        activeLanguagePair: QuizLanguagePair,
        difficultyState: DifficultyState,
        challenge: QuizChallenge = QuizChallengeProvider.current
    ): QuizQuestionResult {
        if (!concept.active || activeLanguagePair.sourceLanguage.isBlank() || activeLanguagePair.targetLanguage.isBlank()) return QuizQuestionResult.FlashcardFallback
        val allContents = contentRepository.getAll()
        val prompt = allContents.firstOrNull { it.conceptId == concept.id && it.languageCode == activeLanguagePair.sourceLanguage && it.text.isNotBlank() } ?: return QuizQuestionResult.FlashcardFallback
        val correct = allContents.firstOrNull { it.conceptId == concept.id && it.languageCode == activeLanguagePair.targetLanguage && it.text.isNotBlank() } ?: return QuizQuestionResult.FlashcardFallback
        val categoryId = concept.categoryId ?: return QuizQuestionResult.FlashcardFallback
        val activeConcepts = conceptRepository.getAllActive().filter { other ->
            other.id != concept.id && other.categoryId == categoryId &&
                allContents.any { it.conceptId == other.id && it.languageCode == activeLanguagePair.sourceLanguage && it.text.isNotBlank() } &&
                allContents.any { it.conceptId == other.id && it.languageCode == activeLanguagePair.targetLanguage && it.text.isNotBlank() }
        }
        val normalizedCorrect = normalizeQuizText(correct.text)
        fun unique(values: List<Content>) = values.filter { it.text.isNotBlank() }.distinctBy { normalizeQuizText(it.text) }.filter { normalizeQuizText(it.text) != normalizedCorrect }
        val targetContents = allContents.filter { it.languageCode == activeLanguagePair.targetLanguage && it.text.isNotBlank() }.groupBy { it.conceptId }
        suspend fun candidatesFor(level: QuizChallenge): List<Content> {
            val concepts = activeConcepts.filter { other ->
                when (level) {
                    QuizChallenge.A -> true
                    QuizChallenge.B -> difficultyStateRepository.get(other.id)?.current == difficultyState.current
                    QuizChallenge.C -> difficultyStateRepository.get(other.id)?.current == difficultyState.current && other.entryType == concept.entryType
                }
            }
            return unique(concepts.flatMap { targetContents[it.id].orEmpty() })
        }
        val levels = when (challenge) {
            QuizChallenge.A -> listOf(QuizChallenge.A)
            QuizChallenge.B -> listOf(QuizChallenge.B, QuizChallenge.A)
            QuizChallenge.C -> listOf(QuizChallenge.C, QuizChallenge.B, QuizChallenge.A)
        }
        var candidates = emptyList<Content>()
        for (level in levels) {
            candidates = unique(candidates + candidatesFor(level))
            if (candidates.size >= 3) break
        }
        if (candidates.size < 3) return QuizQuestionResult.FlashcardFallback
        val wrongOptions = candidates.shuffled().take(3).map { it.text }
        return QuizQuestionResult.QuizQuestion(prompt.text, correct.text, (listOf(correct.text) + wrongOptions).shuffled())
    }
}
