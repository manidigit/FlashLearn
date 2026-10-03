package com.flashlearn.app

import androidx.compose.runtime.*
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.flashlearn.app.ui.AppearanceMode
import com.flashlearn.app.ui.settings.SettingsScreen
import com.flashlearn.app.ui.theme.FlashLearnTheme
import com.flashlearn.app.ui.theme.FlashLearnThemeSpec
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsThemeDropdownTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComposeTestActivity>()

    @Test
    fun themeSelectorUsesDropdownAndChangesSelection() {
        composeRule.setContent {
            var selectedThemeId by remember { mutableStateOf(FlashLearnThemeSpec.GROK.id) }
            FlashLearnTheme(themeId = selectedThemeId) {
                SettingsScreen(
                    appearance = AppearanceMode.SYSTEM,
                    onAppearanceChange = {},
                    themeId = selectedThemeId,
                    themes = FlashLearnThemeSpec.BUILT_IN,
                    onThemeChange = { selectedThemeId = it }
                )
            }
        }

        composeRule.onAllNodesWithText(FlashLearnThemeSpec.GROK.name)[0].assertIsDisplayed().performClick()
        composeRule.onNodeWithText(FlashLearnThemeSpec.CLAUD.name).assertIsDisplayed().performClick()
        composeRule.onAllNodesWithText(FlashLearnThemeSpec.CLAUD.name)[0].assertIsDisplayed()
    }
}
