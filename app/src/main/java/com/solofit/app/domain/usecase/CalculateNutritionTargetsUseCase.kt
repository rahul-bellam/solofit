package com.solofit.app.domain.usecase

import com.solofit.app.domain.model.ActivityLevel
import com.solofit.app.domain.model.FitnessGoal
import com.solofit.app.domain.model.Gender
import com.solofit.app.domain.model.NutritionTargets
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * Core offline engine. Calculates BMR, TDEE and macro split from
 * physiological inputs using the Mifflin-St Jeor Equation.
 *
 * Mifflin-St Jeor:
 *   Men:   BMR = 10*kg + 6.25*cm - 5*age + 5
 *   Women: BMR = 10*kg + 6.25*cm - 5*age - 161
 *
 * Macro strategy (hardcoded, evidence-based defaults):
 *   - Protein: 2.0 g per kg of body weight
 *   - Fat:     25% of target calories (9 kcal/g)
 *   - Carbs:   remaining calories (4 kcal/g)
 */
class CalculateNutritionTargetsUseCase @Inject constructor() {

    companion object {
        const val PROTEIN_PER_KG = 2.0
        const val FAT_CALORIE_RATIO = 0.25
        const val KCAL_PER_G_PROTEIN = 4.0
        const val KCAL_PER_G_CARB = 4.0
        const val KCAL_PER_G_FAT = 9.0
    }

    data class Params(
        val age: Int,
        val gender: Gender,
        val weightKg: Double,
        val heightCm: Double,
        val activityLevel: ActivityLevel,
        val goal: FitnessGoal
    )

    operator fun invoke(params: Params): NutritionTargets {
        require(params.age in 1..120) { "Age must be between 1 and 120" }
        require(params.weightKg in 20.0..400.0) { "Weight out of valid range" }
        require(params.heightCm in 80.0..260.0) { "Height out of valid range" }

        val genderConstant = if (params.gender == Gender.MALE) 5.0 else -161.0
        val bmr = (10 * params.weightKg) +
                (6.25 * params.heightCm) -
                (5 * params.age) +
                genderConstant

        val tdee = bmr * params.activityLevel.factor
        val targetCalories = (tdee + params.goal.calorieOffset).coerceAtLeast(1000.0)

        // Macros
        val proteinG = PROTEIN_PER_KG * params.weightKg
        val proteinKcal = proteinG * KCAL_PER_G_PROTEIN

        val fatKcal = targetCalories * FAT_CALORIE_RATIO
        val fatG = fatKcal / KCAL_PER_G_FAT

        val remainingKcal = (targetCalories - proteinKcal - fatKcal).coerceAtLeast(0.0)
        val carbsG = remainingKcal / KCAL_PER_G_CARB

        return NutritionTargets(
            bmr = bmr.roundToInt(),
            tdee = tdee.roundToInt(),
            targetCalories = targetCalories.roundToInt(),
            targetProteinG = proteinG.roundToInt(),
            targetCarbsG = carbsG.roundToInt(),
            targetFatsG = fatG.roundToInt()
        )
    }
}
