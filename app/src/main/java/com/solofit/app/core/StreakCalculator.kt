package com.solofit.app.core

import java.time.LocalDate

/** Pure, testable streak logic so it can be unit-tested without Android. */
object StreakCalculator {

    /**
     * Current consecutive-day streak ending today (or yesterday).
     * A streak is unbroken if there's a workout today, OR the most recent day with
     * a workout was yesterday (today not done yet still counts the prior run).
     *
     * @param workoutDates ISO yyyy-MM-dd strings (any order, may contain dups)
     * @param today reference date
     */
    fun currentStreak(workoutDates: Collection<String>, today: LocalDate): Int {
        if (workoutDates.isEmpty()) return 0
        val days = workoutDates.mapNotNull { runCatching { LocalDate.parse(it) }.getOrNull() }.toSet()
        if (days.isEmpty()) return 0

        // Anchor: today if present, else yesterday if present, else no active streak.
        var cursor = when {
            days.contains(today) -> today
            days.contains(today.minusDays(1)) -> today.minusDays(1)
            else -> return 0
        }
        var streak = 0
        while (days.contains(cursor)) {
            streak++
            cursor = cursor.minusDays(1)
        }
        return streak
    }

    /** Count of distinct workout days within the last [windowDays] (inclusive of today). */
    fun daysActiveInWindow(
        workoutDates: Collection<String>,
        today: LocalDate,
        windowDays: Int = 7
    ): Int {
        val start = today.minusDays((windowDays - 1).toLong())
        return workoutDates
            .mapNotNull { runCatching { LocalDate.parse(it) }.getOrNull() }
            .filter { !it.isBefore(start) && !it.isAfter(today) }
            .distinct()
            .size
    }
}
