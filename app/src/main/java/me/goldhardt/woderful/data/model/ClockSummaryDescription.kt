package me.goldhardt.woderful.data.model

import androidx.compose.runtime.Composable

fun interface ClockSummaryDetails {

    fun getDetailsComposable(
        properties: Map<String, Any>
    ): @Composable () -> Unit
}