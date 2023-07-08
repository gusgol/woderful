package me.goldhardt.woderful.extensions

private const val MINUTES_AND_SECONDS = "%02d:%02d"

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