package com.nrvz.prebuildapplication.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.nrvz.prebuildapplication.constants.SharedPrefsConstants
import com.nrvz.prebuildapplication.database.AppDatabase
import com.nrvz.prebuildapplication.database.entity.NoteEntity
import com.nrvz.prebuildapplication.database.entity.toDomain
import com.nrvz.prebuildapplication.database.entity.toEntity
import com.nrvz.prebuildapplication.models.Note
import com.nrvz.prebuildapplication.network.ApiService
import com.nrvz.prebuildapplication.network.NetworkResult
import com.nrvz.prebuildapplication.network.safeApiCall
import com.nrvz.prebuildapplication.utilities.helpers.SharedPrefs
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ============================================================================
 *  NoteRepository — the ONE source of truth for notes.
 * ============================================================================
 *
 * ONBOARDING NOTE — this is the most important class in an MVVM project.
 *
 * WHAT IT IS: the only place that knows BOTH where data comes from (Room,
 * Retrofit, SharedPreferences) and how to combine them. Everything above it -
 * ViewModels, Activities, adapters - talks to the repository and never touches a
 * DAO or an ApiService directly.
 *
 * WHY THE VIEWMODEL NEVER TOUCHES THE DAO DIRECTLY:
 *   - the ViewModel stays small and testable (swap the repository for a fake)
 *   - "should this read from cache or network?" is answered in exactly one place
 *   - when you add a second data source, only this class changes
 *
 * @Singleton  -> Hilt creates ONE instance for the whole app, shared by every
 *                ViewModel. That is safe because this class holds no UI state.
 * @Inject constructor -> Hilt builds it, so it can be injected anywhere.
 *                The `database` and `apiService` parameters are provided by
 *                DatabaseModule / NetworkModule - you never construct this by hand.
 *
 * Notice: we inject `AppDatabase`, not individual DAOs, and reach the table with
 * `database.noteDao()`. That keeps DatabaseModule short. Add a @Provides for a
 * DAO only when it is injected somewhere that is not a repository.
 *
 * RockyGo equivalent: repositories/AboutUsRepository.kt (it also owns the
 * isLoading / networkResponse LiveData that its ViewModel re-exposes).
 */
@Singleton
class NoteRepository @Inject constructor(
    private val database: AppDatabase,
    private val apiService: ApiService,
    private val sharedPrefs: SharedPrefs,
) {

    private val noteDao = database.noteDao()

    // ------------------------------------------------------------------------
    //  READS (observable)
    // ------------------------------------------------------------------------

    /**
     * The list screen's data.
     *
     * `LiveData.map` transforms what Room emits: the DAO hands us
     * List<NoteEntity> and we hand the UI List<Note>. The mapping runs on the
     * main thread, so keep transforms cheap - never do I/O inside `map`.
     *
     * Because the source is a LiveData fed by Room, this re-emits automatically
     * after ANY insert/update/delete on the `note` table. The list screen needs
     * no refresh call and no result callback from the editor screen.
     */
    fun observeNotes(): LiveData<List<Note>> =
        noteDao.getAll().map { entities -> entities.map(NoteEntity::toDomain) }

    /**
     * Filtered read. An EMPTY query returns everything - that is why the blank
     * check happens here and not in the ViewModel: "what does blank mean" is a
     * data question, so it is answered in the data layer.
     */
    fun observeNotes(query: String): LiveData<List<Note>> =
        if (query.isBlank()) observeNotes()
        else noteDao.search(query).map { entities -> entities.map(NoteEntity::toDomain) }

    /** One-shot read for the detail screen. */
    suspend fun getNote(id: Long): Note? = noteDao.getById(id)?.toDomain()

    suspend fun count(): Int = noteDao.count()

    // ------------------------------------------------------------------------
    //  WRITES
    // ------------------------------------------------------------------------

    /** @return the new row id, because the caller sometimes needs it right away. */
    suspend fun addNote(title: String, body: String, tags: List<String>): Long {
        val id = noteDao.insert(
            NoteEntity(title = title.trim(), body = body.trim(), tags = tags)
        )
        bumpAddedCount()
        return id
    }

    /**
     * @return true when a row was actually changed. `update()` returns the number
     * of affected rows, and 0 is a real answer: it means the note was deleted
     * from another screen while this editor was open. The UI can then say so
     * instead of pretending the save worked.
     */
    suspend fun updateNote(note: Note): Boolean =
        noteDao.update(note.toEntity()) > 0

    suspend fun deleteNote(id: Long): Boolean = noteDao.deleteById(id) > 0

    // ------------------------------------------------------------------------
    //  REMOTE
    // ------------------------------------------------------------------------

    /**
     * Pulls posts from the network and stores them as local notes.
     *
     * ⚠️ "OFFLINE-FIRST" IS A DECISION, NOT A DEFAULT.
     * This example writes the server's data straight into Room and lets the
     * normal LiveData flow update the screen - so the UI code is identical
     * whether the data came from a tap, a cache read or the network.
     * RockyGo goes further and has a sync queue for writes made while offline
     * (see `data/sync/` in the RFID apps). The shape of the answer is always:
     * network -> map Dto to entity -> persist -> let LiveData do the UI work.
     *
     * Note there is NO try/catch here. safeApiCall already converted every
     * failure into a NetworkResult, and we only chose to change behaviour on
     * success. The ViewModel turns the Error branch into a message for the user.
     */
    suspend fun syncFromServer(): NetworkResult<Int> {
        return when (val result = safeApiCall { apiService.getPosts() }) {
            is NetworkResult.Success -> {
                val entities = result.data.map { dto ->
                    NoteEntity(
                        title = dto.title.ifBlank { "Untitled" },
                        body = dto.body,
                        tags = listOf("server"),
                        isSynced = true,
                    )
                }
                noteDao.insertAll(entities)
                sharedPrefs.save(SharedPrefsConstants.SP_LAST_SYNC, System.currentTimeMillis())
                NetworkResult.Success(entities.size)
            }

            is NetworkResult.Error -> result
        }
    }

    // ------------------------------------------------------------------------
    //  FIRST LAUNCH
    // ------------------------------------------------------------------------

    /**
     * Fills the database the first time the app ever runs, so a new developer
     * has something on screen immediately.
     *
     * The "have I already run?" flag lives in SharedPreferences. The guard is
     * redundant with the DAO's own count check - that redundancy is intentional
     * here: prefs can be cleared while the database survives, and vice versa.
     * Belt and braces beats an empty screen with a confused new hire looking at it.
     */
    suspend fun seedSampleNotesIfEmpty() {
        if (noteDao.count() > 0) return
        val now = System.currentTimeMillis()
        noteDao.insertAll(
            listOf(
                NoteEntity(
                    title = "Welcome to the onboarding project",
                    body = "You are reading this from a Room database. Open " +
                        "MainActivity.kt and follow the numbered comments to see " +
                        "how this row got here.",
                    tags = listOf("readme"),
                    createdAt = now,
                ),
                NoteEntity(
                    title = "Try the Sync button",
                    body = "Tap the sync icon in the toolbar to download sample posts " +
                        "from jsonplaceholder.typicode.com and store them as notes.",
                    tags = listOf("network"),
                    createdAt = now - 1_000,
                ),
                NoteEntity(
                    title = "Delete me",
                    body = "Tap any row to open the detail screen and delete it. " +
                        "Watch the list update on its own - that is LiveData, not a refresh call.",
                    tags = listOf("room", "livedata"),
                    createdAt = now - 2_000,
                ),
            )
        )
        sharedPrefs.save(SharedPrefsConstants.SP_IS_FIRST_LAUNCH, false)
    }

    /** Small counter that proves SharedPreferences writes actually persist. */
    private fun bumpAddedCount() {
        val current = sharedPrefs.getInt(SharedPrefsConstants.SP_NOTES_ADDED_COUNT, 0)
        sharedPrefs.save(SharedPrefsConstants.SP_NOTES_ADDED_COUNT, current + 1)
    }

    /** "Last synced" label for the UI. 0L means "never". */
    fun lastSyncAt(): Long =
        sharedPrefs.getLong(SharedPrefsConstants.SP_LAST_SYNC, 0L)
}
