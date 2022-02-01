package com.nrvz.prebuildapplication.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nrvz.prebuildapplication.database.dto.DatabaseSample

@Dao
interface SampleDao {
    @Query("DELETE FROM sample")
    fun clear()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entity: DatabaseSample?)

    @Query("SELECT * FROM sample ORDER BY id ASC")
    fun getAll(): LiveData<List<DatabaseSample>>
}