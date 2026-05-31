package com.solofit.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Daily wellbeing / recovery inputs (manually entered). One row per date.
 * Feeds the Recovery / Training-Readiness score and the morning dashboard.
 */
@Entity(
    tableName = "daily_metrics",
    indices = [Index(value = ["date"], unique = true)]
)
data class DailyMetricEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,               // ISO yyyy-MM-dd
    val sleepHours: Double? = null,
    val steps: Int? = null,
    val moodScore: Int? = null,     // 1..10
    val energyScore: Int? = null,   // 1..10
    val updatedAt: Long = System.currentTimeMillis()
)
