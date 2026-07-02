package com.solofit.app.sol

import java.util.Locale

data class MicroWin(
    val metric: String,
    val description: String,
    val improvement: String,
    val priority: Int
)

object MicroWinEngine {

    fun detect(
        todaySteps: Int?,
        yesterdaySteps: Int?,
        todaySleep: Double?,
        yesterdaySleep: Double?,
        todayProtein: Double?,
        yesterdayProtein: Double?,
        todayRecovery: Int?,
        yesterdayRecovery: Int?,
        todayMeditation: Int?,
        yesterdayMeditation: Int?
    ): List<MicroWin> {
        val wins = mutableListOf<MicroWin>()

        if (todaySteps != null && yesterdaySteps != null && yesterdaySteps > 0) {
            val delta = todaySteps - yesterdaySteps
            if (delta > 0) wins.add(MicroWin(
                "steps", "Walking increased by ${delta} steps today.",
                "You walked ${delta} more steps than yesterday.", WellnessThresholds.PRIORITY_MICRO_WIN_STEPS
            ))
        }

        if (todaySleep != null && yesterdaySleep != null && yesterdaySleep > 0) {
            val delta = todaySleep - yesterdaySleep
            if (delta > WellnessThresholds.SLEEP_IMPROVEMENT_DELTA) wins.add(MicroWin(
                "sleep", "Sleep improved by ${String.format(Locale.US, "%.1f", delta)} hours.",
                "You slept ${String.format(Locale.US, "%.1f", delta)} more hours than last night.", WellnessThresholds.PRIORITY_MICRO_WIN_SLEEP
            ))
        }

        if (todayProtein != null && yesterdayProtein != null && yesterdayProtein > 0) {
            val delta = todayProtein - yesterdayProtein
            if (delta > WellnessThresholds.PROTEIN_MICRO_WIN_DELTA) {
                val grams = (delta * WellnessThresholds.PROTEIN_GRAMS_MULTIPLIER).toInt()
                wins.add(MicroWin(
                    "protein", "Protein intake improved by ${grams}g.",
                    "Protein intake is improving.", WellnessThresholds.PRIORITY_MICRO_WIN_PROTEIN
                ))
            }
        }

        if (todayRecovery != null && yesterdayRecovery != null && todayRecovery > yesterdayRecovery + WellnessThresholds.RECOVERY_MICRO_WIN_DELTA) {
            val delta = todayRecovery - yesterdayRecovery
            wins.add(MicroWin(
                "recovery", "Recovery score improved by $delta points.",
                "Recovery is trending upward.", WellnessThresholds.PRIORITY_MICRO_WIN_RECOVERY
            ))
        }

        if (todayMeditation != null && yesterdayMeditation != null && todayMeditation > yesterdayMeditation) {
            wins.add(MicroWin(
                "meditation", "Meditation practice maintained.",
                "You meditated today.", WellnessThresholds.PRIORITY_MICRO_WIN_MEDITATION
            ))
        }

        return wins.sortedByDescending { it.priority }
    }

}
