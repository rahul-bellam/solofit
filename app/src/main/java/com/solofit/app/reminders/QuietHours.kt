package com.solofit.app.reminders

/** Pure quiet-hours math, testable without Android. */
object QuietHours {

    /**
     * True if [minuteOfDay] (0..1439) falls within the quiet window
     * [startMin, endMin). Correctly handles windows that wrap past midnight
     * (e.g. 22:00 -> 07:00).
     */
    fun isQuiet(minuteOfDay: Int, startMin: Int, endMin: Int): Boolean {
        if (startMin == endMin) return false // zero-length window = never quiet
        return if (startMin < endMin) {
            minuteOfDay in startMin until endMin
        } else {
            // wraps midnight
            minuteOfDay >= startMin || minuteOfDay < endMin
        }
    }
}
