package com.flashlearn.domain.usecase

import com.flashlearn.domain.model.*
import com.flashlearn.domain.repository.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant
import java.util.UUID

class QuizUseCasesTest {
    private fun concept(id: UUID = UUID.randomUUID(), active: Boolean = true) = Concept(id, EntryType.WORD, null, false, active, Instant.EPOCH, Instant.EPOCH)
    private fun content(cid: UUID, lang: String, text: String) = Content(UUID.randomUUID(), cid, lang, text, text.trim().lowercase())
    private class CR(private val values: List<Concept>) : ConceptRepository {
        override suspend fun insert(concept: Concept)=concept.id; override suspend fun get(conceptId: UUID)=values.find{it.id==conceptId}; override suspend fun getAllActive()=values.filter{it.active}; override suspend fun searchActive(query:String)=emptyList<Concept>(); override suspend fun update(concept:Concept){}; override suspend fun softDelete(conceptId:UUID,now:Instant){}
    }
    private class CoR(private val values: List<Content>) : ContentRepository {
        override suspend fun findByUuid(uuid:UUID)=values.find{it.id==uuid}; override suspend fun find(conceptId:UUID,languageCode:String)=values.find{it.conceptId==conceptId&&it.languageCode==languageCode}; override suspend fun upsert(content:Content){}; override suspend fun getAll()=values
    }
    private class DR(private val values: Map<UUID,DifficultyState>) : DifficultyStateRepository {
        override suspend fun get(conceptId:UUID)=values[conceptId]; override suspend fun upsert(state:DifficultyState){}; override suspend fun delete(conceptId:UUID){}; override suspend fun getAll()=values.values.toList()
    }
    @Test fun generatesExactlyFourUniqueOptionsAndCorrectAnswer() = runBlocking {
        val cs = (1..4).map { concept() }; val all = cs.flatMapIndexed { i,c -> listOf(content(c.id,"es","s$i"), content(c.id,"fa","answer$i")) }
        val ds = cs.associate { it.id to DifficultyState(UUID.randomUUID(),it.id,VocabularyDifficulty.EASY,0,0,false) }
        val r = GenerateQuizQuestionUseCase(CoR(all), CR(cs), DR(ds))(cs.first(), QuizLanguagePair("es","fa"), ds.getValue(cs.first().id))
        assertTrue(r is QuizQuestionResult.QuizQuestion); r as QuizQuestionResult.QuizQuestion
        assertEquals(4, r.options.size); assertEquals(4, r.options.distinct().size); assertTrue(r.correctAnswerText in r.options); assertEquals("s0", r.promptText)
    }
    @Test fun doesNotUseSameConceptAsDistractorAndFallsBackWithoutThreeDistractors() = runBlocking {
        val c1=concept(); val c2=concept(); val all=listOf(content(c1.id,"es","hola"),content(c1.id,"fa","سلام"),content(c1.id,"fa","سلام2"),content(c2.id,"es","adios"),content(c2.id,"fa","خداحافظ"))
        val ds=mapOf(c1.id to DifficultyState(UUID.randomUUID(),c1.id,VocabularyDifficulty.EASY,0,0,false), c2.id to DifficultyState(UUID.randomUUID(),c2.id,VocabularyDifficulty.EASY,0,0,false))
        val r=GenerateQuizQuestionUseCase(CoR(all),CR(listOf(c1,c2)),DR(ds))(c1,QuizLanguagePair("es","fa"),ds.getValue(c1.id)); assertEquals(QuizQuestionResult.FlashcardFallback,r)
    }
    @Test fun ignoresDuplicateTextsAcrossDifferentConcepts() = runBlocking {
        val cs=(1..5).map{concept()}; val all=cs.flatMapIndexed{i,c->listOf(content(c.id,"es","s$i"),content(c.id,"fa",if(i==4)"same" else "same$i"))}; val ds=cs.associate{it.id to DifficultyState(UUID.randomUUID(),it.id,VocabularyDifficulty.EASY,0,0,false)}
        val r=GenerateQuizQuestionUseCase(CoR(all),CR(cs),DR(ds))(cs.first(),QuizLanguagePair("es","fa"),ds.getValue(cs.first().id)) as QuizQuestionResult.QuizQuestion; assertEquals(4,r.options.distinct().size)
    }
    @Test fun prioritizesSameDifficultyBeforeAdjacentAndFullBank() = runBlocking {
        val target=concept(); val same=concept(); val adjacent=concept(); val fullBank=concept(); val all=listOf(content(target.id,"es","target"),content(target.id,"fa","هدف"),content(same.id,"es","same-es"),content(same.id,"fa","همان"),content(adjacent.id,"es","adj-es"),content(adjacent.id,"fa","نزدیک"),content(fullBank.id,"es","full-es"),content(fullBank.id,"fa","دور")); val ds=mapOf(target.id to DifficultyState(UUID.randomUUID(),target.id,VocabularyDifficulty.MEDIUM,0,0,false),same.id to DifficultyState(UUID.randomUUID(),same.id,VocabularyDifficulty.MEDIUM,0,0,false),adjacent.id to DifficultyState(UUID.randomUUID(),adjacent.id,VocabularyDifficulty.HARD,0,0,false),fullBank.id to DifficultyState(UUID.randomUUID(),fullBank.id,VocabularyDifficulty.VERY_HARD,0,0,false)); val result=GenerateQuizQuestionUseCase(CoR(all),CR(listOf(target,same,adjacent,fullBank)),DR(ds))(target,QuizLanguagePair("es","fa"),ds.getValue(target.id)) as QuizQuestionResult.QuizQuestion; assertTrue("همان" in result.options); assertTrue("نزدیک" in result.options); assertTrue("دور" in result.options)
    }
    @Test fun fallsBackWhenPromptOrCorrectLanguageIsMissing() = runBlocking {
        val target=concept(); val d1=concept(); val d2=concept(); val d3=concept(); val all=listOf(content(target.id,"es","hola"),content(d1.id,"es","uno"),content(d1.id,"fa","یک"),content(d2.id,"es","dos"),content(d2.id,"fa","دو"),content(d3.id,"es","tres"),content(d3.id,"fa","سه")); val concepts=listOf(target,d1,d2,d3); val ds=concepts.associate{it.id to DifficultyState(UUID.randomUUID(),it.id,VocabularyDifficulty.EASY,0,0,false)}; val result=GenerateQuizQuestionUseCase(CoR(all),CR(concepts),DR(ds))(target,QuizLanguagePair("es","fa"),ds.getValue(target.id)); assertEquals(QuizQuestionResult.FlashcardFallback,result)
    }
    @Test fun excludesInactiveConceptsFromDistractors() = runBlocking {
        val target=concept(); val inactive=concept(active=false); val active2=concept(); val active3=concept(); val active4=concept(); val concepts=listOf(target,inactive,active2,active3,active4); val all=concepts.flatMapIndexed{i,c->listOf(content(c.id,"es","s$i"),content(c.id,"fa","t$i"))}; val ds=concepts.associate{it.id to DifficultyState(UUID.randomUUID(),it.id,VocabularyDifficulty.EASY,0,0,false)}; val result=GenerateQuizQuestionUseCase(CoR(all),CR(concepts),DR(ds))(target,QuizLanguagePair("es","fa"),ds.getValue(target.id)) as QuizQuestionResult.QuizQuestion; assertFalse("t1" in result.options)
    }
    @Test fun normalizationPreventsCaseWhitespaceAndUnicodeDuplicateOptions() = runBlocking {
        val target=concept(); val d1=concept(); val d2=concept(); val d3=concept(); val d4=concept(); val all=listOf(content(target.id,"es","q"),content(target.id,"fa","CAFÉ"),content(d1.id,"es","a"),content(d1.id,"fa"," cafe\u0301 "),content(d2.id,"es","b"),content(d2.id,"fa","B"),content(d3.id,"es","c"),content(d3.id,"fa","C"),content(d4.id,"es","d"),content(d4.id,"fa","D")); val concepts=listOf(target,d1,d2,d3,d4); val ds=concepts.associate{it.id to DifficultyState(UUID.randomUUID(),it.id,VocabularyDifficulty.EASY,0,0,false)}; val result=GenerateQuizQuestionUseCase(CoR(all),CR(concepts),DR(ds))(target,QuizLanguagePair("es","fa"),ds.getValue(target.id)) as QuizQuestionResult.QuizQuestion; assertEquals(4,result.options.size); assertEquals(4,result.options.map{java.text.Normalizer.normalize(it.trim().replace(Regex("\\s+")," "),java.text.Normalizer.Form.NFC).lowercase(java.util.Locale.ROOT)}.distinct().size); assertFalse(result.options.any{it.trim().equals("cafe\u0301",ignoreCase=true)})
    }
    @Test(expected=IllegalArgumentException::class) fun quizQuestion_rejectsDuplicateNormalizedOptions() { QuizQuestionResult.QuizQuestion("hola","سلام",listOf("سلام","  DOG ","dog","cat")) }
    @Test fun generator_doesNotDependOnVocabularyDifficultyWhenFullBankIsNeeded() = runBlocking {
        val target=concept(); val others=(1..3).map{concept()}; val allConcepts=listOf(target)+others; val all=allConcepts.flatMapIndexed{i,c->listOf(content(c.id,"es","q$i"),content(c.id,"fa","a$i"))}; val states=mapOf(target.id to DifficultyState(UUID.randomUUID(),target.id,VocabularyDifficulty.VERY_HARD,0,0,false),others[0].id to DifficultyState(UUID.randomUUID(),others[0].id,VocabularyDifficulty.EASY,0,0,false),others[1].id to DifficultyState(UUID.randomUUID(),others[1].id,VocabularyDifficulty.MEDIUM,0,0,false),others[2].id to DifficultyState(UUID.randomUUID(),others[2].id,VocabularyDifficulty.HARD,0,0,false)); val result=GenerateQuizQuestionUseCase(CoR(all),CR(allConcepts),DR(states))(target,QuizLanguagePair("es","fa"),states.getValue(target.id)); assertTrue(result is QuizQuestionResult.QuizQuestion); assertEquals(4,(result as QuizQuestionResult.QuizQuestion).options.size)
    }
    @Test fun generator_allowsMissingTargetDifficultyState() = runBlocking {
        val target=concept(); val others=(1..3).map{concept()}; val concepts=listOf(target)+others; val all=concepts.flatMapIndexed{i,c->listOf(content(c.id,"es","q$i"),content(c.id,"fa","a$i"))}; val states=others.associate{it.id to DifficultyState(UUID.randomUUID(),it.id,VocabularyDifficulty.EASY,0,0,false)}
        val result=GenerateQuizQuestionUseCase(CoR(all),CR(concepts),DR(states))(target,QuizLanguagePair("es","fa"),null)
        assertTrue("Missing difficulty state must not force flashcard fallback", result is QuizQuestionResult.QuizQuestion)
        assertEquals(4,(result as QuizQuestionResult.QuizQuestion).options.size)
    }
}
