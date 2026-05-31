package com.solofit.app.reminders

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuietHoursTest {

    // 22:00 (1320) -> 07:00 (420), wraps midnight
    private val start = 22 * 60
    private val end = 7 * 60

    @Test
    fun `late night is quiet`() {
        assertTrue(QuietHours.isQuiet(23 * 60, start, end))   // 23:00
        assertTrue(QuietHours.isQuiet(2 * 60, start, end))    // 02:00
        assertTrue(QuietHours.isQuiet(start, start, end))     // exactly 22:00
    }

    @Test
    fun `daytime is not quiet`() {
        assertFalse(QuietHours.isQuiet(12 * 60, start, end))  // noon
        assertFalse(QuietHours.isQuiet(end, start, end))      // exactly 07:00 (exclusive)
        assertFalse(QuietHours.isQuiet(21 * 60 + 59, start, end))
    }

    @Test
    fun `non-wrapping window works`() {
        // quiet 01:00 -> 05:00
        val s = 60; val e = 5 * 60
        assertTrue(QuietHours.isQuiet(3 * 60, s, e))
        assertFalse(QuietHours.isQuiet(6 * 60, s, e))
    }

    @Test
    fun `zero length window is never quiet`() {
        assertFalse(QuietHours.isQuiet(0, 480, 480))
        assertFalse(QuietHours.isQuiet(480, 480, 480))
    }
}
