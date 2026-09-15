package com.flashlearn.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RuntimeGatePreflightTest {
    @Test fun applicationContextIsAvailable() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        assertNotNull(context)
        assertEquals("com.flashlearn.app", context.packageName)
    }

    @Test fun releaseVersionMatchesCurrentBuild() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        assertEquals("5.73", packageInfo.versionName)
        assertEquals(73, packageInfo.longVersionCode.toInt())
    }
}
