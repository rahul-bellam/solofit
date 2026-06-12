package com.solofit.app.sol

/**
 * Protein intake status for a single day.
 */
enum class ProteinDayStatus {
    ON_TRACK,
    SLIGHTLY_LOW,
    MISSED,
    UNKNOWN
}

/**
 * Overall protein pattern over multiple windows.
 */
data class ProteinPattern(
    val todayStatus: ProteinDayStatus,
    val todayPct: Double,
    val avg3dPct: Double,
    val avg7dPct: Double,
    val lowDaysIn7: Int,
    val lowDaysIn14: Int,
    val trend: String,
    val message: String,
    val action: String
)

/**
 * Deterministic rule engine that evaluates local protein logs.
 *
 * Works entirely from daily protein totals — no cloud, no AI, no remote calls.
 * Windows: today, last 3 days, last 7 days, last 14 days.
 */
object ProteinPatternEngine {

    private const val ON_TRACK_THRESHOLD = 0.90
    private const val SLIGHTLY_LOW_THRESHOLD = 0.70
    private const val REPEATEDLY_LOW_DAYS = 3

    /**
     * Evaluate protein pattern from a list of daily (proteinG, targetG) pairs.
     *
     * @param recentDays list of (proteinG, targetG) for recent days, most recent first
     */
    fun evaluate(recentDays: List<Pair<Double, Double>>): ProteinPattern {
        if (recentDays.isEmpty()) {
            return ProteinPattern(
                todayStatus = ProteinDayStatus.UNKNOWN,
                todayPct = 0.0,
                avg3dPct = 0.0,
                avg7dPct = 0.0,
                lowDaysIn7 = 0,
                lowDaysIn14 = 0,
                trend = "stable",
                message = "Log meals to track protein patterns.",
                action = "Start by logging a meal."
            )
        }

        val today = recentDays.first()
        val todayPct = pct(today)
        val todayStatus = classify(todayPct)

        val last3 = recentDays.take(3)
        val last7 = recentDays.take(7)
        val last14 = recentDays.take(14)

        val avg3dPct = if (last3.isNotEmpty()) last3.map { pct(it) }.average() else 0.0
        val avg7dPct = if (last7.isNotEmpty()) last7.map { pct(it) }.average() else 0.0

        val lowDaysIn7 = last7.count { pct(it) < SLIGHTLY_LOW_THRESHOLD }
        val lowDaysIn14 = last14.count { pct(it) < SLIGHTLY_LOW_THRESHOLD }

        val trend = computeTrend(last7)
        val (message, action) = buildMessage(todayStatus, todayPct, avg7dPct, lowDaysIn7, trend)

        return ProteinPattern(
            todayStatus = todayStatus,
            todayPct = todayPct,
            avg3dPct = avg3dPct,
            avg7dPct = avg7dPct,
            lowDaysIn7 = lowDaysIn7,
            lowDaysIn14 = lowDaysIn14,
            trend = trend,
            message = message,
            action = action
        )
    }

    private fun pct(day: Pair<Double, Double>): Double {
        val (protein, target) = day
        if (target <= 0) return 0.0
        return (protein / target).coerceIn(0.0, 1.5)
    }

    private fun classify(pct: Double): ProteinDayStatus = when {
        pct >= ON_TRACK_THRESHOLD -> ProteinDayStatus.ON_TRACK
        pct >= SLIGHTLY_LOW_THRESHOLD -> ProteinDayStatus.SLIGHTLY_LOW
        pct > 0 -> ProteinDayStatus.MISSED
        else -> ProteinDayStatus.UNKNOWN
    }

    private fun computeTrend(last7: List<Pair<Double, Double>>): String {
        if (last7.size < 3) return "stable"
        val first3 = last7.takeLast(3).map { pct(it) }.average()
        val last3 = last7.take(3).map { pct(it) }.average()
        val diff = last3 - first3
        return when {
            diff > 0.1 -> "improving"
            diff < -0.1 -> "declining"
            else -> "stable"
        }
    }

    private fun buildMessage(
        status: ProteinDayStatus,
        todayPct: Double,
        avg7dPct: Double,
        lowDaysIn7: Int,
        trend: String
    ): Pair<String, String> {
        val pctDisplay = "${(todayPct * 100).toInt()}%"

        if (status == ProteinDayStatus.UNKNOWN) {
            return "Log meals to track protein patterns." to "Start by logging a meal."
        }

        // Repeatedly low takes priority
        if (lowDaysIn7 >= REPEATEDLY_LOW_DAYS) {
            return when {
                trend == "declining" ->
                    "Protein has been trending lower this week." to "A protein-rich meal now would help close the gap."
                else ->
                    "Your protein has been below target for $lowDaysIn7 days this week." to "Consider adding a protein source to your next meal."
            }
        }

        if (status == ProteinDayStatus.MISSED) {
            return when {
                todayPct < 0.3 ->
                    "Protein intake is significantly below target today." to "A protein-rich meal or shake may help."
                else ->
                    "Protein intake is a little low today." to "Adding a protein source at your next meal may help."
            }
        }

        if (status == ProteinDayStatus.SLIGHTLY_LOW) {
            return when {
                trend == "declining" ->
                    "Protein consistency is slipping." to "Try to include protein at your next meal."
                else ->
                    "Protein intake is close to target." to "A small adjustment could bring it within range."
            }
        }

        // ON_TRACK
        return when {
            avg7dPct >= ON_TRACK_THRESHOLD && trend == "improving" ->
                "Protein intake has been consistently on target this week." to "Keep doing what you are doing."
            avg7dPct >= ON_TRACK_THRESHOLD ->
                "Protein intake is on target." to "Keep maintaining your current habits."
            else ->
                "Protein intake is on track today." to "Maintain your current approach."
        }
    }
}
