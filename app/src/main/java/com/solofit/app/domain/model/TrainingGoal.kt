package com.solofit.app.domain.model

/**
 * High-level training intent. Drives emphasis in the Transformation Score and
 * (future) recommendations. Distinct from FitnessGoal (calorie offset).
 */
enum class TrainingGoal(
    val displayName: String,
    val description: String,
    // weights for the Transformation Score components (sum need not be 1; normalized)
    val wStrength: Double,
    val wWaist: Double,
    val wConsistency: Double,
    val wRecovery: Double
) {
    FAT_LOSS("Fat Loss", "Lean down, keep muscle", 0.20, 0.45, 0.25, 0.10),
    ATHLETIC("Athletic", "Balanced performance", 0.30, 0.25, 0.25, 0.20),
    STRENGTH("Strength", "Get strong", 0.50, 0.15, 0.25, 0.10),
    BODYBUILDING("Bodybuilding", "Build the V-taper", 0.35, 0.35, 0.20, 0.10)
}
