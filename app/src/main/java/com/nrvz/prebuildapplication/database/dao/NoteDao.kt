package com.nrvz.prebuildapplication.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nrvz.prebuildapplication.database.entity.NoteEntity

/**
 * ============================================================================
 *  NoteDao — every SQL statement the app can run, in one place.
 * ============================================================================
 *
 * ONBOARDING NOTE
 * ---------------
 * A DAO is an INTERFACE. You never implement it - Room generates the
 * implementation at compile time (kapt). That is why the methods are `abstract`
 * by omission and why a typo in a @Query string is a BUILD error, not a runtime
 * crash. The SQL is verified against the schema during compilation.
 *
 * suspend vs LiveData - the rule we follow:
 *   `suspend fun` + return value  -> ONE-SHOT work: insert, update, delete,
 *                                    count. Must be called from a coroutine.
 *   `fun` returning LiveData      -> OBSERVABLE reads. Room re-emits every time
 *                                    the `note` table changes, so the UI
 *                                    refreshes itself with no manual reload.
 *
 * That second one is the whole reason the list screen never has a "refresh()"
 * call. It is also why the Delete button on the detail screen does not have to
 * tell the list screen anything.
 */
@Dao
interface NoteDao {

    /**
     * The list screen. ORDER BY created_at DESC = newest first.
     * Returning LiveData means: run this again and push the result to whoever is
     * observing, every time the table changes.
     */
    @Query("SELECT * FROM note ORDER BY created_at DESC")
    fun getAll(): LiveData<List<NoteEntity>>

    /**
     * Search. `||` is SQLite's string concatenation, so this becomes
     * '%' + :query + '%' -> a contains-match on title OR body.
     *
     * SECURITY NOTE: :query is a BOUND PARAMETER, not string interpolation. Room
     * escapes it for us. NEVER build SQL by concatenating user input in Kotlin.
     */
    @Query(
        """
        SELECT * FROM note
        WHERE title LIKE '%' || :query || '%'
           OR body  LIKE '%' || :query || '%'
        ORDER BY created_at DESC
        """
    )
    fun search(query: String): LiveData<List<NoteEntity>>

    /** Single row by id, for the detail screen. Nullable: the row may be deleted. */
    @Query("SELECT * FROM note WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): NoteEntity?

    @Query("SELECT COUNT(*) FROM note")
    suspend fun count(): Int

    /**
     * REPLACE = "insert, or overwrite if the primary key already exists".
     * Without it, inserting a duplicate id throws SQLiteConstraintException.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity): Long

    /** Bulk insert - used by the first-launch seed and by the network sync. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notes: List<NoteEntity>): List<Long>

    /**
     * @Update matches on the primary key and updates the other columns.
     * Returns the number of rows changed - 0 means "no such id", which is a real
     * signal worth checking when a save silently does nothing.
     */
    @Update
    suspend fun update(note: NoteEntity): Int

    @Query("DELETE FROM note WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("DELETE FROM note")
    suspend fun clear()
}
