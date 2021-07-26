package dk.appdo.calendarx.util

import android.annotation.SuppressLint
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.*

fun LocalDate.firstWeekOfMonth(weekFields: WeekFields = WeekFields.of(Locale.getDefault())): LocalDate {
    return withDayOfMonth(1)
        .with(TemporalAdjusters.previousOrSame(weekFields.firstDayOfWeek))
}

fun LocalDate.prevWeek(): List<LocalDate> {
    return minusDays(7L).getWeek()
}

fun LocalDate.nextWeek(): List<LocalDate> {
    return plusDays(7L).getWeek()
}

fun LocalDate.getWeek(): List<LocalDate> {
    return List(7) { idx -> plusDays(idx.toLong()) }
}
