package com.solofit.app.data.repository

import com.solofit.app.data.local.dao.WeeklyPlanDao
import com.solofit.app.data.local.entity.PlannedExerciseEntity
import com.solofit.app.data.local.entity.WeeklyPlanEntity
import com.solofit.app.domain.repository.WeeklyPlanRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeeklyPlanRepositoryImpl @Inject constructor(
    private val dao: WeeklyPlanDao
) : WeeklyPlanRepository {

    override fun observeAllPlans(): Flow<List<WeeklyPlanEntity>> = dao.observeAllPlans()

    override fun observePlanForDay(dayOfWeek: Int): Flow<WeeklyPlanEntity?> =
        dao.observePlanForDay(dayOfWeek)

    override suspend fun getPlanForDay(dayOfWeek: Int): WeeklyPlanEntity? =
        dao.getPlanForDay(dayOfWeek)

    override fun observeExercisesForPlan(planId: Long): Flow<List<PlannedExerciseEntity>> =
        dao.observeExercisesForPlan(planId)

    override fun observeIncompleteCount(planId: Long): Flow<Int> =
        dao.observeIncompleteCount(planId)

    override suspend fun savePlan(plan: WeeklyPlanEntity): Long = dao.insertPlan(plan)

    override suspend fun addExercise(exercise: PlannedExerciseEntity): Long =
        dao.insertExercise(exercise)

    override suspend fun updateExercise(exercise: PlannedExerciseEntity) =
        dao.updateExercise(exercise)

    override suspend fun deleteExercise(exercise: PlannedExerciseEntity) =
        dao.deleteExercise(exercise)

    override suspend fun setExerciseCompleted(id: Long, completed: Boolean) =
        dao.setExerciseCompleted(id, completed)

    override suspend fun resetPlan(planId: Long) = dao.resetPlan(planId)

    override suspend fun deletePlan(plan: WeeklyPlanEntity) = dao.deletePlan(plan)
}
