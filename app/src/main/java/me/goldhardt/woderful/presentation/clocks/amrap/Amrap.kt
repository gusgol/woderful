package me.goldhardt.woderful.presentation.clocks.amrap

import android.annotation.SuppressLint
import android.content.ContentValues
import android.os.CountDownTimer
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
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
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.android.horologist.composables.ExperimentalHorologistComposablesApi
import com.google.android.horologist.composables.ProgressIndicatorSegment
import com.google.android.horologist.composables.SegmentedProgressIndicator
import kotlinx.coroutines.launch
import me.goldhardt.woderful.R
import me.goldhardt.woderful.data.ClockType
import me.goldhardt.woderful.data.ServiceState
import me.goldhardt.woderful.data.Workout
import me.goldhardt.woderful.extensions.isRound
import me.goldhardt.woderful.extensions.toMinutesAndSeconds
import me.goldhardt.woderful.presentation.clocks.TimeConfiguration
import me.goldhardt.woderful.presentation.component.HeartRateMonitor
import me.goldhardt.woderful.presentation.component.RoundsCounter
import me.goldhardt.woderful.presentation.component.StopWorkoutContainer
import me.goldhardt.woderful.presentation.component.WorkoutInfoItem
import me.goldhardt.woderful.presentation.theme.WODerfulTheme
import java.util.Date

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

const val DEFAULT_AMRAP_TIME = 10

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AmrapScreen(
    viewModel: AmrapViewModel = hiltViewModel()
) {

    val serviceState by viewModel.exerciseServiceState

    var step: AmrapFlow by remember { mutableStateOf(AmrapFlow.TimeConfig) }
    var time by remember { mutableIntStateOf(DEFAULT_AMRAP_TIME) }

    when (serviceState) {
        is ServiceState.Connected -> {

            val getExerciseServiceState by (serviceState as ServiceState.Connected).exerciseServiceState.collectAsState()
            val exerciseMetrics by mutableStateOf(getExerciseServiceState.exerciseMetrics)

            LaunchedEffect(Unit) {
                viewModel.prepareExercise()
            }

            when (step) {
                AmrapFlow.TimeConfig -> {
                    AmrapConfiguration(
                        viewModel.permissions,
                        onConfirm = { selectedTime ->
                            time = selectedTime
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
                        viewModel.startExercise()
                    }
                    AmrapTracker(
                        timeMin = time,
                        exerciseMetrics = exerciseMetrics,
                        onMinuteChange = {
                            viewModel.markLap()
                        }
                    ) { workout ->
                        viewModel.endExercise()
                        viewModel.insertWorkout(workout)
                        step = AmrapFlow.Summary(workout)
                    }
                }
                is AmrapFlow.Summary -> {
                    val workout = (step as AmrapFlow.Summary).workout
                    AmrapFinished(
                        duration = workout.durationMs.toMinutesAndSeconds(),
                        roundCount = workout.rounds,
                        calories = workout.calories,
                        avgHeartRate = workout.avgHeartRate?.toInt(),
                    )
                }
            }
        }

        ServiceState.Disconnected -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = stringResource(R.string.title_loading),
                    style = MaterialTheme.typography.body1
                )
            }
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
    permissions: Array<String>,
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
            permissionLauncher.launch(permissions)
        }
    }

    TimeConfiguration(
        title = stringResource(id = R.string.title_how_long),
        onConfirm = onConfirm
    )
}


@OptIn(ExperimentalHorologistComposablesApi::class)
@Composable
internal fun AmrapTracker(
    timeMin: Int,
    exerciseMetrics: DataPointContainer? = null,
    onMinuteChange: () -> Unit = {},
    onFinished: (Workout) -> Unit = {},
) {
    val segments = mutableListOf<ProgressIndicatorSegment>()
    repeat(timeMin) {
        segments.add(
            ProgressIndicatorSegment(
                weight = 1f,
                indicatorColor = MaterialTheme.colors.primary,
                trackColor = MaterialTheme.colors.secondary.copy(alpha = 0.2f)
            )
        )
    }

    val totalMs = timeMin * 60 * 1_000L
    var remainingMillis by remember { mutableLongStateOf(-1L) }

    var progress by remember { mutableFloatStateOf(0F) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
        label = "Progress"
    )

    var roundCount by remember { mutableIntStateOf(0) }

    var calories by remember { mutableDoubleStateOf(0.0) }
    exerciseMetrics?.getData(DataType.CALORIES_TOTAL)?.total?.let {
        calories = it
    }

    val endWorkout: (Long) -> Unit = { totalTimeMs ->
        val avgHeartRate = exerciseMetrics?.getData(DataType.HEART_RATE_BPM_STATS)?.average
        onFinished(
            Workout(
                durationMs = totalTimeMs,
                type = ClockType.AMRAP,
                rounds = roundCount,
                calories = calories,
                avgHeartRate = avgHeartRate,
                createdAt = Date().time
            )
        )
    }

    val countDownTimer: CountDownTimer = object : CountDownTimer(
        totalMs,
        1_000L
    ) {
        override fun onTick(millisUntilFinished: Long) {
            remainingMillis = millisUntilFinished
            progress = 1F - (millisUntilFinished.toFloat() / totalMs.toFloat())

            if (remainingMillis.isRound()) {
                onMinuteChange()
            }
        }

        override fun onFinish() {
            endWorkout(totalMs)
        }
    }

    var heartRate by remember { mutableDoubleStateOf(0.0) }
    if (exerciseMetrics?.getData(DataType.HEART_RATE_BPM)?.isNotEmpty() == true) {
        heartRate = exerciseMetrics.getData(DataType.HEART_RATE_BPM).last().value
    }

    StopWorkoutContainer(
        onConfirm = {
            val totalTimeMs = totalMs - remainingMillis
            endWorkout(totalTimeMs)
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
                Text(
                    text = remainingMillis.toMinutesAndSeconds(),
                    style = MaterialTheme.typography.display1
                )
                Row(horizontalArrangement = Arrangement.Center) {
                    HeartRateMonitor(hr = heartRate)
                    Spacer(modifier = Modifier.width(16.dp))
                    RoundsCounter(roundCount)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        countDownTimer.start()
    }
}

@Composable
internal fun AmrapFinished(
    duration: String,
    roundCount: Int,
    calories: Double?,
    avgHeartRate: Int?,
) {
    val listState = rememberScalingLazyListState()
    Box(
        contentAlignment = Alignment.Center,
    ) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.title_workout_finished),
                    style = MaterialTheme.typography.title2,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(4.dp)
                )
            }
            item {
                WorkoutInfoItem(value = duration, text = stringResource(R.string.title_workout_duration))
            }
            item {
                WorkoutInfoItem(value = roundCount.toString(), text = stringResource(R.string.title_rounds))
            }
            if (calories != null) {
                item {
                    WorkoutInfoItem(value = String.format("%.2f", calories), text = stringResource(R.string.title_calories))
                }
            }
            if (avgHeartRate != null && avgHeartRate > 0) {
                item {
                    WorkoutInfoItem(value = avgHeartRate.toString(), text = stringResource(R.string.title_avg_heart_rate))
                }
            }
        }
    }
}
internal enum class AmrapInstructionsStep {
    TAP,
    DOUBLE_TAP,
    DONE,
}

@Preview(
    device = Devices.WEAR_OS_SMALL_ROUND,
    showSystemUi = true,
    backgroundColor = 0xff000000,
    showBackground = true
)
@Composable
fun AmrapClockPreview() {
    WODerfulTheme {
        AmrapFinished("12:00", 12, 120.0, 120)
    }
}


