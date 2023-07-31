package me.goldhardt.woderful.domain

import kotlinx.coroutines.flow.Flow
import me.goldhardt.woderful.data.Workout
import me.goldhardt.woderful.data.local.WorkoutRepository
import javax.inject.Inject

class StreamWorkoutsUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    operator fun invoke(): Flow<List<Workout>> = workoutRepository.workouts
}