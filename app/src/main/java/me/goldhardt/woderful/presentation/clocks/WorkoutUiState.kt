package me.goldhardt.woderful.presentation.clocks

import androidx.health.services.client.data.ExerciseState
import androidx.health.services.client.data.ExerciseUpdate
import me.goldhardt.woderful.data.ServiceState
import me.goldhardt.woderful.service.ExerciseEvent
import me.goldhardt.woderful.service.WorkoutMetrics
import me.goldhardt.woderful.service.WorkoutState
import java.time.Duration
import java.time.Instant

data class WorkoutUiState(
    val hasExerciseCapabilities: Boolean,
    val isTrackingAnotherExercise: Boolean,
    val serviceState: ServiceState,
    val workoutState: WorkoutState?,
) {
    val isEnded: Boolean
        get() = workoutState?.exerciseState?.isEnded == true
}

fun FakeExerciseScreenState(
    heartRate: Double = 62.0,
    calories: Double = 100.0,
    heartRateAverage: Double = 74.0,
    laps: Int = 2,
    duration: Long = 90,
    exerciseEvent: ExerciseEvent? = null
): WorkoutUiState {
    return WorkoutUiState(
        hasExerciseCapabilities = true,
        isTrackingAnotherExercise = false,
        serviceState = ServiceState.Connected(
            workoutState = WorkoutState(
                exerciseState = ExerciseState.ACTIVE,
                workoutMetrics = WorkoutMetrics(
                    heartRate = heartRate,
                    calories = calories,
                    heartRateAverage = heartRateAverage
                ),
                exerciseLaps = laps,
                activeDurationCheckpoint = ExerciseUpdate.ActiveDurationCheckpoint(
                    time = Instant.EPOCH,
                    activeDuration = Duration.ofSeconds(duration)
                ),
                exerciseEvent = exerciseEvent,
            )
        ),
        workoutState = null
    )
}