package me.goldhardt.woderful.data.model

import me.goldhardt.woderful.R
import kotlin.time.Duration.Companion.seconds

enum class ClockType(
    val displayName: Int,
    val image: Int
) : ClockMilestone {

    AMRAP(R.string.amrap, R.drawable.img_wod_1) {
        override fun getThresholdS(configuration: WorkoutConfiguration): Long {
            return 60.seconds.inWholeMinutes
        }
    },

    EMOM(R.string.emom, R.drawable.img_wod_2) {
        override fun getThresholdS(configuration: WorkoutConfiguration): Long {
            return configuration.activeTimeS + configuration.restTimeS
        }
    },

    TABATA(R.string.tabata, R.drawable.img_wod_3) {
        override fun getThresholdS(configuration: WorkoutConfiguration): Long {
            return configuration.activeTimeS + configuration.restTimeS
        }
    },

    FOR_TIME(R.string.for_time, R.drawable.img_wod_4) {
        override fun getThresholdS(configuration: WorkoutConfiguration): Long {
            return 60.seconds.inWholeMinutes
        }
    },
}