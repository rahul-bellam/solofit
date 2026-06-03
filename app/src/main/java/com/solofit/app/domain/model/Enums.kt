package com.solofit.app.domain.model

/** Biological sex used by the Mifflin-St Jeor BMR equation. */
enum class Gender(val displayName: String) {
    MALE("Male"),
    FEMALE("Female")
}

/**
 * Activity level multipliers applied to BMR to estimate TDEE
 * (Total Daily Energy Expenditure).
 */
enum class ActivityLevel(val displayName: String, val factor: Double, val description: String) {
    SEDENTARY("Sedentary", 1.2, "Little or no exercise, desk job"),
    LIGHT("Light", 1.375, "Light exercise 1-3 days/week"),
    MODERATE("Moderate", 1.55, "Moderate exercise 3-5 days/week"),
    ACTIVE("Active", 1.725, "Hard exercise 6-7 days/week"),
    VERY_ACTIVE("Very Active", 1.9, "Physical job or 2x/day training")
}

/**
 * Fitness goal that applies a caloric offset to TDEE.
 *  - Lose Weight: -500 kcal/day
 *  - Maintain:      0 kcal/day
 *  - Gain Muscle: +300 kcal/day
 */
enum class FitnessGoal(val displayName: String, val calorieOffset: Int) {
    LOSE_WEIGHT("Lose Weight", -500),
    MAINTAIN("Maintain", 0),
    GAIN_MUSCLE("Gain Muscle", 300)
}

/** Meal categories for the daily nutrition diary. */
enum class MealCategory(val displayName: String) {
    BREAKFAST("Breakfast"),
    PRE_WORKOUT("Pre-Workout"),
    LUNCH("Lunch"),
    POST_WORKOUT("Post-Workout"),
    DINNER("Dinner"),
    SNACKS("Snacks")
}
