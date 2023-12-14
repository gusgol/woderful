package me.goldhardt.woderful.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import me.goldhardt.woderful.data.model.Workout

@Dao
interface WorkoutDao {

    @Query("SELECT * FROM workout ORDER BY created_at DESC")
    fun getWorkouts(): PagingSource<Int, Workout>

    @Insert
    fun insertWorkout(workout: Workout)
}