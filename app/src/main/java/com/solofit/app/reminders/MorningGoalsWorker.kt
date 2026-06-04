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

/**
 * Morning "plan your day" prompt (silent). Self-reschedules for tomorrow.
 */
@HiltWorker
class MorningGoalsWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val prefs: UserPreferences,
    private val notifier: SoloNotifier,
    private val scheduler: ReminderScheduler
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val settings = withContext(Dispatchers.IO) { prefs.reminderSettings.first() }
        if (settings.morningGoalsEnabled) {
            notifier.notify(
                channelId = SoloNotifier.CHANNEL_JOURNAL,
                notificationId = SoloNotifier.ID_MORNING,
                title = "Good morning ☀️ Plan your day",
                message = "Take a minute to set 3 goals for today. Small wins build momentum."
            )
        }
        scheduler.scheduleMorningGoals(settings)
        return Result.success()
    }
}
