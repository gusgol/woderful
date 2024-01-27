package me.goldhardt.woderful.presentation.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import kotlinx.coroutines.delay
import me.goldhardt.woderful.R
import me.goldhardt.woderful.presentation.theme.WODerfulTheme

@Composable
internal fun CountdownScreen(
    seconds: Int = 3,
    onCountdownFinished: () -> Unit = {}
) {
    var countdown by remember { mutableIntStateOf(seconds) }

    LaunchedEffect(key1 = true) {
        for (i in 3 downTo 0) {
            countdown = i
            delay(1000L)
        }
        onCountdownFinished()
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val color by animateColorAsState(
            targetValue = when (countdown) {
                2 -> MaterialTheme.colors.secondary
                1 -> MaterialTheme.colors.error
                0 -> MaterialTheme.colors.onBackground
                else -> MaterialTheme.colors.primary
            },
            label = "color"
        )

        Canvas(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
        ) {
            drawCircle(
                color = color,
                radius = size.minDimension / 2,
                center = center,
                style = Stroke(width = 12f) // This draws a stroke instead of a filled circle
            )
        }

        AnimatedContent(targetState = countdown, label = "countdown") { countdownValue ->
            Text(
                text = if (countdownValue > 0) {
                    countdownValue.toString()
                } else {
                    stringResource(R.string.title_go)
                },
                style = MaterialTheme.typography.display1
            )
        }
    }
}

@WearPreviewDevices
@Composable
fun MinutesTimeConfigurationPreview() {
    WODerfulTheme {
        CountdownScreen()
    }
}