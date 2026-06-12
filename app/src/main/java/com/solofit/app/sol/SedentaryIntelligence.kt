package com.solofit.app.sol

enum class MovementRiskLevel { LOW, MODERATE, HIGH, CRITICAL }

data class MovementRisk(
    val level: MovementRiskLevel,
    val consecutiveLowDays: Int,
    val avgSteps7d: Int,
    val recommendation: String
)

object SedentaryIntelligence {

    fun assess(
        todaySteps: Int?,
        weeklySteps: List<Int>,
        stepGoal: Int
    ): MovementRisk {
        val avg7d = if (weeklySteps.isNotEmpty()) weeklySteps.average().toInt() else todaySteps ?: 0
        val consecutiveLow = countConsecutiveLow(weeklySteps, stepGoal)
        val tooLowCount = weeklySteps.count { it < stepGoal * WellnessThresholds.MOVEMENT_RISK_TOO_LOW_FRACTION }

        val level = when {
            avg7d >= stepGoal -> MovementRiskLevel.LOW
            consecutiveLow >= WellnessThresholds.MOVEMENT_CRITICAL_CONSECUTIVE || (avg7d < stepGoal * WellnessThresholds.MOVEMENT_RISK_CRITICAL_AVG) -> MovementRiskLevel.CRITICAL
            consecutiveLow >= WellnessThresholds.MOVEMENT_HIGH_CONSECUTIVE || (avg7d < stepGoal * WellnessThresholds.MOVEMENT_RISK_HIGH_AVG) -> MovementRiskLevel.HIGH
            tooLowCount >= WellnessThresholds.MOVEMENT_MODERATE_TOO_LOW_DAYS || avg7d < stepGoal * WellnessThresholds.MOVEMENT_RISK_MODERATE_AVG -> MovementRiskLevel.MODERATE
            else -> MovementRiskLevel.LOW
        }

        val recommendation = when (level) {
            MovementRiskLevel.LOW -> "Movement patterns are consistent."
            MovementRiskLevel.MODERATE -> "Movement has been below average. A short walk today may help."
            MovementRiskLevel.HIGH -> "Movement has been low for several days. Consider a 15-minute walk or light mobility."
            MovementRiskLevel.CRITICAL -> "Low movement for an extended period. Start with 5 minutes of walking and build gradually."
        }

        return MovementRisk(level, consecutiveLow, avg7d, recommendation)
    }

    private fun countConsecutiveLow(steps: List<Int>, goal: Int): Int {
        var count = 0
        for (s in steps.reversed()) {
            if (s < goal * WellnessThresholds.MOVEMENT_RISK_TOO_LOW_FRACTION) count++ else break
        }
        return count
    }
}
