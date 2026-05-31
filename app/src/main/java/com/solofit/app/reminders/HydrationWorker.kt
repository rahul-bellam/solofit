package com.solofit.app.reminders

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.solofit.app.core.DateUtils
import com.solofit.app.data.local.UserPreferences
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.Calendar

/**
 * Periodic hydration nudge. Posts a SILENT notification unless:
 *  - hydration reminders are disabled,
 *  - we're inside quiet hours, or
 *  - the daily water goal is already met.
 */
@HiltWorker
class HydrationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val prefs: UserPreferences,
    private val notifier: SoloNotifier
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val settings = prefs.reminderSettings.first()
        if (!settings.hydrationEnabled) return Result.success()

        val now = Calendar.getInstance()
        val minuteOfDay = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        if (QuietHours.isQuiet(minuteOfDay, settings.quietStartMinutes, settings.quietEndMinutes)) {
            return Result.success()
        }

        // Skip if already hydrated enough today.
        val today = DateUtils.today()
        val current = prefs.waterMl(today).first()
        val goal = 3000
        if (current >= goal) return Result.success()

        val remaining = goal - current
        notifier.notify(
            channelId = SoloNotifier.CHANNEL_HYDRATION,
            notificationId = SoloNotifier.ID_HYDRATION,
            title = "Time for water 💧",
            message = "You've had ${current} ml today. About ${remaining} ml to go — sip and log it."
        )
        return Result.success()
    }
}
