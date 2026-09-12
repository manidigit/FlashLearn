package com.flashlearn.app.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppNavigationContractTest {
    @Test
    fun allPrimaryDestinationsAreDeclared() {
        assertEquals(
            listOf(
                "home",
                "review",
                "progress",
                "settings",
                "add_word",
                "bulk_import",
                "backup",
                "library",
                "library_detail"
            ),
            AppRoutes.all()
        )
    }

    @Test
    fun destinationsHaveStableRoutes() {
        assertTrue(AppDestination.Home.route == AppRoutes.HOME)
        assertTrue(AppDestination.Review.route == AppRoutes.REVIEW)
        assertTrue(AppDestination.Progress.route == AppRoutes.PROGRESS)
        assertTrue(AppDestination.Settings.route == AppRoutes.SETTINGS)
    }
}
