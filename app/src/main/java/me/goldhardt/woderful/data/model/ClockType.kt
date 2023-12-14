package me.goldhardt.woderful.data.model

import me.goldhardt.woderful.R

enum class ClockType(
    val displayName: Int,
    val image: Int
) {
    AMRAP(R.string.amrap, R.drawable.img_wod_1),
    EMOM(R.string.emom, R.drawable.img_wod_2),
    TABATA(R.string.tabata, R.drawable.img_wod_3),
    FOR_TIME(R.string.for_time, R.drawable.img_wod_4),
}