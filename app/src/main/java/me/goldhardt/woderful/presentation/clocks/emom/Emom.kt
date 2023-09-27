package me.goldhardt.woderful.presentation.clocks.emom

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Picker
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.rememberPickerState
import me.goldhardt.woderful.R
import me.goldhardt.woderful.data.ServiceState
import me.goldhardt.woderful.presentation.clocks.MinutesAndSecondsTimeConfiguration
import me.goldhardt.woderful.presentation.clocks.amrap.ExerciseViewModel
import me.goldhardt.woderful.presentation.component.LoadingWorkout
import me.goldhardt.woderful.presentation.component.PickerOptionText

/**
 * To do's:
 *
 *      1. Change rest to only take seconds (maybe?)
 *      2. Change seconds picker to use values such as (5, 10, 15, etc)
 *      3. Round texts
 */

internal sealed class EmomFlow {
    object TimeConfig : EmomFlow()
    object RoundsConfig : EmomFlow()
    object RestConfig : EmomFlow()
    object Tracker : EmomFlow()
}

@Composable
fun EmomScreen(
    viewModel: ExerciseViewModel = hiltViewModel()
) {
    // Data state 
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Flow state
    var step: EmomFlow by remember { mutableStateOf(EmomFlow.TimeConfig) }

    // Workout configuration
    var roundDuration by remember { mutableStateOf(0) }
    var roundCount by remember { mutableStateOf(0) }
    var restDuration by remember { mutableStateOf(0) }
    
    when (uiState.serviceState) {
        is ServiceState.Connected -> {

            LaunchedEffect(Unit) {
                viewModel.prepareExercise()
            }

            when (step) {
                EmomFlow.TimeConfig -> EmomTimeConfiguration { minute, second ->
                    roundDuration = minute * 60 + second
                    step = EmomFlow.RoundsConfig
                }
                EmomFlow.RoundsConfig -> EmomRoundsConfiguration {
                    roundCount = it
                    step = EmomFlow.RestConfig
                }
                EmomFlow.RestConfig -> EmomRestConfiguration { minute, second ->
                    restDuration = minute * 60 + second
                    step = EmomFlow.Tracker
                }
                EmomFlow.Tracker -> {
                    Tracker(total = (roundDuration + restDuration) * roundCount)
                }
            }
        }
        ServiceState.Disconnected -> {
            LoadingWorkout()
        }
    }
}

@Composable
fun EmomTimeConfiguration(
    onConfirm: (Int, Int) -> Unit
) {
    MinutesAndSecondsTimeConfiguration(
        title = stringResource(R.string.title_every),
        confirmIcon = Icons.Filled.ArrowForward
    ) { minute, second ->
        onConfirm(minute, second)
    }
}

internal const val MAX_ROUNDS = 99

@Composable
fun EmomRoundsConfiguration(
    onConfirm: (Int) -> Unit
) {
    val state = rememberPickerState(initialNumberOfOptions = MAX_ROUNDS)
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.title_how_many_rounds),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Picker(
                modifier = Modifier.weight(1f),
                state = state,
                contentDescription = stringResource(R.string.title_number_of_rounds),
            ) {
                val currentRound = it + 1
                PickerOptionText(selected = it == state.selectedOption, onSelected = {}, text = "%d".format(currentRound), style = MaterialTheme.typography.display3)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    onConfirm(state.selectedOption + 1)
                },
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowForward,
                    contentDescription =  stringResource(R.string.action_confirm),
                    modifier = Modifier
                        .size(24.dp)
                        .wrapContentSize(align = Alignment.Center)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun EmomRestConfiguration(
    onConfirm: (Int, Int) -> Unit
) {
    MinutesAndSecondsTimeConfiguration(
        title = stringResource(R.string.title_rest),
        confirmIcon = Icons.Filled.Check
    ) { minute, second ->
        onConfirm(minute, second)
    }
}

@Composable
fun Tracker(total: Int) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(text = "$total seconds")
    }
}