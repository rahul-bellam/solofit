package com.solofit.app.sol

/**
 * Local, offline burnout-risk engine for knowledge workers — the signals that actually
 * cause people to stop (mental fatigue, sleep debt, stress, movement decline), not just
 * calories. It never diagnoses ("you are burned out"); it surfaces *trends* gently.
 *
 * Pure functions only — fully unit-testable, no Android, no network.
 */

enum class BurnoutLevel(val label: String) {
    LOW("Low"),
    MODERATE("Moderate"),
    ELEVATED("Elevated"),
    HIGH("High")
}

enum class SignalDirection { DOWN, UP, FLAT }

data class BurnoutContributor(
    val label: String,
    val direction: SignalDirection,
    val note: String
)

/** A gentle, non-clinical narrative (title / observation / action). */
data class BurnoutInsight(
    val title: String,
    val observation: String,
    val action: String
)

data class BurnoutAssessment(
    val score: Int,                       // 0..100 — higher means more risk
    val level: BurnoutLevel,
    val contributors: List<BurnoutContributor>,
    val insight: BurnoutInsight?,
    val recommendedFocus: String,
    val suggestedActions: List<String>
)

data class BurnoutInput(
    val weeklySleep: List<Double> = emptyList(),     // chronological, last ~7 days
    val weeklyRecovery: List<Int> = emptyList(),
    val weeklySteps: List<Int> = emptyList(),
    val recoveryScore: Int? = null,
    val sleepHours: Double? = null,
    val stressLevel: Int = 3,                         // 1 calm .. 5 overwhelmed
    val journalSentiment: JournalSentiment? = null,
    val usualWeeklyWorkouts: Int = 0,
    val recentWeeklyWorkouts: Int = 0,
    val trainingLoadIncreasing: Boolean = false
)

object BurnoutEngine {

    private fun List<Double>.decliningD(): Boolean =
        size >= 3 && takeLast(3).zipWithNext().all { (a, b) -> b <= a } && last() < first()

    private fun List<Int>.decliningI(): Boolean =
        size >= 3 && takeLast(3).zipWithNext().all { (a, b) -> b <= a } && last() < first()

    /** Count of trailing days at/under a sleep threshold (most recent first). */
    private fun List<Double>.trailingLowSleepDays(threshold: Double): Int =
        reversed().takeWhile { it < threshold }.size

    fun assess(input: BurnoutInput): BurnoutAssessment {
        var score = 0.0
        val contributors = mutableListOf<BurnoutContributor>()

        // ── Sleep ──
        val avgSleep = input.weeklySleep.takeIf { it.isNotEmpty() }?.average()
        val sleepDeclining = input.weeklySleep.decliningD()
        if (avgSleep != null) {
            score += when {
                avgSleep < WellnessThresholds.SLEEP_VERY_POOR -> 25.0
                avgSleep < WellnessThresholds.SLEEP_POOR -> 18.0
                avgSleep < WellnessThresholds.SLEEP_LOW -> 12.0
                avgSleep < WellnessThresholds.SLEEP_ADEQUATE -> 6.0
                else -> 0.0
            }
        }
        if (sleepDeclining) {
            score += 10.0
            contributors += BurnoutContributor("Sleep", SignalDirection.DOWN, "trending down")
        } else if (avgSleep != null && avgSleep < WellnessThresholds.SLEEP_LOW) {
            contributors += BurnoutContributor("Sleep", SignalDirection.DOWN, "below your usual")
        }

        // ── Recovery ──
        val avgRecovery = input.weeklyRecovery.takeIf { it.isNotEmpty() }?.average()
        val recoveryDeclining = input.weeklyRecovery.decliningI()
        if (avgRecovery != null) {
            score += when {
                avgRecovery < WellnessThresholds.RECOVERY_VERY_LOW -> 22.0
                avgRecovery < WellnessThresholds.RECOVERY_LOW -> 15.0
                avgRecovery < WellnessThresholds.RECOVERY_MODERATE -> 8.0
                else -> 0.0
            }
        }
        if (recoveryDeclining) {
            score += 8.0
            contributors += BurnoutContributor("Recovery", SignalDirection.DOWN, "trending down")
        }

        // ── Movement ──
        val avgSteps = input.weeklySteps.takeIf { it.isNotEmpty() }?.average()
        val movementDeclining = input.weeklySteps.decliningI()
        if (movementDeclining) {
            score += 10.0
            contributors += BurnoutContributor("Movement", SignalDirection.DOWN, "trending down")
        }
        if (avgSteps != null && avgSteps < WellnessThresholds.SEDENTARY_STEPS) {
            score += 8.0
        }

        // ── Stress check-in (1..5) ──
        val stressOver = (input.stressLevel - 3).coerceAtLeast(0)
        if (stressOver > 0) {
            score += stressOver * 10.0
            contributors += BurnoutContributor("Stress", SignalDirection.UP, "elevated lately")
        }

        // ── Journal sentiment ──
        when (input.journalSentiment) {
            JournalSentiment.CHALLENGING -> {
                score += 12.0
                contributors += BurnoutContributor("Mood", SignalDirection.DOWN, "recent entries feel heavier")
            }
            JournalSentiment.POSITIVE -> score -= 8.0     // protective
            else -> {}
        }

        // ── Workload imbalance & adherence ──
        if (input.trainingLoadIncreasing && sleepDeclining) score += 10.0
        if (input.usualWeeklyWorkouts >= WellnessThresholds.ACTIVE_WEEK_DAYS &&
            input.recentWeeklyWorkouts == 0
        ) {
            score += 6.0
        }

        val finalScore = score.coerceIn(0.0, 100.0).toInt()
        val level = when {
            finalScore < 25 -> BurnoutLevel.LOW
            finalScore < 45 -> BurnoutLevel.MODERATE
            finalScore < 65 -> BurnoutLevel.ELEVATED
            else -> BurnoutLevel.HIGH
        }

        val insight = detectInsight(input, sleepDeclining, recoveryDeclining, movementDeclining, avgRecovery)
        val (focus, actions) = recommend(level, contributors)

        return BurnoutAssessment(
            score = finalScore,
            level = level,
            contributors = contributors.distinctBy { it.label },
            insight = insight,
            recommendedFocus = focus,
            suggestedActions = actions
        )
    }

    /** The four detection rules, in priority order. Returns the most relevant story. */
    private fun detectInsight(
        input: BurnoutInput,
        sleepDeclining: Boolean,
        recoveryDeclining: Boolean,
        movementDeclining: Boolean,
        avgRecovery: Double?
    ): BurnoutInsight? {
        val lowSleepStreak = input.weeklySleep.trailingLowSleepDays(WellnessThresholds.SLEEP_LOW)
        val recoveryLow = (avgRecovery ?: 100.0) < WellnessThresholds.RECOVERY_MODERATE
        val stressHigh = input.stressLevel >= 4
        val journalNegative = input.journalSentiment == JournalSentiment.CHALLENGING

        // 1. Recovery Spiral — sustained short sleep dragging recovery down.
        if (lowSleepStreak >= 3 && recoveryDeclining) {
            return BurnoutInsight(
                title = "Recovery Is Slipping",
                observation = "Sleep appears to be affecting recovery.",
                action = "Protecting rest today may improve the next several days."
            )
        }
        // 2. Stress Accumulation — mental load is high across several signals.
        if (stressHigh && recoveryLow && journalNegative) {
            return BurnoutInsight(
                title = "Stress Has Been Elevated Recently",
                observation = "Your recent entries suggest higher mental load.",
                action = "Recovery activities may have a larger impact than additional training."
            )
        }
        // 3. Workload Imbalance — effort rising faster than recovery.
        if (input.trainingLoadIncreasing && sleepDeclining) {
            return BurnoutInsight(
                title = "Training Load Is Outpacing Recovery",
                observation = "Your effort is increasing faster than recovery.",
                action = "A lighter session may help maintain progress."
            )
        }
        // 4. Early Warning — several recovery signals drifting down together.
        if (sleepDeclining && recoveryDeclining && movementDeclining) {
            return BurnoutInsight(
                title = "Energy Is Trending Down",
                observation = "Several recovery signals are moving in the wrong direction.",
                action = "Prioritize sleep and lighter activity today."
            )
        }
        return null
    }

    private fun recommend(
        level: BurnoutLevel,
        contributors: List<BurnoutContributor>
    ): Pair<String, List<String>> = when (level) {
        BurnoutLevel.HIGH, BurnoutLevel.ELEVATED -> "Recovery" to listOf(
            "A 20-minute walk",
            "An earlier bedtime tonight",
            "A 5-minute breathing session"
        )
        BurnoutLevel.MODERATE -> "Balance" to buildList {
            add("A short walk to reset")
            if (contributors.any { it.label == "Sleep" }) add("Protect your sleep window")
            else add("Protect your energy today")
        }
        BurnoutLevel.LOW -> "Maintain" to listOf("Keep the momentum — your habits are working together")
    }
}
