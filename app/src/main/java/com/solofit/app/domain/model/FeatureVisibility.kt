package com.solofit.app.domain.model

/**
 * Adaptive visibility engine — hides features until they become relevant.
 *
 * Beginner (<14 days): Sol, Today's Priority, Movement, Nutrition, Water, Micro Wins
 * Returning low-consistency: Rebuild Mode, Walking, Recovery, Simple Wins
 * Intermediate: Recovery, Meditation, Journal, Reflections
 * Advanced: Body Recomp, Monthly Reflections, Discipline Features
 */
data class VisibilityContext(
    val daysTracked: Int,
    val daysActiveThisWeek: Int,
    val streakDays: Int,
    val recoveryScore: Int?,
    val workoutToday: Boolean,
    val weeklyWorkoutCount: Int,
    val meditationMinutes: Int,
    val journalDays: Int,
    val lifestyleModeName: String
)

object FeatureVisibility {

    fun shouldShowWeeklyReflection(ctx: VisibilityContext): Boolean =
        ctx.daysTracked >= 7 && ctx.weeklyWorkoutCount > 0

    fun shouldShowMonthlyReflection(ctx: VisibilityContext): Boolean =
        ctx.daysTracked >= 28

    fun shouldShowWaterProminent(ctx: VisibilityContext): Boolean {
        val recoveryLow = ctx.recoveryScore != null && ctx.recoveryScore < 40
        val activeWeek = ctx.daysActiveThisWeek >= 3
        return recoveryLow || activeWeek
    }

    fun shouldShowRecoveryCard(ctx: VisibilityContext): Boolean =
        ctx.daysTracked >= 7 && ctx.recoveryScore != null

    fun shouldShowMeditation(ctx: VisibilityContext): Boolean =
        ctx.daysTracked >= 14

    fun shouldShowJournal(ctx: VisibilityContext): Boolean =
        ctx.daysTracked >= 14

    fun shouldShowBodyRecomp(ctx: VisibilityContext): Boolean =
        ctx.daysTracked >= 30 && ctx.daysActiveThisWeek >= 3

    fun shouldShowMicroWins(ctx: VisibilityContext): Boolean {
        if (ctx.daysTracked < 3) return true
        if (ctx.daysTracked >= 30 && ctx.daysActiveThisWeek >= 4) return false
        return true
    }

    fun waterProminentReason(ctx: VisibilityContext): String = when {
        ctx.recoveryScore != null && ctx.recoveryScore < 40 -> "Recovery is low — hydration supports recovery"
        ctx.daysActiveThisWeek >= 3 -> "Active week — keep hydration top of mind"
        else -> ""
    }

    /**
     * Beginner: first 14 days, show only essentials.
     */
    fun isBeginner(ctx: VisibilityContext): Boolean = ctx.daysTracked < 14

    /**
     * Returning after low activity.
     */
    fun isReturningLowConsistency(ctx: VisibilityContext): Boolean =
        ctx.daysTracked >= 14 && ctx.daysActiveThisWeek < 2 && ctx.streakDays < 3

    /**
     * Intermediate: consistent for 2+ weeks.
     */
    fun isIntermediate(ctx: VisibilityContext): Boolean =
        ctx.daysTracked >= 14 && ctx.daysActiveThisWeek >= 3

    /**
     * Advanced: consistent for 30+ days with high activity.
     */
    fun isAdvanced(ctx: VisibilityContext): Boolean =
        ctx.daysTracked >= 30 && ctx.daysActiveThisWeek >= 4
}
