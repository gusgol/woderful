package me.goldhardt.woderful.presentation.history

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.wear.compose.foundation.lazy.AutoCenteringParams
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TitleCard
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import me.goldhardt.woderful.R
import me.goldhardt.woderful.data.model.ClockProperties
import me.goldhardt.woderful.data.model.ClockType
import me.goldhardt.woderful.data.model.Workout
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
    val uiState = viewModel.workouts.collectAsLazyPagingItems()
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        HistoryItems(items = uiState)
    }
}


@Composable
fun HistoryItems(
    items: LazyPagingItems<Workout>
) {
    val listState = rememberScalingLazyListState()
    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        autoCentering = AutoCenteringParams(itemIndex = 0),
        state = listState
    ) {
        if (items.loadState.refresh == LoadState.Loading) {
            item {
                Text(
                    text = stringResource(id = R.string.title_loading),
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth(Alignment.CenterHorizontally)
                )
            }
        }
        items(count = items.itemCount) { index ->
            items[index]?.let { workout ->
                HistoryItem(workout)
            }
        }
        if (items.loadState.append == LoadState.Loading) {
            item {
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth(Alignment.CenterHorizontally)
                )
            }
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
        time = { Text(
            item.createdAt.formatDate(),
            style = MaterialTheme.typography.caption3,
        ) },
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.Timer,
                    contentDescription = stringResource(id = R.string.title_workout_duration),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.size(2.dp))
                Text(item.durationMs.toMinutesAndSeconds())
                if (item.calories != null && item.calories > 0) {
                    Text(" • ")
                    Text(
                        "${DecimalFormat(HISTORY_CALS_FORMAT).format(item.calories)} kcals",
                        style = MaterialTheme.typography.caption1,
                    )
                }
            }
            if (item.avgHeartRate != null && item.avgHeartRate > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.FavoriteBorder,
                        contentDescription = stringResource(id = R.string.title_workout_duration),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.size(2.dp))
                    Text(
                        stringResource(
                            R.string.msg_avg,
                            DecimalFormat(HISTORY_AVG_HR_FORMAT).format(item.avgHeartRate)
                        ),
                    )
                }
            }
            if (item.properties != null) {
                item.type.getDetailsComposable(item.properties).invoke()
            }
        }
    }
}

@Composable
internal fun EmomProperties(properties: Map<String, Any>) {
    val activeTime = properties[ClockProperties.Configuration.CONFIG_ACTIVE_TIME_S] as? Double
    val roundCount = properties[ClockProperties.Configuration.CONFIG_ROUNDS] as? Double
    val restTime = properties[ClockProperties.Configuration.CONFIG_REST_TIME] as? Double

    if (activeTime != null && roundCount != null && restTime != null) {
        val roundsDescription =
            "${roundCount.toInt()} " +
                    pluralStringResource(id = R.plurals.message_rounds, count = roundCount.toInt())
        val configurationSummary = stringResource(
            R.string.title_emom_summary_desc,
            activeTime.toInt().toMinutesAndSeconds(),
            roundsDescription,
            restTime.toInt().toMinutesAndSeconds()
        )
        Divider(
            thickness = 1.dp,
            color = MaterialTheme.colors.onSurface,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Text(
            text = configurationSummary,
            style = MaterialTheme.typography.caption1,
        )
    }
}

@WearPreviewDevices
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