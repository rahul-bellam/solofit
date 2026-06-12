package com.solofit.app.domain.model

/**
 * Normalizes meal names so similar foods map together.
 *
 * Examples:
 *   "Chicken Curry"        -> "chicken curry"
 *   "chicken masala curry" -> "chicken masala curry"
 *   "Curry, Chicken"       -> "chicken curry"
 *   "  CHICKEN  CURRY  "   -> "chicken curry"
 */
object MealNameNormalizer {

    private val punctuation = Regex("[,\\.!?;:()\\[\\]{}「」『』【】]")
    private val extraSpaces = Regex("\\s+")

    fun normalize(raw: String): String {
        if (raw.isBlank()) return raw.lowercase().trim()
        return raw
            .lowercase()
            .trim()
            .replace(punctuation, " ")
            .replace(extraSpaces, " ")
            .trim()
    }
}

/**
 * Local memory of a user's frequent meals.
 * Supports save, recall, search, and prune.
 */
data class MealMemoryEntry(
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
) {
    companion object {
        fun fromMeal(name: String, calories: Double, protein: Double, carbs: Double, fats: Double, fiber: Double = 0.0): MealMemoryEntry {
            val norm = MealNameNormalizer.normalize(name)
            return MealMemoryEntry(
                name = name.trim(),
                normalizedName = norm,
                caloriesPer100g = calories,
                proteinPer100g = protein,
                carbsPer100g = carbs,
                fatsPer100g = fats,
                fiberPer100g = fiber
            )
        }
    }
}
