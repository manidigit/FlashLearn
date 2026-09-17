package com.flashlearn.domain.usecase

import com.flashlearn.domain.model.*
import com.flashlearn.domain.repository.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant
import java.util.UUID

class QuizDifficultySelectionTest {
    private fun concept(id: UUID = UUID.randomUUID(), categoryId: UUID? = null, entryType: EntryType = EntryType.WORD) =
        Concept(id, entryType, categoryId, false, true, Instant.EPOCH, Instant.EPOCH)

    private fun content(cid: UUID, lang: String, text: String) =
        Content(UUID.randomUUID(), cid, lang, text, text.trim().lowercase())

    private class CR(private val values: List<Concept>) : ConceptRepository {
        override suspend fun insert(concept: Concept) = concept.id
        override suspend fun get(conceptId: UUID) = values.find { it.id == conceptId }
        override suspend fun getAllActive() = values
        override suspend fun searchActive(query: String) = emptyList<Concept>()
        override suspend fun update(concept: Concept) {}
        override suspend fun softDelete(conceptId: UUID, now: Instant) {}
    }

    private class CoR(private val values: List<Content>) : ContentRepository {
        override suspend fun findByUuid(uuid: UUID) = values.find { it.id == uuid }
        override suspend fun find(conceptId: UUID, languageCode: String) =
            values.find { it.conceptId == conceptId && it.languageCode == languageCode }
        override suspend fun findForConcepts(conceptIds: List<UUID>) = values.filter { it.conceptId in conceptIds }
        override suspend fun upsert(content: Content) {}
        override suspend fun getAll() = values
    }

    private class DR(private val values: Map<UUID, DifficultyState>) : DifficultyStateRepository {
        override suspend fun get(conceptId: UUID) = values[conceptId]
        override suspend fun upsert(state: DifficultyState) {}
        override suspend fun delete(conceptId: UUID) {}
        override suspend fun getAll() = values.values.toList()
    }

    private fun state(id: UUID, difficulty: VocabularyDifficulty) =
        DifficultyState(UUID.randomUUID(), id, difficulty, 0, 0, false)

    private fun generate(
        target: Concept,
        concepts: List<Concept>,
        contents: List<Content>,
        states: Map<UUID, DifficultyState>,
        quizDifficulty: QuizDifficulty
    ): QuizQuestionResult.QuizQuestion = runBlocking {
        val result = GenerateQuizQuestionUseCase(CoR(contents), CR(concepts), DR(states))(
            target,
            QuizLanguagePair("es", "fa"),
            states[target.id],
            quizDifficulty
        )
        assertTrue(result is QuizQuestionResult.QuizQuestion)
        result as QuizQuestionResult.QuizQuestion
    }

    @Test
    fun easyPrefersClearlyDifferentDistractors() {
        val category = UUID.randomUUID()
        val target = concept(categoryId = category)
        val clear1 = concept()
        val clear2 = concept()
        val clear3 = concept()
        val close = concept(categoryId = category)
        val concepts = listOf(target, clear1, clear2, clear3, close)
        val contents = listOf(
            content(target.id, "es", "casa"), content(target.id, "fa", "خانه"),
            content(clear1.id, "es", "perro"), content(clear1.id, "fa", "سگ"),
            content(clear2.id, "es", "libro"), content(clear2.id, "fa", "کتاب"),
            content(clear3.id, "es", "mesa"), content(clear3.id, "fa", "میز"),
            content(close.id, "es", "casas"), content(close.id, "fa", "خانه‌ها")
        )
        val states = concepts.associate { it.id to state(it.id, VocabularyDifficulty.MEDIUM) }
        val result = generate(target, concepts, contents, states, QuizDifficulty.EASY)
        assertFalse(result.options.contains("خانه‌ها"))
        assertTrue(result.options.containsAll(listOf("سگ", "کتاب", "میز")))
    }

    @Test
    fun mediumPrefersSameCategoryAndPlausibleDistractors() {
        val category = UUID.randomUUID()
        val otherCategory = UUID.randomUUID()
        val target = concept(categoryId = category)
        val c1 = concept(categoryId = category)
        val c2 = concept(categoryId = category)
        val c3 = concept(categoryId = category)
        val outside = concept(categoryId = otherCategory)
        val concepts = listOf(target, c1, c2, c3, outside)
        val contents = listOf(
            content(target.id, "es", "casa"), content(target.id, "fa", "خانه"),
            content(c1.id, "es", "casas"), content(c1.id, "fa", "خانه‌ها"),
            content(c2.id, "es", "casita"), content(c2.id, "fa", "خانه کوچک"),
            content(c3.id, "es", "casero"), content(c3.id, "fa", "خانگی"),
            content(outside.id, "es", "casa"), content(outside.id, "fa", "منزل")
        )
        val states = concepts.associate { it.id to state(it.id, VocabularyDifficulty.MEDIUM) }
        val result = generate(target, concepts, contents, states, QuizDifficulty.MEDIUM)
        assertFalse(result.options.contains("منزل"))
        assertTrue(result.options.containsAll(listOf("خانه‌ها", "خانه کوچک", "خانگی")))
    }

    @Test
    fun hardPrefersSameCategoryAndEntryTypeWithHighSimilarity() {
        val category = UUID.randomUUID()
        val target = concept(categoryId = category, entryType = EntryType.WORD)
        val close1 = concept(categoryId = category, entryType = EntryType.WORD)
        val close2 = concept(categoryId = category, entryType = EntryType.WORD)
        val close3 = concept(categoryId = category, entryType = EntryType.WORD)
        val differentType = concept(categoryId = category, entryType = EntryType.PHRASE)
        val concepts = listOf(target, close1, close2, close3, differentType)
        val contents = listOf(
            content(target.id, "es", "comer"), content(target.id, "fa", "خوردن"),
            content(close1.id, "es", "comiendo"), content(close1.id, "fa", "در حال خوردن"),
            content(close2.id, "es", "comió"), content(close2.id, "fa", "خورد"),
            content(close3.id, "es", "comemos"), content(close3.id, "fa", "می‌خوریم"),
            content(differentType.id, "es", "comer"), content(differentType.id, "fa", "مصرف کردن")
        )
        val states = concepts.associate { it.id to state(it.id, VocabularyDifficulty.MEDIUM) }
        val result = generate(target, concepts, contents, states, QuizDifficulty.HARD)
        assertFalse(result.options.contains("مصرف کردن"))
        assertTrue(result.options.containsAll(listOf("در حال خوردن", "خورد", "می‌خوریم")))
    }

    @Test
    fun quizDifficultyDoesNotChangeVocabularyDifficultyPoolRules() {
        val target = concept()
        val same1 = concept()
        val same2 = concept()
        val same3 = concept()
        val adjacent = concept()
        val concepts = listOf(target, same1, same2, same3, adjacent)
        val contents = concepts.flatMapIndexed { i, c ->
            listOf(content(c.id, "es", "q$i"), content(c.id, "fa", "a$i"))
        }
        val states = mapOf(
            target.id to state(target.id, VocabularyDifficulty.MEDIUM),
            same1.id to state(same1.id, VocabularyDifficulty.MEDIUM),
            same2.id to state(same2.id, VocabularyDifficulty.MEDIUM),
            same3.id to state(same3.id, VocabularyDifficulty.MEDIUM),
            adjacent.id to state(adjacent.id, VocabularyDifficulty.EASY)
        )
        val easy = generate(target, concepts, contents, states, QuizDifficulty.EASY)
        val hard = generate(target, concepts, contents, states, QuizDifficulty.HARD)
        assertFalse(easy.options.contains("a4"))
        assertFalse(hard.options.contains("a4"))
    }
}
