package me.goldhardt.woderful.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text

@Composable
fun RoundsCounter(
    count: Int,
    minimumRadius: Dp = 24.dp,
) {
    CircleContainer(
        minimumRadius = minimumRadius,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "Rounds",
                color = MaterialTheme.colors.primary,
                style = MaterialTheme.typography.caption2
            )
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.title1
            )
        }
    }
}