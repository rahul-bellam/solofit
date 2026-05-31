package com.solofit.app.core

import kotlin.math.roundToInt

/**
 * Pure, Android-free fitness calculations so they're unit-testable.
 */
object FitnessMath {

    /**
     * Estimated one-rep max (Epley formula): 1RM = w * (1 + reps/30).
     * Returns the input weight for a single rep, 0 for invalid input.
     */
    fun epley1RM(weightKg: Double, reps: Int): Double {
        if (weightKg <= 0 || reps <= 0) return 0.0
        if (reps == 1) return weightKg
        return weightKg * (1.0 + reps / 30.0)
    }

    fun epley1RMRounded(weightKg: Double, reps: Int): Int = epley1RM(weightKg, reps).roundToInt()

    /**
     * V-Taper score = shoulder-to-waist ratio. A higher number is a more aesthetic
     * "V" physique. The classic "golden" target is ~1.618 (phi).
     * Returns null if inputs are missing/invalid.
     */
    fun vTaperRatio(shouldersCm: Double?, waistCm: Double?): Double? {
        if (shouldersCm == null || waistCm == null) return null
        if (shouldersCm <= 0 || waistCm <= 0) return null
        return shouldersCm / waistCm
    }

    /** Human label for a V-taper ratio. */
    fun vTaperLabel(ratio: Double?): String = when {
        ratio == null -> "—"
        ratio >= 1.6 -> "Elite V-taper"
        ratio >= 1.45 -> "Strong V-taper"
        ratio >= 1.3 -> "Developing"
        else -> "Building base"
    }

    /**
     * Recovery / training-readiness score (0..100) from manual daily inputs.
     * Weighted blend; each component contributes only if present, and the score is
     * normalized by the weights actually available so partial data still works.
     *
     *  sleep      40%  (target 8h, linear, capped)
     *  steps      20%  (target 8000)
     *  workoutDone15%
     *  water      15%  (vs goalMl)
     *  energy     10%  (1..10)
     */
    fun recoveryScore(
        sleepHours: Double?,
        steps: Int?,
        workoutDone: Boolean?,
        waterMl: Int?,
        waterGoalMl: Int = 3000,
        energyScore: Int? = null
    ): Int? {
        data class C(val weight: Double, val value: Double)
        val parts = buildList {
            sleepHours?.let { add(C(0.40, (it / 8.0).coerceIn(0.0, 1.0))) }
            steps?.let { add(C(0.20, (it / 8000.0).coerceIn(0.0, 1.0))) }
            workoutDone?.let { add(C(0.15, if (it) 1.0 else 0.0)) }
            waterMl?.let {
                val goal = if (waterGoalMl > 0) waterGoalMl else 3000
                add(C(0.15, (it.toDouble() / goal).coerceIn(0.0, 1.0)))
            }
            energyScore?.let { add(C(0.10, (it / 10.0).coerceIn(0.0, 1.0))) }
        }
        if (parts.isEmpty()) return null
        val totalWeight = parts.sumOf { it.weight }
        val weighted = parts.sumOf { it.weight * it.value }
        return ((weighted / totalWeight) * 100).roundToInt()
    }

    /** Readiness label from a recovery score. */
    fun readinessLabel(score: Int?): String = when {
        score == null -> "—"
        score >= 80 -> "High"
        score >= 60 -> "Moderate"
        score >= 40 -> "Low"
        else -> "Rest day"
    }

    /** Auto-progression suggestion for an exercise's completed sets. */
    enum class Progression(val message: String) {
        INCREASE("Increase weight next session 💪"),
        HOLD("Repeat — aim for more reps next time"),
        DELOAD("Drop the weight a little and rebuild form"),
        NONE("")
    }

    /**
     * Decide progression from completed sets of one exercise.
     * @param repsPerSet reps achieved on each completed working set
     * @param rirPerSet matching RIR per set (null where not recorded)
     * @param topOfRange the top of the target rep range (e.g. 12 for 8-12)
     *
     * Rules (simple double-progression):
     *  - all sets >= topOfRange AND (RIR unknown or <=1)  -> INCREASE
     *  - any set very low (< topOfRange/2) with RIR 0       -> DELOAD
     *  - otherwise                                          -> HOLD
     */
    fun progression(
        repsPerSet: List<Int>,
        rirPerSet: List<Int?>,
        topOfRange: Int
    ): Progression {
        val working = repsPerSet.filter { it > 0 }
        if (working.isEmpty() || topOfRange <= 0) return Progression.NONE
        val allHitTop = working.all { it >= topOfRange }
        val lowRir = rirPerSet.filterNotNull()
        val rirOk = lowRir.isEmpty() || lowRir.all { it <= 1 }
        val struggled = working.any { it < topOfRange / 2 } &&
            rirPerSet.filterNotNull().any { it == 0 }
        return when {
            allHitTop && rirOk -> Progression.INCREASE
            struggled -> Progression.DELOAD
            else -> Progression.HOLD
        }
    }


    /**
     * Transformation Score (0..100): a single number blending the things that
     * actually predict physique change, weighted by the user's TrainingGoal.
     *
     * Each component is already normalized to 0..1:
     * @param strengthProgress 0..1 (e.g. recent 1RM gain vs a target gain)
     * @param waistProgress 0..1 (waist reduction vs a target; 0.5 = no change)
     * @param consistency 0..1 (workouts done vs planned over the window)
     * @param recovery 0..1 (recovery score / 100)
     * @param w* component weights (from TrainingGoal); normalized internally.
     */
    fun transformationScore(
        strengthProgress: Double,
        waistProgress: Double,
        consistency: Double,
        recovery: Double,
        wStrength: Double,
        wWaist: Double,
        wConsistency: Double,
        wRecovery: Double
    ): Int {
        val parts = listOf(
            wStrength to strengthProgress.coerceIn(0.0, 1.0),
            wWaist to waistProgress.coerceIn(0.0, 1.0),
            wConsistency to consistency.coerceIn(0.0, 1.0),
            wRecovery to recovery.coerceIn(0.0, 1.0)
        )
        val totalW = parts.sumOf { it.first }.takeIf { it > 0 } ?: return 0
        val weighted = parts.sumOf { it.first * it.second }
        return ((weighted / totalW) * 100).roundToInt()
    }

}
