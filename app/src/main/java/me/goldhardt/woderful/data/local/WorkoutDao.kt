package me.goldhardt.woderful.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import me.goldhardt.woderful.data.Workout

@Dao
interface WorkoutDao {

    @Query("SELECT * FROM workout")
    fun getWorkouts(): Flow<List<Workout>>

    @Insert
    fun insertWorkout(workout: Workout)
}