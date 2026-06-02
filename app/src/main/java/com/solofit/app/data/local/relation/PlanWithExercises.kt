package com.solofit.app.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.solofit.app.data.local.entity.PlannedExerciseEntity
import com.solofit.app.data.local.entity.WeeklyPlanEntity

data class PlanWithExercises(
    @Embedded val plan: WeeklyPlanEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "planId"
    )
    val exercises: List<PlannedExerciseEntity>
) {
    val allDone: Boolean get() = exercises.isNotEmpty() && exercises.all { it.isCompleted }
}
