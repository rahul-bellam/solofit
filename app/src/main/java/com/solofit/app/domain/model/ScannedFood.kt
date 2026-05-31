package com.solofit.app.domain.model

/**
 * Normalized result of a barcode lookup, ready to be shown and/or saved
 * into the local food DB. Macros are per 100g (matching FoodItemEntity).
 */
data class ScannedFood(
    val barcode: String,
    val name: String,
    val caloriesPer100g: Double,
    val proteinPer100g: Double,
    val carbsPer100g: Double,
    val fatsPer100g: Double
)

/** Outcome of a remote barcode lookup. */
sealed interface BarcodeLookupResult {
    data class Found(val food: ScannedFood) : BarcodeLookupResult
    /** Product not in OFF (or missing macros) -> show the manual-entry fallback form. */
    data class NotFound(val barcode: String) : BarcodeLookupResult
    data class Error(val message: String) : BarcodeLookupResult
}
