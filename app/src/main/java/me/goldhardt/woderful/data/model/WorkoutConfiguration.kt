package me.goldhardt.woderful.data.model

/**
 * Represents a workout configuration.
 * @param rounds The number of rounds.
 * @param activeTimeS The duration of the active time in seconds.
 * @param restTimeS The duration of the rest time in seconds.
 */
open class WorkoutConfiguration(
    val activeTimeS: Long,
    val restTimeS: Long,
    val rounds: Int = 1,
) {

    /**
    * The total duration of the workout in seconds.
    */
    fun getTotalDurationS(): Long {
        return rounds * (activeTimeS + restTimeS)
    }
}