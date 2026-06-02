package com.solofit.app.domain.repository

import com.solofit.app.data.local.entity.PlannedExerciseEntity
import com.solofit.app.data.local.entity.WeeklyPlanEntity
import kotlinx.coroutines.flow.Flow

interface WeeklyPlanRepository {
    fun observeAllPlans(): Flow<List<WeeklyPlanEntity>>
    fun observePlanForDay(dayOfWeek: Int): Flow<WeeklyPlanEntity?>
    suspend fun getPlanForDay(dayOfWeek: Int): WeeklyPlanEntity?
    fun observeExercisesForPlan(planId: Long): Flow<List<PlannedExerciseEntity>>
    fun observeIncompleteCount(planId: Long): Flow<Int>

    suspend fun savePlan(plan: WeeklyPlanEntity): Long
    suspend fun addExercise(exercise: PlannedExerciseEntity): Long
    suspend fun updateExercise(exercise: PlannedExerciseEntity)
    suspend fun deleteExercise(exercise: PlannedExerciseEntity)
    suspend fun setExerciseCompleted(id: Long, completed: Boolean)
    suspend fun resetPlan(planId: Long)
    suspend fun deletePlan(plan: WeeklyPlanEntity)
}
