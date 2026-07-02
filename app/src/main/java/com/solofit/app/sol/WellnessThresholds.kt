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
    const val STREAK_MILESTONE_30 = 30
    const val STREAK_MILESTONE_100 = 100

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

    // ─── Trend & Delta Thresholds ───
    const val STEPS_SIGNIFICANT_CHANGE = 500
    const val STEPS_TREND_SIGNIFICANT_PCT = 5
    const val STEPS_IMPROVEMENT_FACTOR = 1.2
    const val RECOVERY_SIGNIFICANT_DELTA = 5
    const val RECOVERY_MICRO_WIN_DELTA = 3
    const val SLEEP_SIGNIFICANT_DELTA_HOURS = 0.3
    const val STRESS_HIGH_THRESHOLD = 4
    const val STRESS_CRITICAL_THRESHOLD = 7
    const val STRESS_IMPROVEMENT_DELTA = 1
    const val SLEEP_DEFICIENCY_HOURS = 6.0
    const val MOVEMENT_RISK_LOW_AVG = 3000
    const val TREND_WINDOW_DAYS = 7
    const val CONSISTENCY_WINDOW_DAYS = 7

    // ─── Risk Severity Weights ───
    const val RISK_SEVERITY_SLEEP = 0.7f
    const val RISK_SEVERITY_STRESS = 0.6f
    const val RISK_SEVERITY_MOVEMENT = 0.5f

    // ─── Wellness State Thresholds ───
    const val THRIVING_CONSISTENCY_SCORE = 0.7f
    const val THRIVING_RECOVERY_SCORE = 60
    const val MAINTAINING_CONSISTENCY_SCORE = 0.4f
    const val AT_RISK_SEVERITY_THRESHOLD = 0.6f
    const val MIN_DAYS_FOR_MEANINGFUL_DATA = 3
    const val MIN_DAYS_FOR_PATTERNS = 14

    // ─── Momentum Thresholds ───
    const val MOMENTUM_RECOVERY_NEGATIVE = 40
    const val MOMENTUM_POS_COUNT_EXCELLENT = 4
    const val MOMENTUM_NEG_COUNT_EXCELLENT = 1
    const val MOMENTUM_POS_COUNT_STRONG = 3
    const val MOMENTUM_RATIO_STRONG = 0.6
    const val MOMENTUM_POS_COUNT_STABLE = 2

    // ─── Trailing Window ───
    const val TRAILING_WINDOW_SIZE = 3

    // ─── Micro-Win Thresholds ───
    const val PROTEIN_GRAMS_MULTIPLIER = 150

    // ─── Calorie Goal Range ───
    const val CALORIE_GOAL_RANGE_LOW = 0.9
    const val CALORIE_GOAL_RANGE_HIGH = 1.1

    // ─── Journal ───
    const val JOURNAL_STREAK_DAYS = 3

    // ─── Trend Building ───
    const val MIN_TREND_POINTS = 2
    const val MIN_STRENGTH_SAMPLES = 3
    const val SUPPLEMENTARY_INSIGHT_COUNT = 3

    // ─── Protein Adherence ───
    const val PROTEIN_ADHERENCE_RATIO = 0.8

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
