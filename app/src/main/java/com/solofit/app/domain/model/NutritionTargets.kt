package com.solofit.app.domain.model

/**
 * The computed daily targets derived purely from local formulas.
 *
 * @param bmr Basal Metabolic Rate (kcal/day)
 * @param tdee Total Daily Energy Expenditure (kcal/day)
 * @param targetCalories TDEE adjusted by the goal offset
 * @param targetProteinG protein target in grams
 * @param targetCarbsG carbohydrate target in grams
 * @param targetFatsG fat target in grams
 */
data class NutritionTargets(
    val bmr: Int,
    val tdee: Int,
    val targetCalories: Int,
    val targetProteinG: Int,
    val targetCarbsG: Int,
    val targetFatsG: Int
)

/** A simple immutable bundle of consumed macros for a day. */
data class MacroTotals(
    val calories: Double = 0.0,
    val proteinG: Double = 0.0,
    val carbsG: Double = 0.0,
    val fatsG: Double = 0.0
) {
    operator fun plus(other: MacroTotals) = MacroTotals(
        calories = calories + other.calories,
        proteinG = proteinG + other.proteinG,
        carbsG = carbsG + other.carbsG,
        fatsG = fatsG + other.fatsG
    )
}
