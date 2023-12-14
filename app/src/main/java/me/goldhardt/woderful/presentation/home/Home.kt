package me.goldhardt.woderful.presentation.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.AutoCenteringParams
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.OutlinedChip
import androidx.wear.compose.material.Text
import me.goldhardt.woderful.R
import me.goldhardt.woderful.data.model.ClockType

@Composable
fun HomeScreen(
    listState: ScalingLazyListState,
    onHistoryClick: () -> Unit,
    onItemClick: (ClockType) -> Unit,
) {
    val items = ClockType.values()
    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        autoCentering = AutoCenteringParams(itemIndex = 0),
        state = listState
    ) {
        items(items) { clockType ->
            ClockTypeItem(clockType = clockType, onItemClick = onItemClick)
        }
        item {
            Text(
                text = "More Options",
                style = MaterialTheme.typography.caption1,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .padding(8.dp)
            )
        }
        item {
            OutlinedChip(
                onClick = onHistoryClick,
                enabled = true,
                label = {
                    Text(
                        text = stringResource(id = R.string.title_history),
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = stringResource(id = R.string.title_history),
                        modifier = Modifier
                            .size(ChipDefaults.IconSize)
                            .wrapContentSize(align = Alignment.Center),
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ClockTypeItem(
    clockType: ClockType,
    onItemClick: (ClockType) -> Unit
) {
    Chip(
        label = {
            Text(
                text = stringResource(clockType.displayName),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        colors = ChipDefaults.imageBackgroundChipColors(
            backgroundImagePainter = painterResource(
                id = clockType.image
            )
        ),
        onClick = { onItemClick(clockType) },
        modifier = Modifier.fillMaxWidth()
    )
}