package me.goldhardt.woderful.presentation.clocks.tabata

/**
 * The flow of screens for the Tabata clock type
 */
internal sealed class TabataFlow {
    data object Permissions: TabataFlow()
    data object RoundsConfig : TabataFlow()
    data object Instructions : TabataFlow()
    data object Tracker : TabataFlow()
}