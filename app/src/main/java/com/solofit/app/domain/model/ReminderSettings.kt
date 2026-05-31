package com.solofit.app.domain.model

/**
 * All reminder configuration. Persisted in DataStore. Times are stored as
 * minutes-from-midnight to stay timezone-trivial and easy to compare.
 */
data class ReminderSettings(
    val hydrationEnabled: Boolean = false,
    val hydrationIntervalMinutes: Int = 120,   // every 2h by default
    val workoutEnabled: Boolean = false,
    val workoutTimeMinutes: Int = 18 * 60,     // 6:00 PM
    // Journaling prompts
    val morningGoalsEnabled: Boolean = false,
    val morningGoalsTimeMinutes: Int = 8 * 60, // 8:00 AM — "plan your day"
    val eveningGratitudeEnabled: Boolean = false,
    val eveningGratitudeTimeMinutes: Int = 21 * 60, // 9:00 PM — "2-min gratitude"
    // Quiet hours: no reminders fire between these (handles overnight wrap).
    val quietStartMinutes: Int = 22 * 60,      // 10:00 PM
    val quietEndMinutes: Int = 7 * 60          // 7:00 AM
) {
    companion object {
        /** Selectable hydration cadences (label -> minutes). */
        val HYDRATION_INTERVALS = listOf(
            "Every 1 hour" to 60,
            "Every 90 min" to 90,
            "Every 2 hours" to 120,
            "Every 3 hours" to 180,
            "Every 4 hours" to 240
        )
    }
}

/** Formats minutes-from-midnight as a 24h HH:mm string. */
fun Int.asClockString(): String {
    val h = (this / 60) % 24
    val m = this % 60
    return "%02d:%02d".format(h, m)
}
