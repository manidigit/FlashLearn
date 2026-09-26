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
}
