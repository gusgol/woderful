package me.goldhardt.woderful.presentation.clocks.amrap

/**
 * Flow for the Amrap screen.
 */
internal sealed class AmrapFlow {
    data object Permissions: AmrapFlow()
    data object TimeConfig : AmrapFlow()
    data object Instructions : AmrapFlow()
    data object Tracker : AmrapFlow()
}