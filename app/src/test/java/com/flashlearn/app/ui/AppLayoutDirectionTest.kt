package com.flashlearn.app.ui

import androidx.compose.ui.unit.LayoutDirection
import org.junit.Assert.assertEquals
import org.junit.Test

class AppLayoutDirectionTest {
    @Test fun rtlMapsToComposeRtl() {
        assertEquals(LayoutDirection.Rtl, AppLayoutDirection.RTL.toComposeLayoutDirection())
    }

    @Test fun ltrMapsToComposeLtr() {
        assertEquals(LayoutDirection.Ltr, AppLayoutDirection.LTR.toComposeLayoutDirection())
    }
}
