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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.layout.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
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
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjuster
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.*
import java.util.concurrent.Executors
import kotlin.math.roundToInt

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
fun CalendarView() {
    val formatter = remember { DateTimeFormatter.ofPattern("MMMM y") }
    val firstDay = remember { WeekDataHelper.firstWeekOfMonth(LocalDate.now()) }
    val scrollableState = remember { InfiniteScrollableState() }
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
                WeekRow(
                    height = maxHeight / 5,
                    days = WeekDataHelper.getWeekFrom(firstDay.plusWeeks(idx.toLong()))
                ) { day ->
                    if (day.monthValue % 2 == 0) {
                        colors.background.copy(alpha = 0.8f).compositeOver(if (colors.isLight) Color.Black else Color.White)
                    } else {
                        colors.background
                    }
                }
            }
        }
    }
}

@Composable
fun MonthlyCalendar() {
    LazyColumn() {

    }
}

@Composable
fun WeekRow(height: Dp, days: List<LocalDate>, cellColor: (LocalDate) -> Color) {
    val cellColorState by rememberUpdatedState(newValue = cellColor)
    Row(
        modifier = Modifier
            .requiredHeight(height = height)
            .fillMaxWidth()
    ) {
        days.forEach { day ->
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .border(1.dp, color = MaterialTheme.colors.secondary)
                    .background(cellColorState(day))
                    .wrapContentHeight(),
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
    scrollableState: InfiniteScrollableState = remember { InfiniteScrollableState() },
    contentFactory: @Composable (idx: Int) -> Unit
) {
    val factory by rememberUpdatedState(newValue = contentFactory)

    Log.d("Infinite", "Composing")
    SubcomposeLayout(
        modifier = Modifier
            .scrollable(
                scrollableState,
                orientation = if (isVertical) Orientation.Vertical else Orientation.Horizontal
            )
            .then(scrollableState.remeasurementModifier),

        state = remember { SubcomposeLayoutState(maxSlotsToRetainForReuse = 2) }
    ) { constraints ->
        val childConstraints = Constraints(maxWidth = constraints.maxWidth)
        layout(
            width = constraints.maxWidth,
            height = constraints.maxHeight
        ) {
            Log.d("Layout", "Doing layout pass")
            var index = scrollableState.index
            var currentY = scrollableState.offset.roundToInt()
            var remainingDim =
                (if (isVertical) constraints.maxHeight else constraints.maxWidth) - currentY

            var upY = currentY
            var upIdx = index - 1
            // place upwards first
            while (upY > 0) {
                val height = placeSubcomposables(
                    scope = this@SubcomposeLayout,
                    childConstraints = childConstraints,
                    position = { placeable -> IntOffset(x = 0, y = upY - placeable.height) },
                    index = upIdx,
                    factory
                )
                upY -= height
                // If we get here, the view was scrolled down to reveal a new top item.
                // Store it here so next layout pass will start from the top.
                scrollableState.updateOffset(upIdx, scrollableState.offset - height)

                upIdx--
            }

            while (remainingDim > 0) {
                val height = placeSubcomposables(
                    scope = this@SubcomposeLayout,
                    childConstraints = childConstraints,
                    position = { IntOffset(x = 0, y = currentY) },
                    index = index,
                    factory
                )
                remainingDim -= height
                currentY += height
                index++

                // If we're still above the screen, the top item was scrolled out.
                if (currentY < 0) {
                    scrollableState.updateOffset(
                        scrollableState.index + 1,
                        scrollableState.offset + height
                    )
                }
            }
        }
    }
}

private fun Placeable.PlacementScope.placeSubcomposables(
    scope: SubcomposeMeasureScope,
    childConstraints: Constraints,
    position: (Placeable) -> IntOffset,
    index: Int,
    content: @Composable (Int) -> Unit
): Int {
    return scope.subcompose(index) {
        content(index)
    }.map { measurable ->
        measurable.measure(childConstraints)
    }.onEach { placeable ->
        placeable.place(position = position(placeable))
    }.sumOf { placeable ->
        placeable.height
    }
}

@Stable
class InfiniteScrollableState : ScrollableState {

    private val delegate: ScrollableState = ScrollableState(::onScroll)

    private lateinit var remeasurement: Remeasurement

    val remeasurementModifier: RemeasurementModifier = object : RemeasurementModifier {
        override fun onRemeasurementAvailable(remeasurement: Remeasurement) {
            this@InfiniteScrollableState.remeasurement = remeasurement
        }
    }

    private val indexState = mutableStateOf(0)
    var index: Int = 0
        private set
    val indexObservable: Int get() = indexState.value

    private val offsetState = mutableStateOf(0f)
    val offsetObservable: Float get() = offsetState.value
    var offset = 0f
        private set

    private fun onScroll(delta: Float): Float {
        setOffset(offset + delta)
        remeasurement.forceRemeasure()
        return delta
    }

    fun updateOffset(index: Int, offset: Float) {
        setIndex(index)
        setOffset(offset)
    }

    private fun setIndex(index: Int) {
        if (index != this.index) {
            this.index = index
            indexState.value = index
        }
    }

    private fun setOffset(offset: Float) {
        if (offset != this.offset) {
            this.offset = offset
            offsetState.value = offset
        }
    }

    override val isScrollInProgress: Boolean get() = delegate.isScrollInProgress

    override fun dispatchRawDelta(delta: Float): Float {
        return delegate.dispatchRawDelta(delta)
    }

    override suspend fun scroll(
        scrollPriority: MutatePriority,
        block: suspend ScrollScope.() -> Unit
    ) {
        delegate.scroll(scrollPriority, block)
    }
}