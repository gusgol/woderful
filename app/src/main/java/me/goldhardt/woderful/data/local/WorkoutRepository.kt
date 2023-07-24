package me.goldhardt.woderful.data.local

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import me.goldhardt.woderful.data.Workout
import javax.inject.Inject

interface WorkoutRepository {
    val workouts: Flow<List<Workout>>

    suspend fun add(workout: Workout)
}

class DefaultWorkoutRepository @Inject constructor(
    private val workoutDao: WorkoutDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : WorkoutRepository {

    override val workouts: Flow<List<Workout>> =
        workoutDao.getWorkouts()

    override suspend fun add(workout: Workout) {
        // TODO move this to a datasource
        withContext(ioDispatcher) {
            workoutDao.insertWorkout(workout)
        }
    }
}