package com.solofit.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.solofit.app.domain.model.ReminderSettings
import com.solofit.app.domain.model.ThemeMode
import com.solofit.app.domain.model.TrainingGoal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "solofit_prefs")

/**
 * Tiny local key-value store for app preferences:
 *  - onboarding completion flag
 *  - theme mode (System / Light / Dark)
 *  - per-day water intake (ml), keyed by ISO date so it resets each day
 *  - reminder settings (hydration + workout + quiet hours)
 */
@Singleton
class UserPreferences @Inject constructor(
    private val context: Context
) {
    private val onboardingCompleteKey = booleanPreferencesKey("onboarding_complete")
    private val themeModeKey = stringPreferencesKey("theme_mode")
    private val animationsEnabledKey = booleanPreferencesKey("fun_animations_enabled")
    private val reducedMotionAppliedKey = booleanPreferencesKey("reduced_motion_applied")
    private val phaseNameKey = stringPreferencesKey("phase_name")
    private val phaseStartDateKey = stringPreferencesKey("phase_start_date")
    private val phaseTargetDaysKey = intPreferencesKey("phase_target_days")
    private val trainingGoalKey = stringPreferencesKey("training_goal")

    // Reminder keys
    private val hydrationEnabledKey = booleanPreferencesKey("rem_hydration_enabled")
    private val hydrationIntervalKey = intPreferencesKey("rem_hydration_interval")
    private val workoutEnabledKey = booleanPreferencesKey("rem_workout_enabled")
    private val workoutTimeKey = intPreferencesKey("rem_workout_time")
    private val morningGoalsEnabledKey = booleanPreferencesKey("rem_morning_enabled")
    private val morningGoalsTimeKey = intPreferencesKey("rem_morning_time")
    private val eveningGratitudeEnabledKey = booleanPreferencesKey("rem_evening_enabled")
    private val eveningGratitudeTimeKey = intPreferencesKey("rem_evening_time")
    private val waterGoalMlKey = intPreferencesKey("water_goal_ml")
    private val quietStartKey = intPreferencesKey("rem_quiet_start")
    private val quietEndKey = intPreferencesKey("rem_quiet_end")

    val onboardingComplete: Flow<Boolean> =
        context.dataStore.data.map { it[onboardingCompleteKey] ?: false }

    suspend fun setOnboardingComplete(value: Boolean) {
        context.dataStore.edit { it[onboardingCompleteKey] = value }
    }

    val themeMode: Flow<ThemeMode> =
        context.dataStore.data.map { prefs ->
            prefs[themeModeKey]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: ThemeMode.SYSTEM
        }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[themeModeKey] = mode.name }
    }

    /** Playful exercise micro-animations (dumbbell checkbox, scroll press). Default on. */
    val animationsEnabled: Flow<Boolean> =
        context.dataStore.data.map { it[animationsEnabledKey] ?: true }

    suspend fun setAnimationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[animationsEnabledKey] = enabled }
    }

    /** One-time flag so OS reduce-motion is auto-applied once, never overriding the user later. */
    val reducedMotionApplied: Flow<Boolean> =
        context.dataStore.data.map { it[reducedMotionAppliedKey] ?: false }

    suspend fun setReducedMotionApplied(value: Boolean) {
        context.dataStore.edit { it[reducedMotionAppliedKey] = value }
    }

    /** Transformation phase: name, start date (ISO), and target length in days. */
    val phaseName: Flow<String> =
        context.dataStore.data.map { it[phaseNameKey] ?: "Foundation Recomp" }

    val phaseStartDate: Flow<String?> =
        context.dataStore.data.map { it[phaseStartDateKey] }

    val phaseTargetDays: Flow<Int> =
        context.dataStore.data.map { it[phaseTargetDaysKey] ?: 365 }

    suspend fun setPhase(name: String, startDateIso: String, targetDays: Int) {
        context.dataStore.edit {
            it[phaseNameKey] = name
            it[phaseStartDateKey] = startDateIso
            it[phaseTargetDaysKey] = targetDays
        }
    }

    val trainingGoal: Flow<TrainingGoal> =
        context.dataStore.data.map { prefs ->
            prefs[trainingGoalKey]?.let { runCatching { TrainingGoal.valueOf(it) }.getOrNull() }
                ?: TrainingGoal.BODYBUILDING
        }

    suspend fun setTrainingGoal(goal: TrainingGoal) {
        context.dataStore.edit { it[trainingGoalKey] = goal.name }
    }

    /** Water intake in millilitres for a given ISO date. */
    fun waterMl(date: String): Flow<Int> {
        val key = intPreferencesKey("water_$date")
        return context.dataStore.data.map { it[key] ?: 0 }
    }

    suspend fun addWaterMl(date: String, deltaMl: Int) {
        val key = intPreferencesKey("water_$date")
        context.dataStore.edit { prefs ->
            val current = prefs[key] ?: 0
            prefs[key] = (current + deltaMl).coerceAtLeast(0)
        }
    }

    suspend fun setWaterMl(date: String, valueMl: Int) {
        val key = intPreferencesKey("water_$date")
        context.dataStore.edit { it[key] = valueMl.coerceAtLeast(0) }
    }

    val waterGoalMl: Flow<Int> =
        context.dataStore.data.map { it[waterGoalMlKey] ?: 3000 }

    suspend fun setWaterGoalMl(ml: Int) {
        context.dataStore.edit { it[waterGoalMlKey] = ml.coerceIn(250, 10000) }
    }

    // ---- Reminders ----
    val reminderSettings: Flow<ReminderSettings> =
        context.dataStore.data.map { p ->
            val defaults = ReminderSettings()
            ReminderSettings(
                hydrationEnabled = p[hydrationEnabledKey] ?: defaults.hydrationEnabled,
                hydrationIntervalMinutes = p[hydrationIntervalKey] ?: defaults.hydrationIntervalMinutes,
                workoutEnabled = p[workoutEnabledKey] ?: defaults.workoutEnabled,
                workoutTimeMinutes = p[workoutTimeKey] ?: defaults.workoutTimeMinutes,
                morningGoalsEnabled = p[morningGoalsEnabledKey] ?: defaults.morningGoalsEnabled,
                morningGoalsTimeMinutes = p[morningGoalsTimeKey] ?: defaults.morningGoalsTimeMinutes,
                eveningGratitudeEnabled = p[eveningGratitudeEnabledKey] ?: defaults.eveningGratitudeEnabled,
                eveningGratitudeTimeMinutes = p[eveningGratitudeTimeKey] ?: defaults.eveningGratitudeTimeMinutes,
                quietStartMinutes = p[quietStartKey] ?: defaults.quietStartMinutes,
                quietEndMinutes = p[quietEndKey] ?: defaults.quietEndMinutes
            )
        }

    suspend fun setReminderSettings(settings: ReminderSettings) {
        context.dataStore.edit { p ->
            p[hydrationEnabledKey] = settings.hydrationEnabled
            p[hydrationIntervalKey] = settings.hydrationIntervalMinutes
            p[workoutEnabledKey] = settings.workoutEnabled
            p[workoutTimeKey] = settings.workoutTimeMinutes
            p[morningGoalsEnabledKey] = settings.morningGoalsEnabled
            p[morningGoalsTimeKey] = settings.morningGoalsTimeMinutes
            p[eveningGratitudeEnabledKey] = settings.eveningGratitudeEnabled
            p[eveningGratitudeTimeKey] = settings.eveningGratitudeTimeMinutes
            p[quietStartKey] = settings.quietStartMinutes
            p[quietEndKey] = settings.quietEndMinutes
        }
    }
}
