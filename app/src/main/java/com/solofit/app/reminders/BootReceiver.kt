package com.solofit.app.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.solofit.app.data.local.UserPreferences
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Re-arms silent reminders after the device reboots (WorkManager jobs don't
 * automatically persist across reboots for periodic re-evaluation of our times).
 */
class BootReceiver : BroadcastReceiver() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface BootEntryPoint {
        fun userPreferences(): UserPreferences
        fun reminderScheduler(): ReminderScheduler
        fun applicationScope(): CoroutineScope
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            BootEntryPoint::class.java
        )
        val prefs = entryPoint.userPreferences()
        val scheduler = entryPoint.reminderScheduler()
        val scope = entryPoint.applicationScope()

        val pending = goAsync()
        scope.launch {
            try {
                val settings = prefs.reminderSettings.first()
                scheduler.apply(settings)
                scheduler.scheduleMaintenance()
            } finally {
                pending.finish()
            }
        }
    }
}
