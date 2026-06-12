package com.solofit.app.sol

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.IsoFields
import kotlin.random.Random

data class PatternDiscovery(
    val observation: String,
    val insight: String,
    val category: String,
    val confidence: String
)

object PatternDiscoveryEngine {

    private val shownThisWeek = mutableSetOf<String>()
    private var lastDiscoveryWeek: Int = -1
    private const val MAX_PER_WEEK = 2

    fun discover(
        trends: List<TrendSummary>,
        signals: List<SignalSummary>,
        baseline: UserBaseline?,
        weeklyWorkoutDays: List<DayOfWeek>,
        stepsByDay: Map<DayOfWeek, Int>,
        daysTracked: Int
    ): List<PatternDiscovery> {
        val currentWeek = LocalDate.now().isoWeekOfWeekyear()

        if (currentWeek != lastDiscoveryWeek) {
            shownThisWeek.clear()
            lastDiscoveryWeek = currentWeek
        }

        if (shownThisWeek.size >= MAX_PER_WEEK) return emptyList()
        if (daysTracked < 14) return emptyList()

        val discoveries = mutableListOf<PatternDiscovery>()

        val bestTrainingDay = weeklyWorkoutDays.groupBy { it }
            .maxByOrNull { it.value.size }
            ?.key
        if (bestTrainingDay != null && "best_day" !in shownThisWeek) {
            val dayName = bestTrainingDay.displayName()
            discoveries.add(PatternDiscovery(
                "You train most consistently on $dayName.",
                "Consider scheduling your hardest sessions on $dayName.",
                "consistency",
                if (weeklyWorkoutDays.size >= 20) "High" else "Medium"
            ))
            shownThisWeek.add("best_day")
        }

        val sleepTrend = trends.firstOrNull { it.label == "Sleep" }
        if (sleepTrend?.direction == TrendDirection.UP && "sleep_up" !in shownThisWeek) {
            discoveries.add(PatternDiscovery(
                "Your sleep has been improving recently.",
                "Earlier bedtimes may be contributing to better recovery.",
                "sleep",
                "Medium"
            ))
            shownThisWeek.add("sleep_up")
        }

        val walkingTrend = trends.firstOrNull { it.label == "Walking" }
        if (walkingTrend?.direction == TrendDirection.UP && "walking_up" !in shownThisWeek) {
            discoveries.add(PatternDiscovery(
                "Your walking volume is trending upward.",
                "This increase in low-intensity movement supports recovery.",
                "movement",
                "Medium"
            ))
            shownThisWeek.add("walking_up")
        }

        val proteinSignal = signals.firstOrNull { it.label == "Nutrition" }
        if (proteinSignal?.status == SignalStatus.GOOD && "protein_good" !in shownThisWeek && Random.nextFloat() > 0.5f) {
            discoveries.add(PatternDiscovery(
                "Your nutrition consistency is strong this week.",
                "This level of adherence supports both performance and recovery.",
                "nutrition",
                "Medium"
            ))
            shownThisWeek.add("protein_good")
        }

        val stepsByWeekday = stepsByDay.filterKeys { it.value in 1..5 }
        val stepsWeekend = stepsByDay.filterKeys { it.value == 6 || it.value == 7 }
        if (stepsByWeekday.isNotEmpty() && stepsWeekend.isNotEmpty() && "weekend_drop" !in shownThisWeek) {
            val weekdayAvg = stepsByWeekday.values.average()
            val weekendAvg = stepsWeekend.values.average()
            if (weekendAvg < weekdayAvg * 0.6) {
                discoveries.add(PatternDiscovery(
                    "Your step count drops on weekends.",
                    "A short weekend walk may help maintain momentum.",
                    "movement",
                    "Low"
                ))
                shownThisWeek.add("weekend_drop")
            }
        }

        val recoverySignal = signals.firstOrNull { it.label == "Recovery" }
        if (recoverySignal?.status == SignalStatus.GOOD && "rec_good" !in shownThisWeek && discoveries.isEmpty()) {
            discoveries.add(PatternDiscovery(
                "Your recovery metrics look good.",
                "Consistent sleep and balanced training are supporting this.",
                "recovery",
                "Medium"
            ))
            shownThisWeek.add("rec_good")
        }

        return discoveries
    }

    private fun DayOfWeek.displayName(): String = when (this) {
        DayOfWeek.MONDAY -> "Mondays"
        DayOfWeek.TUESDAY -> "Tuesdays"
        DayOfWeek.WEDNESDAY -> "Wednesdays"
        DayOfWeek.THURSDAY -> "Thursdays"
        DayOfWeek.FRIDAY -> "Fridays"
        DayOfWeek.SATURDAY -> "Saturdays"
        DayOfWeek.SUNDAY -> "Sundays"
    }

    private fun LocalDate.isoWeekOfWeekyear(): Int =
        this.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
}
