package me.goldhardt.woderful.presentation.clocks.fortime

internal sealed class ForTimeFlow {
    data object Permissions : ForTimeFlow()
    data object TimeConfig : ForTimeFlow()
    data object Countdown : ForTimeFlow()
    data object Tracker : ForTimeFlow()
}