package com.flashlearn.app.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GtpThemeContractTest {

    @Test
    fun gtpIsTheFifthBuiltInTheme() {
        assertEquals(5, FlashLearnThemeSpec.BUILT_IN.size)
        assertEquals("gtp", FlashLearnThemeSpec.BUILT_IN[4].id)
        assertEquals("GTP", FlashLearnThemeSpec.GTP.name)
    }

    @Test
    fun gtpHasDistinctDesignTokens() {
        val gtp = FlashLearnThemeSpec.GTP
        assertEquals("filled", gtp.iconStyle)
        assertEquals(6f, gtp.cornerSmall, 0.001f)
        assertEquals(12f, gtp.cornerMedium, 0.001f)
        assertEquals(18f, gtp.cornerLarge, 0.001f)
        assertTrue(gtp.elevationScale > 1.4f)
        assertTrue(gtp.typographyScale > 1.05f)
        assertTrue(gtp.densityScale < 0.98f)
        assertTrue(gtp.spacingScale < 0.95f)
        assertTrue(gtp.lightPrimary != FlashLearnThemeSpec.GROK.lightPrimary)
        assertTrue(gtp.lightSecondary != FlashLearnThemeSpec.GROK.lightSecondary)
    }

    @Test
    fun grokOwnsTheReferenceLayoutContract() {
        val grok = FlashLearnThemeSpec.GROK
        assertEquals(ButtonStyle.FILLED, grok.design.buttonStyle)
        assertEquals(NavigationStyle.PILL, grok.design.navStyle)
        assertEquals(StatsLayoutStrategy.GRID_4_COLUMNS, grok.design.statsLayout)
        assertEquals(ReviewsLayoutStrategy.HORIZONTAL_CARDS, grok.design.reviewsLayout)
        assertEquals(LibraryLayoutStrategy.GRID_2_COLUMNS, grok.design.libraryLayout)
        assertEquals(ReviewPresentation.SWIPE_STACK, grok.design.reviewPresentation)
        assertEquals(22f, grok.design.metric("screenPadding"), 0.001f)
        assertEquals(20f, grok.design.metric("sectionGap"), 0.001f)
        assertTrue(grok.design.metric("cardBorderAlpha") > 0.5f)
    }

    @Test
    fun themeJsonPersistsDesignContract() {
        val json = FlashLearnThemeSpec.GROK.toJson()
        assertTrue(json.contains("\"design\""))
        assertTrue(json.contains("\"statsLayout\": \"GRID_4_COLUMNS\""))
        assertTrue(json.contains("\"navStyle\": \"PILL\""))
        val restored = FlashLearnThemeSpec.fromJson(json)
        assertEquals(FlashLearnThemeSpec.GROK.design.statsLayout, restored.design.statsLayout)
        assertEquals(FlashLearnThemeSpec.GROK.design.navStyle, restored.design.navStyle)
        assertEquals(FlashLearnThemeSpec.GROK.design.metric("screenPadding"), restored.design.metric("screenPadding"), 0.001f)
    }
}
