package com.flashlearn.app.navigation

import com.flashlearn.app.ui.AppViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.UUID

class AppViewModelNavigationTest {
    @Test fun backFromDetailReturnsToLibrary() {
        val vm = AppViewModel()
        val id = UUID.randomUUID()
        vm.openLibraryDetail(id)
        vm.goBack()
        assertEquals(AppRoutes.LIBRARY, vm.state.value.selectedRoute)
        assertNull(vm.state.value.selectedConceptId)
    }

    @Test fun backFromBackupReturnsToSettings() {
        val vm = AppViewModel()
        vm.navigate(AppRoutes.BACKUP)
        vm.goBack()
        assertEquals(AppRoutes.SETTINGS, vm.state.value.selectedRoute)
    }

    @Test fun backFromPrimaryDestinationReturnsHome() {
        val vm = AppViewModel()
        vm.navigate(AppRoutes.PROGRESS)
        vm.goBack()
        assertEquals(AppRoutes.HOME, vm.state.value.selectedRoute)
    }
}
