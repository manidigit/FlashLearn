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
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min
import kotlinx.coroutines.sync.withLock

private val QUIZ_WHITESPACE_REGEX = Regex("\\s+")
private val QUIZ_TOKEN_SPLIT_REGEX = Regex("[^\\p{L}\\p{N}]+")

private fun normalizeQuizLanguage(language: String): String = language.trim().lowercase(Locale.ROOT)

private fun normalizeQuizText(text: String): String =
    Normalizer.normalize(text.trim().replace(QUIZ_WHITESPACE_REGEX, " "), Normalizer.Form.NFC).lowercase(Locale.ROOT)

/** Normalized text, tokens and bigrams computed once per entry instead of once per comparison. */
private class QuizTextProfile(text: String) {
    val normalized: String = normalizeQuizText(text)
    val tokens: Set<String> = normalized.split(QUIZ_TOKEN_SPLIT_REGEX).filter { it.isNotBlank() }.toSet()
    val bigrams: Set<String> = if (normalized.length <= 2) setOf(normalized) else normalized.windowed(2).toSet()
}

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

private fun quizLexicalSimilarity(left: QuizTextProfile, right: QuizTextProfile): Double {
    val tokenSimilarity = if (left.tokens.isEmpty() && right.tokens.isEmpty()) 1.0
    else if (left.tokens.isEmpty() || right.tokens.isEmpty()) 0.0
    else left.tokens.intersect(right.tokens).size.toDouble() / left.tokens.union(right.tokens).size
    val ngramSimilarity = if (left.bigrams.isEmpty() && right.bigrams.isEmpty()) 1.0
    else if (left.bigrams.isEmpty() || right.bigrams.isEmpty()) 0.0
    else left.bigrams.intersect(right.bigrams).size.toDouble() / left.bigrams.union(right.bigrams).size
    return (tokenSimilarity * 0.45 + levenshteinSimilarity(left.normalized, right.normalized) * 0.35 + ngramSimilarity * 0.20).coerceIn(0.0, 1.0)
}

private fun vocabularyDifficultyDistance(effective: VocabularyDifficulty, other: VocabularyDifficulty?): Int {
    if (other == null) return 3
    return when (effective) {
        VocabularyDifficulty.EASY -> when (other) {
            VocabularyDifficulty.EASY -> 0
            VocabularyDifficulty.MEDIUM -> 1
            VocabularyDifficulty.HARD -> 2
            VocabularyDifficulty.VERY_HARD -> 3
        }
        VocabularyDifficulty.MEDIUM -> when (other) {
            VocabularyDifficulty.MEDIUM -> 0
            VocabularyDifficulty.EASY, VocabularyDifficulty.HARD -> 1
            VocabularyDifficulty.VERY_HARD -> 2
        }
        VocabularyDifficulty.HARD -> when (other) {
            VocabularyDifficulty.HARD -> 0
            VocabularyDifficulty.MEDIUM, VocabularyDifficulty.VERY_HARD -> 1
            VocabularyDifficulty.EASY -> 2
        }
        VocabularyDifficulty.VERY_HARD -> when (other) {
            VocabularyDifficulty.VERY_HARD -> 0
            VocabularyDifficulty.HARD -> 1
            VocabularyDifficulty.MEDIUM -> 2
            VocabularyDifficulty.EASY -> 3
        }
    }
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

data class QuizLanguagePair(val sourceLanguage: String, val targetLanguage: String) {
    init {
        require(sourceLanguage.isNotBlank()) { "sourceLanguage must not be blank" }
        require(targetLanguage.isNotBlank()) { "targetLanguage must not be blank" }
        require(!sourceLanguage.equals(targetLanguage, ignoreCase = true)) { "sourceLanguage and targetLanguage must differ" }
    }
}

private class QuizLanguageEntry(
    val concept: Concept,
    val displayText: String,
    val profile: QuizTextProfile,
    val canonicalKeys: Set<String>
)

/**
 * Refreshing the bank is O(N): it only stores raw data. The per-language entry list
 * is built lazily once per language, and similarity is computed per question only for
 * candidates that actually reach ranking. (v6.83 precomputed all N x N similarities
 * at refresh time, which froze the screen on large vocabularies.)
 */
private class QuizBank(
    val contentsByConcept: Map<UUID, List<Content>>,
    val concepts: List<Concept>,
    val difficultiesById: Map<UUID, DifficultyState>
) {
    private val languageIndexes = ConcurrentHashMap<String, List<QuizLanguageEntry>>()

    fun entriesFor(language: String): List<QuizLanguageEntry> {
        val key = normalizeQuizLanguage(language)
        return languageIndexes[key] ?: buildEntries(key).also { languageIndexes[key] = it }
    }

    private fun buildEntries(language: String): List<QuizLanguageEntry> =
        concepts.asSequence()
            .filter { it.active }
            .mapNotNull { concept ->
                val values = contentsByConcept[concept.id].orEmpty()
                    .filter { normalizeQuizLanguage(it.languageCode) == language && it.text.isNotBlank() }
                    .sortedWith(compareBy<Content> { it.translationIndex }.thenBy { it.id.toString() })
                    .distinctBy { normalizeQuizText(it.text) }
                if (values.isEmpty()) null else {
                    val displayText = values.joinToString(" / ") { it.text.trim() }
                    QuizLanguageEntry(
                        concept = concept,
                        displayText = displayText,
                        profile = QuizTextProfile(displayText),
                        canonicalKeys = values.map { normalizeQuizText(it.canonicalKey) }.filter { it.isNotBlank() }.toSet()
                    )
                }
            }
            .toList()
}

private class DistractorCandidate(
    val displayText: String,
    val vocabularyDifficultyDistance: Int,
    val categoryMatch: Boolean,
    val entryTypeMatch: Boolean,
    private val entryProfile: QuizTextProfile,
    private val correctProfile: QuizTextProfile
) {
    val lexicalSimilarity: Double by lazy(LazyThreadSafetyMode.NONE) {
        quizLexicalSimilarity(correctProfile, entryProfile)
    }
}

class GenerateQuizQuestionUseCase @Inject constructor(
    private val contentRepository: ContentRepository,
    private val conceptRepository: ConceptRepository,
    private val difficultyStateRepository: DifficultyStateRepository
) {
    @Volatile private var bank: QuizBank? = null
    private val bankMutex = kotlinx.coroutines.sync.Mutex()

    suspend fun refreshBank() = bankMutex.withLock {
        val contents = contentRepository.getAll()
        val concepts = conceptRepository.getAllActive()
        val difficultiesById = difficultyStateRepository.getAll().associateBy { it.conceptId }
        bank = QuizBank(
            contentsByConcept = contents.groupBy { it.conceptId },
            concepts = concepts,
            difficultiesById = difficultiesById
        )
    }

    private suspend fun snapshotBank(): QuizBank = bank ?: run { refreshBank(); bank!! }

    suspend operator fun invoke(
        concept: Concept,
        activeLanguagePair: QuizLanguagePair,
        difficultyState: DifficultyState?,
        difficulty: QuizDifficulty = QuizDifficulty.MEDIUM,
        excludedDistractorTexts: Set<String> = emptySet()
    ): QuizQuestionResult {
        if (!concept.active) return QuizQuestionResult.FlashcardFallback

        val snapshot = snapshotBank()
        val contentsByConcept = snapshot.contentsByConcept
        val conceptContents = contentsByConcept[concept.id].orEmpty()
        // Quiz prompts are always Spanish. The answer language is still controlled
        // by the active target language, which gives the two documented modes:
        // Spanish -> Spanish (Word Recognition) and Spanish -> Persian (Meaning Recognition).
        val prompt = conceptContents.firstOrNull {
            it.languageCode.equals(activeLanguagePair.sourceLanguage, true) && it.text.isNotBlank()
        } ?: return QuizQuestionResult.FlashcardFallback
        val correct = conceptContents.firstOrNull {
            it.languageCode.equals(activeLanguagePair.targetLanguage, true) && it.text.isNotBlank()
        } ?: return QuizQuestionResult.FlashcardFallback
        val targetTranslations = conceptContents
            .filter { it.languageCode.equals(activeLanguagePair.targetLanguage, true) && it.text.isNotBlank() }
            .sortedWith(compareBy<Content> { it.translationIndex }.thenBy { it.id.toString() })
            .distinctBy { normalizeQuizText(it.text) }
        if (targetTranslations.isEmpty()) return QuizQuestionResult.FlashcardFallback

        fun displayTranslations(values: List<Content>): String =
            values.joinToString(" / ") { it.text.trim() }

        val correctDisplayText = displayTranslations(targetTranslations)
        val normalizedCorrect = normalizeQuizText(correctDisplayText)
        val normalizedCorrectCanonicalKeys = targetTranslations
            .map { normalizeQuizText(it.canonicalKey) }
            .filter { it.isNotBlank() }
            .toSet()

        // Vocabulary Difficulty controls the documented candidate pool. Quiz Difficulty
        // is independent and controls how close/plausible the wrong answers are.
        val effectiveVocabularyDifficulty = difficultyState?.current
            ?: snapshot.difficultiesById[concept.id]?.current
            ?: VocabularyDifficulty.MEDIUM
        val correctProfile = QuizTextProfile(correctDisplayText)

        val candidates = snapshot.entriesFor(activeLanguagePair.targetLanguage).asSequence()
            .filter { it.concept.id != concept.id }
            .filter { it.profile.normalized != normalizedCorrect }
            .distinctBy { it.profile.normalized }
            .filter { entry ->
                normalizedCorrectCanonicalKeys.isEmpty() ||
                    entry.canonicalKeys.none { key -> key in normalizedCorrectCanonicalKeys }
            }
            .map { entry ->
                DistractorCandidate(
                    displayText = entry.displayText,
                    vocabularyDifficultyDistance = vocabularyDifficultyDistance(
                        effectiveVocabularyDifficulty,
                        snapshot.difficultiesById[entry.concept.id]?.current
                    ),
                    categoryMatch = concept.categoryId != null && concept.categoryId == entry.concept.categoryId,
                    entryTypeMatch = concept.entryType == entry.concept.entryType,
                    entryProfile = entry.profile,
                    correctProfile = correctProfile
                )
            }
            .toList()

        // Prefer distractors that have not already appeared earlier in the same
        // review session. If fewer than three fresh distractors exist, fall back
        // to the complete valid pool so small vocabulary banks still produce quizzes.
        val normalizedExcludedDistractors = excludedDistractorTexts.map(::normalizeQuizText).filter { it.isNotBlank() }.toSet()
        val freshCandidates = candidates.filter {
            normalizeQuizText(it.displayText) !in normalizedExcludedDistractors
        }
        val selectionCandidates = if (freshCandidates.size >= 3) freshCandidates else candidates

        if (selectionCandidates.size < 3) return QuizQuestionResult.FlashcardFallback

        fun confusabilityScore(candidate: DistractorCandidate): Double {
            val category = if (candidate.categoryMatch) 1.0 else 0.0
            val entryType = if (candidate.entryTypeMatch) 1.0 else 0.0
            return (
                candidate.lexicalSimilarity * 0.55 +
                    category * 0.25 +
                    entryType * 0.20
                ).coerceIn(0.0, 1.0)
        }

        val sameDifficultyPool = selectionCandidates.filter { it.vocabularyDifficultyDistance == 0 }
        val adjacentPool = selectionCandidates.filter { it.vocabularyDifficultyDistance == 1 }
        val selectedPool = when {
            sameDifficultyPool.size >= 3 -> sameDifficultyPool
            (sameDifficultyPool + adjacentPool).distinctBy { normalizeQuizText(it.displayText) }.size >= 3 ->
                (sameDifficultyPool + adjacentPool).distinctBy { normalizeQuizText(it.displayText) }
            else -> selectionCandidates
        }

        /*
         * Quiz Difficulty is a semantic selection policy layered on top of the
         * Vocabulary Difficulty pool:
         *
         * EASY:
         *   Prefer candidates outside the target category and entry type, then
         *   choose the least-confusable ones.
         *
         * MEDIUM:
         *   Prefer candidates from the same category but a different entry type,
         *   then choose the most plausible middle-band candidates.
         *
         * HARD:
         *   Prefer candidates from the same category and the same entry type,
         *   then choose the most-confusable ones.
         *
         * Each preferred tier falls back to a broader tier when the vocabulary
         * bank is too small. This makes the level observable without allowing a
         * numeric similarity threshold to discard otherwise valid distractors.
         */
        val easyPreferred = selectedPool.filter { !it.categoryMatch && !it.entryTypeMatch }
        val easyFallback = selectedPool.filter { !it.categoryMatch }
        val mediumPreferred = selectedPool.filter { it.categoryMatch && !it.entryTypeMatch }
        val mediumFallback = selectedPool.filter { it.categoryMatch }
        val hardPreferred = selectedPool.filter { it.categoryMatch && it.entryTypeMatch }
        val hardFallback = selectedPool.filter { it.categoryMatch }

        fun ranked(candidates: List<DistractorCandidate>, descending: Boolean): List<DistractorCandidate> =
            candidates.sortedWith(
                if (descending) {
                    compareByDescending<DistractorCandidate> { confusabilityScore(it) }
                        .thenBy { normalizeQuizText(it.displayText) }
                } else {
                    compareBy<DistractorCandidate> { confusabilityScore(it) }
                        .thenBy { normalizeQuizText(it.displayText) }
                }
            )

        fun chooseTier(
            preferred: List<DistractorCandidate>,
            fallback: List<DistractorCandidate>,
            descending: Boolean
        ): List<DistractorCandidate> {
            val source = when {
                preferred.size >= 3 -> preferred
                fallback.size >= 3 -> fallback
                else -> selectedPool
            }
            val ordered = ranked(source, descending)
            return if (ordered.size <= 3) {
                ordered
            } else {
                val start = ((ordered.size - 3) / 2).coerceAtLeast(0)
                ordered.drop(start).take(3)
            }
        }

        val rankedPool = when (difficulty) {
            QuizDifficulty.EASY -> chooseTier(easyPreferred, easyFallback, descending = false)
            QuizDifficulty.MEDIUM -> chooseTier(mediumPreferred, mediumFallback, descending = false)
            QuizDifficulty.HARD -> {
                val source = when {
                    hardPreferred.size >= 3 -> hardPreferred
                    hardFallback.size >= 3 -> hardFallback
                    else -> selectedPool
                }
                ranked(source, descending = true).take(3)
            }
        }

        val wrongOptions = rankedPool.map { it.displayText }
        if (wrongOptions.size < 3) return QuizQuestionResult.FlashcardFallback

        val options = (listOf(correctDisplayText) + wrongOptions).shuffled()
        if (
            options.size != 4 ||
            options.map(::normalizeQuizText).distinct().size != 4 ||
            options.count { normalizeQuizText(it) == normalizedCorrect } != 1
        ) return QuizQuestionResult.FlashcardFallback

        return QuizQuestionResult.QuizQuestion(
            promptText = prompt.text,
            correctAnswerText = correctDisplayText,
            options = options
        )
    }
}
