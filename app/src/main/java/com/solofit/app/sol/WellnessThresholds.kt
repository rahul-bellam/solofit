package com.solofit.app.sol

object WellnessThresholds {

    // ─── Steps ───
    const val SEDENTARY_STEPS = 3000
    const val LOW_MOVEMENT_STEPS = 4000
    const val MODERATE_MOVEMENT_STEPS = 5000
    const val PERFORMANCE_STEPS = 6000
    const val DEFAULT_STEP_GOAL = 8000

    // ─── Recovery Score (0–100) ───
    const val RECOVERY_VERY_LOW = 30
    const val RECOVERY_LOW = 40
    const val RECOVERY_MODERATE = 50
    const val RECOVERY_EARLY_WARNING_MAX = 55
    const val RECOVERY_GOOD = 60
    const val RECOVERY_PERFORMANCE = 65
    const val RECOVERY_HIGH = 70
    const val RECOVERY_EXCELLENT = 80
    const val RECOVERY_WARNING_DELTA = 10

    // ─── Sleep (hours) ───
    const val SLEEP_VERY_POOR = 5.0
    const val SLEEP_POOR = 5.5
    const val SLEEP_LOW = 6.0
    const val SLEEP_ADEQUATE = 6.5
    const val SLEEP_OPTIMAL = 7.0
    const val SLEEP_IMPROVEMENT_DELTA = 0.5
    const val SLEEP_BASELINE_DELTA = 0.5

    // ─── Protein (ratio of target) ───
    const val PROTEIN_LOW = 0.5
    const val PROTEIN_NEAR = 0.8
    const val PROTEIN_GOOD = 0.9
    const val PROTEIN_GOAL_MET = 1.0
    const val PROTEIN_SLIPPING_THRESHOLD = 0.7
    const val PROTEIN_BASELINE_DELTA = 0.2
    const val PROTEIN_MICRO_WIN_DELTA = 0.1

    // ─── Water (ratio of goal) ───
    const val WATER_LOW = 0.5
    const val WATER_DEFAULT_GOAL_ML = 3000

    // ─── Movement Risk ───
    const val MOVEMENT_RISK_CRITICAL_AVG = 0.3
    const val MOVEMENT_RISK_HIGH_AVG = 0.5
    const val MOVEMENT_RISK_MODERATE_AVG = 0.7
    const val MOVEMENT_RISK_TOO_LOW_FRACTION = 0.5
    const val MOVEMENT_CRITICAL_CONSECUTIVE = 5
    const val MOVEMENT_HIGH_CONSECUTIVE = 3
    const val MOVEMENT_MODERATE_TOO_LOW_DAYS = 3

    // ─── Activity Days ───
    const val CONSISTENT_WEEK_DAYS = 4
    const val ACTIVE_WEEK_DAYS = 3
    const val LOW_ACTIVITY_DAYS = 2

    // ─── Streak Milestones ───
    const val STREAK_MILESTONE_7 = 7

    // ─── Body Recomp (4-week rates) ───
    const val RECOMP_WAIST_DECREASE_CM = 0.5
    const val RECOMP_WEIGHT_STABLE_KG = 1.0
    const val RECOMP_MUSCLE_LOSS_RATE_KG = 2.0
    const val RECOMP_WEIGHT_GAIN_RATE_KG = 2.0
    const val RECOMP_GOOD_TREND_STABLE_KG = 0.5
    const val RECOMP_TREND_DIRECTION_DIFF = 0.3

    // ─── Baseline Comparison Multipliers ───
    const val BASELINE_STEPS_FRACTION = 0.7

    // ─── Recovery Score Weights ───
    const val RECOVERY_WEIGHT_SLEEP = 0.40
    const val RECOVERY_WEIGHT_STEPS = 0.20
    const val RECOVERY_WEIGHT_WORKOUT = 0.15
    const val RECOVERY_WEIGHT_WATER = 0.15
    const val RECOVERY_WEIGHT_ENERGY = 0.10

    // ─── Insight Priority ───
    const val PRIORITY_STREAK_100 = 1000
    const val PRIORITY_STREAK_30 = 900
    const val PRIORITY_STREAK_7 = 800
    const val PRIORITY_WORKLOAD_BALANCE = 530
    const val PRIORITY_POOR_SLEEP = 500
    const val PRIORITY_RECOVERY_EARLY_WARNING = 450
    const val PRIORITY_PROTEIN_GOAL_MET = 400
    const val PRIORITY_MICRO_WIN_STEPS = 400
    const val PRIORITY_NEAR_PROTEIN_GOAL = 390
    const val PRIORITY_LOW_PROTEIN = 380
    const val PRIORITY_PROTEIN_SLIPPING = 375
    const val PRIORITY_MISSED_WORKOUT = 290
    const val PRIORITY_WALKING_GOAL = 280
    const val PRIORITY_SEDENTARY_DAY = 270
    const val PRIORITY_LOW_MOVEMENT_WEEK = 265
    const val PRIORITY_WALKING_IMPROVED = 260
    const val PRIORITY_SLEEP_IMPROVED = 260
    const val PRIORITY_RECOVERY_IMPROVING = 250
    const val PRIORITY_RECOVERY_DECLINING = 240
    const val PRIORITY_RECOVERY_STABLE = 230
    const val PRIORITY_SLEEP_CONSISTENT = 220
    const val PRIORITY_CONSISTENCY_PRAISE = 200
    const val PRIORITY_MICRO_WIN_SLEEP = 350
    const val PRIORITY_MICRO_WIN_PROTEIN = 300
    const val PRIORITY_MICRO_WIN_RECOVERY = 250
    const val PRIORITY_MICRO_WIN_MEDITATION = 200
    const val PRIORITY_STRESS_IMPROVED = 160
    const val PRIORITY_MEDITATION_STREAK = 150
    const val PRIORITY_HIGH_STRESS = 140
    const val PRIORITY_JOURNAL_POSITIVE = 110
    const val PRIORITY_JOURNAL_CHALLENGING = 110
    const val PRIORITY_WATER_LOW = 110
    const val PRIORITY_JOURNAL_STREAK = 100
    const val PRIORITY_FALLBACK = 10
}
