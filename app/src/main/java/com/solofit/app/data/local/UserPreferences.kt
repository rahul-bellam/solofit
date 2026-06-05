package com.solofit.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import dagger.hilt.android.qualifiers.ApplicationContext
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.solofit.app.domain.model.SoloFitModule
import com.solofit.app.domain.model.ReminderSettings
import com.solofit.app.domain.model.ThemeMode
import com.solofit.app.domain.model.TrainingGoal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
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
    @ApplicationContext private val context: Context
) {
    private val onboardingCompleteKey = booleanPreferencesKey("onboarding_complete")
    private val themeModeKey = stringPreferencesKey("theme_mode")
    private val animationsEnabledKey = booleanPreferencesKey("fun_animations_enabled")
    private val reducedMotionAppliedKey = booleanPreferencesKey("reduced_motion_applied")
    private val phaseNameKey = stringPreferencesKey("phase_name")
    private val phaseStartDateKey = stringPreferencesKey("phase_start_date")
    private val phaseTargetDaysKey = intPreferencesKey("phase_target_days")
    private val trainingGoalKey = stringPreferencesKey("training_goal")
    private val voicePersonalityKey = stringPreferencesKey("voice_personality")

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
    private val stepGoalKey = intPreferencesKey("step_goal")

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

    val voicePersonality: Flow<com.solofit.app.sol.VoicePersonality> =
        context.dataStore.data.map { prefs ->
            prefs[voicePersonalityKey]?.let {
                runCatching { com.solofit.app.sol.VoicePersonality.valueOf(it) }.getOrNull()
            } ?: com.solofit.app.sol.VoicePersonality.COMPANION
        }

    suspend fun setVoicePersonality(personality: com.solofit.app.sol.VoicePersonality) {
        context.dataStore.edit { it[voicePersonalityKey] = personality.name }
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

    val stepGoal: Flow<Int> =
        context.dataStore.data.map { it[stepGoalKey] ?: 8000 }

    suspend fun setStepGoal(goal: Int) {
        context.dataStore.edit { it[stepGoalKey] = goal.coerceIn(1000, 50000) }
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

    // ──────────────────────────────────────────────
    //  Module System
    // ──────────────────────────────────────────────

    private val enabledModulesKey = stringPreferencesKey("enabled_modules")
    private val moduleSelectionCompleteKey = booleanPreferencesKey("module_selection_complete")
    private val moduleOrderKey = stringPreferencesKey("module_order")

    val moduleSelectionComplete: Flow<Boolean> =
        context.dataStore.data.map { it[moduleSelectionCompleteKey] ?: false }

    suspend fun setModuleSelectionComplete(value: Boolean) {
        context.dataStore.edit { it[moduleSelectionCompleteKey] = value }
    }

    val enabledModules: Flow<List<SoloFitModule>> =
        context.dataStore.data.map { prefs ->
            val raw = prefs[enabledModulesKey] ?: return@map SoloFitModule.DEFAULT_ENABLED
            raw.split(",").mapNotNull { SoloFitModule.fromId(it.trim()) }
                .ifEmpty { SoloFitModule.DEFAULT_ENABLED }
        }

    suspend fun setEnabledModules(modules: List<SoloFitModule>) {
        context.dataStore.edit { it[enabledModulesKey] = modules.joinToString(",") { it.id } }
    }

    val moduleOrder: Flow<List<SoloFitModule>> =
        context.dataStore.data.map { prefs ->
            val raw = prefs[moduleOrderKey] ?: return@map emptyList()
            raw.split(",").mapNotNull { SoloFitModule.fromId(it.trim()) }
        }

    suspend fun setModuleOrder(modules: List<SoloFitModule>) {
        context.dataStore.edit { it[moduleOrderKey] = modules.joinToString(",") { it.id } }
    }

    suspend fun clearModulePreferences() {
        context.dataStore.edit {
            it.remove(enabledModulesKey)
            it.remove(moduleSelectionCompleteKey)
            it.remove(moduleOrderKey)
        }
    }

    // ──────────────────────────────────────────────
    //  Wellness / Daily Check-In
    // ──────────────────────────────────────────────

    /** Sleep hours for a given ISO date. */
    fun sleepHours(date: String): Flow<Float> {
        val key = stringPreferencesKey("sleep_$date")
        return context.dataStore.data.map { it[key]?.toFloatOrNull() ?: 0f }
    }

    suspend fun setSleepHours(date: String, hours: Float) {
        val key = stringPreferencesKey("sleep_$date")
        context.dataStore.edit { it[key] = hours.coerceIn(0f, 24f).toString() }
    }

    /** Stress level 1–5 for a given ISO date. */
    fun stressLevel(date: String): Flow<Int> {
        val key = intPreferencesKey("stress_$date")
        return context.dataStore.data.map { it[key]?.coerceIn(1, 5) ?: 3 }
    }

    suspend fun setStressLevel(date: String, level: Int) {
        val key = intPreferencesKey("stress_$date")
        context.dataStore.edit { it[key] = level.coerceIn(1, 5) }
    }

    /** Mood level 1–5 for a given ISO date. */
    fun moodLevel(date: String): Flow<Int> {
        val key = intPreferencesKey("mood_$date")
        return context.dataStore.data.map { it[key]?.coerceIn(1, 5) ?: 3 }
    }

    suspend fun setMoodLevel(date: String, level: Int) {
        val key = intPreferencesKey("mood_$date")
        context.dataStore.edit { it[key] = level.coerceIn(1, 5) }
    }

    /** Energy level 1–5 for a given ISO date. */
    fun energyLevel(date: String): Flow<Int> {
        val key = intPreferencesKey("energy_$date")
        return context.dataStore.data.map { it[key]?.coerceIn(1, 5) ?: 3 }
    }

    suspend fun setEnergyLevel(date: String, level: Int) {
        val key = intPreferencesKey("energy_$date")
        context.dataStore.edit { it[key] = level.coerceIn(1, 5) }
    }

    /** Meditation minutes for a given ISO date. */
    fun meditationMinutes(date: String): Flow<Int> {
        val key = intPreferencesKey("meditation_$date")
        return context.dataStore.data.map { it[key] ?: 0 }
    }

    suspend fun setMeditationMinutes(date: String, minutes: Int) {
        val key = intPreferencesKey("meditation_$date")
        context.dataStore.edit { it[key] = minutes.coerceAtLeast(0) }
    }

    private val customHabitsKey = stringPreferencesKey("custom_habits")

    /** Habit completed flag for a given date + habit id. */
    fun habitCompleted(date: String, habitId: String): Flow<Boolean> {
        val key = booleanPreferencesKey("habit_${date}_$habitId")
        return context.dataStore.data.map { it[key] ?: false }
    }

    suspend fun setHabitCompleted(date: String, habitId: String, completed: Boolean) {
        val key = booleanPreferencesKey("habit_${date}_$habitId")
        context.dataStore.edit { it[key] = completed }
    }

    val customHabits: Flow<List<Pair<String, String>>> =
        context.dataStore.data.map { prefs ->
            prefs[customHabitsKey]?.split("|")?.mapNotNull { entry ->
                val parts = entry.split(":", limit = 2)
                if (parts.size == 2) parts[0] to parts[1] else null
            } ?: emptyList()
        }

    suspend fun addCustomHabit(habitId: String, displayName: String) {
        context.dataStore.edit { prefs ->
            val existing = prefs[customHabitsKey].orEmpty()
            val updated = if (existing.isNotEmpty()) "$existing|$habitId:$displayName" else "$habitId:$displayName"
            prefs[customHabitsKey] = updated
        }
    }

    suspend fun removeCustomHabit(habitId: String) {
        context.dataStore.edit { prefs ->
            val entries = prefs[customHabitsKey]?.split("|")?.filter {
                !it.startsWith("$habitId:")
            } ?: emptyList()
            prefs[customHabitsKey] = entries.joinToString("|")
        }
    }

    /** All wellness data for a date packed into a single snapshot. */
    data class DailyWellness(
        val sleepHours: Float = 0f,
        val stressLevel: Int = 3,
        val moodLevel: Int = 3,
        val energyLevel: Int = 3,
        val meditationMinutes: Int = 0
    )

    fun dailyWellness(date: String): Flow<DailyWellness> {
        val sleepKey = stringPreferencesKey("sleep_$date")
        val stressKey = intPreferencesKey("stress_$date")
        val moodKey = intPreferencesKey("mood_$date")
        val energyKey = intPreferencesKey("energy_$date")
        val meditationKey = intPreferencesKey("meditation_$date")
        return context.dataStore.data.map { prefs ->
            DailyWellness(
                sleepHours = prefs[sleepKey]?.toFloatOrNull() ?: 0f,
                stressLevel = (prefs[stressKey] ?: 3).coerceIn(1, 5),
                moodLevel = (prefs[moodKey] ?: 3).coerceIn(1, 5),
                energyLevel = (prefs[energyKey] ?: 3).coerceIn(1, 5),
                meditationMinutes = prefs[meditationKey] ?: 0
            )
        }
    }

    /** Compute a readiness score 0–100 from today's wellness data. */
    fun readinessScore(date: String): Flow<Int> = dailyWellness(date).map { w ->
        val sleep = ((w.sleepHours / 8f).coerceAtMost(1f) * 30).toInt()
        val stress = ((5 - w.stressLevel) / 4f * 20).toInt()
        val mood = (w.moodLevel / 5f * 20).toInt()
        val energy = (w.energyLevel / 5f * 30).toInt()
        (sleep + stress + mood + energy).coerceIn(0, 100)
    }

    /**
     * Remove stale per-day wellness keys older than [keepDays] to prevent
     * DataStore file bloat.
     */
    suspend fun pruneOldData(keepDays: Long = 90) {
        val cutoff = LocalDate.now().minusDays(keepDays)
        context.dataStore.edit { prefs ->
            prefs.asMap().keys
                .filter { key ->
                    val datePrefix = listOf("water_", "sleep_", "stress_", "mood_", "energy_", "meditation_", "habit_")
                        .firstOrNull { key.name.startsWith(it) }
                    if (datePrefix != null) {
                        val dateStr = key.name.removePrefix(datePrefix)
                        val datePart = if (datePrefix == "habit_") {
                            dateStr.substringBefore("_")
                        } else dateStr
                        runCatching { LocalDate.parse(datePart).isBefore(cutoff) }.getOrDefault(false)
                    } else false
                }
                .forEach { prefs.remove(it) }
        }
    }
}
