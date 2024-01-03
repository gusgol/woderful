package me.goldhardt.woderful.service

import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
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
import java.time.Duration
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds


@AndroidEntryPoint
class ExerciseService : LifecycleService() {

    @Inject
    lateinit var exerciseClientManager: ExerciseClientManager

    @Inject
    lateinit var exerciseNotificationManager: ExerciseNotificationManager

    private var isBound = false
    private var isStarted = false
    private val localBinder = LocalBinder()

    private suspend fun isExerciseInProgress() =
        exerciseClientManager.exerciseClient.isExerciseInProgress()

    private val serviceRunningInForeground: Boolean
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            this.foregroundServiceType != ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
        } else {
            this.foregroundServiceType != ServiceInfo.FOREGROUND_SERVICE_TYPE_NONE
        }

    private val _workoutState = MutableStateFlow(WorkoutState())
    val workoutState: StateFlow<WorkoutState> = _workoutState.asStateFlow()

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
                                _workoutState.update { oldState ->
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

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        handleBind()
        return localBinder
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
        handleBind()
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
        postOngoingActivityNotification(clockType)
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
        removeOngoingActivityNotification()
    }


    /**
     * Marks lap for stats.
     */
    suspend fun markLap() {
        exerciseClientManager.markLap()
    }

    private fun stopSelfIfNotRunning() {
        lifecycleScope.launch {
            if (!isExerciseInProgress()) {
                if (workoutState.value.exerciseState == ExerciseState.PREPARING) {
                    lifecycleScope.launch {
                        endExercise()
                    }
                }
                stopSelf()
            }
        }
    }

    private fun processExerciseUpdate(exerciseUpdate: ExerciseUpdate) {
        if (exerciseUpdate.exerciseStateInfo.state.isEnded) {
            removeOngoingActivityNotification()
        }

        _workoutState.update { old ->
            old.copy(
                exerciseState = exerciseUpdate.exerciseStateInfo.state,
                workoutMetrics = old.workoutMetrics.update(exerciseUpdate.latestMetrics),
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
            ExerciseEvent.Milestone
        } else {
            ExerciseEvent.Progress
        }

    private fun handleBind() {
        if (!isBound) {
            isBound = true
            startService(Intent(this, this::class.java))
        }
    }

    private fun postOngoingActivityNotification(
        clockType: ClockType,
    ) {
        if (!serviceRunningInForeground) {
            Log.d(TAG, "Posting ongoing activity notification")

            exerciseNotificationManager.createNotificationChannel()
            val serviceState = workoutState.value
            startForeground(
                ExerciseNotificationManager.NOTIFICATION_ID,
                exerciseNotificationManager.buildNotification(
                    clockType,
                    serviceState.activeDurationCheckpoint?.activeDuration ?: Duration.ZERO
                )
            )
        }
    }

    fun removeOngoingActivityNotification() {
        if (serviceRunningInForeground) {
            Log.d(TAG, "Removing ongoing activity notification")
            stopForeground(STOP_FOREGROUND_REMOVE)
        }
    }

    /** Local clients will use this to access the service. */
    inner class LocalBinder : Binder() {
        fun getService() = this@ExerciseService

        val workoutState: Flow<WorkoutState>
            get() = this@ExerciseService.workoutState
    }

    companion object {
        private val UNBIND_DELAY_MILLIS = 3.seconds
    }
}
