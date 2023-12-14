package me.goldhardt.woderful.presentation.clocks

import androidx.health.services.client.data.ExerciseState
import androidx.health.services.client.data.ExerciseUpdate
import me.goldhardt.woderful.data.ServiceState
import me.goldhardt.woderful.service.ExerciseEvent
import me.goldhardt.woderful.service.ExerciseMetrics
import me.goldhardt.woderful.service.ExerciseServiceState
import java.time.Duration
import java.time.Instant

data class ExerciseScreenState(
    val hasExerciseCapabilities: Boolean,
    val isTrackingAnotherExercise: Boolean,
    val serviceState: ServiceState,
    val exerciseState: ExerciseServiceState?,
) {
    val isEnded: Boolean
        get() = exerciseState?.exerciseState?.isEnded == true
}

fun FakeExerciseScreenState(
    heartRate: Double = 62.0,
    calories: Double = 100.0,
    heartRateAverage: Double = 74.0,
    laps: Int = 2,
    duration: Long = 90,
    exerciseEvent: ExerciseEvent? = null
): ExerciseScreenState {
    return ExerciseScreenState(
        hasExerciseCapabilities = true,
        isTrackingAnotherExercise = false,
        serviceState = ServiceState.Connected(
            exerciseServiceState = ExerciseServiceState(
                exerciseState = ExerciseState.ACTIVE,
                exerciseMetrics = ExerciseMetrics(
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
        exerciseState = null
    )
}