package me.goldhardt.woderful.presentation.component

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.MaterialTheme

@Composable
fun CircleContainer(
    modifier: Modifier = Modifier,
    circleColor: Color = MaterialTheme.colors.surface,
    radius: Dp? = null,
    minimumRadius: Dp = 24.dp,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.drawWithContent {
        val circleRadius = radius?.toPx()
            ?: maxOf(
                minimumRadius.toPx(),
                size.width.coerceAtMost(size.height) / 2
            )
        val circleCenter = Offset(size.width / 2, size.height / 2)

        drawCircle(color = circleColor, radius = circleRadius, center = circleCenter)
        drawContent()
    }) {
        content()
    }
}
