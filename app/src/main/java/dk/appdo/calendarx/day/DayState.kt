package dk.appdo.calendarx.day

import androidx.compose.runtime.Immutable
import java.time.LocalDate

@Immutable
data class DayState(
    val day: LocalDate
)