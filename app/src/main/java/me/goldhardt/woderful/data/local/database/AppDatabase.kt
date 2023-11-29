package me.goldhardt.woderful.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import me.goldhardt.woderful.data.local.WorkoutDao
import me.goldhardt.woderful.data.model.Workout

@Database(
    entities = [
        Workout::class,
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(HashMapTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
}