package me.goldhardt.woderful.data.local.database

import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class HashMapTypeConverter {

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val type = Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
    private val adapter = moshi.adapter<Map<String, Any>>(type)

    @TypeConverter
    fun fromHashMap(value: Map<String, Any>?): String {
        return adapter.toJson(value)
    }

    @TypeConverter
    fun toHashMap(value: String): Map<String, Any> {
        return adapter.fromJson(value) ?: emptyMap()
    }
}