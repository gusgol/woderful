package me.goldhardt.woderful.presentation.clocks.amrap

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
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
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HeartBroken
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.DataTypeAvailability
import androidx.wear.compose.material.*
import com.google.android.horologist.composables.ExperimentalHorologistComposablesApi
import com.google.android.horologist.composables.ProgressIndicatorSegment
import com.google.android.horologist.composables.SegmentedProgressIndicator
import me.goldhardt.woderful.R
import me.goldhardt.woderful.presentation.clocks.TimeConfiguration
import me.goldhardt.woderful.presentation.theme.WODerfulTheme
import me.goldhardt.woderful.presentation.component.CircleContainer
import me.goldhardt.woderful.presentation.component.RoundsCounter
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch
import me.goldhardt.woderful.data.ServiceState


const val PERMISSION = android.Manifest.permission.BODY_SENSORS

enum class AmrapFlow {
    TIME_CONFIG,
    INSTRUCTIONS,
    CLOCK,
    RESULT
}

const val DEFAULT_AMRAP_TIME = 10

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AmrapScreen(
    viewModel: AmrapViewModel = hiltViewModel()
) {

    val serviceState by viewModel.exerciseServiceState

    var step by remember { mutableStateOf(AmrapFlow.TIME_CONFIG) }
    var time by remember { mutableIntStateOf(DEFAULT_AMRAP_TIME) }

    when (serviceState) {
        is ServiceState.Connected -> {

            val getExerciseServiceState by (serviceState as ServiceState.Connected).exerciseServiceState.collectAsState()
            val exerciseMetrics by mutableStateOf(getExerciseServiceState.exerciseMetrics)

            LaunchedEffect(Unit) {
                viewModel.prepareExercise()
            }

            when (step) {
                AmrapFlow.TIME_CONFIG -> {
                    AmrapConfiguration(
                        viewModel.permissions,
                        onConfirm = { selectedTime ->
                            time = selectedTime
                            step = AmrapFlow.INSTRUCTIONS
                        }
                    )
                }
                AmrapFlow.INSTRUCTIONS -> {
                    AmrapInstructions {
                        viewModel.startExercise()
                        step = AmrapFlow.CLOCK
                    }
                }
                AmrapFlow.CLOCK -> {
                    AmrapClock(
                        timeMin = time,
                        exerciseMetrics = exerciseMetrics,
                        onMinuteChange = {
                            viewModel.markLap()
                        }
                    ) {
                        viewModel.endExercise()
                        step = AmrapFlow.RESULT
                    }
                }
                AmrapFlow.RESULT -> {
                    AmrapFinished()
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

@ExperimentalPermissionsApi
@Composable
fun AmrapConfiguration(
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
fun AmrapClock(
    timeMin: Int,
    exerciseMetrics: DataPointContainer? = null,
    onMinuteChange: () -> Unit = {},
    onFinished: () -> Unit = {},
) {
    val segments = mutableListOf<ProgressIndicatorSegment>()
    repeat(timeMin) {
        segments.add(
            ProgressIndicatorSegment(
                weight = 1f,
                indicatorColor = MaterialTheme.colors.primary,
                trackColor = MaterialTheme.colors.secondary.copy(alpha = 0.1f)
            )
        )
    }

    val totalMs = timeMin * 60 * 1000L
    var remainingMillis by remember { mutableStateOf(-1L) }

    var progress by remember { mutableStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
    )

    val countDownTimer: CountDownTimer = object : CountDownTimer(
        totalMs,
        1000
    ) {
        override fun onTick(millisUntilFinished: Long) {
            remainingMillis = millisUntilFinished
            progress = 1f - (millisUntilFinished.toFloat() / totalMs.toFloat())
        }

        override fun onFinish() {
            onFinished()
        }
    }

    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = LocalContext.current.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        LocalContext.current.getSystemService(Context.VIBRATOR_SERVICE)
                as Vibrator
    }

    var roundCount by remember { mutableStateOf(0) }

    val tempHeartRate = remember { mutableStateOf(0.0) }
    if (exerciseMetrics?.getData(DataType.HEART_RATE_BPM)
            ?.isNotEmpty() == true
    ) tempHeartRate.value =
        exerciseMetrics?.getData(DataType.HEART_RATE_BPM)
            ?.last()?.value!!
    else tempHeartRate.value = tempHeartRate.value

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
                text = "Time",
                color = MaterialTheme.colors.primary,
                style = MaterialTheme.typography.caption1
            )
            Text(
                text = getRemainingTime(remainingMillis) {
                    val vibrationEffect1: VibrationEffect =
                        VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
                    vibrator.cancel()
                    vibrator.vibrate(vibrationEffect1)
                    onMinuteChange()
                },
                style = MaterialTheme.typography.display1
            )
            Row(horizontalArrangement = Arrangement.Center) {
                HeartRateMonitor(hr = tempHeartRate.value)
                Spacer(modifier = Modifier.width(16.dp))
                RoundsCounter(roundCount)
            }
        }
    }

    LaunchedEffect(Unit) {
        countDownTimer.start()
    }
}

@Composable
fun AmrapFinished() {
    Box(
        contentAlignment = Alignment.Center,
    ) {
        Text(text = "Workout finished!")
    }
}

private fun getRemainingTime(
    millis: Long,
    onMinuteChange: () -> Unit = {}
): String {
    val minutes: Long = millis / 1000 / 60
    val seconds: Long = millis / 1000 % 60
    if (seconds == 0L) {
        onMinuteChange()
    }
    return "%02d:%02d".format(minutes, seconds)
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
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .height(IntrinsicSize.Min)

            ) {
                Text(
                    text = "Rounds",
                    style = MaterialTheme.typography.caption3
                )
                Spacer(modifier = Modifier.width(8.dp))
                Divider(
                    color = MaterialTheme.colors.onSurfaceVariant.copy(
                        alpha = 0.4F
                    ),
                    modifier = Modifier
                        .height(8.dp)
                        .width(1.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "4",
                    style = MaterialTheme.typography.caption3
                )
            }
            Text(
                text = "09:32",
                style = MaterialTheme.typography.display1,
                modifier = Modifier.padding(0.dp)
            )
        }
    }
}

internal enum class AmrapInstructionsStep {
    TAP,
    DOUBLE_TAP,
    DONE,
}

@Composable
fun AmrapInstructions(
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
                    delayMillis = 1000
                )
            ),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colors.secondary)
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

@Composable
fun HeartRateMonitor(
    hr: Double = 0.0,
    availability: DataTypeAvailability = DataTypeAvailability.AVAILABLE,
) {
    val icon = when (availability) {
        DataTypeAvailability.AVAILABLE -> Icons.Default.Favorite
        DataTypeAvailability.ACQUIRING -> Icons.Default.MonitorHeart
        DataTypeAvailability.UNAVAILABLE,
        DataTypeAvailability.UNAVAILABLE_DEVICE_OFF_BODY -> Icons.Default.HeartBroken
        else -> Icons.Default.QuestionMark
    }
    val text = if (availability == DataTypeAvailability.AVAILABLE) {
        hr.toInt().toString()
    } else {
        stringResource(id = R.string.title_no_hr_reading)
    }

    CircleContainer(
        minimumRadius = 24.dp,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription =  stringResource(R.string.action_confirm),
                tint = MaterialTheme.colors.primary,
                modifier = Modifier
                    .size(8.dp)
                    .wrapContentSize(align = Alignment.Center)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.title1
            )
        }
    }
}


