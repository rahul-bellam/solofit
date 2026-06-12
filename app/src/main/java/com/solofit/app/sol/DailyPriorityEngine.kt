package com.solofit.app.sol

enum class DailyPriority(
    val displayName: String,
    val emoji: String
) {
    MOVEMENT("Movement Day", "\uD83C\uDFC3"),
    RECOVERY("Recovery Day", "\uD83D\uDE34"),
    NUTRITION("Nutrition Day", "\uD83E\uDD55"),
    SLEEP("Sleep Day", "\uD83C\uDF19"),
    PERFORMANCE("Performance Day", "\u26A1"),
    CONSISTENCY("Consistency Day", "\uD83D\uDD04"),
    MINDFULNESS("Mindfulness Day", "\uD83E\uDDD8")
}

data class DailyPriorityResult(
    val priority: DailyPriority,
    val reason: String,
    val action: String
)

object DailyPriorityEngine {

    fun determine(
        recoveryScore: Int?,
        sleepHours: Double?,
        baseline: UserBaseline?,
        todaySteps: Int?,
        todayProtein: Double?,
        daysActiveThisWeek: Int,
        meditationToday: Boolean,
        stressLevel: Int?
    ): DailyPriorityResult {
        val rec = recoveryScore ?: 50
        val sleep = sleepHours ?: 7.0

        val sleepBelowBaseline = baseline?.avgSleep7d != null && sleep < baseline.avgSleep7d!! - WellnessThresholds.SLEEP_BASELINE_DELTA
        val stepsBelowBaseline = baseline?.avgSteps7d != null && (todaySteps ?: 0) < baseline.avgSteps7d!! * WellnessThresholds.BASELINE_STEPS_FRACTION
        val proteinBelowBaseline = baseline?.avgProteinAdherence7d != null && (todayProtein ?: 0.0) < baseline.avgProteinAdherence7d!! - WellnessThresholds.PROTEIN_BASELINE_DELTA
        val recBelowBaseline = baseline?.avgRecovery7d != null && rec < baseline.avgRecovery7d!! - WellnessThresholds.RECOVERY_WARNING_DELTA
        val stressed = stressLevel != null && stressLevel >= 4

        return when {
            rec < WellnessThresholds.RECOVERY_LOW || (recBelowBaseline && sleep < WellnessThresholds.SLEEP_LOW) -> DailyPriorityResult(
                DailyPriority.RECOVERY,
                "Recovery is below normal range.",
                "Prioritise rest. A lighter session or recovery day may be beneficial."
            )
            sleepBelowBaseline || sleep < WellnessThresholds.SLEEP_POOR -> DailyPriorityResult(
                DailyPriority.SLEEP,
                "Sleep has been below your baseline.",
                "Aim for an earlier bedtime tonight."
            )
            stepsBelowBaseline || (todaySteps ?: 0) < WellnessThresholds.LOW_MOVEMENT_STEPS -> DailyPriorityResult(
                DailyPriority.MOVEMENT,
                "Movement has been lower than your usual pattern.",
                "Walk for 15 minutes at a comfortable pace."
            )
            proteinBelowBaseline -> DailyPriorityResult(
                DailyPriority.NUTRITION,
                "Protein intake has been inconsistent.",
                "Prioritise protein at your next meal."
            )
            rec >= WellnessThresholds.RECOVERY_PERFORMANCE && sleep >= WellnessThresholds.SLEEP_OPTIMAL && daysActiveThisWeek >= WellnessThresholds.ACTIVE_WEEK_DAYS -> DailyPriorityResult(
                DailyPriority.PERFORMANCE,
                "Your body is showing favourable conditions for training.",
                "Proceed with your planned workout."
            )
            stressed -> DailyPriorityResult(
                DailyPriority.MINDFULNESS,
                "Stress levels are higher than usual.",
                "A short meditation or journaling session may help."
            )
            daysActiveThisWeek >= 3 -> DailyPriorityResult(
                DailyPriority.CONSISTENCY,
                "You have been consistent this week.",
                "Maintain your current routine."
            )
            else -> DailyPriorityResult(
                DailyPriority.CONSISTENCY,
                "Building consistency takes time.",
                "Complete one small wellness action today."
            )
        }
    }
}
