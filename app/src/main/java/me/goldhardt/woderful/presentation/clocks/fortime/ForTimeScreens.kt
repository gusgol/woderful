package me.goldhardt.woderful.presentation.clocks.fortime

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.ProgressIndicatorDefaults
import androidx.wear.compose.material.Text
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.composables.ProgressIndicatorSegment
import com.google.android.horologist.composables.SegmentedProgressIndicator
import kotlinx.coroutines.launch
import me.goldhardt.woderful.R
import me.goldhardt.woderful.data.ServiceState
import me.goldhardt.woderful.data.model.ClockType
import me.goldhardt.woderful.extensions.getElapsedTimeMs
import me.goldhardt.woderful.extensions.toSeconds
import me.goldhardt.woderful.presentation.clocks.ExercisePermissions
import me.goldhardt.woderful.presentation.clocks.ExercisePermissionsLauncher
import me.goldhardt.woderful.presentation.clocks.ExerciseViewModel
import me.goldhardt.woderful.presentation.clocks.FakeExerciseScreenState
import me.goldhardt.woderful.presentation.clocks.MinutesTimeConfiguration
import me.goldhardt.woderful.presentation.clocks.WorkoutUiState
import me.goldhardt.woderful.presentation.clocks.amrap.Duration
import me.goldhardt.woderful.presentation.component.CircleContainer
import me.goldhardt.woderful.presentation.component.HeartRateMonitor
import me.goldhardt.woderful.presentation.component.INITIAL_PAGE
import me.goldhardt.woderful.presentation.component.LoadingWorkout
import me.goldhardt.woderful.presentation.component.PAGE_COUNT
import me.goldhardt.woderful.presentation.component.StopWorkoutContainer
import me.goldhardt.woderful.presentation.component.SummaryScreen
import me.goldhardt.woderful.presentation.component.toDefaultSummarySections
import me.goldhardt.woderful.presentation.theme.WODerfulTheme
import me.goldhardt.woderful.service.WorkoutState

@Composable
fun ForTimeScreen(
    viewModel: ExerciseViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val initialScreen = if (ExercisePermissions.hasPermissions(LocalContext.current)) {
        ForTimeFlow.TimeConfig
    } else {
        ForTimeFlow.Permissions
    }

    var step: ForTimeFlow by remember { mutableStateOf(initialScreen) }

    var config: ForTimeConfiguration by remember { mutableStateOf(ForTimeConfiguration(0L)) }

    when (uiState.serviceState) {
        is ServiceState.Connected -> {
            LaunchedEffect(Unit) {
                viewModel.prepareExercise()
            }
            if (uiState.isEnded && uiState.workoutState != null) {
                ForTimeSummary(
                    workoutState = requireNotNull(uiState.workoutState),
                    onClose = onClose,
                )
            } else {
                when (step) {
                    ForTimeFlow.Permissions -> {
                        ExercisePermissionsLauncher {
                            step = ForTimeFlow.TimeConfig
                        }
                    }

                    ForTimeFlow.TimeConfig -> {
                        ForTimeTimeConfiguration(
                            onConfirm = {
                                config = ForTimeConfiguration(it.toSeconds())
                                step = ForTimeFlow.Tracker
                            }
                        )
                    }

                    ForTimeFlow.Tracker -> {
                        LaunchedEffect(Unit) {
                            viewModel.startExercise(ClockType.EMOM, config)
                        }
                        ForTimeTracker(
                            configuration = config,
                            uiState = uiState,
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
internal fun ForTimeTimeConfiguration(
    onConfirm: (Int) -> Unit,
) {
    MinutesTimeConfiguration(
        title = stringResource(id = R.string.title_time_cap),
        onConfirm = onConfirm
    )
}

@OptIn(ExperimentalHorologistApi::class, ExperimentalFoundationApi::class)
@Composable
internal fun ForTimeTracker(
    configuration: ForTimeConfiguration,
    uiState: WorkoutUiState,
    onFinished: () -> Unit,
) {
    val metrics = uiState.workoutState?.workoutMetrics

    val segments = mutableListOf(
        ProgressIndicatorSegment(
            weight = 1f,
            indicatorColor = MaterialTheme.colors.surface.copy(alpha = 0.8f),
            trackColor = MaterialTheme.colors.primary
        )
    )

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

    val pagerState = rememberPagerState(
        initialPage = INITIAL_PAGE,
        pageCount = { PAGE_COUNT }
    )
    val coroutineScope = rememberCoroutineScope()

    StopWorkoutContainer(
        pagerState = pagerState,
        coroutineScope = coroutineScope,
        onConfirm = onFinished
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
                    text = stringResource(R.string.title_time),
                    color = MaterialTheme.colors.primary,
                    style = MaterialTheme.typography.caption1
                )
                Duration(uiState = uiState)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    HeartRateMonitor(hr = heartRate)
                    Spacer(modifier = Modifier.width(8.dp))
                    EndWorkout {
                        coroutineScope.launch {
                            pagerState.scrollToPage(0)
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun ForTimeSummary(
    workoutState: WorkoutState,
    onClose: () -> Unit,
) {
    SummaryScreen(workoutState.toDefaultSummarySections(), onClose)
}

@Composable
internal fun EndWorkout(
    onClick: () -> Unit,
) {
    val radius = 28.dp
    CircleContainer(
        radius = radius,
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = rememberRipple(bounded = false),
            onClick = onClick
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.title_end),
                color = MaterialTheme.colors.error,
                style = MaterialTheme.typography.caption1,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(radius * 2)
            )
            Icon(
                imageVector = Icons.Filled.Stop,
                contentDescription = stringResource(R.string.title_end_workout),
                tint = MaterialTheme.colors.error,
                modifier = Modifier
                    .size(24.dp)
            )
        }
    }
}

@WearPreviewDevices
@Composable
fun EmomSummaryPreview() {
    WODerfulTheme {
        ForTimeTracker(
            configuration = ForTimeConfiguration(12L),
            uiState = FakeExerciseScreenState(),
            onFinished = {}
        )
    }
}