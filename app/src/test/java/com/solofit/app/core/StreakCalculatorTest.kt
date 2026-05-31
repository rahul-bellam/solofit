package com.solofit.app.core

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class StreakCalculatorTest {

    private val today = LocalDate.of(2026, 5, 30)

    @Test
    fun `empty is zero`() {
        assertEquals(0, StreakCalculator.currentStreak(emptyList(), today))
    }

    @Test
    fun `streak counts consecutive days ending today`() {
        val dates = listOf("2026-05-30", "2026-05-29", "2026-05-28")
        assertEquals(3, StreakCalculator.currentStreak(dates, today))
    }

    @Test
    fun `streak still counts when today not done but yesterday done`() {
        val dates = listOf("2026-05-29", "2026-05-28")
        assertEquals(2, StreakCalculator.currentStreak(dates, today))
    }

    @Test
    fun `gap breaks the streak`() {
        // today + a gap (28th missing) -> only today counts
        val dates = listOf("2026-05-30", "2026-05-27", "2026-05-26")
        assertEquals(1, StreakCalculator.currentStreak(dates, today))
    }

    @Test
    fun `old workouts with no recent activity is zero`() {
        val dates = listOf("2026-05-01", "2026-04-30")
        assertEquals(0, StreakCalculator.currentStreak(dates, today))
    }

    @Test
    fun `duplicates do not inflate the streak`() {
        val dates = listOf("2026-05-30", "2026-05-30", "2026-05-29")
        assertEquals(2, StreakCalculator.currentStreak(dates, today))
    }

    @Test
    fun `days active in window counts distinct days only`() {
        val dates = listOf("2026-05-30", "2026-05-30", "2026-05-28", "2026-05-20")
        // window = last 7 days (24th..30th): 30th and 28th -> 2
        assertEquals(2, StreakCalculator.daysActiveInWindow(dates, today, 7))
    }
}
