package com.solofit.app.reminders

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.solofit.app.core.DateUtils
import com.solofit.app.data.local.UserPreferences
import com.solofit.app.domain.repository.WorkoutRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

/**
 * Daily workout reminder (silent). Skips if a workout was already completed today,
 * then reschedules itself for the next day's chosen time.
 */
@HiltWorker
class WorkoutWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val prefs: UserPreferences,
    private val workoutRepository: WorkoutRepository,
    private val scheduler: ReminderScheduler,
    private val notifier: SoloNotifier
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val settings = withContext(Dispatchers.IO) { prefs.reminderSettings.first() }
        if (!settings.workoutEnabled) return Result.success()

        val today = DateUtils.today()
        val didWorkoutToday = withContext(Dispatchers.IO) { workoutRepository.observeHistory().first() }
            .any { it.session.date == today }

        if (!didWorkoutToday) {
            notifier.notify(
                channelId = SoloNotifier.CHANNEL_WORKOUT,
                notificationId = SoloNotifier.ID_WORKOUT,
                title = "Workout time 🏋️",
                message = "No session logged yet today. A quick routine keeps your streak alive."
            )
        }

        // Re-arm for tomorrow.
        scheduler.scheduleWorkout(settings)
        return Result.success()
    }
}
