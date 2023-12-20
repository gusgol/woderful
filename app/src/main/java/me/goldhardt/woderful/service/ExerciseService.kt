package me.goldhardt.woderful.service

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.health.services.client.data.ExerciseState
import androidx.health.services.client.data.ExerciseUpdate
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.goldhardt.woderful.data.ExerciseClientManager
import me.goldhardt.woderful.data.ExerciseInfo
import me.goldhardt.woderful.data.model.ClockType
import me.goldhardt.woderful.data.model.WorkoutConfiguration
import me.goldhardt.woderful.extensions.isExerciseInProgress
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds


@AndroidEntryPoint
class ExerciseService : LifecycleService() {

    @Inject
    lateinit var exerciseClientManager: ExerciseClientManager

    private var isBound = false
    private var isStarted = false
    private val localBinder = LocalBinder()

    private val _exerciseServiceState = MutableStateFlow(ExerciseServiceState())
    val exerciseServiceState: StateFlow<ExerciseServiceState> = _exerciseServiceState.asStateFlow()

    private suspend fun isExerciseInProgress() =
        exerciseClientManager.exerciseClient.isExerciseInProgress()

    /**
     * Prepare exercise in this service's coroutine context.
     */
    suspend fun prepareExercise() {
        exerciseClientManager.prepareExercise()
    }

    /**
     * Start exercise in this service's coroutine context.
     */
    suspend fun startExercise(
        clockType: ClockType,
        workoutConfiguration: WorkoutConfiguration
    ) {
        exerciseClientManager.startExercise(clockType, workoutConfiguration)
    }

    /**
     * Pause exercise in this service's coroutine context.
     */
    suspend fun pauseExercise() {
        exerciseClientManager.pauseExercise()
    }

    /**
     * Resume exercise in this service's coroutine context.
     */
    suspend fun resumeExercise() {
        exerciseClientManager.resumeExercise()
    }

    /**
     * End exercise in this service's coroutine context.
     */
    suspend fun endExercise() {
        exerciseClientManager.endExercise()
    }


    /**
     * Marks lap for stats.
     */
    suspend fun markLap() {
        exerciseClientManager.markLap()
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d(TAG, "onStartCommand")

        if (!isStarted) {
            isStarted = true

            if (!isBound) {
                stopSelfIfNotRunning()
            }
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    exerciseClientManager.exerciseUpdateFlow.collect {
                        when (it) {
                            is ExerciseInfo.ExerciseUpdateInfo ->
                                processExerciseUpdate(it.exerciseUpdate)

                            is ExerciseInfo.LapSummaryInfo ->
                                _exerciseServiceState.update { oldState ->
                                    oldState.copy(
                                        exerciseLaps = it.lapSummary.lapCount
                                    )
                                }
                        }
                    }
                }
            }
        }
        return START_STICKY
    }

    private fun stopSelfIfNotRunning() {
        lifecycleScope.launch {
            if (!isExerciseInProgress()) {
                if (exerciseServiceState.value.exerciseState == ExerciseState.PREPARING) {
                    lifecycleScope.launch {
                        endExercise()
                    }
                }
                stopSelf()
            }
        }
    }

    private fun processExerciseUpdate(exerciseUpdate: ExerciseUpdate) {
        /**
         * TODO - react different for different end reasons.
         *
         * This is how to check for end reason:
         *      when (exerciseUpdate.exerciseStateInfo.endReason) {
         *          ExerciseEndReason.AUTO_END_SUPERSEDED -> {
         *     ...
         *
         * TODO - in case we have an active notification, this is where we would remove it.
         *
         * How to check if exercise is ended:
         *
         *      exerciseUpdate.exerciseStateInfo.state.isEnded
         */

        _exerciseServiceState.update { old ->
            old.copy(
                exerciseState = exerciseUpdate.exerciseStateInfo.state,
                exerciseMetrics = old.exerciseMetrics.update(exerciseUpdate.latestMetrics),
                exerciseEvent = getExerciseEvent(exerciseUpdate),
                activeDurationCheckpoint = exerciseUpdate.activeDurationCheckpoint
                    ?: old.activeDurationCheckpoint,
            )

        }
    }

    private fun getExerciseEvent(exerciseUpdate: ExerciseUpdate) =
        if (exerciseUpdate.latestAchievedGoals.isNotEmpty()) {
            ExerciseEvent.TimeEnded
        } else if (exerciseUpdate.latestMilestoneMarkerSummaries.isNotEmpty()) {
            ExerciseEvent.Lap
        } else {
            null
        }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        handleBind()
        return localBinder
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
        handleBind()
    }

    private fun handleBind() {
        if (!isBound) {
            isBound = true
            startService(Intent(this, this::class.java))
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        isBound = false
        lifecycleScope.launch {
            delay(UNBIND_DELAY_MILLIS)
            if (!isBound) {
                stopSelfIfNotRunning()
            }
        }
        return true
    }

    /** Local clients will use this to access the service. */
    inner class LocalBinder : Binder() {
        fun getService() = this@ExerciseService

        val exerciseServiceState: Flow<ExerciseServiceState>
            get() = this@ExerciseService.exerciseServiceState
    }

    companion object {
        private val UNBIND_DELAY_MILLIS = 3.seconds
    }
}
