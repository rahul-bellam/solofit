package com.solofit.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.solofit.app.domain.repository.FoodRepository
import com.solofit.app.reminders.ReminderScheduler
import com.solofit.app.reminders.SoloNotifier
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class SoloFitApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var notifier: SoloNotifier
    @Inject lateinit var foodRepository: FoodRepository
    @Inject lateinit var appScope: CoroutineScope
    @Inject lateinit var reminderScheduler: ReminderScheduler

    override fun onCreate() {
        super.onCreate()
        // Create the silent notification channels up-front.
        notifier.ensureChannels()

        // Read-ahead / prefetch: warm the food DB off the main thread so the user's
        // first nutrition search is instant (OS paging / storage prefetch principle).
        appScope.launch(Dispatchers.IO) {
            runCatching { foodRepository.warmUp() }
        }

        // Arm periodic DB compaction (idempotent: KEEP policy). Keeps footprint lean.
        runCatching { reminderScheduler.scheduleMaintenance() }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
