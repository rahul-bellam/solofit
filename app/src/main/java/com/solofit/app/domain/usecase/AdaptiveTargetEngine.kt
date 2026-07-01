package com.solofit.app.domain.usecase

import com.solofit.app.data.local.entity.UserProfileEntity
import com.solofit.app.data.local.entity.WeightEntryEntity
import com.solofit.app.domain.model.MacroTotals
import com.solofit.app.domain.model.NutritionTargets
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.roundToInt

data class AdaptedTargets(
    val targetCalories: Int,
    val targetProteinG: Int,
    val targetCarbsG: Int,
    val targetFatsG: Int,
    val bmr: Int,
    val tdee: Int,
    val isAdapted: Boolean,
    val reason: String?
)

class AdaptiveTargetEngine @Inject constructor(
    private val calculateTargets: CalculateNutritionTargetsUseCase
) {
    suspend operator fun invoke(
        profile: UserProfileEntity,
        latestWeight: WeightEntryEntity?,
        recentDailyTotals: List<MacroTotals>
    ): AdaptedTargets {
        val effectiveWeight = latestWeight?.weightKg
        val weightDiff = if (effectiveWeight != null) {
            abs(effectiveWeight - profile.weightKg)
        } else 0.0
        val weightChanged = weightDiff >= 1.0

        val useWeight = effectiveWeight ?: profile.weightKg

        val recalculated = calculateTargets(
            CalculateNutritionTargetsUseCase.Params(
                age = profile.age,
                gender = profile.gender,
                weightKg = useWeight,
                heightCm = profile.heightCm,
                activityLevel = profile.activityLevel,
                goal = profile.goal
            )
        )

        val daysWithFood = recentDailyTotals.filter { it.calories > 0 }
        val proteinHits = daysWithFood.count { t ->
            t.proteinG >= recalculated.targetProteinG * 0.8
        }
        val adherenceRate = if (daysWithFood.size >= 3) {
            proteinHits.toDouble() / daysWithFood.size
        } else 0.0

        val bumpProtein = adherenceRate >= 0.5
        val maxProteinPerKg = CalculateNutritionTargetsUseCase.PROTEIN_PER_KG + 0.5
        val extraProteinG = if (bumpProtein) {
            ((0.2 * useWeight).roundToInt()).coerceAtMost(
                (maxProteinPerKg * useWeight).roundToInt() - recalculated.targetProteinG
            )
        } else 0

        val finalTargets: NutritionTargets
        val reasons = mutableListOf<String>()

        if (weightChanged) {
            reasons.add("adjusted for ${(effectiveWeight ?: profile.weightKg).roundToInt()} kg")
        }

        if (extraProteinG > 0) {
            val extraKcal = (extraProteinG * CalculateNutritionTargetsUseCase.KCAL_PER_G_PROTEIN).roundToInt()
            finalTargets = recalculated.copy(
                targetCalories = recalculated.targetCalories + extraKcal,
                targetProteinG = recalculated.targetProteinG + extraProteinG
            )
            reasons.add("protein +${extraProteinG}g (adherence)")
        } else {
            finalTargets = recalculated
        }

        return AdaptedTargets(
            targetCalories = finalTargets.targetCalories,
            targetProteinG = finalTargets.targetProteinG,
            targetCarbsG = finalTargets.targetCarbsG,
            targetFatsG = finalTargets.targetFatsG,
            bmr = finalTargets.bmr,
            tdee = finalTargets.tdee,
            isAdapted = reasons.isNotEmpty(),
            reason = reasons.takeIf { it.isNotEmpty() }?.joinToString(" · ")
        )
    }
}
