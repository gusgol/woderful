package me.goldhardt.woderful.presentation.clocks.amrap

import android.Manifest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.goldhardt.woderful.data.HealthServicesRepository
import me.goldhardt.woderful.data.ServiceState
import me.goldhardt.woderful.data.Workout
import me.goldhardt.woderful.data.local.UserPreferencesRepository
import me.goldhardt.woderful.domain.InsertWorkoutUseCase
import me.goldhardt.woderful.domain.VibrateUseCase
import me.goldhardt.woderful.service.ExerciseServiceState
import javax.inject.Inject

data class ExerciseScreenState(
    val hasExerciseCapabilities: Boolean,
    val isTrackingAnotherExercise: Boolean,
    val serviceState: ServiceState,
    val exerciseState: ExerciseServiceState?
) {
    val isEnded: Boolean
        get() = exerciseState?.exerciseState?.isEnded == true
}

@HiltViewModel
class ExerciseViewModel @Inject constructor(
    private val healthServicesRepository: HealthServicesRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val vibrateUseCase: VibrateUseCase,
    private val insertWorkoutUseCase: InsertWorkoutUseCase,
) : ViewModel() {

    var hasShownCounterInstructions = false

    val permissions = arrayOf(
        Manifest.permission.BODY_SENSORS,
        Manifest.permission.ACTIVITY_RECOGNITION
    )

    val uiState: StateFlow<ExerciseScreenState> = healthServicesRepository.serviceState.map {
        ExerciseScreenState(
            hasExerciseCapabilities = healthServicesRepository.hasExerciseCapability(),
            isTrackingAnotherExercise = healthServicesRepository.isTrackingExerciseInAnotherApp(),
            serviceState = it,
            exerciseState = (it as? ServiceState.Connected)?.exerciseServiceState
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(3_000),
        healthServicesRepository.serviceState.value.let {
            ExerciseScreenState(
                true,
                false,
                it,
                (it as? ServiceState.Connected)?.exerciseServiceState
            )
        }
    )

    init {
        getUserPreferences()
        healthServicesRepository.createService()
    }

    suspend fun isExerciseInProgress(): Boolean {
        return healthServicesRepository.isExerciseInProgress()
    }

    fun prepareExercise() {
        healthServicesRepository.prepareExercise()
    }

    fun startExercise() {
        healthServicesRepository.startExercise()
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
        vibrate()
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
}