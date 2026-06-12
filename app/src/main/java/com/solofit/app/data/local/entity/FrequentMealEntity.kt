package com.solofit.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "frequent_meals",
    indices = [Index(value = ["normalizedName"], unique = true)]
)
data class FrequentMealEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val normalizedName: String,
    val caloriesPer100g: Double,
    val proteinPer100g: Double,
    val carbsPer100g: Double,
    val fatsPer100g: Double,
    val fiberPer100g: Double = 0.0,
    val logCount: Int = 1,
    val lastLoggedAt: Long = System.currentTimeMillis(),
    val confidence: String = "MEDIUM"
)
