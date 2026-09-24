package com.flashlearn.domain.progress

import com.flashlearn.domain.model.Stage
import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressScoringTest {
    @Test
    fun stageScoresMatchSpecification() {
        assertEquals(0, ProgressScoring.score(null, false))
        assertEquals(15, ProgressScoring.score(null, true))
        assertEquals(0, ProgressScoring.score(Stage.DAILY, false))
        assertEquals(35, ProgressScoring.score(Stage.DAILY, true))
        assertEquals(0, ProgressScoring.score(Stage.WEEKLY, false))
        assertEquals(0, ProgressScoring.score(Stage.MONTHLY, false))
        assertEquals(0, ProgressScoring.score(Stage.LEARNED, false))
        assertEquals(60, ProgressScoring.score(Stage.WEEKLY, true))
        assertEquals(80, ProgressScoring.score(Stage.MONTHLY, true))
        assertEquals(100, ProgressScoring.score(Stage.LEARNED, true))
    }
}
