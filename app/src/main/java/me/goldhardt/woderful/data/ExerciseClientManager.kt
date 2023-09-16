package me.goldhardt.woderful.data

import android.util.Log
import androidx.concurrent.futures.await
import androidx.health.services.client.ExerciseUpdateCallback
import androidx.health.services.client.HealthServicesClient
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.ComparisonType
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.DataTypeCondition
import androidx.health.services.client.data.ExerciseConfig
import androidx.health.services.client.data.ExerciseGoal
import androidx.health.services.client.data.ExerciseLapSummary
import androidx.health.services.client.data.ExerciseType
import androidx.health.services.client.data.ExerciseTypeCapabilities
import androidx.health.services.client.data.ExerciseUpdate
import androidx.health.services.client.data.WarmUpConfig
import androidx.health.services.client.getCapabilities
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.callbackFlow
import me.goldhardt.woderful.extensions.isExerciseInProgress
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ExerciseClientManager @Inject constructor(
    healthServicesClient: HealthServicesClient,
) {
    val exerciseClient = healthServicesClient.exerciseClient

    val exerciseUpdateFlow = callbackFlow {
        val callback = object : ExerciseUpdateCallback {
            override fun onExerciseUpdateReceived(update: ExerciseUpdate) {
                trySendBlocking(ExerciseInfo.ExerciseUpdateInfo(update))
            }

            override fun onLapSummaryReceived(lapSummary: ExerciseLapSummary) {
                trySendBlocking(ExerciseInfo.LapSummaryInfo(lapSummary))
            }

            override fun onRegistered() {}

            override fun onRegistrationFailed(throwable: Throwable) {}

            override fun onAvailabilityChanged(
                dataType: DataType<*, *>,
                availability: Availability,
            ) {}
        }
        exerciseClient.setUpdateCallback(callback)
        awaitClose {
            exerciseClient.clearUpdateCallbackAsync(callback)
        }
    }

    suspend fun getExerciseCapabilities(): ExerciseTypeCapabilities? {
        val capabilities = exerciseClient.getCapabilities()

        return if (ExerciseType.WORKOUT in capabilities.supportedExerciseTypes) {
            capabilities.getExerciseTypeCapabilities(ExerciseType.WORKOUT)
        } else {
            null
        }
    }

    suspend fun startExercise(totalDurationTimeGoalS: Long, ) {
        Log.d(OUTPUT, "Starting exercise")

        val capabilities = getExerciseCapabilities() ?: return
        val dataTypes = setOf(
            DataType.HEART_RATE_BPM,
            DataType.HEART_RATE_BPM_STATS,
            DataType.CALORIES_TOTAL,
        ).intersect(capabilities.supportedDataTypes)

        val totalTimeGoal = ExerciseGoal.createOneTimeGoal(
            condition = DataTypeCondition(
                dataType = DataType.ACTIVE_EXERCISE_DURATION_TOTAL,
                threshold = totalDurationTimeGoalS,
                comparisonType = ComparisonType.GREATER_THAN_OR_EQUAL
            )
        )

        // TODO Need to change this for other workout types
        val intervalThreshold = 60L

        val intervalGoal = ExerciseGoal.createMilestone(
            condition = DataTypeCondition(
                dataType = DataType.ACTIVE_EXERCISE_DURATION_TOTAL,
                threshold = intervalThreshold,
                comparisonType = ComparisonType.GREATER_THAN_OR_EQUAL
            ),
            period = intervalThreshold
        )

        val config = ExerciseConfig(
            exerciseType = ExerciseType.WORKOUT,
            dataTypes = dataTypes,
            isAutoPauseAndResumeEnabled = false,
            isGpsEnabled = false,
            exerciseGoals = listOf(totalTimeGoal, intervalGoal)
        )
        exerciseClient.startExerciseAsync(config).await()
    }

    suspend fun prepareExercise() {
        Log.d(OUTPUT, "Preparing an exercise")

        val warmUpConfig = WarmUpConfig(
            exerciseType = ExerciseType.WORKOUT,
            dataTypes = setOf(DataType.HEART_RATE_BPM)
        )
        try {
            exerciseClient.prepareExerciseAsync(warmUpConfig).await()
        } catch (e: Exception) {
            Log.e(OUTPUT, "Prepare exercise failed - ${e.message}")
        }
    }

    suspend fun endExercise() {
        Log.d(OUTPUT, "Ending exercise")
        exerciseClient.endExerciseAsync().await()
    }

    suspend fun pauseExercise() {
        Log.d(OUTPUT, "Pausing exercise")
        exerciseClient.pauseExerciseAsync().await()
    }

    suspend fun resumeExercise() {
        Log.d(OUTPUT, "Resuming exercise")
        exerciseClient.resumeExerciseAsync().await()
    }

    suspend fun markLap() {
        if (exerciseClient.isExerciseInProgress()) {
            exerciseClient.markLapAsync().await()
        }
    }

    private companion object {
        const val OUTPUT = "Output"
    }
}


sealed class ExerciseInfo {
    class ExerciseUpdateInfo(val exerciseUpdate: ExerciseUpdate) : ExerciseInfo()
    class LapSummaryInfo(val lapSummary: ExerciseLapSummary) : ExerciseInfo()
}



