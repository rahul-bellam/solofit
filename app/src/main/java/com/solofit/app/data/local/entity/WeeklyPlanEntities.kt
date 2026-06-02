package com.solofit.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "weekly_plans")
data class WeeklyPlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayOfWeek: Int, // 1=Monday … 7=Sunday
    val name: String
)

@Entity(
    tableName = "planned_exercises",
    foreignKeys = [
        ForeignKey(
            entity = WeeklyPlanEntity::class,
            parentColumns = ["id"],
            childColumns = ["planId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("planId")]
)
data class PlannedExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val planId: Long,
    val exerciseName: String,
    val sets: Int,
    val reps: Int,
    val weight: Double,
    val weightUnit: String = "kg", // "kg" or "lbs"
    val sortOrder: Int = 0,
    val isCompleted: Boolean = false
)
