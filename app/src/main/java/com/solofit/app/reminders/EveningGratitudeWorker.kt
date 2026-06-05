package com.solofit.app.reminders

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.solofit.app.data.local.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class EveningGratitudeWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val prefs: UserPreferences,
    private val notifier: SoloNotifier,
    private val scheduler: ReminderScheduler
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val settings = withContext(Dispatchers.IO) { prefs.reminderSettings.first() }
        if (settings.eveningGratitudeEnabled) {
            notifier.notify(
                channelId = SoloNotifier.CHANNEL_JOURNAL,
                notificationId = SoloNotifier.ID_EVENING,
                title = "Evening Check-In",
                message = "Today included meaningful progress. Small consistent actions tend to compound over time."
            )
        }
        scheduler.scheduleEveningGratitude(settings)
        return Result.success()
    }
}
