package me.goldhardt.woderful.domain

import me.goldhardt.woderful.data.Workout
import me.goldhardt.woderful.data.local.WorkoutRepository
import javax.inject.Inject

class InsertWorkoutUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {

    suspend operator fun invoke(workout: Workout) {
        workoutRepository.add(workout)
    }
}