package me.goldhardt.woderful.presentation.clocks.fortime

import me.goldhardt.woderful.data.model.WorkoutConfiguration

class ForTimeConfiguration(
    activeTimeS: Long,
) : WorkoutConfiguration(
    activeTimeS = activeTimeS,
    restTimeS = 0,
    rounds = 1,
)