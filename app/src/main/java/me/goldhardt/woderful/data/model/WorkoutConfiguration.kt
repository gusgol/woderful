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

    /**
     * Returns whether the given time is in the active interval.
     */
    fun isInActiveInterval(elapseTimeS: Long): Boolean {
        val roundTime = activeTimeS + restTimeS
        val timeAtRound = elapseTimeS % roundTime
        return timeAtRound < activeTimeS
    }
}