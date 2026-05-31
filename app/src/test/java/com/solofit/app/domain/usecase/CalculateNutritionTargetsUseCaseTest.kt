package com.solofit.app.domain.usecase

import com.solofit.app.domain.model.ActivityLevel
import com.solofit.app.domain.model.FitnessGoal
import com.solofit.app.domain.model.Gender
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculateNutritionTargetsUseCaseTest {

    private val useCase = CalculateNutritionTargetsUseCase()

    @Test
    fun `male maintain matches Mifflin-St Jeor`() {
        // 30y, 80kg, 180cm male:
        // BMR = 10*80 + 6.25*180 - 5*30 + 5 = 800 + 1125 - 150 + 5 = 1780
        val r = useCase(
            CalculateNutritionTargetsUseCase.Params(
                age = 30, gender = Gender.MALE, weightKg = 80.0, heightCm = 180.0,
                activityLevel = ActivityLevel.MODERATE, goal = FitnessGoal.MAINTAIN
            )
        )
        assertEquals(1780, r.bmr)
        // TDEE = 1780 * 1.55 = 2759
        assertEquals(2759, r.tdee)
        assertEquals(2759, r.targetCalories)
        // Protein = 2g/kg * 80 = 160g
        assertEquals(160, r.targetProteinG)
    }

    @Test
    fun `female lose weight applies minus 500 offset`() {
        // 25y, 60kg, 165cm female:
        // BMR = 10*60 + 6.25*165 - 5*25 - 161 = 600 + 1031.25 - 125 - 161 = 1345.25
        val r = useCase(
            CalculateNutritionTargetsUseCase.Params(
                age = 25, gender = Gender.FEMALE, weightKg = 60.0, heightCm = 165.0,
                activityLevel = ActivityLevel.SEDENTARY, goal = FitnessGoal.LOSE_WEIGHT
            )
        )
        assertEquals(1345, r.bmr)
        // TDEE = 1345.25 * 1.2 = 1614.3 -> 1614; target = 1614.3 - 500 = 1114.3 -> 1114
        assertEquals(1614, r.tdee)
        assertEquals(1114, r.targetCalories)
        assertEquals(120, r.targetProteinG) // 2 * 60
    }

    @Test
    fun `gain muscle applies plus 300 offset`() {
        val r = useCase(
            CalculateNutritionTargetsUseCase.Params(
                age = 22, gender = Gender.MALE, weightKg = 75.0, heightCm = 178.0,
                activityLevel = ActivityLevel.ACTIVE, goal = FitnessGoal.GAIN_MUSCLE
            )
        )
        assertEquals(r.tdee + 300, r.targetCalories)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `invalid age throws`() {
        useCase(
            CalculateNutritionTargetsUseCase.Params(
                age = 200, gender = Gender.MALE, weightKg = 75.0, heightCm = 178.0,
                activityLevel = ActivityLevel.ACTIVE, goal = FitnessGoal.MAINTAIN
            )
        )
    }
}
