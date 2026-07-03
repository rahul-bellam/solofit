package com.solofit.app.core

import com.solofit.app.sol.WellnessThresholds
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
        waterGoalMl: Int = WellnessThresholds.WATER_DEFAULT_GOAL_ML,
        energyScore: Int? = null
    ): Int? {
        data class C(val weight: Double, val value: Double)
        val parts = buildList {
            sleepHours?.let { add(C(WellnessThresholds.RECOVERY_WEIGHT_SLEEP, (it / WellnessThresholds.SLEEP_OPTIMAL).coerceIn(0.0, 1.0))) }
            steps?.let { add(C(WellnessThresholds.RECOVERY_WEIGHT_STEPS, (it / WellnessThresholds.DEFAULT_STEP_GOAL.toDouble()).coerceIn(0.0, 1.0))) }
            workoutDone?.let { add(C(WellnessThresholds.RECOVERY_WEIGHT_WORKOUT, if (it) 1.0 else 0.0)) }
            waterMl?.let {
                val goal = if (waterGoalMl > 0) waterGoalMl else WellnessThresholds.WATER_DEFAULT_GOAL_ML
                add(C(WellnessThresholds.RECOVERY_WEIGHT_WATER, (it.toDouble() / goal).coerceIn(0.0, 1.0)))
            }
            energyScore?.let { add(C(WellnessThresholds.RECOVERY_WEIGHT_ENERGY, (it / 10.0).coerceIn(0.0, 1.0))) }
        }
        if (parts.isEmpty()) return null
        val totalWeight = parts.sumOf { it.weight }
        val weighted = parts.sumOf { it.weight * it.value }
        return ((weighted / totalWeight) * 100).roundToInt()
    }

    /**
     * Subjective daily readiness (0..100) from self-reported check-in inputs.
     * Distinct from [recoveryScore], which blends objective signals (steps, water,
     * workout completion). Kept here (Android-free) so both scores are single-sourced
     * and unit-testable rather than reimplemented inside the DataStore layer.
     *
     *  sleep  30%  (target 8h)
     *  stress 20%  (1 = high stress … 5 = low)
     *  mood   20%  (1..5)
     *  energy 30%  (1..5)
     */
    fun subjectiveReadinessScore(
        sleepHours: Float,
        stressLevel: Int,
        moodLevel: Int,
        energyLevel: Int
    ): Int {
        val sleep = ((sleepHours / 8f).coerceAtMost(1f) * 30).toInt()
        val stress = ((5 - stressLevel) / 4f * 20).toInt()
        val mood = (moodLevel / 5f * 20).toInt()
        val energy = (energyLevel / 5f * 30).toInt()
        return (sleep + stress + mood + energy).coerceIn(0, 100)
    }

    /** Readiness label from a recovery score. */
    fun readinessLabel(score: Int?): String = when {
        score == null -> "—"
        score >= WellnessThresholds.RECOVERY_EXCELLENT -> "High"
        score >= WellnessThresholds.RECOVERY_GOOD -> "Moderate"
        score >= WellnessThresholds.RECOVERY_LOW -> "Low"
        else -> "Rest day"
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
