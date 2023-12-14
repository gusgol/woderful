package me.goldhardt.woderful.presentation.clocks

import android.util.Log
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
import me.goldhardt.woderful.data.model.Workout
import me.goldhardt.woderful.domain.InsertWorkoutUseCase
import me.goldhardt.woderful.domain.VibrateUseCase
import me.goldhardt.woderful.service.ExerciseEvent
import javax.inject.Inject

@HiltViewModel
class ExerciseViewModel @Inject constructor(
    private val healthServicesRepository: HealthServicesRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val vibrateUseCase: VibrateUseCase,
    private val insertWorkoutUseCase: InsertWorkoutUseCase,
) : ViewModel() {

    var hasShownCounterInstructions = false

    val uiState: StateFlow<ExerciseScreenState> = healthServicesRepository.serviceState.map {
        ExerciseScreenState(
            hasExerciseCapabilities = healthServicesRepository.hasExerciseCapability(),
            isTrackingAnotherExercise = healthServicesRepository.isTrackingExerciseInAnotherApp(),
            serviceState = it,
            exerciseState = (it as? ServiceState.Connected)?.exerciseServiceState,
        )
    }.onEach {
        when (it.exerciseState?.exerciseEvent) {
            ExerciseEvent.Lap -> {
                vibrate()
            }
            ExerciseEvent.TimeEnded -> {
                vibrate()
            }
            else -> {}
        }
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

    private var durationGoalS: Long? = null

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

    fun startExercise(durationGoalS: Long) {
        this.durationGoalS = durationGoalS
        healthServicesRepository.startExercise(durationGoalS)
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
        Log.e("ExerciseViewModel", "markLap")
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
}