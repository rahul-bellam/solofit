package com.solofit.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.solofit.app.data.local.entity.PlannedExerciseEntity
import com.solofit.app.data.local.entity.WeeklyPlanEntity
import com.solofit.app.data.local.relation.PlanWithExercises
import kotlinx.coroutines.flow.Flow

@Dao
interface WeeklyPlanDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: WeeklyPlanEntity): Long

    @Update
    suspend fun updatePlan(plan: WeeklyPlanEntity)

    @Delete
    suspend fun deletePlan(plan: WeeklyPlanEntity)

    @Query("SELECT * FROM weekly_plans ORDER BY dayOfWeek ASC")
    fun observeAllPlans(): Flow<List<WeeklyPlanEntity>>

    @Query("SELECT * FROM weekly_plans WHERE id = :id")
    suspend fun getPlan(id: Long): WeeklyPlanEntity?

    @Query("SELECT * FROM weekly_plans WHERE dayOfWeek = :dayOfWeek LIMIT 1")
    fun observePlanForDay(dayOfWeek: Int): Flow<WeeklyPlanEntity?>

    @Query("SELECT * FROM weekly_plans WHERE dayOfWeek = :dayOfWeek LIMIT 1")
    suspend fun getPlanForDay(dayOfWeek: Int): WeeklyPlanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: PlannedExerciseEntity): Long

    @Update
    suspend fun updateExercise(exercise: PlannedExerciseEntity)

    @Delete
    suspend fun deleteExercise(exercise: PlannedExerciseEntity)

    @Query("SELECT * FROM planned_exercises WHERE planId = :planId ORDER BY sortOrder ASC")
    fun observeExercisesForPlan(planId: Long): Flow<List<PlannedExerciseEntity>>

    @Query("SELECT * FROM planned_exercises WHERE planId = :planId ORDER BY sortOrder ASC")
    suspend fun getExercisesForPlan(planId: Long): List<PlannedExerciseEntity>

    @Query("DELETE FROM planned_exercises WHERE planId = :planId")
    suspend fun clearExercises(planId: Long)

    @Query("SELECT COUNT(*) FROM planned_exercises WHERE planId = :planId AND isCompleted = 0")
    fun observeIncompleteCount(planId: Long): Flow<Int>

    @Query("UPDATE planned_exercises SET isCompleted = :completed WHERE id = :id")
    suspend fun setExerciseCompleted(id: Long, completed: Boolean)

    @Query("UPDATE planned_exercises SET isCompleted = 0 WHERE planId = :planId")
    suspend fun resetPlan(planId: Long)
}
