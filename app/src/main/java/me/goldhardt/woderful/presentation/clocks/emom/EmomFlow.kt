package me.goldhardt.woderful.presentation.clocks.emom

/**
 * Represents the flow of the Emom workout configuration.
 */
internal sealed class EmomFlow {
    data object Permissions : EmomFlow()
    data object TimeConfig : EmomFlow()
    data object RoundsConfig : EmomFlow()
    data object RestConfig : EmomFlow()
    data object Tracker : EmomFlow()
}