package me.goldhardt.woderful.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a workout.
 * @param durationMs The duration of the workout in milliseconds.
 * @param type The type of workout.
 * @param rounds The number of rounds.
 * @param calories The number of calories burned.
 * @param avgHeartRate The average heart rate during the workout.
 */
@Entity
data class Workout(
    @ColumnInfo(name = "duration_ms") val durationMs: Long,
    @ColumnInfo(name = "type") val type: ClockType,
    @ColumnInfo(name = "rounds") val rounds: Int,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "calories")val calories: Double?,
    @ColumnInfo(name = "avg_heart_rate") val avgHeartRate: Double?,
    @ColumnInfo(name = "properties") val properties: Map<String, Any>? = null
) {

    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0
}