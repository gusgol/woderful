package me.goldhardt.woderful.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import me.goldhardt.woderful.R
import me.goldhardt.woderful.presentation.theme.WODerfulTheme

@Composable
fun SummaryScreen(
    sections: List<SummarySection>,
    descriptionSlot: @Composable () -> Unit = {},
) {
    val listState = rememberScalingLazyListState()
    Box(
        contentAlignment = Alignment.Center,
    ) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.title_workout_finished),
                    style = MaterialTheme.typography.title2,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(4.dp)
                )
            }
            item {
                descriptionSlot()
            }
            sections.forEach { section ->
                item {
                    WorkoutInfoItem(value = section.value, text = section.title)
                }
            }
        }
    }
}

data class SummarySection(
    val title: Int,
    val value: String,
)

fun defaultSummarySections(
    duration: String,
    roundCount: Int?,
    calories: Double?,
    avgHeartRate: Int?,
): List<SummarySection> {
    val sections = mutableListOf(
        SummarySection(
            title = R.string.title_workout_duration,
            value = duration,
        )
    )
    if (roundCount != null) {
        sections.add(
            SummarySection(
                title = R.string.title_rounds,
                value = roundCount.toString(),
            )
        )
    }
    if (calories != null) {
        sections.add(
            SummarySection(
                title = R.string.title_calories,
                value = String.format("%.2f", calories),
            )
        )
    }
    if (avgHeartRate != null && avgHeartRate > 0) {
        sections.add(
            SummarySection(
                title = R.string.title_avg_heart_rate,
                value = avgHeartRate.toString(),
            )
        )
    }
    return sections
}

@WearPreviewDevices
@Composable
fun AmrapClockPreview() {
    WODerfulTheme {
        SummaryScreen(defaultSummarySections("12:00", 12, 120.0, 120))
    }
}