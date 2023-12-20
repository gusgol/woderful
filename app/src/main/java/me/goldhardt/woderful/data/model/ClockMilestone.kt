package me.goldhardt.woderful.data.model

interface ClockMilestone {

    /**
     * The duration of the milestone in seconds.
     */
    fun getThresholdS(configuration: WorkoutConfiguration): Long
}