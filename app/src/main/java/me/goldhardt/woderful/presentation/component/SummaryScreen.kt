package me.goldhardt.woderful.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import me.goldhardt.woderful.R
import me.goldhardt.woderful.extensions.getElapsedTimeMs
import me.goldhardt.woderful.extensions.toMinutesAndSeconds
import me.goldhardt.woderful.presentation.theme.WODerfulTheme
import me.goldhardt.woderful.service.WorkoutState

@Composable
fun SummaryScreen(
    sections: List<SummarySection>,
    onClose: () -> Unit = {},
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
            item {
                Chip(
                    onClick = onClose,
                    icon = {
                        Icon(
                          imageVector = Icons.Outlined.Close,
                            contentDescription = stringResource(R.string.action_close),
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(R.string.action_close),
                            maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                    },
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}

data class SummarySection(
    val title: Int,
    val value: String,
)

fun WorkoutState.toDefaultSummarySections(): List<SummarySection> {
    val duration =
        (activeDurationCheckpoint?.getElapsedTimeMs() ?: 0L).toMinutesAndSeconds()
    val roundCount = exerciseLaps
    val calories = workoutMetrics.calories
    val avgHeartRate = workoutMetrics.heartRateAverage?.toInt()
    return defaultSummarySections(duration, roundCount, calories, avgHeartRate)
}

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