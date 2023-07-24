package me.goldhardt.woderful.presentation.clocks.amrap

import android.Manifest
import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.goldhardt.woderful.data.HealthServicesRepository
import me.goldhardt.woderful.data.ServiceState
import me.goldhardt.woderful.data.Workout
import me.goldhardt.woderful.domain.InsertWorkoutUseCase
import me.goldhardt.woderful.domain.VibrateUseCase
import javax.inject.Inject


@HiltViewModel
class AmrapViewModel @Inject constructor(
    private val healthServicesRepository: HealthServicesRepository,
    private val vibrateUseCase: VibrateUseCase,
    private val insertWorkoutUseCase: InsertWorkoutUseCase,
) : ViewModel() {

    val permissions = arrayOf(
        Manifest.permission.BODY_SENSORS,
        Manifest.permission.ACTIVITY_RECOGNITION
    )

    val uiState: StateFlow<ExerciseUiState> = flow {
        emit(
            ExerciseUiState(
                hasExerciseCapabilities = healthServicesRepository.hasExerciseCapability(),
                isTrackingAnotherExercise = healthServicesRepository.isTrackingExerciseInAnotherApp(),
            )
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(3_000),
        ExerciseUiState()
    )

    private var _exerciseServiceState: MutableState<ServiceState> =
        healthServicesRepository.serviceState
    val exerciseServiceState = _exerciseServiceState

    init {
        viewModelScope.launch {
            healthServicesRepository.createService()
        }
    }

    suspend fun isExerciseInProgress(): Boolean {
        return healthServicesRepository.isExerciseInProgress()
    }

    fun prepareExercise() = viewModelScope.launch { healthServicesRepository.prepareExercise() }
    fun startExercise() = viewModelScope.launch { healthServicesRepository.startExercise() }
    fun pauseExercise() = viewModelScope.launch { healthServicesRepository.pauseExercise() }
    fun endExercise() = viewModelScope.launch { healthServicesRepository.endExercise() }
    fun resumeExercise() = viewModelScope.launch { healthServicesRepository.resumeExercise() }
    fun markLap() {
        viewModelScope.launch { healthServicesRepository.markLap() }
        vibrate()
    }

    fun insertWorkout(workout: Workout) {
        viewModelScope.launch {
            insertWorkoutUseCase(workout)
        }
    }

    private fun vibrate() {
        vibrateUseCase(500L)
    }
}

data class ExerciseUiState(
    val hasExerciseCapabilities: Boolean = true,
    val isTrackingAnotherExercise: Boolean = false,
)