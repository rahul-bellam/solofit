package com.solofit.app.core

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/** Small offline date helpers; ISO yyyy-MM-dd is used as the day key in the DB. */
object DateUtils {
    private val iso: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun today(): String = LocalDate.now().format(iso)

    fun parse(date: String): LocalDate = LocalDate.parse(date, iso)

    fun prettyMedium(date: String): String {
        val d = parse(date)
        val dow = d.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
        val month = d.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
        return "$dow, $month ${d.dayOfMonth}"
    }
}
