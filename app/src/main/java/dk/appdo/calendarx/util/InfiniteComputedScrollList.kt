package dk.appdo.calendarx.util

import android.util.Log
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.SubcomposeLayoutState
import androidx.compose.ui.layout.SubcomposeMeasureScope
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt


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

