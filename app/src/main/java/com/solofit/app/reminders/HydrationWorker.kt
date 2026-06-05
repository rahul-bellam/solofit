package com.solofit.app.reminders

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.solofit.app.core.DateUtils
import com.solofit.app.data.local.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.Calendar

@HiltWorker
class HydrationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val prefs: UserPreferences,
    private val notifier: SoloNotifier
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val settings = withContext(Dispatchers.IO) { prefs.reminderSettings.first() }
        if (!settings.hydrationEnabled) return Result.success()

        val now = Calendar.getInstance()
        val minuteOfDay = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        if (QuietHours.isQuiet(minuteOfDay, settings.quietStartMinutes, settings.quietEndMinutes)) {
            return Result.success()
        }

        val today = DateUtils.today()
        val current = withContext(Dispatchers.IO) { prefs.waterMl(today).first() }
        val goal = withContext(Dispatchers.IO) { prefs.waterGoalMl.first() }
        if (current >= goal) return Result.success()

        val remaining = goal - current
        notifier.notify(
            channelId = SoloNotifier.CHANNEL_HYDRATION,
            notificationId = SoloNotifier.ID_HYDRATION,
            title = "Hydration Check",
            message = "You've logged ${current}ml of ${goal}ml today. Staying hydrated supports recovery."
        )
        return Result.success()
    }
}
