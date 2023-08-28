/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.goldhardt.woderful.data

import android.util.Log
import androidx.concurrent.futures.await
import androidx.health.services.client.ExerciseUpdateCallback
import androidx.health.services.client.HealthServicesClient
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.ExerciseConfig
import androidx.health.services.client.data.ExerciseLapSummary
import androidx.health.services.client.data.ExerciseTrackedStatus
import androidx.health.services.client.data.ExerciseType
import androidx.health.services.client.data.ExerciseTypeCapabilities
import androidx.health.services.client.data.ExerciseUpdate
import androidx.health.services.client.data.WarmUpConfig
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.callbackFlow


class ExerciseClientManager @Inject constructor(
    healthServicesClient: HealthServicesClient,
    coroutineScope: CoroutineScope
) {
    private val exerciseClient = healthServicesClient.exerciseClient
    private var exerciseCapabilities: ExerciseTypeCapabilities? = null
    private var capabilitiesLoaded = false

    suspend fun getExerciseCapabilities(): ExerciseTypeCapabilities? {
        val capabilities = exerciseClient.getCapabilitiesAsync().await()
        if (!capabilitiesLoaded) {
            if (ExerciseType.WORKOUT in capabilities.supportedExerciseTypes) {
                exerciseCapabilities =
                    capabilities.getExerciseTypeCapabilities(ExerciseType.WORKOUT)
            }
        }
        return exerciseCapabilities
    }

    suspend fun isExerciseInProgress(): Boolean {
        val exerciseInfo = exerciseClient.getCurrentExerciseInfoAsync().await()
        return exerciseInfo.exerciseTrackedStatus == ExerciseTrackedStatus.OWNED_EXERCISE_IN_PROGRESS
    }

    suspend fun isTrackingExerciseInAnotherApp(): Boolean {
        val exerciseInfo = exerciseClient.getCurrentExerciseInfoAsync().await()
        return exerciseInfo.exerciseTrackedStatus == ExerciseTrackedStatus.OTHER_APP_IN_PROGRESS
    }

    suspend fun startExercise() {
        Log.d(OUTPUT, "Starting exercise")
        val capabilities = getExerciseCapabilities() ?: return
        val dataTypes = setOf(
            DataType.HEART_RATE_BPM,
            DataType.HEART_RATE_BPM_STATS,
            DataType.CALORIES_TOTAL,
        ).intersect(capabilities.supportedDataTypes)

        val config = ExerciseConfig(
            exerciseType = ExerciseType.WORKOUT,
            dataTypes = dataTypes,
            isAutoPauseAndResumeEnabled = false,
            isGpsEnabled = false,
        )
        exerciseClient.startExerciseAsync(config).await()
    }
    suspend fun prepareExercise() {
        Log.d(OUTPUT, "Preparing an exercise")
        val warmUpConfig = WarmUpConfig(
            ExerciseType.WORKOUT, setOf(
                DataType.HEART_RATE_BPM,
            )
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
        if (isExerciseInProgress()) {
            exerciseClient.markLapAsync().await()
        }
    }

    /**
     * When the flow starts, it will register an [ExerciseUpdateCallback] and start to emit
     * messages. When there are no more subscribers, or when the coroutine scope is
     * cancelled, this flow will unregister the listener.
     * [callbackFlow] is used to bridge between a callback-based API and Kotlin flows.
     */
    val exerciseUpdateFlow = callbackFlow {
        val callback = object : ExerciseUpdateCallback {
            override fun onExerciseUpdateReceived(update: ExerciseUpdate) {
                coroutineScope.runCatching {
                    trySendBlocking(ExerciseInfo.ExerciseUpdateInfo(update))
                }
            }

            override fun onLapSummaryReceived(lapSummary: ExerciseLapSummary) {
                coroutineScope.runCatching {
                    trySendBlocking(ExerciseInfo.LapSummaryInfo(lapSummary))
                }
            }

            override fun onRegistered() {
            }

            override fun onRegistrationFailed(throwable: Throwable) {
                TODO("Not yet implemented")
            }

            override fun onAvailabilityChanged(
                dataType: DataType<*, *>, availability: Availability
            ) {
            }
        }
        exerciseClient.setUpdateCallback(callback)
        awaitClose {
            exerciseClient.clearUpdateCallbackAsync(callback)
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



