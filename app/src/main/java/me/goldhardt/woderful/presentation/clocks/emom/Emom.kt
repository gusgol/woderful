@file:OptIn(ExperimentalHorologistApi::class)

package me.goldhardt.woderful.presentation.clocks.emom

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.Card
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
import me.goldhardt.woderful.data.model.ClockProperties
import me.goldhardt.woderful.data.model.ClockType
import me.goldhardt.woderful.data.model.WorkoutConfiguration
import me.goldhardt.woderful.extensions.getElapsedTimeMs
import me.goldhardt.woderful.extensions.toMinutesAndSeconds
import me.goldhardt.woderful.extensions.toSeconds
import me.goldhardt.woderful.presentation.clocks.ExercisePermissions
import me.goldhardt.woderful.presentation.clocks.ExercisePermissionsLauncher
import me.goldhardt.woderful.presentation.clocks.ExerciseViewModel
import me.goldhardt.woderful.presentation.clocks.FakeExerciseScreenState
import me.goldhardt.woderful.presentation.clocks.MinutesAndSecondsTimeConfiguration
import me.goldhardt.woderful.presentation.clocks.WorkoutUiState
import me.goldhardt.woderful.presentation.clocks.amrap.Duration
import me.goldhardt.woderful.presentation.component.CircleContainer
import me.goldhardt.woderful.presentation.component.ConfigurationButton
import me.goldhardt.woderful.presentation.component.HeartRateMonitor
import me.goldhardt.woderful.presentation.component.LoadingWorkout
import me.goldhardt.woderful.presentation.component.PickerOptionText
import me.goldhardt.woderful.presentation.component.RoundText
import me.goldhardt.woderful.presentation.component.StopWorkoutContainer
import me.goldhardt.woderful.presentation.component.SummaryScreen
import me.goldhardt.woderful.presentation.component.toDefaultSummarySections
import me.goldhardt.woderful.presentation.theme.WODerfulTheme
import me.goldhardt.woderful.service.WorkoutState
import kotlin.time.Duration.Companion.seconds



/**
 * Converts a [WorkoutConfiguration] to a map of properties.
 */
fun WorkoutConfiguration.toProperties(): Map<String, Any> {
    return mapOf(
        ClockProperties.EMOM.CONFIG_ACTIVE_TIME_S to activeTimeS,
        ClockProperties.EMOM.CONFIG_ROUNDS to rounds,
        ClockProperties.EMOM.CONFIG_REST_TIME to restTimeS,
    )
}

@Composable
fun EmomScreen(
    viewModel: ExerciseViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
) {
    // Data state 
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Flow state
    val initialScreen = if (ExercisePermissions.hasPermissions(LocalContext.current)) {
        EmomFlow.TimeConfig
    } else {
        EmomFlow.Permissions
    }

    var step: EmomFlow by remember { mutableStateOf(initialScreen) }

    // Workout configuration
    var roundDurationS by remember { mutableStateOf(0L) }
    var restDurationS by remember { mutableStateOf(0L) }
    var roundCount by remember { mutableStateOf(0) }
    var configuration by remember { mutableStateOf(WorkoutConfiguration(0, 0, 0)) }

    when (uiState.serviceState) {
        is ServiceState.Connected -> {
            LaunchedEffect(Unit) {
                viewModel.prepareExercise()
            }

            if (uiState.isEnded && uiState.workoutState != null) {
                EmomSummary(
                    configuration = configuration,
                    workoutState = requireNotNull(uiState.workoutState),
                    onClose = onClose,
                )
            } else {
                when (step) {
                    EmomFlow.Permissions -> {
                        ExercisePermissionsLauncher {
                            step = EmomFlow.TimeConfig
                        }
                    }
                    EmomFlow.TimeConfig -> EmomTimeConfiguration { minute, second ->
                        roundDurationS = (minute * 60.seconds.inWholeSeconds) + second
                        step = EmomFlow.RoundsConfig
                    }
                    EmomFlow.RoundsConfig -> EmomRoundsConfiguration {
                        roundCount = it
                        step = EmomFlow.RestConfig
                    }
                    EmomFlow.RestConfig -> EmomRestConfiguration { minute, second ->
                        restDurationS = (minute * 60.seconds.inWholeSeconds) + second
                        step = EmomFlow.Tracker
                    }
                    EmomFlow.Tracker -> {
                        configuration = WorkoutConfiguration(roundDurationS, restDurationS, roundCount)
                        LaunchedEffect(Unit) {
                            viewModel.startExercise(ClockType.EMOM, configuration)
                        }
                        EmomTracker(
                            configuration,
                            uiState = uiState,
                            onFinished = {
                                uiState.workoutState?.let {
                                    viewModel.endWorkout(it)
                                }
                            }
                        )
                    }
                }
            }

        }
        ServiceState.Disconnected -> {
            LoadingWorkout()
        }
    }
}

@Composable
internal fun EmomTimeConfiguration(
    onConfirm: (Int, Int) -> Unit
) {
    MinutesAndSecondsTimeConfiguration(
        title = stringResource(R.string.title_every),
        confirmIcon = Icons.AutoMirrored.Filled.ArrowForward,
        confirmButtonEnabledCondition = { minute, second ->
             minute > 0 || second > 0
        }
    ) { minute, second ->
        onConfirm(minute, second)
    }
}

internal const val MAX_ROUNDS = 99

@Composable
internal fun EmomRoundsConfiguration(
    onConfirm: (Int) -> Unit
) {
    val state = rememberPickerState(initialNumberOfOptions = MAX_ROUNDS)
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
                icon = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = stringResource(R.string.action_confirm),
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
internal fun EmomRestConfiguration(
    onConfirm: (Int, Int) -> Unit
) {
    MinutesAndSecondsTimeConfiguration(
        title = stringResource(R.string.title_rest_question),
        confirmIcon = Icons.Filled.Check
    ) { minute, second ->
        onConfirm(minute, second)
    }
}

@OptIn(ExperimentalHorologistApi::class, ExperimentalFoundationApi::class)
@Composable
internal fun EmomTracker(
    configuration: WorkoutConfiguration,
    uiState: WorkoutUiState,
    onFinished: () -> Unit,
) {
    val metrics = uiState.workoutState?.workoutMetrics

    val totalRoundTimeS = (configuration.activeTimeS + configuration.restTimeS).toFloat()
    val activeSegmentWeight = configuration.activeTimeS / totalRoundTimeS
    val restSegmentWeight = configuration.restTimeS / totalRoundTimeS

    val elapsedSegmentColor = MaterialTheme.colors.surface.copy(alpha = 0.8f)

    val segments = mutableListOf<ProgressIndicatorSegment>()
    repeat(configuration.rounds) {
        segments.add(
            ProgressIndicatorSegment(
                weight = activeSegmentWeight,
                indicatorColor = elapsedSegmentColor,
                trackColor = MaterialTheme.colors.primary
            )
        )
        if (configuration.restTimeS > 0) {
            segments.add(
                ProgressIndicatorSegment(
                    weight = restSegmentWeight,
                    indicatorColor = elapsedSegmentColor,
                    trackColor = MaterialTheme.colors.error
                )
            )
        }
    }

    var elapsedTimeMs by remember { mutableLongStateOf(0L) }
    val activeDuration = uiState.workoutState?.activeDurationCheckpoint
    elapsedTimeMs = activeDuration?.getElapsedTimeMs() ?: elapsedTimeMs

    var progress by remember { mutableFloatStateOf(0F) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
        label = "Progress"
    )
    progress = elapsedTimeMs / (configuration.getTotalDurationS() * 1_000F)

    var heartRate by remember { mutableDoubleStateOf(0.0) }
    metrics?.heartRateAverage?.let {
        heartRate = it
    }

    var isInActiveInterval by remember { mutableStateOf(true) }
    isInActiveInterval = configuration.isInActiveInterval(elapsedTimeMs.toSeconds())

    val completedRounds = (progress * configuration.rounds).toInt()

    StopWorkoutContainer(
        onConfirm = onFinished,
    ) {
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
                    RoundMonitor( completedRounds + 1, configuration.rounds)
                }
            }
        }
    }
}

@Composable
internal fun EmomSummary(
    configuration: WorkoutConfiguration,
    workoutState: WorkoutState,
    onClose: () -> Unit = {},
) {
    val roundsDescription =
        "${configuration.rounds} ${pluralStringResource(id = R.plurals.message_rounds, count = configuration.rounds)}"
    val configurationSummary = stringResource(
        R.string.title_emom_summary_desc,
        configuration.activeTimeS.toInt().toMinutesAndSeconds(),
        roundsDescription,
        configuration.restTimeS.toInt().toMinutesAndSeconds()
    )
    SummaryScreen(
        workoutState.toDefaultSummarySections(),
        onClose = onClose,
        descriptionSlot = {
            Card(
                onClick = {},
            ) {
                Column {
                    Text(
                        text = stringResource(id = R.string.emom),
                        style = MaterialTheme.typography.caption1,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colors.primary
                    )
                    Text(
                        text = configurationSummary,
                        style = MaterialTheme.typography.body2,
                        modifier = Modifier.padding(4.dp),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        },
    )
}

@Composable
fun RoundMonitor(
    currentRound: Int,
    totalRounds: Int,
) {
    val text = "$currentRound/$totalRounds"
    val textStyle = MaterialTheme.typography.title1
    val radius = 28.dp
    CircleContainer(
        radius = radius,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Round",
                color = MaterialTheme.colors.primary,
                style = MaterialTheme.typography.caption2
            )
            Text(
                text = text,
                style = textStyle,
                modifier = Modifier.width(radius * 2),
                textAlign = TextAlign.Center,
                fontSize = if (text.count() > 5) {
                    textStyle.fontSize * 0.6
                } else if (text.count() > 4){
                    textStyle.fontSize * 0.7
                } else {
                    textStyle.fontSize
                }
            )
        }
    }
}

@WearPreviewDevices
@Composable
fun EmomTrackerPreview() {
    WODerfulTheme {
        EmomTracker(
            configuration = WorkoutConfiguration(90, 4, 30),
            uiState = FakeExerciseScreenState(),
            onFinished = {}
        )
    }
}
