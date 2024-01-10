package me.goldhardt.woderful.presentation.clocks.amrap

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.ProgressIndicatorDefaults
import androidx.wear.compose.material.Text
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.composables.ProgressIndicatorSegment
import com.google.android.horologist.composables.SegmentedProgressIndicator
import com.google.android.horologist.health.composables.ActiveDurationText
import me.goldhardt.woderful.R
import me.goldhardt.woderful.data.ServiceState
import me.goldhardt.woderful.data.model.ClockType
import me.goldhardt.woderful.extensions.formatElapsedTime
import me.goldhardt.woderful.extensions.getElapsedTimeMs
import me.goldhardt.woderful.presentation.clocks.ExercisePermissions
import me.goldhardt.woderful.presentation.clocks.ExercisePermissionsLauncher
import me.goldhardt.woderful.presentation.clocks.ExerciseViewModel
import me.goldhardt.woderful.presentation.clocks.MinutesTimeConfiguration
import me.goldhardt.woderful.presentation.clocks.WorkoutUiState
import me.goldhardt.woderful.presentation.component.HeartRateMonitor
import me.goldhardt.woderful.presentation.component.LoadingWorkout
import me.goldhardt.woderful.presentation.component.RoundsCounter
import me.goldhardt.woderful.presentation.component.StopWorkoutContainer
import me.goldhardt.woderful.presentation.component.SummaryScreen
import me.goldhardt.woderful.presentation.component.toDefaultSummarySections
import me.goldhardt.woderful.service.WorkoutState
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AmrapScreen(
    viewModel: ExerciseViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val initialScreen = if (ExercisePermissions.hasPermissions(LocalContext.current)) {
        AmrapFlow.TimeConfig
    } else {
        AmrapFlow.Permissions
    }

    var step: AmrapFlow by remember {
        mutableStateOf(initialScreen)
    }
    var config by remember { mutableStateOf(AmrapConfiguration(0L)) }

    when (uiState.serviceState) {
        is ServiceState.Connected -> {
            LaunchedEffect(Unit) {
                viewModel.prepareExercise()
            }
            if (uiState.isEnded && uiState.workoutState != null) {
                AmrapSummary(
                    workoutState = requireNotNull(uiState.workoutState),
                    onClose = onClose
                )
            } else {
                when (step) {
                    AmrapFlow.Permissions -> {
                        ExercisePermissionsLauncher {
                            step = AmrapFlow.TimeConfig
                        }
                    }

                    AmrapFlow.TimeConfig -> {
                        AmrapConfiguration(
                            onConfirm = { selectedTime ->
                                config =
                                    AmrapConfiguration(selectedTime * 60.seconds.inWholeSeconds)
                                step = if (false) {
                                    AmrapFlow.Tracker
                                } else {
                                    AmrapFlow.Instructions
                                }
                            }
                        )
                    }

                    AmrapFlow.Instructions -> {
                        AmrapInstructions {
                            /**
                             * We need to mark the instructions as shown before starting the exercise.
                             * This is prevent this screen from being shown again when the user returns to it.
                             */
                            viewModel.onCounterInstructionsShown()

                            step = AmrapFlow.Tracker
                        }
                    }

                    AmrapFlow.Tracker -> {
                        LaunchedEffect(Unit) {
                            viewModel.startExercise(ClockType.AMRAP, config)
                        }
                        AmrapTracker(
                            durationS = config.getTotalDurationS(),
                            uiState = uiState,
                            onRoundIncreased = {
                                viewModel.markLap()
                            },
                        ) {
                            uiState.workoutState?.let {
                                viewModel.endWorkout(it)
                            }
                        }
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
internal fun AmrapInstructions(
    modifier: Modifier = Modifier,
    onFinished: () -> Unit,
) {
    var step by remember { mutableStateOf(AmrapInstructionsStep.DOUBLE_TAP) }
    var roundCounter by remember { mutableIntStateOf(0) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        roundCounter++
                        step = AmrapInstructionsStep.DONE
                    }
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.title_instructions),
                style = MaterialTheme.typography.caption2,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
            Text(
                text = stringResource(R.string.action_increase_rounds),
                style = MaterialTheme.typography.body1,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
            RoundsCounter(roundCounter)
        }
        AnimatedVisibility(
            visible = step == AmrapInstructionsStep.DONE,
            enter = fadeIn(
                animationSpec = tween(
                    durationMillis = 500,
                    delayMillis = 750
                )
            ),
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colors.background)
                    .padding(32.dp)
            ) {
                Text(
                    text = stringResource(R.string.title_that_is_right),
                    style = MaterialTheme.typography.title1,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                )
                Text(
                    text = stringResource(R.string.title_you_are_all_set),
                    style = MaterialTheme.typography.body1,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Chip(
                    onClick = onFinished,
                    label = {
                        Text(
                            text = stringResource(R.string.action_lets_start),
                            maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                    },
                )
            }
        }
    }
}

@ExperimentalPermissionsApi
@Composable
internal fun AmrapConfiguration(
    onConfirm: (Int) -> Unit = {}
) {
    MinutesTimeConfiguration(
        title = stringResource(id = R.string.title_how_long),
        onConfirm = onConfirm
    )
}


@OptIn(ExperimentalHorologistApi::class, ExperimentalFoundationApi::class)
@Composable
internal fun AmrapTracker(
    durationS: Long,
    uiState: WorkoutUiState,
    onRoundIncreased: () -> Unit = {},
    onEndWorkout: () -> Unit,
) {
    val metrics = uiState.workoutState?.workoutMetrics

    val segments = mutableListOf<ProgressIndicatorSegment>()
    repeat(durationS.toInt() / 60) {
        segments.add(
            ProgressIndicatorSegment(
                weight = 1f,
                indicatorColor = MaterialTheme.colors.primary,
                trackColor = MaterialTheme.colors.secondary.copy(alpha = 0.2f)
            )
        )
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
    progress = elapsedTimeMs / (durationS * 1000).toFloat()

    var calories by remember { mutableDoubleStateOf(0.0) }
    metrics?.calories?.let {
        calories = it
    }

    var heartRate by remember { mutableDoubleStateOf(0.0) }
    metrics?.heartRateAverage?.let {
        heartRate = it
    }

    StopWorkoutContainer(
        onConfirm = onEndWorkout,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            onRoundIncreased()
                        }
                    )
                }
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
                    text = stringResource(R.string.title_time),
                    color = MaterialTheme.colors.primary,
                    style = MaterialTheme.typography.caption1
                )
                Duration(uiState = uiState)
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    HeartRateMonitor(hr = heartRate)
                    Spacer(modifier = Modifier.width(16.dp))
                    RoundsCounter(uiState.workoutState?.exerciseLaps ?: 0)
                }
            }
        }
    }
}

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun Duration(uiState: WorkoutUiState) {
    val lastActiveDurationCheckpoint = uiState.workoutState?.activeDurationCheckpoint
    val exerciseState = uiState.workoutState?.exerciseState

    if (exerciseState != null && lastActiveDurationCheckpoint != null) {
        ActiveDurationText(
            checkpoint = lastActiveDurationCheckpoint,
            state = uiState.workoutState.exerciseState
        ) {
            Text(
                text = formatElapsedTime(it),
                style = MaterialTheme.typography.display1
            )
        }
    } else {
        Text(text = "--", style = MaterialTheme.typography.display1)
    }
}

@Composable
internal fun AmrapSummary(
    workoutState: WorkoutState,
    onClose: () -> Unit,
) {
    SummaryScreen(workoutState.toDefaultSummarySections(), onClose)
}

internal enum class AmrapInstructionsStep {
    DOUBLE_TAP,
    DONE,
}


