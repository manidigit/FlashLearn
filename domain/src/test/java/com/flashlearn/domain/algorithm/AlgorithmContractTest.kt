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
    @Test fun weeklyWrong(){val r=calculateLearningTransition(state(Stage.WEEKLY),false,now,ZoneOffset.UTC);assertEquals(Stage.DAILY,r.newStage);assertFalse(r.hasPathFailure);assertEquals(0,r.monthlyWrongCount)}
    @Test fun monthlyWrong(){val r=calculateLearningTransition(state(Stage.MONTHLY,2,true),false,now,ZoneOffset.UTC);assertEquals(2,r.monthlyWrongCount);assertTrue(r.hasPathFailure)}
    @Test fun monthlyCorrect(){val r=calculateLearningTransition(state(Stage.MONTHLY,1,true),true,now,ZoneOffset.UTC);assertEquals(Stage.LEARNED,r.newStage);assertNull(r.nextReviewAt);assertTrue(r.hasPathFailure);assertEquals(1,r.monthlyWrongCount)}
    @Test fun threeConsecutiveWrongsFromEasyGoToMedium(){var s=difficulty(); repeat(2){s=calculateDifficulty(s,false)}; assertEquals(VocabularyDifficulty.EASY,s.current); s=calculateDifficulty(s,false,ReviewType.DAILY,0); assertEquals(VocabularyDifficulty.MEDIUM,s.current); assertEquals(0,s.consecutiveWrong); assertEquals(0,s.consecutiveCorrect)}
    @Test fun threeConsecutiveCorrectsAtEasyStayEasy(){var s=difficulty(); repeat(3){s=calculateDifficulty(s,true)}; assertEquals(VocabularyDifficulty.EASY,s.current); assertEquals(0,s.consecutiveCorrect); assertEquals(0,s.consecutiveWrong)}
    @Test fun weeklyAndMonthlyWrongFollowTheSameThresholdRule(){
        val weekly = calculateDifficulty(difficulty(VocabularyDifficulty.EASY), false)
        val monthly = calculateDifficulty(difficulty(VocabularyDifficulty.EASY), false)
        assertEquals(VocabularyDifficulty.EASY, weekly.current)
        assertEquals(VocabularyDifficulty.EASY, monthly.current)
    }
    @Test fun learnedReviewAlsoUsesOnlyAnswerOutcomeForDifficulty(){
        val original = difficulty(VocabularyDifficulty.VERY_HARD, cw = 2, reached = true)
        val result = calculateDifficulty(original, false)
        assertEquals(VocabularyDifficulty.VERY_HARD, result.current)
        assertEquals(0, result.consecutiveCorrect)
        assertEquals(0, result.consecutiveWrong)
    }

}
