package com.solofit.app.sol

enum class MomentumLevel(val displayName: String) {
    BUILDING("Building"),
    STABLE("Stable"),
    STRONG("Strong"),
    EXCELLENT("Excellent")
}

data class MomentumState(
    val level: MomentumLevel,
    val description: String,
    val direction: TrendDirection,
    val factors: List<String>
)

object MomentumCalculator {

    fun compute(
        daysActiveThisWeek: Int,
        recoveryScore: Int?,
        weeklySteps: List<Int>,
        weeklyProteinPct: List<Double>,
        sleepHours: Double?,
        streakDays: Int,
        baseline: UserBaseline?
    ): MomentumState {
        val positives = mutableListOf<String>()
        val negatives = mutableListOf<String>()

        val active = daysActiveThisWeek >= WellnessThresholds.ACTIVE_WEEK_DAYS
        val recGood = recoveryScore != null && recoveryScore >= WellnessThresholds.RECOVERY_GOOD
        val stepsGood = weeklySteps.takeLast(3).average().toInt() >= WellnessThresholds.MODERATE_MOVEMENT_STEPS
        val proteinGood = weeklyProteinPct.takeLast(3).average() >= WellnessThresholds.PROTEIN_SLIPPING_THRESHOLD
        val sleepOk = sleepHours != null && sleepHours >= WellnessThresholds.SLEEP_ADEQUATE
        val hasStreak = streakDays >= WellnessThresholds.STREAK_MILESTONE_7

        if (active) positives.add("workout")
        if (recGood) positives.add("recovery")
        if (stepsGood) positives.add("movement")
        if (proteinGood) positives.add("nutrition")
        if (sleepOk) positives.add("sleep")

        if (!active) negatives.add("workout")
        if (recoveryScore != null && recoveryScore < 40) negatives.add("recovery")
        if (weeklySteps.size >= 3 && stepsGood.not()) negatives.add("movement")
        if (weeklyProteinPct.size >= 3 && proteinGood.not()) negatives.add("nutrition")
        if (sleepHours != null && sleepHours < 6.0) negatives.add("sleep")

        val posCount = positives.size
        val negCount = negatives.size
        val ratio = if (posCount + negCount > 0) posCount.toDouble() / (posCount + negCount) else 0.0

        val level = when {
            posCount >= 4 && negCount <= 1 -> MomentumLevel.EXCELLENT
            posCount >= 3 && ratio >= 0.6 -> MomentumLevel.STRONG
            posCount >= 2 -> MomentumLevel.STABLE
            else -> MomentumLevel.BUILDING
        }

        val improving = level >= MomentumLevel.STABLE
        val direction = if (level == MomentumLevel.BUILDING && posCount > 0) TrendDirection.UP
        else if (level == MomentumLevel.BUILDING) TrendDirection.DOWN
        else TrendDirection.STABLE

        val description = when (level) {
            MomentumLevel.EXCELLENT -> "Consistent habits across most wellness areas."
            MomentumLevel.STRONG -> "Healthy patterns are holding across multiple areas."
            MomentumLevel.STABLE -> "Some areas are consistent. Others need attention."
            MomentumLevel.BUILDING -> "Recovery and habits are in a rebuilding phase."
        }

        return MomentumState(level, description, direction, positives)
    }
}
