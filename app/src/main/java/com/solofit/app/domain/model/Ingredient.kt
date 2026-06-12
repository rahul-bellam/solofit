package com.solofit.app.domain.model

/**
 * An ingredient used in a mixed-food dish (curry, dal, biryani, etc.).
 * @param name ingredient name
 * @param grams estimated weight in the total dish
 * @param caloriesPer100g nutrition per 100g of this ingredient
 * @param proteinPer100g protein per 100g
 * @param carbsPer100g carbs per 100g
 * @param fatsPer100g fats per 100g
 */
data class Ingredient(
    val name: String,
    val grams: Double,
    val caloriesPer100g: Double,
    val proteinPer100g: Double,
    val carbsPer100g: Double = 0.0,
    val fatsPer100g: Double = 0.0
) {
    val calories: Double get() = caloriesPer100g * grams / 100.0
    val protein: Double get() = proteinPer100g * grams / 100.0
    val carbs: Double get() = carbsPer100g * grams / 100.0
    val fats: Double get() = fatsPer100g * grams / 100.0
}

/**
 * Common Indian / mixed-food ingredient database for quick-add ingredient breakdowns.
 */
object CommonIngredients {
    val list: List<Ingredient> = listOf(
        Ingredient("Chicken (boneless)", 100.0, 165.0, 31.0, 0.0, 3.6),
        Ingredient("Cooking Oil", 15.0, 884.0, 0.0, 0.0, 100.0),
        Ingredient("Onion", 50.0, 40.0, 1.1, 9.3, 0.1),
        Ingredient("Tomato", 50.0, 18.0, 0.9, 3.9, 0.2),
        Ingredient("Rice (cooked)", 150.0, 130.0, 2.7, 28.0, 0.3),
        Ingredient("Basmati Rice (cooked)", 150.0, 140.0, 3.0, 30.0, 0.4),
        Ingredient("Panner", 100.0, 265.0, 18.0, 1.2, 20.0),
        Ingredient("Potato", 100.0, 77.0, 2.0, 17.0, 0.1),
        Ingredient("Green Peas", 30.0, 81.0, 5.4, 14.5, 0.4),
        Ingredient("Ghee", 10.0, 900.0, 0.0, 0.0, 100.0),
        Ingredient("Butter", 10.0, 717.0, 0.9, 0.1, 81.0),
        Ingredient("Cream", 30.0, 340.0, 2.8, 2.8, 36.0),
        Ingredient("Coconut Milk", 50.0, 230.0, 2.3, 5.5, 24.0),
        Ingredient("Yogurt / Curd", 50.0, 61.0, 3.5, 4.7, 3.3),
        Ingredient("Lentil / Dal (cooked)", 100.0, 116.0, 9.0, 20.0, 0.4),
        Ingredient("Chickpea / Chhole (cooked)", 100.0, 139.0, 7.2, 25.0, 2.1),
        Ingredient("Naan / Roti", 80.0, 260.0, 8.0, 46.0, 3.5),
        Ingredient("Egg", 50.0, 155.0, 13.0, 1.1, 11.0),
        Ingredient("Cheese", 30.0, 402.0, 25.0, 1.3, 33.0),
        Ingredient("Mixed Vegetables", 100.0, 45.0, 2.0, 9.0, 0.3)
    )
}

/**
 * Computed result from ingredient breakdown analysis.
 */
data class IngredientMeal(
    val name: String,
    val ingredients: List<Ingredient>,
    val totalGrams: Double,
    val totalCalories: Double,
    val totalProtein: Double,
    val totalCarbs: Double,
    val totalFats: Double
) {
    /** Macros per 100g for the whole dish. */
    val caloriesPer100g: Double get() = if (totalGrams > 0) totalCalories / totalGrams * 100 else 0.0
    val proteinPer100g: Double get() = if (totalGrams > 0) totalProtein / totalGrams * 100 else 0.0
    val carbsPer100g: Double get() = if (totalGrams > 0) totalCarbs / totalGrams * 100 else 0.0
    val fatsPer100g: Double get() = if (totalGrams > 0) totalFats / totalGrams * 100 else 0.0
}
