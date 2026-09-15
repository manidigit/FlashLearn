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
                "needs_review",
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
    fun everyDestinationUsesItsDeclaredRoute() {
        val destinations = listOf(
            AppDestination.Home,
            AppDestination.Review,
            AppDestination.NeedsReview,
            AppDestination.Progress,
            AppDestination.Settings,
            AppDestination.AddWord,
            AppDestination.BulkImport,
            AppDestination.Backup,
            AppDestination.Library,
            AppDestination.LibraryDetail
        )
        assertEquals(AppRoutes.all(), destinations.map { it.route })
        assertEquals(destinations.map { it.route }.toSet().size, destinations.size)
    }

    @Test
    fun destinationsHaveStablePrimaryRoutes() {
        assertTrue(AppDestination.Home.route == AppRoutes.HOME)
        assertTrue(AppDestination.Review.route == AppRoutes.REVIEW)
        assertTrue(AppDestination.Progress.route == AppRoutes.PROGRESS)
        assertTrue(AppDestination.Settings.route == AppRoutes.SETTINGS)
    }
}
