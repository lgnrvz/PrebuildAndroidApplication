package com.nrvz.prebuildapplication.database

import android.content.Context
import androidx.room.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nrvz.prebuildapplication.database.dao.SampleDao
import com.nrvz.prebuildapplication.database.dto.DatabaseSample
import java.lang.reflect.Type

const val ROOM_DATABASE_NAME = "database"

@Database(
    entities =
    [
        DatabaseSample::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(MapConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract val sampleDao: SampleDao
}

private lateinit var INSTANCE: AppDatabase

fun getDatabase(context: Context): AppDatabase {
    synchronized(AppDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room
                .databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    ROOM_DATABASE_NAME
                )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
    return INSTANCE
}

class MapConverter {
    @TypeConverter
    fun stringToMap(value: String): Map<String, Int?>? {
        val mapType: Type = object : TypeToken<Map<String, Int?>?>() {}.type
        return Gson().fromJson(value, mapType)
    }

    @TypeConverter
    fun mapToString(map: Map<String, Int?>?): String? {
        val gson = Gson()
        return gson.toJson(map)
    }

    @TypeConverter
    fun stringToStringList(value: String): List<String>? {
        val mapType: Type = object : TypeToken<List<String>?>() {}.type
        return Gson().fromJson(value, mapType)
    }

    @TypeConverter
    fun stingListToString(map: List<String>): String? {
        val gson = Gson()
        return gson.toJson(map)
    }
}