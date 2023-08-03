package me.goldhardt.woderful.presentation.history

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.wear.compose.foundation.lazy.AutoCenteringParams
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TitleCard
import me.goldhardt.woderful.R
import me.goldhardt.woderful.data.ClockType
import me.goldhardt.woderful.data.Workout
import me.goldhardt.woderful.extensions.formatDate
import me.goldhardt.woderful.extensions.toMinutesAndSeconds
import me.goldhardt.woderful.presentation.theme.WODerfulTheme
import java.text.DecimalFormat
import java.util.Date

private const val HISTORY_AVG_HR_FORMAT = "#"
private const val HISTORY_CALS_FORMAT = "#"

@Composable
fun History(
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val uiState by produceState<HistoryUiState>(
        initialValue = HistoryUiState.Loading,
        key1 = lifecycle,
        key2 = viewModel
    ) {
        lifecycle.repeatOnLifecycle(state = Lifecycle.State.STARTED) {
            viewModel.uiState.collect { value = it }
        }
    }
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        when (uiState) {
            is HistoryUiState.Success -> {
                HistoryItems((uiState as HistoryUiState.Success).workouts)
            }

            is HistoryUiState.Error -> {
                Text(text = (uiState as HistoryUiState.Error).throwable.message!!)
            }

            HistoryUiState.Loading -> {
                Text(text = "Loading...")
            }
        }
    }
}

@Composable
fun HistoryItems(
    items: List<Workout>
) {
    val listState = rememberScalingLazyListState()
    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        autoCentering = AutoCenteringParams(itemIndex = 0),
        state = listState
    ) {
        items(items) { workout ->
            HistoryItem(workout)
        }
    }
}

@Composable
fun HistoryItem(
    item: Workout
) {
    TitleCard(
        onClick = {},
        title = {
            Text(text = stringResource(item.type.displayName))
        },
        time = { Text(item.createdAt.formatDate()) },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.Timer,
                contentDescription = stringResource(id = R.string.title_workout_duration),
                modifier = Modifier.size(14.dp)
            )
            Text(item.durationMs.toMinutesAndSeconds())
            if (item.avgHeartRate != null && item.avgHeartRate > 0) {
                Text(" • ")
                Icon(
                    Icons.Outlined.FavoriteBorder,
                    contentDescription = stringResource(id = R.string.title_workout_duration),
                    modifier = Modifier.size(14.dp)
                )
                Text(DecimalFormat(HISTORY_AVG_HR_FORMAT).format(item.avgHeartRate))
            }
            if (item.calories != null && item.calories > 0) {
                Text(" • ")
                Text("${DecimalFormat(HISTORY_CALS_FORMAT).format(item.calories)} kcals")
            }
        }
    }
}

@Preview(
    device = Devices.WEAR_OS_SMALL_ROUND,
    showSystemUi = true,
    backgroundColor = 0xff000000,
    showBackground = true
)
@Composable
fun TimeConfigurationPreview() {
    WODerfulTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            HistoryItem(
                Workout(
                    durationMs = 12 * 60 * 1000,
                    type = ClockType.AMRAP,
                    rounds = 8,
                    calories = 135.0,
                    avgHeartRate = 90.3,
                    createdAt = Date().time
                )
            )
        }
    }
}