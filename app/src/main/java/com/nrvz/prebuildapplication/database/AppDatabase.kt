package com.nrvz.prebuildapplication.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nrvz.prebuildapplication.database.dao.NoteDao
import com.nrvz.prebuildapplication.database.entity.NoteEntity

/**
 * ============================================================================
 *  AppDatabase — the Room database and its schema version.
 * ============================================================================
 *
 * ONBOARDING NOTE
 * ---------------
 * CLASS vs INSTANCE: this is the abstract CLASS Room extends to generate
 * AppDatabase_Impl. You never call `new` - you build the ONE instance in
 * utilities/di/DatabaseModule.kt and Hilt hands it to whoever needs it. The
 * old version of this repo built the instance with a `lateinit var INSTANCE`
 * + `synchronized` block; DI replaces that pattern everywhere we work now.
 *
 * EVERY entity in your app must be listed in `entities`. Forget one and Room
 * never creates the table - your query then fails with "no such table".
 *
 * `version` is the migration counter. Change ANY entity (add a column, rename a
 * column, add an index) and you MUST bump it, or existing installs crash with
 * "Room cannot verify the data integrity".
 *
 * exportSchema = false here to keep a tutorial app simple. RockyGo sets it to
 * true and commits the generated JSON under app/schemas/, which is what lets
 * you diff two schema versions before writing a migration. Turning it on here
 * just needs a kapt argument for `room.schemaLocation`.
 *
 * @TypeConverters points at the class below that teaches Room how to store
 * types SQLite does not understand (List<String>, Date, custom objects...).
 */
@Database(
    entities = [
        NoteEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(StringListConverter::class)
abstract class AppDatabase : RoomDatabase() {

    /**
     * One abstract getter per DAO. This is the ONLY way to reach your tables.
     *
     * Notice there is no @Provides for individual DAOs in DatabaseModule that
     * have to exist just for the repository - the repository injects AppDatabase
     * and calls `database.noteDao()`. Add a @Provides only when a DAO is
     * injected somewhere that is NOT a repository (e.g. straight into a
     * ViewModel), otherwise Dagger reports a [Dagger/MissingBinding] error.
     */
    abstract fun noteDao(): NoteDao
}

/**
 * Gson-backed TypeConverters for List<String>.
 *
 * HOW IT WORKS
 * ------------
 * Room sees a `List<String> tags` property and asks: "how do I put this in a
 * TEXT column?" It finds `tagsToString` (List<String> -> String) on the way in,
 * and `stringToTags` (String -> List<String>) on the way out.
 *
 * Stored as a JSON array: ["work","urgent"]. That is fine for small data. It is
 * NOT queryable - you cannot "find all notes tagged urgent" with SQL and expect
 * an index to help. If you ever need to FILTER on tags, that is the signal to
 * promote tags to their own table with a many-to-many join table.
 *
 * RockyGo's AppDatabase has half a dozen of these converters (OrderDetailConverter,
 * VehicleDetailsConverter, ...) for exactly the same reason.
 */
class StringListConverter {

    @TypeConverter
    fun tagsToString(tags: List<String>?): String =
        Gson().toJson(tags ?: emptyList<String>())

    @TypeConverter
    fun stringToTags(value: String?): List<String> {
        if (value.isNullOrEmpty()) return emptyList()
        val type = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, type) ?: emptyList()
    }
}
