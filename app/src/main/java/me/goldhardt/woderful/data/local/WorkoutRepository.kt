package me.goldhardt.woderful.data.local

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import me.goldhardt.woderful.data.Workout
import javax.inject.Inject

interface WorkoutRepository {
    val workouts: Flow<PagingData<Workout>>

    suspend fun add(workout: Workout)
}

class DefaultWorkoutRepository @Inject constructor(
    private val workoutDao: WorkoutDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : WorkoutRepository {

    override val workouts: Flow<PagingData<Workout>> =
        Pager(
            config = PagingConfig(pageSize = 5),
            pagingSourceFactory = { workoutDao.getWorkouts() }
        ).flow

    override suspend fun add(workout: Workout) {
        // TODO move this to a datasource
        withContext(ioDispatcher) {
            workoutDao.insertWorkout(workout)
        }
    }
}