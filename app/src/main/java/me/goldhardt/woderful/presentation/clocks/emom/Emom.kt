@file:OptIn(ExperimentalHorologistApi::class)

package me.goldhardt.woderful.presentation.clocks.emom

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.filled.ArrowForward
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
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
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
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.composables.ProgressIndicatorSegment
import com.google.android.horologist.composables.SegmentedProgressIndicator
import me.goldhardt.woderful.R
import me.goldhardt.woderful.data.ClockType
import me.goldhardt.woderful.data.ServiceState
import me.goldhardt.woderful.data.Workout
import me.goldhardt.woderful.extensions.getElapsedTimeMs
import me.goldhardt.woderful.extensions.toMinutesAndSeconds
import me.goldhardt.woderful.presentation.clocks.ExercisePermissions
import me.goldhardt.woderful.presentation.clocks.ExercisePermissionsLauncher
import me.goldhardt.woderful.presentation.clocks.ExerciseScreenState
import me.goldhardt.woderful.presentation.clocks.ExerciseViewModel
import me.goldhardt.woderful.presentation.clocks.MinutesAndSecondsTimeConfiguration
import me.goldhardt.woderful.presentation.clocks.amrap.Duration
import me.goldhardt.woderful.presentation.component.CircleContainer
import me.goldhardt.woderful.presentation.component.ConfigurationButton
import me.goldhardt.woderful.presentation.component.HeartRateMonitor
import me.goldhardt.woderful.presentation.component.LoadingWorkout
import me.goldhardt.woderful.presentation.component.PickerOptionText
import me.goldhardt.woderful.presentation.component.RoundText
import me.goldhardt.woderful.presentation.component.StopWorkoutContainer
import me.goldhardt.woderful.presentation.component.SummaryScreen
import me.goldhardt.woderful.presentation.component.defaultSummarySections
import me.goldhardt.woderful.presentation.theme.WODerfulTheme
import me.goldhardt.woderful.service.ExerciseEvent
import java.util.Date

/**
 * To do's
 *
 * 1. TODO Save configuration to database
 * 2. TODO Fix size of Monitors (CircleContainer)
 */

/**
 * Represents the flow of the Emom workout configuration.
 */
internal sealed class EmomFlow {
    object Permissions: EmomFlow()
    object TimeConfig : EmomFlow()
    object RoundsConfig : EmomFlow()
    object RestConfig : EmomFlow()
    object Tracker : EmomFlow()
    data class Summary(
        val workout: Workout,
        val configuration: EmomConfiguration,
    ) : EmomFlow()
}

/**
 * Represents the configuration of an Emom workout.
 * @param activeDurationS The duration of each round in seconds.
 * @param roundCount The number of rounds.
 * @param restDurationS The duration of the rest period in seconds.
 */
data class EmomConfiguration(
    val activeDurationS: Int,
    val roundCount: Int,
    val restDurationS: Int,
)

@Composable
fun EmomScreen(
    viewModel: ExerciseViewModel = hiltViewModel()
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
    var roundDuration by remember { mutableStateOf(0) }
    var roundCount by remember { mutableStateOf(0) }
    var restDuration by remember { mutableStateOf(0) }

    when (uiState.serviceState) {
        is ServiceState.Connected -> {

            LaunchedEffect(Unit) {
                viewModel.prepareExercise()
            }

            when (step) {
                EmomFlow.Permissions -> {
                    ExercisePermissionsLauncher {
                        step = EmomFlow.TimeConfig
                    }
                }
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
                    val totalDurationS = (roundDuration + restDuration) * roundCount
                    val emomConfiguration = EmomConfiguration(
                        activeDurationS = roundDuration,
                        roundCount = roundCount,
                        restDurationS = restDuration,
                    )
                    LaunchedEffect(Unit) {
                        viewModel.startExercise(totalDurationS.toLong())
                    }
                    EmomTracker(
                        emomConfiguration,
                        uiState = uiState,
                        onFinished = { workout ->
                            with (viewModel) {
                                endExercise()
                                insertWorkout(workout)
                            }
                            step = EmomFlow.Summary(workout, emomConfiguration)
                        }
                    )
                }
                is EmomFlow.Summary -> {
                    val summary = (step as EmomFlow.Summary)
                    val workout = summary.workout
                    EmomSummary(
                        duration = workout.durationMs.toMinutesAndSeconds(),
                        roundCount = workout.rounds,
                        calories = workout.calories,
                        avgHeartRate = workout.avgHeartRate?.toInt(),
                        configuration = summary.configuration,
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
internal fun EmomTimeConfiguration(
    onConfirm: (Int, Int) -> Unit
) {
    MinutesAndSecondsTimeConfiguration(
        title = stringResource(R.string.title_every),
        confirmIcon = Icons.Filled.ArrowForward,
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
                icon = Icons.Filled.ArrowForward,
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

@OptIn(ExperimentalHorologistApi::class)
@Composable
internal fun EmomTracker(
    emomConfiguration: EmomConfiguration,
    uiState: ExerciseScreenState,
    onFinished: (Workout) -> Unit,
) {
    val metrics = uiState.exerciseState?.exerciseMetrics

    val totalRoundTimeS = (emomConfiguration.activeDurationS + emomConfiguration.restDurationS).toFloat()
    val activeSegmentWeight = emomConfiguration.activeDurationS / totalRoundTimeS
    val restSegmentWeight = emomConfiguration.restDurationS / totalRoundTimeS

    val elapsedSegmentColor = MaterialTheme.colors.surface.copy(alpha = 0.8f)

    val segments = mutableListOf<ProgressIndicatorSegment>()
    repeat(emomConfiguration.roundCount) {
        segments.add(
            ProgressIndicatorSegment(
                weight = activeSegmentWeight,
                indicatorColor = elapsedSegmentColor,
                trackColor = MaterialTheme.colors.primary
            )
        )
        if (emomConfiguration.restDurationS > 0) {
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
    val activeDuration = uiState.exerciseState?.activeDurationCheckpoint
    elapsedTimeMs = activeDuration?.getElapsedTimeMs() ?: elapsedTimeMs

    var progress by remember { mutableFloatStateOf(0F) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
        label = "Progress"
    )
    progress = elapsedTimeMs / (totalRoundTimeS * 1_000 * emomConfiguration.roundCount)

    var heartRate by remember { mutableDoubleStateOf(0.0) }
    metrics?.heartRateAverage?.let {
        heartRate = it
    }

    var isInActiveInterval by remember { mutableStateOf(true) }
    isInActiveInterval = isInActiveInterval(emomConfiguration, elapsedTimeMs)

    val completedRounds = (progress * emomConfiguration.roundCount).toInt()

    val finishWorkout: () -> Unit = {
        onFinished(
            Workout(
                durationMs = elapsedTimeMs,
                type = ClockType.EMOM,
                rounds = completedRounds,
                calories = metrics?.calories,
                avgHeartRate = metrics?.heartRateAverage,
                createdAt = Date().time
            )
        )
    }

    if (uiState.exerciseState?.exerciseEvent == ExerciseEvent.TimeEnded) {
        finishWorkout()
    }

    StopWorkoutContainer(
        onConfirm = {
            finishWorkout()
        }
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
                Row(horizontalArrangement = Arrangement.Center) {
                    HeartRateMonitor(hr = heartRate)
                    Spacer(modifier = Modifier.width(16.dp))
                    RoundMonitor( completedRounds + 1, emomConfiguration.roundCount)
                }
            }
        }
    }
}

@Composable
internal fun EmomSummary(
    configuration: EmomConfiguration,
    duration: String,
    roundCount: Int,
    calories: Double?,
    avgHeartRate: Int?,
) {
    val roundsDescription =
        "${configuration.roundCount} ${pluralStringResource(id = R.plurals.message_rounds, count = configuration.roundCount)}"
    val configurationSummary = stringResource(
        R.string.title_emom_summary_desc,
        configuration.activeDurationS.toMinutesAndSeconds(),
        roundsDescription,
        configuration.restDurationS.toMinutesAndSeconds()
    )
    SummaryScreen(
        defaultSummarySections(duration, roundCount, calories, avgHeartRate),
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
        }
    )
}

@Composable
fun RoundMonitor(
    currentRound: Int,
    totalRounds: Int,
    minimumRadius: Dp = 24.dp,
) {
    CircleContainer(
        minimumRadius = minimumRadius,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "Round",
                color = MaterialTheme.colors.primary,
                style = MaterialTheme.typography.caption2
            )
            Text(
                text = "$currentRound/$totalRounds",
                style = MaterialTheme.typography.title2
            )
        }
    }
}

private fun isInActiveInterval(emomConfiguration: EmomConfiguration, elapsedTimeMs: Long): Boolean {
    val totalRoundTimeMs = (emomConfiguration.activeDurationS + emomConfiguration.restDurationS)
    val roundTimeS = (elapsedTimeMs / 1_000) % totalRoundTimeMs
    return roundTimeS < emomConfiguration.activeDurationS
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
        EmomSummary(
            EmomConfiguration(80, 1, 20),
            "12:00",
            12,
            120.0,
            120
        )
    }
}
