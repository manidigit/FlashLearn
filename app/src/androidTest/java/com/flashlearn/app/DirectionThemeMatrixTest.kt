package com.flashlearn.app

import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.SemanticsMatcher
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.testTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import com.flashlearn.app.ui.AppLayoutDirection
import com.flashlearn.app.ui.AppearanceMode
import com.flashlearn.app.ui.theme.FlashLearnTheme
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

private val TestLayoutDirectionKey = SemanticsPropertyKey<LayoutDirection>("TestLayoutDirection")
private var SemanticsPropertyReceiver.testLayoutDirection by TestLayoutDirectionKey

private val TestBackgroundColorKey = SemanticsPropertyKey<Color>("TestBackgroundColor")
private var SemanticsPropertyReceiver.testBackgroundColor by TestBackgroundColorKey

@RunWith(AndroidJUnit4::class)
class DirectionThemeMatrixTest {
    private val expectedLightBackground = Color(0xFFF8F7FC)
    private val expectedDarkBackground = Color(0xFF0B0D12)

    @Test
    fun rtlLtrAndLightDarkMatrixPreservesRootDirectionAndTheme() {
        val rule = androidx.compose.ui.test.junit4.createAndroidComposeRule<MainActivity>()

        listOf(
            Triple(AppLayoutDirection.RTL, AppearanceMode.LIGHT, LayoutDirection.Rtl),
            Triple(AppLayoutDirection.RTL, AppearanceMode.DARK, LayoutDirection.Rtl),
            Triple(AppLayoutDirection.LTR, AppearanceMode.LIGHT, LayoutDirection.Ltr),
            Triple(AppLayoutDirection.LTR, AppearanceMode.DARK, LayoutDirection.Ltr)
        ).forEach { (appDirection, appearance, expectedDirection) ->
            rule.setContent {
                FlashLearnTheme(appearance = appearance) {
                    CompositionLocalProvider(
                        LocalLayoutDirection provides appDirection.toComposeLayoutDirection()
                    ) {
                        Text(
                            text = "کتاب Book ۱۰",
                            modifier = androidx.compose.ui.Modifier
                                .testTag("direction-probe")
                                .semantics {
                                    testLayoutDirection = LocalLayoutDirection.current
                                    testBackgroundColor = MaterialTheme.colorScheme.background
                                }
                        )
                    }
                }
            }
            rule.onNodeWithTag("direction-probe")
                .assertExists()
                .assert(SemanticsMatcher.expectValue(TestLayoutDirectionKey, expectedDirection))
                .assert(
                    SemanticsMatcher.expectValue(
                        TestBackgroundColorKey,
                        if (appearance == AppearanceMode.LIGHT) expectedLightBackground else expectedDarkBackground
                    )
                )
        }
    }
}
