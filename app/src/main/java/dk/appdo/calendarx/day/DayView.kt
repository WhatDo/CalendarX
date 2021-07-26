package dk.appdo.calendarx.day

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.time.LocalDate

@Composable
fun RowScope.DayView(
    modifier: Modifier,
    day: DayState,
    content: @Composable () -> Unit = {
        Text(
            modifier = Modifier.padding(12.dp),
            text = day.day.dayOfMonth.toString()
        )
    }
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .weight(1f)
            .border(1.dp, color = MaterialTheme.colors.secondary)
            .then(modifier)
            .wrapContentHeight(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        content()
    }
}