package me.goldhardt.woderful.data

import android.content.Context
import dagger.hilt.android.ActivityRetainedLifecycle
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.goldhardt.woderful.data.model.ClockType
import me.goldhardt.woderful.data.model.WorkoutConfiguration
import me.goldhardt.woderful.di.bindService
import me.goldhardt.woderful.extensions.isExerciseInProgress
import me.goldhardt.woderful.extensions.isTrackingExerciseInAnotherApp
import me.goldhardt.woderful.service.ExerciseService
import me.goldhardt.woderful.service.ExerciseServiceState
import javax.inject.Inject


@ActivityRetainedScoped
class HealthServicesRepository @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
    private val exerciseClientManager: ExerciseClientManager,
    private val coroutineScope: CoroutineScope,
    val lifecycle: ActivityRetainedLifecycle
) {
    private val binderConnection =
        lifecycle.bindService<ExerciseService.LocalBinder, ExerciseService>(applicationContext)

    val serviceState: StateFlow<ServiceState> =
        binderConnection.flowWhenConnected(ExerciseService.LocalBinder::exerciseServiceState).map {
            ServiceState.Connected(it)
        }.stateIn(
            coroutineScope,
            started = SharingStarted.Eagerly,
            initialValue = ServiceState.Disconnected
        )

    suspend fun hasExerciseCapability(): Boolean = getExerciseCapabilities() != null

    private suspend fun getExerciseCapabilities() = exerciseClientManager.getExerciseCapabilities()

    suspend fun isExerciseInProgress(): Boolean =
        exerciseClientManager.exerciseClient.isExerciseInProgress()

    suspend fun isTrackingExerciseInAnotherApp(): Boolean =
        exerciseClientManager.exerciseClient.isTrackingExerciseInAnotherApp()

    fun prepareExercise() = serviceCall { prepareExercise() }

    private fun serviceCall(function: suspend ExerciseService.() -> Unit) = coroutineScope.launch {
        binderConnection.runWhenConnected {
            function(it.getService())
        }
    }

    fun startExercise(
        clockType: ClockType,
        workoutConfiguration: WorkoutConfiguration
    ) = serviceCall { startExercise(clockType, workoutConfiguration) }
    fun pauseExercise() = serviceCall { pauseExercise() }
    fun endExercise() = serviceCall { endExercise() }
    fun resumeExercise() = serviceCall { resumeExercise() }
    fun markLap() = serviceCall { markLap() }
}

/** Store exercise values in the service state. While the service is connected,
 * the values will persist.**/
sealed class ServiceState {
    object Disconnected : ServiceState()
    data class Connected(
        val exerciseServiceState: ExerciseServiceState,
    ) : ServiceState()
}






