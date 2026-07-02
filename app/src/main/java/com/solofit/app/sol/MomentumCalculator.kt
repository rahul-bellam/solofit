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
        val stepsGood = weeklySteps.takeLast(WellnessThresholds.TRAILING_WINDOW_SIZE).average().toInt() >= WellnessThresholds.MODERATE_MOVEMENT_STEPS
        val proteinGood = weeklyProteinPct.takeLast(WellnessThresholds.TRAILING_WINDOW_SIZE).average() >= WellnessThresholds.PROTEIN_SLIPPING_THRESHOLD
        val sleepOk = sleepHours != null && sleepHours >= WellnessThresholds.SLEEP_ADEQUATE
        val hasStreak = streakDays >= WellnessThresholds.STREAK_MILESTONE_7

        if (active) positives.add("workout")
        if (recGood) positives.add("recovery")
        if (stepsGood) positives.add("movement")
        if (proteinGood) positives.add("nutrition")
        if (sleepOk) positives.add("sleep")

        if (!active) negatives.add("workout")
        if (recoveryScore != null && recoveryScore < WellnessThresholds.MOMENTUM_RECOVERY_NEGATIVE) negatives.add("recovery")
        if (weeklySteps.size >= WellnessThresholds.TRAILING_WINDOW_SIZE && stepsGood.not()) negatives.add("movement")
        if (weeklyProteinPct.size >= WellnessThresholds.TRAILING_WINDOW_SIZE && proteinGood.not()) negatives.add("nutrition")
        if (sleepHours != null && sleepHours < WellnessThresholds.SLEEP_DEFICIENCY_HOURS) negatives.add("sleep")

        val posCount = positives.size
        val negCount = negatives.size
        val ratio = if (posCount + negCount > 0) posCount.toDouble() / (posCount + negCount) else 0.0

        val level = when {
            posCount >= WellnessThresholds.MOMENTUM_POS_COUNT_EXCELLENT && negCount <= WellnessThresholds.MOMENTUM_NEG_COUNT_EXCELLENT -> MomentumLevel.EXCELLENT
            posCount >= WellnessThresholds.MOMENTUM_POS_COUNT_STRONG && ratio >= WellnessThresholds.MOMENTUM_RATIO_STRONG -> MomentumLevel.STRONG
            posCount >= WellnessThresholds.MOMENTUM_POS_COUNT_STABLE -> MomentumLevel.STABLE
            else -> MomentumLevel.BUILDING
        }

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
