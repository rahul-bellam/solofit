package com.solofit.app.domain.model

/**
 * Domain model for a meal remembered by the app.
 */
data class FrequentMeal(
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
