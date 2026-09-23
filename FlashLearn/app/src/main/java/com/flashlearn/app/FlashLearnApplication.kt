package com.flashlearn.app

import android.app.Application
import com.flashlearn.domain.usecase.RefreshDataUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@EntryPoint
@InstallIn(SingletonComponent::class)
interface DataRefreshEntryPoint {
    fun refreshDataUseCase(): RefreshDataUseCase
}

@HiltAndroidApp
class FlashLearnApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        val refreshData = EntryPointAccessors.fromApplication(this, DataRefreshEntryPoint::class.java)
            .refreshDataUseCase()
        applicationScope.launch { runCatching { refreshData() } }
    }
}
