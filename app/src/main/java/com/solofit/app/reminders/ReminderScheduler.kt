package com.solofit.app.reminders

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.Constraints
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.solofit.app.domain.model.ReminderSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Arms/cancels the WorkManager jobs that drive silent reminders.
 * WorkManager survives process death and (with RECEIVE_BOOT_COMPLETED + the boot
 * receiver) reboots, so reminders are reliable without a foreground service.
 */
@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notifier: SoloNotifier
) {
    private val workManager get() = WorkManager.getInstance(context)

    /** Apply the full settings snapshot: (re)schedule or cancel each reminder. */
    fun apply(settings: ReminderSettings) {
        notifier.ensureChannels()
        if (settings.hydrationEnabled) scheduleHydration(settings) else cancelHydration()
        if (settings.workoutEnabled) scheduleWorkout(settings) else cancelWorkout()
        if (settings.morningGoalsEnabled) scheduleMorningGoals(settings) else cancelMorningGoals()
        if (settings.eveningGratitudeEnabled) scheduleEveningGratitude(settings) else cancelEveningGratitude()
    }

    fun scheduleHydration(settings: ReminderSettings) {
        // WorkManager periodic minimum is 15 minutes; clamp defensively.
        val interval = settings.hydrationIntervalMinutes.coerceAtLeast(15).toLong()
        val request = PeriodicWorkRequestBuilder<HydrationWorker>(
            interval, TimeUnit.MINUTES
        ).build()
        workManager.enqueueUniquePeriodicWork(
            WORK_HYDRATION,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun cancelHydration() = workManager.cancelUniqueWork(WORK_HYDRATION)

    /** One-shot scheduled at the next occurrence of the chosen time; it re-arms itself. */
    fun scheduleWorkout(settings: ReminderSettings) {
        val request = OneTimeWorkRequestBuilder<WorkoutWorker>()
            .setInitialDelay(millisUntilNext(settings.workoutTimeMinutes), TimeUnit.MILLISECONDS)
            .build()
        workManager.enqueueUniqueWork(WORK_WORKOUT, ExistingWorkPolicy.REPLACE, request)
    }

    fun cancelWorkout() = workManager.cancelUniqueWork(WORK_WORKOUT)

    fun scheduleMorningGoals(settings: ReminderSettings) {
        val request = OneTimeWorkRequestBuilder<MorningGoalsWorker>()
            .setInitialDelay(millisUntilNext(settings.morningGoalsTimeMinutes), TimeUnit.MILLISECONDS)
            .build()
        workManager.enqueueUniqueWork(WORK_MORNING, ExistingWorkPolicy.REPLACE, request)
    }

    fun cancelMorningGoals() = workManager.cancelUniqueWork(WORK_MORNING)

    fun scheduleEveningGratitude(settings: ReminderSettings) {
        val request = OneTimeWorkRequestBuilder<EveningGratitudeWorker>()
            .setInitialDelay(millisUntilNext(settings.eveningGratitudeTimeMinutes), TimeUnit.MILLISECONDS)
            .build()
        workManager.enqueueUniqueWork(WORK_EVENING, ExistingWorkPolicy.REPLACE, request)
    }

    fun cancelEveningGratitude() = workManager.cancelUniqueWork(WORK_EVENING)

    /** Weekly DB compaction; constrained to idle + not-low-battery to stay invisible. */
    fun scheduleMaintenance() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .setRequiresDeviceIdle(true)
            .build()
        val request = PeriodicWorkRequestBuilder<DbMaintenanceWorker>(
            7, TimeUnit.DAYS
        ).setConstraints(constraints).build()
        workManager.enqueueUniquePeriodicWork(
            WORK_MAINTENANCE,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    private fun millisUntilNext(minuteOfDay: Int): Long {
        val now = Calendar.getInstance()
        val target = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, minuteOfDay / 60)
            set(Calendar.MINUTE, minuteOfDay % 60)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (!target.after(now)) target.add(Calendar.DAY_OF_YEAR, 1)
        return target.timeInMillis - now.timeInMillis
    }

    companion object {
        const val WORK_HYDRATION = "solofit_hydration_reminder"
        const val WORK_WORKOUT = "solofit_workout_reminder"
        const val WORK_MORNING = "solofit_morning_goals_reminder"
        const val WORK_EVENING = "solofit_evening_gratitude_reminder"
        const val WORK_MAINTENANCE = "solofit_db_maintenance"
    }
}
