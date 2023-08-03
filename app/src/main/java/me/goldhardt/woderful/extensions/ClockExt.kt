package me.goldhardt.woderful.extensions

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val MINUTES_AND_SECONDS = "%02d:%02d"
private const val DEFAULT_DATE_FORMAT = "MMM d',' h:mm a"

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