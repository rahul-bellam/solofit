package com.solofit.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A single whole-food entry stored locally. All macro values are per 100g.
 * The table is pre-populated at first launch from [com.solofit.app.data.local.seed.FoodSeedData].
 *
 * v1.1: added [barcode] so scanned products can be cached locally.
 * v2.2: added [servingGrams]/[servingLabel] for count-based manual entry
 *       (e.g. "5 eggs" -> 5 * 50g). Null when only grams make sense (e.g. oils).
 */
@Entity(
    tableName = "food_items",
    indices = [
        Index(value = ["name"]),
        Index(value = ["category"]),
        Index(value = ["barcode"], unique = true)
    ]
)
data class FoodItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String,
    val caloriesPer100g: Double,
    val proteinPer100g: Double,
    val carbsPer100g: Double,
    val fatsPer100g: Double,
    val fiberPer100g: Double = 0.0,
    val isCustom: Boolean = false,
    val barcode: String? = null,
    /** grams in one countable unit (e.g. 50.0 for an egg); null = grams-only food. */
    val servingGrams: Double? = null,
    /** singular unit label, e.g. "egg", "slice", "scoop". */
    val servingLabel: String? = null
)
