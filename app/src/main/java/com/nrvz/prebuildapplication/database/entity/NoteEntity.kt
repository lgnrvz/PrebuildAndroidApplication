package com.nrvz.prebuildapplication.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nrvz.prebuildapplication.models.Note

/**
 * ============================================================================
 *  NoteEntity — the Room table. One row = one note.
 * ============================================================================
 *
 * ONBOARDING NOTE
 * ---------------
 * @Entity(tableName = "note") -> a SQLite table called `note`.
 * @PrimaryKey(autoGenerate = true) -> SQLite assigns the id for us. Because we
 *   default it to 0L, inserting a brand-new note is just `insert(NoteEntity(...))`
 *   and Room fills the id in.
 * @ColumnInfo(name = "created_at") -> the Kotlin property is `createdAt` but the
 *   COLUMN is `created_at`. Renaming a Kotlin property is then a refactor;
 *   renaming a column is a database migration. Keeping them separate is how you
 *   stay safe. (RockyGo does the same thing.)
 *
 * RULE: never rename a column or change its type without writing a Migration.
 * The skill/setup notes live in database/AppDatabase.kt.
 */
@Entity(tableName = "note")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "body")
    val body: String,

    /**
     * A List<String> cannot be stored in SQLite directly. Room needs a
     * @TypeConverter to squeeze it into a single TEXT column - see
     * StringListConverter in database/AppDatabase.kt.
     */
    @ColumnInfo(name = "tags")
    val tags: List<String> = emptyList(),

    /** true when the row came from the network instead of being typed by hand. */
    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
)

// ============================================================================
//  Mappers: entity <-> domain
// ============================================================================
// Keep them next to the entity they serve. They are 3 lines each; do NOT build
// a mapper class hierarchy for this. RockyGo uses extension functions the same
// way (`fun OrderTable.toDomain(): OrderDomain`).

/** Database row -> what the UI thinks in. */
fun NoteEntity.toDomain(): Note = Note(
    id = id,
    title = title,
    body = body,
    tags = tags,
    isSynced = isSynced,
    createdAt = createdAt,
)

/** UI object -> database row. Called right before insert/update. */
fun Note.toEntity(): NoteEntity = NoteEntity(
    id = id,
    title = title,
    body = body,
    tags = tags,
    isSynced = isSynced,
    createdAt = createdAt,
)
