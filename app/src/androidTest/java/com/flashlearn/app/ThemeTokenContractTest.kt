package com.flashlearn.app

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.flashlearn.app.ui.theme.FlashLearnTheme
import com.flashlearn.app.ui.theme.FlashLearnThemeSpec
import com.flashlearn.app.ui.theme.LocalFlashLearnThemeTokens
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ThemeTokenContractTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComposeTestActivity>()

    @Test
    fun semanticTokensDriveMaterialShapesAndCoreDimensions() {
        var controlHeight = 0f
        var mediumCorner = 0f
        var reviewHeaderHeight = 0f
        var reviewChoiceHeight = 0f
        composeRule.setContent {
            FlashLearnTheme(themeId = FlashLearnThemeSpec.MODERN_MINIMAL.id) {
                val tokens = LocalFlashLearnThemeTokens.current
                controlHeight = tokens.controlHeight.value
                mediumCorner = tokens.cornerMedium.value
                reviewHeaderHeight = tokens.reviewHeaderHeight.value
                reviewChoiceHeight = tokens.reviewChoiceHeight.value
            }
        }
        composeRule.runOnIdle {
            assertEquals(52f, controlHeight, 0.001f)
            assertEquals(FlashLearnThemeSpec.MODERN_MINIMAL.cornerMedium, mediumCorner, 0.001f)
            assertTrue(mediumCorner > 0f)
            assertEquals(92f, reviewHeaderHeight, 0.001f)
            assertEquals(58f, reviewChoiceHeight, 0.001f)
        }
    }
    @Test
    fun gtpBindsDistinctDensitySpacingAndShapeTokens() {
        var spacingGap = 0f
        var corner = 0f
        var iconStyle = ""
        composeRule.setContent {
            FlashLearnTheme(themeId = FlashLearnThemeSpec.GTP.id) {
                val tokens = LocalFlashLearnThemeTokens.current
                spacingGap = tokens.compactGap.value
                corner = tokens.cornerMedium.value
                iconStyle = tokens.iconStyle.name
            }
        }
        composeRule.runOnIdle {
            assertEquals(8f * FlashLearnThemeSpec.GTP.spacingScale, spacingGap, 0.001f)
            assertEquals(FlashLearnThemeSpec.GTP.cornerMedium, corner, 0.001f)
            assertEquals("FILLED", iconStyle)
        }
    }

}
