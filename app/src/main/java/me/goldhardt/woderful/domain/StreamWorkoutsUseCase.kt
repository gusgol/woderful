package me.goldhardt.woderful.domain

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import me.goldhardt.woderful.data.local.WorkoutRepository
import me.goldhardt.woderful.data.model.Workout
import javax.inject.Inject

class StreamWorkoutsUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {

    operator fun invoke(): Flow<PagingData<Workout>> = workoutRepository.workouts
}