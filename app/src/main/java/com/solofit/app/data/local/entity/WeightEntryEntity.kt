package com.solofit.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A single body-weight measurement. One canonical entry per date (date is unique)
 * so the trend line is clean; logging again on the same day replaces it.
 */
@Entity(
    tableName = "weight_entries",
    indices = [Index(value = ["date"], unique = true)]
)
data class WeightEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,            // ISO yyyy-MM-dd
    val weightKg: Double,
    val loggedAt: Long = System.currentTimeMillis()
)
