package me.goldhardt.woderful.presentation.clocks.tabata

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Picker
import androidx.wear.compose.material.ProgressIndicatorDefaults
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.rememberPickerState
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.composables.ProgressIndicatorSegment
import com.google.android.horologist.composables.SegmentedProgressIndicator
import me.goldhardt.woderful.R
import me.goldhardt.woderful.data.ServiceState
import me.goldhardt.woderful.extensions.getElapsedTimeMs
import me.goldhardt.woderful.presentation.clocks.ExercisePermissions
import me.goldhardt.woderful.presentation.clocks.ExercisePermissionsLauncher
import me.goldhardt.woderful.presentation.clocks.ExerciseScreenState
import me.goldhardt.woderful.presentation.clocks.ExerciseViewModel
import me.goldhardt.woderful.presentation.clocks.FakeExerciseScreenState
import me.goldhardt.woderful.presentation.clocks.amrap.Duration
import me.goldhardt.woderful.presentation.clocks.emom.RoundMonitor
import me.goldhardt.woderful.presentation.component.ConfigurationButton
import me.goldhardt.woderful.presentation.component.HeartRateMonitor
import me.goldhardt.woderful.presentation.component.LoadingWorkout
import me.goldhardt.woderful.presentation.component.PickerOptionText
import me.goldhardt.woderful.presentation.component.RoundText
import me.goldhardt.woderful.presentation.component.StopWorkoutContainer
import me.goldhardt.woderful.presentation.theme.WODerfulTheme

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
    object Instructions : TabataFlow()
    object Tracker : TabataFlow()
}

internal object TabataConfiguration {
    const val WORK_TIME_MS = 20_000
    const val REST_TIME_MS = 10_000
    const val MAX_ROUNDS = 20
    const val INITIAL_OPTION = 4 - 1
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

    var rounds by remember { mutableStateOf(0) }

    var step: TabataFlow by remember { mutableStateOf(initialScreen) }

    when (uiState.serviceState) {
        is ServiceState.Connected -> {
            LaunchedEffect(Unit) {
                viewModel.prepareExercise()
            }

            when (step) {
                TabataFlow.Permissions -> {
                    ExercisePermissionsLauncher {
                        step = TabataFlow.RoundsConfig
                    }
                }
                TabataFlow.RoundsConfig -> {
                    TabataRoundsConfig { selectedRounds ->
                        rounds = selectedRounds
                        step = TabataFlow.Instructions
                    }
                }
                TabataFlow.Instructions -> {
                    TabataInstructions() {
                        step = TabataFlow.Tracker
                    }
                }
                TabataFlow.Tracker -> {
                    LaunchedEffect(Unit) {
                        val totalTimeS = rounds * ((TabataConfiguration.WORK_TIME_MS + TabataConfiguration.REST_TIME_MS) / 1000)
                        viewModel.startExercise(totalTimeS.toLong())
                    }
                    TabataTracker(
                        rounds = rounds,
                        uiState = uiState
                    )
                }
            }
        }
        ServiceState.Disconnected -> {
            LoadingWorkout()
        }
    }
}

@Composable
internal fun TabataRoundsConfig(
    onConfirm: (Int) -> Unit
) {
    val state = rememberPickerState(
        initialNumberOfOptions = TabataConfiguration.MAX_ROUNDS,
        initiallySelectedOption = TabataConfiguration.INITIAL_OPTION,
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

@Composable
fun TabataInstructions(
    modifier: Modifier = Modifier,
    onFinished: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            stringResource(R.string.title_tabata_instructions),
            style = MaterialTheme.typography.title2,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(12.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Chip(
            onClick = onFinished,
            label = {
                Text(
                    text = stringResource(R.string.title_are_you_ready),
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            },
        )
    }
}

@OptIn(ExperimentalHorologistApi::class)
@Composable
internal fun TabataTracker(
    rounds: Int,
    uiState: ExerciseScreenState,
) {
    val metrics = uiState.exerciseState?.exerciseMetrics

    val totalRoundTimeMs = (TabataConfiguration.REST_TIME_MS + TabataConfiguration.WORK_TIME_MS).toFloat()
    val activeSegmentWeight = TabataConfiguration.WORK_TIME_MS / totalRoundTimeMs
    val restSegmentWeight = TabataConfiguration.REST_TIME_MS / totalRoundTimeMs

    val elapsedSegmentColor = MaterialTheme.colors.surface.copy(alpha = 0.8f)

    val segments = mutableListOf<ProgressIndicatorSegment>()
    repeat(rounds) {
        segments.add(
            ProgressIndicatorSegment(
                weight = activeSegmentWeight,
                indicatorColor = elapsedSegmentColor,
                trackColor = MaterialTheme.colors.secondary
            )
        )
        segments.add(
            ProgressIndicatorSegment(
                weight = restSegmentWeight,
                indicatorColor = elapsedSegmentColor,
                trackColor = MaterialTheme.colors.error
            )
        )
    }

    var elapsedTimeMs by remember { mutableLongStateOf(0L) }
    val activeDuration = uiState.exerciseState?.activeDurationCheckpoint
    elapsedTimeMs = activeDuration?.getElapsedTimeMs() ?: elapsedTimeMs

    var progress by remember { mutableFloatStateOf(0F) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
        label = "Progress"
    )
    progress = elapsedTimeMs / (totalRoundTimeMs * rounds)

    var heartRate by remember { mutableDoubleStateOf(0.0) }
    metrics?.heartRateAverage?.let {
        heartRate = it
    }

    var isInActiveInterval by remember { mutableStateOf(true) }
    isInActiveInterval = isInActiveInterval(
        totalRoundTimeMs = totalRoundTimeMs,
        activeRoundDuration = TabataConfiguration.WORK_TIME_MS.toLong(),
        elapsedTimeMs = elapsedTimeMs
    )

    val completedRounds = (progress * rounds).toInt()

    StopWorkoutContainer(onConfirm = {}) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            SegmentedProgressIndicator(
                trackSegments = segments,
                progress = animatedProgress,
                paddingAngle = 2f,
                strokeWidth = 8.dp,
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = if (isInActiveInterval) {
                        stringResource(R.string.title_time)
                    } else {
                        stringResource(R.string.title_rest)
                    },
                    color = if (isInActiveInterval) {
                        MaterialTheme.colors.primary
                    } else {
                        MaterialTheme.colors.error
                    },
                    style = if (isInActiveInterval) {
                        MaterialTheme.typography.caption1
                    } else {
                        MaterialTheme.typography.caption1.copy(fontWeight = FontWeight.ExtraBold)
                    },
                )
                Duration(uiState = uiState)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    HeartRateMonitor(hr = heartRate)
                    Spacer(modifier = Modifier.width(8.dp))
                    RoundMonitor( completedRounds + 1, rounds)
                }
            }
        }
    }
}

private fun isInActiveInterval(
    totalRoundTimeMs: Float,
    activeRoundDuration: Long,
    elapsedTimeMs: Long): Boolean {
    val roundTimeS = elapsedTimeMs % totalRoundTimeMs
    return roundTimeS < activeRoundDuration
}


@WearPreviewDevices
@Composable
fun TabataTrackerPreview() {
    WODerfulTheme {
        TabataTracker(
            rounds = 5,
            uiState = FakeExerciseScreenState(),
        )
    }
}

@WearPreviewDevices
@Composable
fun TabataInstructionsPreview() {
    WODerfulTheme {
        TabataInstructions(onFinished = {})
    }
}