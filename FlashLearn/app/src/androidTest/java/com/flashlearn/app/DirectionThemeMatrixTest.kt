package com.flashlearn.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.LayoutDirection
import com.flashlearn.app.ui.AppLayoutDirection
import com.flashlearn.app.ui.AppearanceMode
import com.flashlearn.app.ui.toComposeLayoutDirection
import com.flashlearn.app.ui.theme.FlashLearnTheme
import com.flashlearn.app.ui.theme.FlashLearnThemeSpec
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class DirectionThemeMatrixTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun rtlLtrAndLightDarkMatrixPreservesRootDirectionAndTheme() {
        val cases = listOf(
            Triple(AppLayoutDirection.RTL, AppearanceMode.LIGHT, LayoutDirection.Rtl),
            Triple(AppLayoutDirection.RTL, AppearanceMode.DARK, LayoutDirection.Rtl),
            Triple(AppLayoutDirection.LTR, AppearanceMode.LIGHT, LayoutDirection.Ltr),
            Triple(AppLayoutDirection.LTR, AppearanceMode.DARK, LayoutDirection.Ltr)
        )
        var currentCase by mutableStateOf(cases.first())
        var observedDirection: LayoutDirection? = null
        var observedBackground: Color? = null

        composeRule.setContent {
            FlashLearnTheme(appearance = currentCase.second) {
                CompositionLocalProvider(
                    LocalLayoutDirection provides currentCase.first.toComposeLayoutDirection()
                ) {
                    observedDirection = LocalLayoutDirection.current
                    observedBackground = MaterialTheme.colorScheme.background
                    Text("کتاب Book ۱۰")
                }
            }
        }

        cases.forEach { (appDirection, appearance, expectedDirection) ->
            composeRule.runOnIdle {
                currentCase = Triple(appDirection, appearance, expectedDirection)
            }
            composeRule.waitForIdle()
            composeRule.runOnIdle {
                assertEquals(expectedDirection, observedDirection)
                val expectedBackground = if (appearance == AppearanceMode.LIGHT) {
                    FlashLearnThemeSpec.MODERN_MINIMAL.lightBackground
                } else {
                    FlashLearnThemeSpec.MODERN_MINIMAL.darkBackground
                }
                assertEquals(Color(expectedBackground), observedBackground)
            }
        }
    }

    @Test
    fun appDirectionMappingIsNotReversed() {
        assertEquals(LayoutDirection.Rtl, AppLayoutDirection.RTL.toComposeLayoutDirection())
        assertEquals(LayoutDirection.Ltr, AppLayoutDirection.LTR.toComposeLayoutDirection())
    }
}
