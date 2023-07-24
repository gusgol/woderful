package me.goldhardt.woderful.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import me.goldhardt.woderful.data.Workout
import me.goldhardt.woderful.data.local.WorkoutDao

@Database(
    entities = [
        Workout::class,
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
}