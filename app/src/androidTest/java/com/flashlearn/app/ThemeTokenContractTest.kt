package com.flashlearn.app

import androidx.compose.ui.test.junit4.createComposeRule
import com.flashlearn.app.ui.theme.FlashLearnTheme
import com.flashlearn.app.ui.theme.LocalFlashLearnThemeTokens
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ThemeTokenContractTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun semanticTokensDriveMaterialShapesAndCoreDimensions() {
        var controlHeight = 0f
        var mediumCorner = 0f
        var reviewHeaderHeight = 0f
        var reviewChoiceHeight = 0f
        composeRule.setContent {
            FlashLearnTheme {
                val tokens = LocalFlashLearnThemeTokens.current
                controlHeight = tokens.controlHeight.value
                mediumCorner = tokens.cornerMedium.value
            }
        }
        composeRule.runOnIdle {
            assertEquals(52f, controlHeight, 0.001f)
            assertEquals(20f, mediumCorner, 0.001f)
            assertTrue(mediumCorner > 0f)
            assertEquals(92f, reviewHeaderHeight, 0.001f)
            assertEquals(58f, reviewChoiceHeight, 0.001f)
        }
    }
}
