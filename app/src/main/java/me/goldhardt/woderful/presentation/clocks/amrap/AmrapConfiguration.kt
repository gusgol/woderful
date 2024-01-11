package me.goldhardt.woderful.presentation.clocks.amrap

import me.goldhardt.woderful.data.model.WorkoutConfiguration

internal class AmrapConfiguration(
    activeTimeS: Long
) : WorkoutConfiguration(
    activeTimeS = activeTimeS,
    restTimeS = 0,
    rounds = 1,
)