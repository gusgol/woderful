package me.goldhardt.woderful.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HeartBroken
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.health.services.client.data.DataTypeAvailability
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import me.goldhardt.woderful.R


@Composable
fun HeartRateMonitor(
    hr: Double = 0.0,
    availability: DataTypeAvailability = DataTypeAvailability.AVAILABLE,
) {
    val icon = when (availability) {
        DataTypeAvailability.AVAILABLE -> Icons.Default.Favorite
        DataTypeAvailability.ACQUIRING -> Icons.Default.MonitorHeart
        DataTypeAvailability.UNAVAILABLE,
        DataTypeAvailability.UNAVAILABLE_DEVICE_OFF_BODY -> Icons.Default.HeartBroken
        else -> Icons.Default.QuestionMark
    }
    val text = if (availability == DataTypeAvailability.AVAILABLE) {
        hr.toInt().toString()
    } else {
        stringResource(id = R.string.title_no_hr_reading)
    }

    CircleContainer(
        minimumRadius = 24.dp,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription =  stringResource(R.string.action_confirm),
                tint = MaterialTheme.colors.primary,
                modifier = Modifier
                    .size(8.dp)
                    .wrapContentSize(align = Alignment.Center)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.title1
            )
        }
    }
}