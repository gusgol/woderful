package me.goldhardt.woderful.presentation.clocks.amrap

import android.annotation.SuppressLint
import android.content.ContentValues
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.composables.ProgressIndicatorSegment
import com.google.android.horologist.composables.SegmentedProgressIndicator
import com.google.android.horologist.health.composables.ActiveDurationText
import kotlinx.coroutines.launch
import me.goldhardt.woderful.R
import me.goldhardt.woderful.data.ServiceState
import me.goldhardt.woderful.data.model.ClockType
import me.goldhardt.woderful.data.model.Workout
import me.goldhardt.woderful.data.model.WorkoutConfiguration
import me.goldhardt.woderful.extensions.formatElapsedTime
import me.goldhardt.woderful.extensions.getElapsedTimeMs
import me.goldhardt.woderful.extensions.toMinutesAndSeconds
import me.goldhardt.woderful.presentation.clocks.ExercisePermissions.DEFAULT_EXERCISE_PERMISSIONS
import me.goldhardt.woderful.presentation.clocks.ExerciseScreenState
import me.goldhardt.woderful.presentation.clocks.ExerciseViewModel
import me.goldhardt.woderful.presentation.clocks.MinutesTimeConfiguration
import me.goldhardt.woderful.presentation.component.HeartRateMonitor
import me.goldhardt.woderful.presentation.component.LoadingWorkout
import me.goldhardt.woderful.presentation.component.RoundsCounter
import me.goldhardt.woderful.presentation.component.StopWorkoutContainer
import me.goldhardt.woderful.presentation.component.SummaryScreen
import me.goldhardt.woderful.presentation.component.defaultSummarySections
import me.goldhardt.woderful.service.ExerciseEvent
import java.util.Date
import kotlin.time.Duration.Companion.seconds

/**
 * To do's:
 *
 *      1. TODO Improve how workout is ended. It's now relying on the view, but ideally it should not need it.
 *      2. TODO Clean up!
 */



/**
 * Flow for the Amrap screen.
 */
internal sealed class AmrapFlow {
    object TimeConfig : AmrapFlow()
    object Instructions : AmrapFlow()
    object Tracker : AmrapFlow()
    data class Summary(
        val workout: Workout,
    ) : AmrapFlow()
}

class AmrapConfiguration(
    activeTimeS: Long
) : WorkoutConfiguration(
    activeTimeS = activeTimeS,
    restTimeS = 0,
    rounds = 1,
)

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AmrapScreen(
    viewModel: ExerciseViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var step: AmrapFlow by remember { mutableStateOf(AmrapFlow.TimeConfig) }
    var config by remember { mutableStateOf(AmrapConfiguration(0L)) }

    if (uiState.isEnded) {
        // TODO act accordingly
    }

    when (uiState.serviceState) {
        is ServiceState.Connected -> {

            LaunchedEffect(Unit) {
                viewModel.prepareExercise()
            }

            when (step) {
                AmrapFlow.TimeConfig -> {
                    AmrapConfiguration(
                        DEFAULT_EXERCISE_PERMISSIONS,
                        onConfirm = { selectedTime ->
                            config = AmrapConfiguration(selectedTime * 60.seconds.inWholeSeconds)
                            step = if (viewModel.hasShownCounterInstructions) {
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
                    ) { workout ->
                        viewModel.endExercise()
                        viewModel.insertWorkout(workout)
                        step = AmrapFlow.Summary(workout)
                    }
                }
                is AmrapFlow.Summary -> {
                    val workout = (step as AmrapFlow.Summary).workout
                    AmrapSummary(
                        duration = workout.durationMs.toMinutesAndSeconds(),
                        roundCount = workout.rounds,
                        calories = workout.calories,
                        avgHeartRate = workout.avgHeartRate?.toInt(),
                        onClose = onClose,
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
internal fun AmrapInstructions(
    modifier: Modifier = Modifier,
    onFinished: () -> Unit,
) {
    var step by remember { mutableStateOf(AmrapInstructionsStep.TAP) }
    var roundCounter by remember { mutableIntStateOf(0) }

    val instructionText = if (step == AmrapInstructionsStep.TAP) {
        stringResource(R.string.action_increase_rounds)
    } else {
        stringResource(R.string.action_decrease_rounds)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        roundCounter++
                        step = AmrapInstructionsStep.DOUBLE_TAP
                    },
                    onDoubleTap = {
                        if (roundCounter > 0) {
                            roundCounter--
                            step = AmrapInstructionsStep.DONE
                        }
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
                text = instructionText,
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
                    text = stringResource(R.string.title_you_are_all_set),
                    style = MaterialTheme.typography.title1,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
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
    permissions: List<String>,
    onConfirm: (Int) -> Unit = {}
) {
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.all { it.value }) {
            Log.d(ContentValues.TAG, "All required permissions granted")
        }
    }

    LaunchedEffect(Unit) {
        launch {
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }

    MinutesTimeConfiguration(
        title = stringResource(id = R.string.title_how_long),
        onConfirm = onConfirm
    )
}


@OptIn(ExperimentalHorologistApi::class, ExperimentalFoundationApi::class)
@Composable
internal fun AmrapTracker(
    durationS: Long,
    uiState: ExerciseScreenState,
    onFinished: (Workout) -> Unit = {},
) {
    val metrics = uiState.exerciseState?.exerciseMetrics

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
    val activeDuration = uiState.exerciseState?.activeDurationCheckpoint
    elapsedTimeMs = activeDuration?.getElapsedTimeMs() ?: elapsedTimeMs

    var progress by remember { mutableFloatStateOf(0F) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
        label = "Progress"
    )
    progress = elapsedTimeMs / (durationS * 1000).toFloat()

    var roundCount by remember { mutableIntStateOf(0) }

    var calories by remember { mutableDoubleStateOf(0.0) }
    metrics?.calories?.let {
        calories = it
    }

    var heartRate by remember { mutableDoubleStateOf(0.0) }
    metrics?.heartRateAverage?.let {
        heartRate = it
    }

    val endWorkout: (Long) -> Unit = { totalTimeMs ->
        onFinished(
            Workout(
                durationMs = totalTimeMs,
                type = ClockType.AMRAP,
                rounds = roundCount,
                calories = calories,
                avgHeartRate = metrics?.heartRateAverage,
                createdAt = Date().time
            )
        )
    }

    if (uiState.exerciseState?.exerciseEvent == ExerciseEvent.TimeEnded) {
        endWorkout(elapsedTimeMs)
    }

    StopWorkoutContainer(
        onConfirm = {
            endWorkout(elapsedTimeMs)
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            roundCount++
                        },
                        onDoubleTap = {
                            if (roundCount > 0) {
                                roundCount--
                            }
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
                    RoundsCounter(roundCount)
                }
            }
        }
    }
}

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun Duration(uiState: ExerciseScreenState) {
    val lastActiveDurationCheckpoint = uiState.exerciseState?.activeDurationCheckpoint
    val exerciseState = uiState.exerciseState?.exerciseState

    if (exerciseState != null && lastActiveDurationCheckpoint != null) {
        ActiveDurationText(
            checkpoint = lastActiveDurationCheckpoint,
            state = uiState.exerciseState.exerciseState
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
    duration: String,
    roundCount: Int,
    calories: Double?,
    avgHeartRate: Int?,
    onClose: () -> Unit,
) {
    SummaryScreen(defaultSummarySections(duration, roundCount, calories, avgHeartRate), onClose)
}

internal enum class AmrapInstructionsStep {
    TAP,
    DOUBLE_TAP,
    DONE,
}


