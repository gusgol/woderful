package me.goldhardt.woderful.presentation.clocks.emom

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.rememberPickerState
import me.goldhardt.woderful.data.ServiceState
import me.goldhardt.woderful.presentation.clocks.MinuteAndSecondPicker
import me.goldhardt.woderful.presentation.clocks.amrap.ExerciseViewModel
import me.goldhardt.woderful.presentation.component.LoadingWorkout

internal sealed class EmomFlow {
    object TimeConfig : EmomFlow()
    object RoundsConfig : EmomFlow()
}

@Composable
fun EmomScreen(
    viewModel: ExerciseViewModel = hiltViewModel()
) {
    // Data state 
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Flow state
    var step: EmomFlow by remember { mutableStateOf(EmomFlow.TimeConfig) } 
    
    when (uiState.serviceState) {
        is ServiceState.Connected -> {

            LaunchedEffect(Unit) {
                viewModel.prepareExercise()
            }

            when (step) {
                EmomFlow.TimeConfig -> EmomTimeConfiguration()
                EmomFlow.RoundsConfig -> {}
            }
        }
        ServiceState.Disconnected -> {
            LoadingWorkout()
        }
    }
}

@Composable
fun EmomTimeConfiguration() {
    val minuteState = rememberPickerState(initialNumberOfOptions = 24)
    val secondState = rememberPickerState(initialNumberOfOptions = 60)
    MinuteAndSecondPicker(minuteState = minuteState, secondState = secondState)
}