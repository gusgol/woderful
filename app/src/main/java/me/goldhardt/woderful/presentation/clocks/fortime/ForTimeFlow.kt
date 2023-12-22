package me.goldhardt.woderful.presentation.clocks.fortime

import me.goldhardt.woderful.data.model.Workout

internal sealed class ForTimeFlow {

    object Permissions : ForTimeFlow()

    object TimeConfig : ForTimeFlow()

    object Tracker : ForTimeFlow()

    class Summary(
        val workout: Workout
    ) : ForTimeFlow()
}