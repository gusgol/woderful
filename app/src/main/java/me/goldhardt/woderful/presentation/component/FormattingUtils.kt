package me.goldhardt.woderful.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.wear.compose.material.MaterialTheme
import kotlin.math.roundToInt




/** Format calories burned to an integer with a "cal" suffix. */
@Composable
fun formatCalories(calories: Double?) = buildAnnotatedString {
    if (calories == null || calories.isNaN()) {
        append("--")
    } else {
        append(calories.roundToInt().toString())
        withStyle(style = MaterialTheme.typography.caption3.toSpanStyle()) {
            append(" cal")
        }
    }
}

/** Format a distance to two decimals with a "km" suffix. */
@Composable
fun formatDistanceKm(meters: Double?) = buildAnnotatedString {
    if (meters == null) {
        append("--")
    } else {
        append("%02.2f".format(meters / 1_000))
        withStyle(style = MaterialTheme.typography.caption3.toSpanStyle()) {
            append("km")
        }
    }
}

/** Format heart rate with a "bpm" suffix. */
@Composable
fun formatHeartRate(bpm: Double?) = buildAnnotatedString {
    if (bpm == null || bpm.isNaN()) {
        append("--")
    } else {
        append("%.0f".format(bpm))
        withStyle(style = MaterialTheme.typography.caption3.toSpanStyle()) {
            append("bpm")
        }
    }
}
