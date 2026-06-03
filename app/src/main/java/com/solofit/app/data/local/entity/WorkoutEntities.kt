package com.solofit.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A reusable workout template, e.g. "Push Day".
 */
@Entity(tableName = "routines")
data class RoutineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * An exercise belonging to a routine. The catalog of exercise names is seeded
 * from [com.solofit.app.data.local.seed.ExerciseSeedData], but each row here is
 * an instance attached to a specific routine (with ordering).
 */
@Entity(
    tableName = "exercises",
    foreignKeys = [
        ForeignKey(
            entity = RoutineEntity::class,
            parentColumns = ["id"],
            childColumns = ["routineId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("routineId")]
)
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val routineId: Long,
    val name: String,
    val muscleGroup: String,
    val orderIndex: Int
)

/**
 * A completed/performed workout session derived from a routine, on a specific date.
 */
@Entity(
    tableName = "workout_sessions",
    foreignKeys = [
        ForeignKey(
            entity = RoutineEntity::class,
            parentColumns = ["id"],
            childColumns = ["routineId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("routineId"), Index("date")]
)
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val routineId: Long?,
    val routineName: String,
    val date: String,
    val startedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val isCompleted: Boolean = false
)

/**
 * A single performed set within a session. Tracks weight, reps and completion
 * for progressive-overload tracking.
 */
@Entity(
    tableName = "exercise_sets",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId")]
)
data class ExerciseSetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val exerciseName: String,
    val muscleGroup: String,
    val orderIndex: Int,
    val setNumber: Int,
    val weightKg: Double,
    val reps: Int,
    val isCompleted: Boolean = false,
    /** Reps In Reserve (how many more you could have done). null = not recorded. */
    val rir: Int? = null,
    /** True if this set is a warm-up (excluded from volume/PR calculations). */
    val isWarmUp: Boolean = false,
    /** Free-text notes for the set (e.g. "felt easy", "slow negative"). */
    val notes: String = "",
    /** Groups sets into supersets. Sets with the same non-null supersetId are performed back-to-back. */
    val supersetId: Int? = null
)

/**
 * Tracks personal records (estimated 1RM) per exercise over time.
 */
@Entity(tableName = "personal_records")
data class PersonalRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val exerciseName: String,
    val bestWeightKg: Double,
    val bestReps: Int,
    val estimated1RM: Double,
    val date: String,
    val sessionId: Long
)
