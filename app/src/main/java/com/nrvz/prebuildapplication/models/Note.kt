package com.nrvz.prebuildapplication.models

/**
 * ============================================================================
 *  Note — the DOMAIN model. This is what your UI layer speaks in.
 * ============================================================================
 *
 * ONBOARDING NOTE
 * ---------------
 * Why not just use the Room entity everywhere? Separation of concerns:
 *
 *   NoteEntity (database/entity)  -> shaped for SQLite. Column names, indices,
 *                                    auto-generated ids, migration concerns.
 *   Note       (models)           -> shaped for the UI. Clean names, computed
 *                                    properties, no persistence details.
 *
 * When the backend renames a column, or you add a cache table, only the entity
 * and the mappers change. Every Activity, adapter and ViewModel keeps working.
 *
 * That mapping happens in [com.nrvz.prebuildapplication.database.entity.toDomain]
 * and [toEntity] - see NoteEntity.kt.
 *
 * RockyGo calls this layer `domains/` (e.g. OrderDomain). Same idea.
 */
data class Note(
    val id: Long = 0L,
    val title: String,
    val body: String,
    val tags: List<String> = emptyList(),
    val isSynced: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
) {
    /**
     * A computed property - lives on the domain model so BOTH the adapter and the
     * detail screen show the same preview text. If this logic lived in two
     * layouts, they would eventually disagree.
     */
    val preview: String
        get() = body.lineSequence().firstOrNull { it.isNotBlank() }?.take(120) ?: ""
}
