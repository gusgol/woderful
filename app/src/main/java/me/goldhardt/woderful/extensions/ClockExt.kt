package me.goldhardt.woderful.extensions

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.wear.compose.material.MaterialTheme
import java.text.SimpleDateFormat
import java.time.Duration
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

private const val MINUTES_AND_SECONDS = "%02d:%02d"
private const val DEFAULT_DATE_FORMAT = "MMM d',' h:mm a"
private val MINUTES_PER_HOUR = TimeUnit.HOURS.toMinutes(1)
private val SECONDS_PER_MINUTE = TimeUnit.MINUTES.toSeconds(1)

@Composable
fun formatElapsedTime(
    elapsedDuration: Duration?,
    countDownFrom: Duration? = null,
) = buildAnnotatedString {
    if (elapsedDuration == null) {
        append("--")
    } else {
        val displayDuration = if (countDownFrom != null) {
            countDownFrom - elapsedDuration
        } else {
            elapsedDuration
        }

        val hours = displayDuration.toHours()
        if (hours > 0) {
            append(hours.toString())
            withStyle(style = MaterialTheme.typography.caption3.toSpanStyle()) {
                append(":")
            }
        }
        val minutes = displayDuration.toMinutes() % MINUTES_PER_HOUR
        val seconds = displayDuration.seconds % SECONDS_PER_MINUTE
        append(MINUTES_AND_SECONDS.format(minutes, seconds))
    }
}

/**
 * Converts a number of milliseconds to a string in the format "mm:ss".
 */
fun Long.toMinutesAndSeconds(): String {
    val minutes = this / 1000 / 60
    val seconds = this / 1000 % 60
    return MINUTES_AND_SECONDS.format(minutes, seconds)
}

/**
 * Returns true if the number is a round number, i.e. it is divisible by 60.
 */
fun Long.isRound(): Boolean =
    this / 1000 % 60 == 0L

/**
 * Converts a number of milliseconds to a string in the format "mm:ss" if the number is a round
 */
fun Long.formatDate(): String {
    val date = Date(this)
    val dateFormat = SimpleDateFormat(DEFAULT_DATE_FORMAT, Locale.getDefault())
    return dateFormat.format(date)
}