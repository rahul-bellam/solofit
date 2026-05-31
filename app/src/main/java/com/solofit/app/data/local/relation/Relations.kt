package com.solofit.app.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.solofit.app.data.local.entity.ExerciseEntity
import com.solofit.app.data.local.entity.ExerciseSetEntity
import com.solofit.app.data.local.entity.RoutineEntity
import com.solofit.app.data.local.entity.WorkoutSessionEntity

/** Routines -> Exercises (one-to-many) */
data class RoutineWithExercises(
    @Embedded val routine: RoutineEntity,
    @Relation(parentColumn = "id", entityColumn = "routineId")
    val exercises: List<ExerciseEntity>
)

/** Session -> Sets (one-to-many) */
data class SessionWithSets(
    @Embedded val session: WorkoutSessionEntity,
    @Relation(parentColumn = "id", entityColumn = "sessionId")
    val sets: List<ExerciseSetEntity>
)
