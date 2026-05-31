package com.solofit.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A set of body measurements on a given date (cm). One row per date (unique) so
 * the trend lines stay clean; re-saving the same day updates the row.
 * All fields nullable so the user can log only what they measured.
 */
@Entity(
    tableName = "body_measurements",
    indices = [Index(value = ["date"], unique = true)]
)
data class BodyMeasurementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,               // ISO yyyy-MM-dd
    val waistCm: Double? = null,
    val chestCm: Double? = null,
    val shouldersCm: Double? = null,
    val armsCm: Double? = null,
    val thighsCm: Double? = null,
    val neckCm: Double? = null,
    val updatedAt: Long = System.currentTimeMillis()
)
