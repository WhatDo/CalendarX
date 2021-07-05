package dk.appdo.calendarx

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.paging.*
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import dk.appdo.calendarx.ui.theme.CalendarXTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.TemporalAdjuster
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.*
import java.util.concurrent.Executors
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.Q)
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
fun CalendarView() {
//    val pager =
//        Pager(PagingConfig(5)) {
//            WeekDataSource()
//        }
    Scaffold {
//        val weeks: LazyPagingItems<List<LocalDate>> = pager.flow.collectAsLazyPagingItems()

        val firstDay = remember { WeekDataHelper.firstWeekOfMonth(LocalDate.now()) }

//        val weeks = remember {
//            mutableStateListOf<List<LocalDate>>(WeekDataHelper.getWeekFrom(firstDay))
//        }

//        val listState = rememberLazyListState()

//        val loadBefore = remember {
//            derivedStateOf {
//                weeks[listState.firstVisibleItemIndex][0]
//            }
//        }

//        val loadAfter = remember {
//            derivedStateOf {
//                val info = listState.layoutInfo
//                val total = info.totalItemsCount - 1
//                val lastIndex = info.visibleItemsInfo.lastOrNull()?.index ?: 0
//
//                Log.d(
//                    "CalendarX",
//                    "New computed state of loadAfter lastIndex $lastIndex, total $total"
//                )
//                weeks[lastIndex][0]
////                lastIndex >= total
//            }
//        }

//        LaunchedEffect(key1 = loadBefore, key2 = loadAfter) {
//            snapshotFlow { loadBefore.value to loadAfter.value }
//                .collect { (before, after) ->
//                    Log.d("CalendarX", "Before $before to After $after")
////                    val first = weeks[0][0]
////                    if (before == first) {
////                        weeks.add(0, WeekDataHelper.prevWeek(first))
////                    }
//
//                    val last = weeks.last()[0]
//                    if (after == last) {
//                        weeks.add(WeekDataHelper.nextWeek(last))
//                    }
//                }
//        }
//
//        LazyColumn(state = listState) {
//            items(
//                weeks,
//                key = { week -> week[0] }) { week ->//}, key = { idx -> weeks[idx][0] }) { idx ->
//                WeekRow(days = week)
//            }
//        }
        InfiniteComputedScrollList { idx ->
            WeekRow(days = WeekDataHelper.getWeekFrom(firstDay.plusWeeks(idx.toLong())))
        }
    }
}

@Composable
fun MonthlyCalendar() {

}

@Composable
fun WeekRow(days: List<LocalDate>) {
    Row(modifier = Modifier.fillMaxWidth()) {
        days.forEach { day ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, color = MaterialTheme.colors.secondary),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(modifier = Modifier.padding(12.dp), text = day.dayOfMonth.toString())
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MaterialTheme {
        CalendarView()
    }
}

object WeekDataHelper {

    @SuppressLint("ConstantLocale")
    private val weekFields = WeekFields.of(Locale.getDefault())

    fun firstWeekOfMonth(month: LocalDate): LocalDate {
        return month.withDayOfMonth(1)
            .with(TemporalAdjusters.previousOrSame(weekFields.firstDayOfWeek))
    }

    fun prevWeek(date: LocalDate): List<LocalDate> {
        return getWeekFrom(date.minusDays(7L))
    }

    fun nextWeek(date: LocalDate): List<LocalDate> {
        return getWeekFrom(date.plusDays(7L))
    }

    fun getWeekFrom(date: LocalDate): List<LocalDate> {
        return List(7) { idx -> date.plusDays(idx.toLong()) }
    }
}

class WeekDataSource : PagingSource<LocalDate, List<LocalDate>>() {
    private val weekDays = WeekFields.of(Locale.getDefault())
    override suspend fun load(params: LoadParams<LocalDate>): LoadResult<LocalDate, List<LocalDate>> {
        val key = params.key ?: LocalDate.now()
            .with(TemporalAdjusters.previousOrSame(weekDays.firstDayOfWeek))

        return LoadResult.Page(
            listOf(List(7) { idx -> key.plusDays(idx.toLong()) }),
            key.minusDays(7),
            key.plusDays(7)
        )
    }

    override fun getRefreshKey(state: PagingState<LocalDate, List<LocalDate>>): LocalDate? {
        return LocalDate.now()
    }
}


@Composable
fun InfiniteComputedScrollList(
    isVertical: Boolean = true,
    contentFactory: @Composable (idx: Int) -> Unit
) {
    val scrollableState = remember {
        InfiniteScrollableState()
    }
    val factory by rememberUpdatedState(newValue = contentFactory)

    SubcomposeLayout(
        modifier = Modifier.scrollable(
            scrollableState,
            orientation = if (isVertical) Orientation.Vertical else Orientation.Horizontal
        )
    ) { constraints ->
        val childConstraints = Constraints(maxWidth = constraints.maxWidth)
        layout(
            width = constraints.maxWidth,
            height = constraints.maxHeight
        ) {
            var index = scrollableState.index
            var currentY = scrollableState.offset.roundToInt()
            var remainingDim =
                if (isVertical) constraints.maxHeight else constraints.maxWidth + currentY

            while (remainingDim > 0) {
                val measureables = subcompose(index) {
                    factory.invoke(index)
                }
                val placeables = measureables.map { measurable ->
                    measurable.measure(childConstraints)
                }.onEach { placeable ->
                    placeable.placeRelative(0, currentY)
                }
                val height = placeables.sumOf { placeable -> placeable.height }
                remainingDim -= height
                currentY += height
                index++
            }
        }
    }
}

class InfiniteScrollableState : ScrollableState {

    var index by mutableStateOf(0)
    var offset by mutableStateOf(0f)

    private val scope = object : ScrollScope {
        override fun scrollBy(pixels: Float): Float {
            offset += pixels
            return pixels
        }
    }

    override var isScrollInProgress: Boolean by mutableStateOf(false)
        private set


    override fun dispatchRawDelta(delta: Float): Float {
        return delta
    }

    override suspend fun scroll(
        scrollPriority: MutatePriority,
        block: suspend ScrollScope.() -> Unit
    ) {
        isScrollInProgress = true
        scope.block()
        isScrollInProgress = false
    }
}