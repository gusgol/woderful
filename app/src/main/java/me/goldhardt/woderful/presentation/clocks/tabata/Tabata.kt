package me.goldhardt.woderful.presentation.clocks.tabata

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Picker
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.rememberPickerState
import me.goldhardt.woderful.R
import me.goldhardt.woderful.data.ServiceState
import me.goldhardt.woderful.presentation.clocks.ExercisePermissions
import me.goldhardt.woderful.presentation.clocks.ExercisePermissionsLauncher
import me.goldhardt.woderful.presentation.clocks.ExerciseViewModel
import me.goldhardt.woderful.presentation.component.ConfigurationButton
import me.goldhardt.woderful.presentation.component.LoadingWorkout
import me.goldhardt.woderful.presentation.component.PickerOptionText
import me.goldhardt.woderful.presentation.component.RoundText

/**
 *  To do's
 *
 *  1. Maybe extract round picker to its own component??
 */


/**
 * The flow of screens for the Tabata clock type
 */
internal sealed class TabataFlow {
    object Permissions: TabataFlow()
    object RoundsConfig : TabataFlow()
    object WorkConfig : TabataFlow()
    object RestConfig : TabataFlow()
}

@Composable
fun TabataScreen(
    viewModel: ExerciseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Flow state
    val initialScreen = if (ExercisePermissions.hasPermissions(LocalContext.current)) {
        TabataFlow.RoundsConfig
    } else {
        TabataFlow.Permissions
    }

    var step: TabataFlow by remember { mutableStateOf(initialScreen) }

    when (uiState.serviceState) {
        is ServiceState.Connected -> {
            when (step) {
                TabataFlow.Permissions -> {
                    ExercisePermissionsLauncher {
                        step = TabataFlow.RoundsConfig
                    }
                }
                TabataFlow.RoundsConfig -> {
                    TabataRoundsConfig {
                        step = TabataFlow.WorkConfig
                    }
                }
                TabataFlow.WorkConfig -> Text("Work config")
                TabataFlow.RestConfig -> Text("Rest config")
            }
        }
        ServiceState.Disconnected -> {
            LoadingWorkout()
        }
    }
}


private const val TABATA_MAX_ROUNDS = 20

// Tabata's default number of rounds is 4
private const val TABATA_INITIAL_OPTION = 4 - 1

@Composable
internal fun TabataRoundsConfig(
    onConfirm: (Int) -> Unit
) {
    val state = rememberPickerState(
        initialNumberOfOptions = TABATA_MAX_ROUNDS,
        initiallySelectedOption = TABATA_INITIAL_OPTION
    )
    Scaffold(
        timeText = {
            RoundText(stringResource(R.string.title_how_many_rounds))
        }
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            Picker(
                modifier = Modifier.weight(1f),
                state = state,
                contentDescription = stringResource(R.string.title_how_many_rounds),
            ) {
                val currentRound = it + 1
                PickerOptionText(
                    selected = it == state.selectedOption,
                    onSelected = {},
                    text = "%d".format(currentRound),
                    style = MaterialTheme.typography.display3
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            ConfigurationButton(
                onConfirm = {
                    onConfirm(state.selectedOption + 1)
                },
                icon = Icons.Filled.ArrowForward,
                contentDescription = stringResource(R.string.action_confirm),
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}