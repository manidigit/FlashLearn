package com.flashlearn.domain.algorithm

import com.flashlearn.domain.model.*
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

class AlgorithmContractTest {
    private val now=Instant.parse("2026-09-10T10:00:00Z")
    private fun state(stage:Stage, wrong:Int=0, failure:Boolean=false)=LearningState(UUID.randomUUID(),UUID.randomUUID(),stage,now,wrong,failure,0,0,null)
    private fun difficulty(level: VocabularyDifficulty = VocabularyDifficulty.EASY, cc: Int = 0, cw: Int = 0, reached: Boolean = false)=DifficultyState(UUID.randomUUID(),UUID.randomUUID(),level,cc,cw,reached)

    @Test fun dailyCorrect(){val r=calculateLearningTransition(state(Stage.DAILY),true,now,ZoneOffset.UTC);assertEquals(Stage.WEEKLY,r.newStage);assertEquals(now.plusSeconds(604800),r.nextReviewAt)}
    @Test fun weeklyWrong(){val r=calculateLearningTransition(state(Stage.WEEKLY),false,now,ZoneOffset.UTC);assertEquals(Stage.DAILY,r.newStage);assertTrue(r.hasPathFailure)}
    @Test fun monthlyWrong(){val r=calculateLearningTransition(state(Stage.MONTHLY,2),false,now,ZoneOffset.UTC);assertEquals(3,r.monthlyWrongCount);assertTrue(r.hasPathFailure)}
    @Test fun monthlyCorrect(){val r=calculateLearningTransition(state(Stage.MONTHLY,1,true),true,now,ZoneOffset.UTC);assertEquals(Stage.LEARNED,r.newStage);assertNull(r.nextReviewAt);assertTrue(r.hasPathFailure);assertEquals(1,r.monthlyWrongCount)}
    @Test fun threeConsecutiveWrongsFromEasyGoToMedium(){var s=difficulty(); repeat(2){s=calculateDifficulty(s,false,ReviewType.DAILY,0)}; assertEquals(VocabularyDifficulty.EASY,s.current); s=calculateDifficulty(s,false,ReviewType.DAILY,0); assertEquals(VocabularyDifficulty.MEDIUM,s.current); assertEquals(0,s.consecutiveWrong); assertEquals(0,s.consecutiveCorrect)}
    @Test fun threeConsecutiveCorrectsAtEasyStayEasy(){var s=difficulty(); repeat(3){s=calculateDifficulty(s,true,ReviewType.DAILY,0)}; assertEquals(VocabularyDifficulty.EASY,s.current); assertEquals(0,s.consecutiveCorrect); assertEquals(0,s.consecutiveWrong)}
    @Test fun weeklyWrongForcesAtLeastMedium(){val s=calculateDifficulty(difficulty(VocabularyDifficulty.EASY),false,ReviewType.WEEKLY,0);assertEquals(VocabularyDifficulty.MEDIUM,s.current);assertEquals(0,s.consecutiveCorrect);assertEquals(0,s.consecutiveWrong)}
    @Test fun monthlyWrongUsesPreIncrementCount(){val first=calculateDifficulty(difficulty(),false,ReviewType.MONTHLY,0);val later=calculateDifficulty(difficulty(),false,ReviewType.MONTHLY,1);assertEquals(VocabularyDifficulty.HARD,first.current);assertEquals(VocabularyDifficulty.VERY_HARD,later.current);assertTrue(later.hasReachedVeryHard)}
}
