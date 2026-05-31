package com.solofit.app.reminders

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.solofit.app.data.local.UserPreferences
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

/**
 * Evening 2-minute gratitude prompt (silent). Self-reschedules for tomorrow.
 */
@HiltWorker
class EveningGratitudeWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val prefs: UserPreferences,
    private val notifier: SoloNotifier,
    private val scheduler: ReminderScheduler
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val settings = prefs.reminderSettings.first()
        if (settings.eveningGratitudeEnabled) {
            notifier.notify(
                channelId = SoloNotifier.CHANNEL_JOURNAL,
                notificationId = SoloNotifier.ID_EVENING,
                title = "Evening reflection 🌙",
                message = "2-minute check-in: what are 3 things you're grateful for today?"
            )
        }
        scheduler.scheduleEveningGratitude(settings)
        return Result.success()
    }
}
