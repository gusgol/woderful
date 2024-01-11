package me.goldhardt.woderful.presentation.clocks.tabata

import me.goldhardt.woderful.data.model.WorkoutConfiguration
import kotlin.time.Duration.Companion.seconds

class TabataConfiguration(
    rounds: Int,
) : WorkoutConfiguration(
    activeTimeS = 20.seconds.inWholeSeconds,
    restTimeS = 10.seconds.inWholeSeconds,
    rounds = rounds,
)