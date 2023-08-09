package me.goldhardt.woderful.domain

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import me.goldhardt.woderful.data.Workout
import me.goldhardt.woderful.data.local.WorkoutRepository
import javax.inject.Inject

class StreamWorkoutsUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {

    operator fun invoke(): Flow<PagingData<Workout>> = workoutRepository.workouts
}