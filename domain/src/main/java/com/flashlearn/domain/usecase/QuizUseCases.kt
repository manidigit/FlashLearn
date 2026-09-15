package com.flashlearn.domain.usecase

import com.flashlearn.domain.model.Content
import com.flashlearn.domain.model.Concept
import com.flashlearn.domain.model.DifficultyState
import com.flashlearn.domain.model.QuizDifficulty
import com.flashlearn.domain.model.VocabularyDifficulty
import com.flashlearn.domain.repository.ContentRepository
import com.flashlearn.domain.repository.ConceptRepository
import com.flashlearn.domain.repository.DifficultyStateRepository
import java.text.Normalizer
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

private fun normalizeQuizText(text: String): String = Normalizer.normalize(text.trim().replace(Regex("\\s+"), " "), Normalizer.Form.NFC).lowercase(Locale.ROOT)

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

data class QuizLanguagePair(val sourceLanguage: String, val targetLanguage: String) {
    init {
        require(sourceLanguage.isNotBlank()) { "sourceLanguage must not be blank" }
        require(targetLanguage.isNotBlank()) { "targetLanguage must not be blank" }
        require(!sourceLanguage.equals(targetLanguage, ignoreCase = true)) { "sourceLanguage and targetLanguage must differ" }
    }
}

private data class QuizBank(
    val contents: List<Content>,
    val concepts: List<Concept>,
    val difficultiesById: Map<UUID, DifficultyState>
)

class GenerateQuizQuestionUseCase @Inject constructor(
    private val contentRepository: ContentRepository,
    private val conceptRepository: ConceptRepository,
    private val difficultyStateRepository: DifficultyStateRepository
) {
    private var bank: QuizBank? = null

    suspend fun refreshBank() {
        bank = QuizBank(
            contents = contentRepository.getAll(),
            concepts = conceptRepository.getAllActive(),
            difficultiesById = difficultyStateRepository.getAll().associateBy { it.conceptId }
        )
    }

    suspend operator fun invoke(
        concept: Concept,
        activeLanguagePair: QuizLanguagePair,
        difficultyState: DifficultyState?,
        difficulty: QuizDifficulty = QuizDifficulty.MEDIUM
    ): QuizQuestionResult {
        if (!concept.active) return QuizQuestionResult.FlashcardFallback

        val snapshot = bank ?: run {
            refreshBank()
            bank!!
        }
        val allContents = snapshot.contents
        val allConcepts = snapshot.concepts
        val difficultiesById = snapshot.difficultiesById
        val contentsByConcept = allContents.groupBy { it.conceptId }

        val conceptContents = contentsByConcept[concept.id].orEmpty()
        val prompt = conceptContents.firstOrNull {
            it.languageCode.equals(activeLanguagePair.sourceLanguage, true) && it.text.isNotBlank()
        } ?: return QuizQuestionResult.FlashcardFallback
        val correct = conceptContents.firstOrNull {
            it.languageCode.equals(activeLanguagePair.targetLanguage, true) && it.text.isNotBlank()
        } ?: return QuizQuestionResult.FlashcardFallback

        val effectiveDifficulty = difficultyState ?: DifficultyState(
            id = UUID.randomUUID(), conceptId = concept.id,
            current = VocabularyDifficulty.MEDIUM, consecutiveCorrect = 0,
            consecutiveWrong = 0, hasReachedVeryHard = false
        )

        val normalizedCorrect = normalizeQuizText(correct.text)
        fun unique(values: List<Content>) = values
            .filter { it.text.isNotBlank() }
            .distinctBy { normalizeQuizText(it.text) }
            .filter { normalizeQuizText(it.text) != normalizedCorrect }

        val targetContents = contentsByConcept.mapValues { (_, values) ->
            values.filter {
                it.languageCode.equals(activeLanguagePair.targetLanguage, true) && it.text.isNotBlank()
            }
        }
        val validConceptIds = targetContents.filterValues { it.isNotEmpty() }.keys
        val sourceConceptIds = contentsByConcept
            .filterValues { values -> values.any { it.languageCode.equals(activeLanguagePair.sourceLanguage, true) && it.text.isNotBlank() } }
            .keys
        val eligible = allConcepts.filter { other ->
            other.id != concept.id && other.id in validConceptIds && other.id in sourceConceptIds
        }

        fun candidatesFor(level: QuizDifficulty, categoryOnly: Boolean): List<Content> {
            val concepts = eligible.filter { other ->
                if (categoryOnly && concept.categoryId != null && other.categoryId != concept.categoryId) return@filter false
                when (level) {
                    QuizDifficulty.EASY -> true
                    QuizDifficulty.MEDIUM -> difficultiesById[other.id]?.current == effectiveDifficulty.current
                    QuizDifficulty.HARD -> difficultiesById[other.id]?.current == effectiveDifficulty.current && other.entryType == concept.entryType
                }
            }
            return unique(concepts.flatMap { targetContents[it.id].orEmpty() })
        }

        val levels = when (difficulty) {
            QuizDifficulty.EASY -> listOf(QuizDifficulty.EASY)
            QuizDifficulty.MEDIUM -> listOf(QuizDifficulty.MEDIUM, QuizDifficulty.EASY)
            QuizDifficulty.HARD -> listOf(QuizDifficulty.HARD, QuizDifficulty.MEDIUM, QuizDifficulty.EASY)
        }

        var candidates = emptyList<Content>()
        for (level in levels) {
            candidates = unique(candidates + candidatesFor(level, categoryOnly = true))
            if (candidates.size >= 3) break
        }
        if (candidates.size < 3) {
            for (level in levels) {
                candidates = unique(candidates + candidatesFor(level, categoryOnly = false))
                if (candidates.size >= 3) break
            }
        }

        if (candidates.size < 3) return QuizQuestionResult.FlashcardFallback
        val wrongOptions = candidates.shuffled().take(3).map { it.text }
        return QuizQuestionResult.QuizQuestion(prompt.text, correct.text, (listOf(correct.text) + wrongOptions).shuffled())
    }
}
