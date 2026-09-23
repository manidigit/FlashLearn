package com.flashlearn.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.createComposeRule
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.graphics.Color
import com.flashlearn.app.ui.AppLayoutDirection
import com.flashlearn.app.ui.AppearanceMode
import com.flashlearn.app.ui.toComposeLayoutDirection
import com.flashlearn.app.ui.theme.FlashLearnTheme
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

        cases.forEach { (appDirection, appearance, expectedDirection) ->
            var observedDirection by mutableStateOf<LayoutDirection?>(null)
            var observedBackground by mutableStateOf<Color?>(null)

            composeRule.setContent {
                FlashLearnTheme(appearance = appearance) {
                    CompositionLocalProvider(
                        LocalLayoutDirection provides appDirection.toComposeLayoutDirection()
                    ) {
                        observedDirection = LocalLayoutDirection.current
                        observedBackground = MaterialTheme.colorScheme.background
                        Text("کتاب Book ۱۰")
                    }
                }
            }

            composeRule.runOnIdle {
                assertEquals(expectedDirection, observedDirection)
                assertEquals(
                    if (appearance == AppearanceMode.LIGHT) Color(0xFFF8F7FC) else Color(0xFF0B0D12),
                    observedBackground
                )
            }
        }
    }

    @Test
    fun appDirectionMappingIsNotReversed() {
        assertEquals(LayoutDirection.Rtl, AppLayoutDirection.RTL.toComposeLayoutDirection())
        assertEquals(LayoutDirection.Ltr, AppLayoutDirection.LTR.toComposeLayoutDirection())
    }
}
