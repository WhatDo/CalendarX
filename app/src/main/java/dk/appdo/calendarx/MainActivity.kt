package dk.appdo.calendarx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.layout.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.paging.*
import dk.appdo.calendarx.day.DayState
import dk.appdo.calendarx.day.DayView
import dk.appdo.calendarx.ui.theme.CalendarXTheme
import dk.appdo.calendarx.util.InfiniteComputedScrollList
import dk.appdo.calendarx.util.InfiniteScrollableState
import dk.appdo.calendarx.util.firstWeekOfMonth
import dk.appdo.calendarx.util.getWeek
import dk.appdo.calendarx.week.WeekState
import dk.appdo.calendarx.week.WeekView
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalendarXTheme {
                CalendarView()
            }
        }
    }
}

@Composable
fun CalendarView(
    formatter: DateTimeFormatter = remember { DateTimeFormatter.ofPattern("MMMM y") },
    firstDay: LocalDate = remember { LocalDate.now().firstWeekOfMonth() },
    scrollableState: InfiniteScrollableState = remember { InfiniteScrollableState() }
) {
    val monthTitle by remember {
        derivedStateOf {
            firstDay.plusWeeks(scrollableState.indexObservable.toLong()).format(formatter)
        }
    }
    Scaffold(topBar = {
        TopAppBar(title = { Text(text = monthTitle) })
    }) {
        val colors = MaterialTheme.colors
        BoxWithConstraints {
            InfiniteComputedScrollList(scrollableState = scrollableState) { idx ->
                val curWeek = firstDay.plusWeeks(idx.toLong())
                val week = remember(curWeek) {
                    WeekState(curWeek.getWeek())
                }
                WeekView(
                    modifier = Modifier
                        .requiredHeight(maxHeight / 5)
                        .draggable(
                            rememberDraggableState(onDelta = {}),
                            orientation = Orientation.Horizontal
                        ),
                    week = week
                )
                { day ->
                    val dayState by rememberUpdatedState(newValue = DayState(day))
                    val backgroundColor by remember {
                        derivedStateOf {
                            if (dayState.day.monthValue % 2 == 0) {
                                colors.background.copy(alpha = 0.8f)
                                    .compositeOver(if (colors.isLight) Color.Black else Color.White)
                            } else {
                                colors.background
                            }
                        }
                    }

                    DayView(
                        modifier = Modifier.background(backgroundColor),
                        day = dayState
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    CalendarXTheme {
        CalendarView()
    }
}

