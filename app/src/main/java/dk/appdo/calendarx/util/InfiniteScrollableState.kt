package dk.appdo.calendarx.util

import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.layout.Remeasurement
import androidx.compose.ui.layout.RemeasurementModifier

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