package com.solofit.app.domain.repository

import com.solofit.app.data.local.dao.LoggedFoodRow
import com.solofit.app.data.local.entity.DailyLogEntity
import com.solofit.app.data.local.entity.ExerciseSetEntity
import com.solofit.app.data.local.entity.FoodItemEntity
import com.solofit.app.data.local.entity.RoutineEntity
import com.solofit.app.data.local.entity.UserProfileEntity
import com.solofit.app.data.local.entity.WorkoutSessionEntity
import com.solofit.app.data.local.relation.RoutineWithExercises
import com.solofit.app.data.local.relation.SessionWithSets
import com.solofit.app.domain.model.ExercisePlan
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun observeProfile(): Flow<UserProfileEntity?>
    suspend fun getProfile(): UserProfileEntity?
    suspend fun saveProfile(profile: UserProfileEntity): Long
    val onboardingComplete: Flow<Boolean>
    suspend fun setOnboardingComplete(value: Boolean)

    val themeMode: Flow<com.solofit.app.domain.model.ThemeMode>
    suspend fun setThemeMode(mode: com.solofit.app.domain.model.ThemeMode)

    val animationsEnabled: Flow<Boolean>
    suspend fun setAnimationsEnabled(enabled: Boolean)

    val reducedMotionApplied: Flow<Boolean>
    suspend fun setReducedMotionApplied(value: Boolean)

    fun waterMl(date: String): Flow<Int>
    suspend fun addWaterMl(date: String, deltaMl: Int)
    suspend fun setWaterMl(date: String, valueMl: Int)

    val phaseName: Flow<String>
    val phaseStartDate: Flow<String?>
    val phaseTargetDays: Flow<Int>
    suspend fun setPhase(name: String, startDateIso: String, targetDays: Int)

    val waterGoalMl: Flow<Int>
    suspend fun setWaterGoalMl(ml: Int)

    val trainingGoal: Flow<com.solofit.app.domain.model.TrainingGoal>
    suspend fun setTrainingGoal(goal: com.solofit.app.domain.model.TrainingGoal)
}

interface BodyRepository {
    fun observeMeasurements(): Flow<List<com.solofit.app.data.local.entity.BodyMeasurementEntity>>
    fun observeLatestMeasurement(): Flow<com.solofit.app.data.local.entity.BodyMeasurementEntity?>
    suspend fun getMeasurementForDate(date: String): com.solofit.app.data.local.entity.BodyMeasurementEntity?
    suspend fun saveMeasurement(entry: com.solofit.app.data.local.entity.BodyMeasurementEntity): Long
    suspend fun deleteMeasurement(id: Long)

    fun observeMetric(date: String): Flow<com.solofit.app.data.local.entity.DailyMetricEntity?>
    suspend fun getMetric(date: String): com.solofit.app.data.local.entity.DailyMetricEntity?
    suspend fun saveMetric(entry: com.solofit.app.data.local.entity.DailyMetricEntity): Long
}

interface FoodRepository {
    fun observeAll(): Flow<List<FoodItemEntity>>
    fun search(query: String): Flow<List<FoodItemEntity>>
    suspend fun getById(id: Long): FoodItemEntity?
    suspend fun addCustomFood(item: FoodItemEntity): Long
    /** Search USDA FoodData Central and persist results as custom foods for offline reuse. */
    suspend fun searchUsda(query: String): List<FoodItemEntity>
    /** Read-ahead: touch the table so SQLite pages are warm before first search. */
    suspend fun warmUp()
}

interface DailyLogRepository {
    fun observeForDate(date: String): Flow<List<LoggedFoodRow>>
    fun observeTotalsForDate(date: String): Flow<com.solofit.app.domain.model.MacroTotals>
    suspend fun logFood(entry: DailyLogEntity): Long
    suspend fun removeEntry(entry: DailyLogEntity)
}

interface WorkoutRepository {
    fun observeRoutines(): Flow<List<RoutineWithExercises>>
    suspend fun getRoutine(id: Long): RoutineWithExercises?
    suspend fun saveRoutine(name: String, notes: String, exercises: List<ExercisePlan>, routineId: Long?): Long
    suspend fun deleteRoutine(routine: RoutineEntity)

    suspend fun startSession(routineId: Long): Long
    fun observeSession(sessionId: Long): Flow<SessionWithSets?>
    suspend fun updateSet(set: ExerciseSetEntity)
    suspend fun addSet(sessionId: Long, exerciseName: String, muscleGroup: String, orderIndex: Int): Long
    suspend fun deleteSet(set: ExerciseSetEntity)
    suspend fun completeSession(session: WorkoutSessionEntity)
    fun observeHistory(): Flow<List<SessionWithSets>>
    fun observeCompletedSetRows(): Flow<List<com.solofit.app.data.local.dao.CompletedSetRow>>
}

interface WeightRepository {
    fun observeAll(): kotlinx.coroutines.flow.Flow<List<com.solofit.app.data.local.entity.WeightEntryEntity>>
    suspend fun logWeight(date: String, weightKg: Double): Long
    suspend fun latest(): com.solofit.app.data.local.entity.WeightEntryEntity?
    suspend fun delete(id: Long)
}

interface JournalRepository {
    fun observeGoals(date: String): kotlinx.coroutines.flow.Flow<List<com.solofit.app.data.local.entity.GoalItemEntity>>
    suspend fun addGoal(date: String, text: String)
    suspend fun toggleGoal(goal: com.solofit.app.data.local.entity.GoalItemEntity)
    suspend fun deleteGoal(id: Long)

    fun observeGratitude(date: String): kotlinx.coroutines.flow.Flow<com.solofit.app.data.local.entity.GratitudeEntryEntity?>
    fun observeRecentGratitude(limit: Int = 14): kotlinx.coroutines.flow.Flow<List<com.solofit.app.data.local.entity.GratitudeEntryEntity>>
    suspend fun saveGratitude(date: String, text: String)
}

interface ProgressPhotoRepository {
    fun observeAll(): kotlinx.coroutines.flow.Flow<List<com.solofit.app.data.local.entity.ProgressPhotoEntity>>
    fun observeByPose(pose: String): kotlinx.coroutines.flow.Flow<List<com.solofit.app.data.local.entity.ProgressPhotoEntity>>
    /** Persist a captured bitmap to private storage and record a DB row. */
    suspend fun savePhoto(bitmap: android.graphics.Bitmap, date: String, pose: String): Long
    /** Absolute file path for a stored photo's file name (for display). */
    fun fileFor(fileName: String): java.io.File
    suspend fun delete(id: Long)
}
