package com.flashlearn.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StartupSmokeTest {
    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java, true, false)

    @Test
    fun mainActivityLaunchesWithoutStartupCrash() {
        val activity = activityRule.launchActivity(null)
        assert(!activity.isFinishing)
        assert(!activity.isDestroyed)
    }
}
