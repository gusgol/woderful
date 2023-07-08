package me.goldhardt.woderful.data

/**
 * Represents a workout.
 * @param durationMs The duration of the workout in milliseconds.
 * @param type The type of workout.
 * @param rounds The number of rounds.
 * @param calories The number of calories burned.
 * @param avgHeartRate The average heart rate during the workout.
 */
data class Workout(
    val durationMs: Long,
    val type: ClockType,
    val rounds: Int,
    val calories: Int,
    val avgHeartRate: Int,
)