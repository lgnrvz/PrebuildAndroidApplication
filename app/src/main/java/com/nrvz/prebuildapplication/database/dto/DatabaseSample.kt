package com.nrvz.prebuildapplication.database.dto

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sample")
data class DatabaseSample(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Long? = null,

    @ColumnInfo(name = "name")
    val name: String,
)