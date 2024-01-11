package me.goldhardt.woderful.presentation.clocks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.goldhardt.woderful.data.HealthServicesRepository
import me.goldhardt.woderful.data.ServiceState
import me.goldhardt.woderful.data.local.UserPreferencesRepository
import me.goldhardt.woderful.data.model.ClockType
import me.goldhardt.woderful.data.model.RoundBehavior
import me.goldhardt.woderful.data.model.Workout
import me.goldhardt.woderful.data.model.WorkoutConfiguration
import me.goldhardt.woderful.domain.InsertWorkoutUseCase
import me.goldhardt.woderful.domain.VibrateUseCase
import me.goldhardt.woderful.extensions.getElapsedTimeMs
import me.goldhardt.woderful.presentation.clocks.emom.toProperties
import me.goldhardt.woderful.service.ExerciseEvent
import me.goldhardt.woderful.service.WorkoutState
import java.util.Date
import javax.inject.Inject

/**
 * //TODO remove unused methods
 * //TODO review function visibility
 * //TODO test behavior alongside other tracking apps
 */

@HiltViewModel
class ExerciseViewModel @Inject constructor(
    private val healthServicesRepository: HealthServicesRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val vibrateUseCase: VibrateUseCase,
    private val insertWorkoutUseCase: InsertWorkoutUseCase,
) : ViewModel() {

    var hasShownCounterInstructions = false

    val uiState: StateFlow<WorkoutUiState> = healthServicesRepository.serviceState.map {
        WorkoutUiState(
            hasExerciseCapabilities = healthServicesRepository.hasExerciseCapability(),
            isTrackingAnotherExercise = healthServicesRepository.isTrackingExerciseInAnotherApp(),
            serviceState = it,
            workoutState = (it as? ServiceState.Connected)?.workoutState,
        )
    }.onEach {
        when (it.workoutState?.exerciseEvent) {
            ExerciseEvent.Milestone -> {
                markLapIfRequired()
                vibrate()
            }
            ExerciseEvent.TimeEnded -> {
                markLapIfRequired()
                endWorkout(it.workoutState)
                vibrate()
            }
            else -> {}
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(3_000),
        healthServicesRepository.serviceState.value.let {
            WorkoutUiState(
                true,
                false,
                it,
                null
            )
        }
    )

    private var clockType: ClockType? = null

    private var configuration: WorkoutConfiguration? = null

    init {
        getUserPreferences()
    }

    suspend fun isExerciseInProgress(): Boolean {
        return healthServicesRepository.isExerciseInProgress()
    }

    fun prepareExercise() {
        healthServicesRepository.prepareExercise()
    }

    fun startExercise(
        clockType: ClockType,
        workoutConfiguration: WorkoutConfiguration
    ) {
        this.clockType = clockType
        this.configuration = workoutConfiguration
        healthServicesRepository.startExercise(clockType, workoutConfiguration)
    }

    fun endWorkout(state: WorkoutState) {
        endExercise()
        state.toWorkout()?.let {
            insertWorkout(it)
        }
    }

    fun pauseExercise() {
        healthServicesRepository.pauseExercise()
    }

    fun endExercise() {
        healthServicesRepository.endExercise()
    }

    fun resumeExercise() {
        healthServicesRepository.resumeExercise()
    }

    fun markLap() {
        healthServicesRepository.markLap()
    }

    fun insertWorkout(workout: Workout) {
        viewModelScope.launch {
            insertWorkoutUseCase(workout)
        }
    }

    fun onCounterInstructionsShown() {
        viewModelScope.launch {
            userPreferencesRepository.setCounterInstructionsShown(true)
        }
    }

    private fun getUserPreferences() {
        viewModelScope.launch {
            hasShownCounterInstructions = userPreferencesRepository.hasShownCounterInstructions()
        }
    }

    private fun vibrate() {
        viewModelScope.launch {
            if (isExerciseInProgress()) {
                vibrateUseCase(500L)
            }
        }
    }

    private fun WorkoutState?.toWorkout(): Workout? {
        val workoutState = this ?: return null
        val totalElapsedTime: Long = workoutState.activeDurationCheckpoint?.getElapsedTimeMs() ?: 0
        return Workout(
            durationMs = totalElapsedTime,
            type = clockType ?: throw IllegalStateException("Clock type is null"),
            rounds = workoutState.exerciseLaps,
            createdAt = Date().time,
            calories = workoutState.workoutMetrics.calories,
            avgHeartRate = workoutState.workoutMetrics.heartRateAverage,
            properties = configuration?.toProperties() ?: emptyMap()
        )
    }

    private fun markLapIfRequired() {
        if (clockType?.roundBehavior == RoundBehavior.Time) {
            markLap()
        }
    }
}