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
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

private fun normalizeQuizText(text: String): String =
    Normalizer.normalize(text.trim().replace(Regex("\\s+"), " "), Normalizer.Form.NFC).lowercase(Locale.ROOT)

private fun quizTokens(text: String): Set<String> =
    normalizeQuizText(text).split(Regex("[^\\p{L}\\p{N}]+" )).filter { it.isNotBlank() }.toSet()

private fun levenshteinSimilarity(a: String, b: String): Double {
    if (a == b) return 1.0
    if (a.isEmpty() || b.isEmpty()) return 0.0
    var previous = IntArray(b.length + 1) { it }
    var current = IntArray(b.length + 1)
    for (i in a.indices) {
        current[0] = i + 1
        for (j in b.indices) {
            val substitution = previous[j] + if (a[i] == b[j]) 0 else 1
            current[j + 1] = min(min(previous[j + 1] + 1, current[j] + 1), substitution)
        }
        val tmp = previous
        previous = current
        current = tmp
    }
    val distance = previous[b.length]
    return 1.0 - distance.toDouble() / max(a.length, b.length).toDouble()
}

private fun ngramSimilarity(a: String, b: String, n: Int = 2): Double {
    fun grams(value: String): Set<String> = if (value.length <= n) setOf(value) else value.windowed(n).toSet()
    val left = grams(a)
    val right = grams(b)
    if (left.isEmpty() && right.isEmpty()) return 1.0
    if (left.isEmpty() || right.isEmpty()) return 0.0
    return left.intersect(right).size.toDouble() / left.union(right).size
}

private fun lexicalSimilarity(a: String, b: String): Double {
    val left = normalizeQuizText(a)
    val right = normalizeQuizText(b)
    val leftTokens = quizTokens(left)
    val rightTokens = quizTokens(right)
    val tokenSimilarity = if (leftTokens.isEmpty() && rightTokens.isEmpty()) 1.0
    else if (leftTokens.isEmpty() || rightTokens.isEmpty()) 0.0
    else leftTokens.intersect(rightTokens).size.toDouble() / leftTokens.union(rightTokens).size
    return (tokenSimilarity * 0.45 + levenshteinSimilarity(left, right) * 0.35 + ngramSimilarity(left, right) * 0.20).coerceIn(0.0, 1.0)
}

private const val MAX_DISTRACTOR_PAIR_SIMILARITY = 0.28

private fun distractorPairSimilarity(a: String, b: String): Double =
    lexicalSimilarity(a, b)

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

private data class QuizBank(val contents: List<Content>, val concepts: List<Concept>, val difficultiesById: Map<UUID, DifficultyState>)

private data class DistractorCandidate(
    val content: Content,
    val vocabularyDifficultyDistance: Int,
    val categoryMatch: Boolean,
    val entryTypeMatch: Boolean,
    val lexicalSimilarity: Double
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
        val contentsByConcept = snapshot.contents.groupBy { it.conceptId }
        val conceptContents = contentsByConcept[concept.id].orEmpty()
        val prompt = conceptContents.firstOrNull {
            it.languageCode.equals(activeLanguagePair.sourceLanguage, true) && it.text.isNotBlank()
        } ?: return QuizQuestionResult.FlashcardFallback
        val correct = conceptContents.firstOrNull {
            it.languageCode.equals(activeLanguagePair.targetLanguage, true) && it.text.isNotBlank()
        } ?: return QuizQuestionResult.FlashcardFallback
        val normalizedCorrect = normalizeQuizText(correct.text)
        val normalizedCorrectCanonical = normalizeQuizText(correct.canonicalKey)

        // Vocabulary Difficulty controls the documented candidate pool. Quiz Difficulty
        // is independent and controls how close/plausible the wrong answers are.
        val effectiveVocabularyDifficulty = difficultyState?.current ?: VocabularyDifficulty.MEDIUM
        val targetLanguageConceptIds = contentsByConcept.filterValues { values ->
            values.any { it.languageCode.equals(activeLanguagePair.targetLanguage, true) && it.text.isNotBlank() }
        }.keys
        val sourceLanguageConceptIds = contentsByConcept.filterValues { values ->
            values.any { it.languageCode.equals(activeLanguagePair.sourceLanguage, true) && it.text.isNotBlank() }
        }.keys
        val eligibleConcepts = snapshot.concepts.asSequence()
            .filter { it.active && it.id != concept.id }
            .filter { it.id in targetLanguageConceptIds && it.id in sourceLanguageConceptIds }
            .toList()

        fun difficultyDistance(other: Concept): Int {
            val otherDifficulty = snapshot.difficultiesById[other.id]?.current ?: return 3
            return when (effectiveVocabularyDifficulty) {
                VocabularyDifficulty.EASY -> when (otherDifficulty) {
                    VocabularyDifficulty.EASY -> 0
                    VocabularyDifficulty.MEDIUM -> 1
                    VocabularyDifficulty.HARD -> 2
                    VocabularyDifficulty.VERY_HARD -> 3
                }
                VocabularyDifficulty.MEDIUM -> when (otherDifficulty) {
                    VocabularyDifficulty.MEDIUM -> 0
                    VocabularyDifficulty.EASY, VocabularyDifficulty.HARD -> 1
                    VocabularyDifficulty.VERY_HARD -> 2
                }
                VocabularyDifficulty.HARD -> when (otherDifficulty) {
                    VocabularyDifficulty.HARD -> 0
                    VocabularyDifficulty.MEDIUM, VocabularyDifficulty.VERY_HARD -> 1
                    VocabularyDifficulty.EASY -> 2
                }
                VocabularyDifficulty.VERY_HARD -> when (otherDifficulty) {
                    VocabularyDifficulty.VERY_HARD -> 0
                    VocabularyDifficulty.HARD -> 1
                    VocabularyDifficulty.MEDIUM -> 2
                    VocabularyDifficulty.EASY -> 3
                }
            }
        }

        val candidates = eligibleConcepts.flatMap { other ->
            contentsByConcept[other.id].orEmpty()
                .filter { it.languageCode.equals(activeLanguagePair.targetLanguage, true) && it.text.isNotBlank() }
                .map { content ->
                    DistractorCandidate(
                        content = content,
                        vocabularyDifficultyDistance = difficultyDistance(other),
                        categoryMatch = concept.categoryId != null && concept.categoryId == other.categoryId,
                        entryTypeMatch = concept.entryType == other.entryType,
                        lexicalSimilarity = lexicalSimilarity(correct.text, content.text)
                    )
                }
        }
            .filter { normalizeQuizText(it.content.text) != normalizedCorrect }
            .filter {
                normalizedCorrectCanonical.isBlank() ||
                    normalizeQuizText(it.content.canonicalKey) != normalizedCorrectCanonical
            }
            .distinctBy { normalizeQuizText(it.content.text) }

        if (candidates.size < 3) return QuizQuestionResult.FlashcardFallback

        fun confusabilityScore(candidate: DistractorCandidate): Double {
            val category = if (candidate.categoryMatch) 1.0 else 0.0
            val entryType = if (candidate.entryTypeMatch) 1.0 else 0.0
            // Higher means "more likely to be confused with the correct answer".
            // This is deliberately local/deterministic: no network or external model.
            return (
                candidate.lexicalSimilarity * 0.55 +
                    category * 0.25 +
                    entryType * 0.20
                ).coerceIn(0.0, 1.0)
        }

        // Exact candidate-pool order from the specification:
        // same Vocabulary Difficulty -> immediate adjacent level(s) -> whole bank.
        val sameDifficultyPool = candidates.filter { it.vocabularyDifficultyDistance == 0 }
        val adjacentPool = candidates.filter { it.vocabularyDifficultyDistance == 1 }
        val selectedPool = when {
            sameDifficultyPool.size >= 3 -> sameDifficultyPool
            (sameDifficultyPool + adjacentPool).distinctBy { normalizeQuizText(it.content.text) }.size >= 3 ->
                (sameDifficultyPool + adjacentPool).distinctBy { normalizeQuizText(it.content.text) }
            else -> candidates
        }

        /*
         * Quiz difficulty is now an actual selection band, not just a weight tweak:
         *   EASY   -> least confusable valid distractors
         *   MEDIUM -> candidates nearest the middle of the confusability range
         *   HARD   -> most confusable valid distractors
         *
         * This makes the selected level observable when the bank contains enough
         * valid candidates, while the hard validity rules still take precedence.
         */
        val rankedPool = when (difficulty) {
            QuizDifficulty.EASY -> selectedPool.sortedWith(
                compareBy<DistractorCandidate> { confusabilityScore(it) }
                    .thenBy { normalizeQuizText(it.content.text) }
            )
            QuizDifficulty.MEDIUM -> selectedPool.sortedWith(
                compareBy<DistractorCandidate> { abs(confusabilityScore(it) - 0.50) }
                    .thenByDescending { confusabilityScore(it) }
                    .thenBy { normalizeQuizText(it.content.text) }
            )
            QuizDifficulty.HARD -> selectedPool.sortedWith(
                compareByDescending<DistractorCandidate> { confusabilityScore(it) }
                    .thenBy { normalizeQuizText(it.content.text) }
            )
        }

        // Do not allow two wrong answers that are near-duplicates of each other.
        // This is a separate constraint from similarity to the correct answer: two
        // distractors can both be individually plausible yet still express the same
        // alternative (for example, two different Persian phrasings of "Skype").
        val wrongOptions = buildList {
            for (candidate in rankedPool) {
                val text = candidate.content.text
                if (all { distractorPairSimilarity(it, text) < MAX_DISTRACTOR_PAIR_SIMILARITY }) {
                    add(text)
                }
                if (size == 3) break
            }
        }
        if (wrongOptions.size < 3) return QuizQuestionResult.FlashcardFallback

        val options = (listOf(correct.text) + wrongOptions).shuffled()
        if (
            options.size != 4 ||
            options.map(::normalizeQuizText).distinct().size != 4 ||
            options.count { normalizeQuizText(it) == normalizedCorrect } != 1
        ) return QuizQuestionResult.FlashcardFallback

        return QuizQuestionResult.QuizQuestion(
            promptText = prompt.text,
            correctAnswerText = correct.text,
            options = options
        )
    }
}
