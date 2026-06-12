package com.solofit.app.sol

import androidx.compose.ui.graphics.Color

enum class LifestyleMode(
    val displayName: String,
    val solLine: String,
    val color: Color
) {
    PERFORMANCE("Performance Mode", "Training conditions are favorable.", Color(0xFF4A9E5E)),
    RECOVERY("Recovery Mode", "Recovery should be today's focus.", Color(0xFFD4A017)),
    WORKLOAD("Elevated Workload", "Recent workload appears elevated. Protect recovery this week.", Color(0xFFC85A5A)),
    REBUILDING("Rebuilding", "Keep expectations simple. Focus on rebuilding consistency.", Color(0xFF6F6A63))
}

object LifestyleModeDetector {

    fun detect(
        recoveryScore: Int?,
        sleepHours: Double?,
        weeklySleep: List<Double>,
        steps: Int?,
        weeklySteps: List<Int>,
        daysActiveThisWeek: Int
    ): LifestyleMode {
        val rec = recoveryScore ?: return LifestyleMode.REBUILDING

        val recGood = rec >= WellnessThresholds.RECOVERY_GOOD
        val sleepStable = sleepHours != null && sleepHours >= WellnessThresholds.SLEEP_ADEQUATE
        val sleepDeclining = weeklySleep.size >= 3 && weeklySleep.takeLast(3).zipWithNext().all { (a, b) -> b <= a }
        val movementDown = weeklySteps.size >= 3 && weeklySteps.takeLast(3).all { it <= WellnessThresholds.MODERATE_MOVEMENT_STEPS }
        val habitsMissed = daysActiveThisWeek < WellnessThresholds.LOW_ACTIVITY_DAYS

        return when {
            recGood && sleepStable -> LifestyleMode.PERFORMANCE
            rec < WellnessThresholds.RECOVERY_LOW || sleepDeclining -> LifestyleMode.RECOVERY
            sleepHours != null && sleepHours < WellnessThresholds.SLEEP_LOW && movementDown && rec < WellnessThresholds.RECOVERY_MODERATE -> LifestyleMode.WORKLOAD
            habitsMissed || rec < WellnessThresholds.RECOVERY_VERY_LOW -> LifestyleMode.REBUILDING
            else -> LifestyleMode.PERFORMANCE
        }
    }
}
