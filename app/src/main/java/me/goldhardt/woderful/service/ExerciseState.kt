package me.goldhardt.woderful.service

import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.ExerciseState
import androidx.health.services.client.data.ExerciseUpdate

data class WorkoutMetrics(
    val heartRate: Double? = null,
    val calories: Double? = null,
    val heartRateAverage: Double? = null,
) {
    fun update(latestMetrics: DataPointContainer): WorkoutMetrics {
        return copy(
            heartRate = latestMetrics.getData(DataType.HEART_RATE_BPM).lastOrNull()?.value
                ?: heartRate,
            calories = latestMetrics.getData(DataType.CALORIES_TOTAL)?.total ?: calories,
            heartRateAverage = latestMetrics.getData(DataType.HEART_RATE_BPM_STATS)?.average
                ?: heartRateAverage
        )
    }
}

data class WorkoutState(
    val exerciseState: ExerciseState? = null,
    val workoutMetrics: WorkoutMetrics = WorkoutMetrics(),
    val exerciseLaps: Int = 0,
    val activeDurationCheckpoint: ExerciseUpdate.ActiveDurationCheckpoint? = null,
    val exerciseEvent: ExerciseEvent? = null
)

sealed interface ExerciseEvent{
    data object Progress : ExerciseEvent
    data object Milestone : ExerciseEvent
    data object TimeEnded : ExerciseEvent
}