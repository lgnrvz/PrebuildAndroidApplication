package com.nrvz.prebuildapplication.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.nrvz.prebuildapplication.models.Note
import com.nrvz.prebuildapplication.network.NetworkResult
import com.nrvz.prebuildapplication.repositories.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ============================================================================
 *  MainViewModel — the list screen's brain. It holds NO Android UI references.
 * ============================================================================
 *
 * ONBOARDING NOTE — WHY A VIEWMODEL AT ALL?
 * -----------------------------------------
 * Rotate the device. The Activity is destroyed and recreated, BUT the ViewModel
 * SURVIVES. Anything you keep in the ViewModel is still there - no reload, no
 * lost scroll position, no re-running the network call. Put the same state in an
 * Activity field and rotation wipes it.
 *
 * It also gives the screen a single place to answer: "what is shown right now?"
 * The Activity's only job becomes: draw what the ViewModel says and forward user
 * input back to it. That is the whole of MVVM.
 *
 * @HiltViewModel  -> Hilt builds it, so the constructor can take dependencies.
 * @Inject constructor -> the parameters ARE the dependency list, which makes it
 *                        obvious how much this screen depends on.
 *
 * RULE: a ViewModel never holds a Context, a View, or an Activity reference.
 * Use AndroidViewModel only for the rare case where you truly need the
 * application Context (e.g. to read a resource string).
 *
 * RockyGo equivalent: viewmodels/MainViewModel.kt (much bigger, same shape).
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
) : ViewModel() {

    // ------------------------------------------------------------------------
    //  STATE
    // ------------------------------------------------------------------------
    // CONVENTION: private MutableLiveData to write, public LiveData to read.
    //   private val _notes  : only this class can change it
    //   val notes           : everyone can observe it, nobody can set it
    // Without this, any screen could overwrite the list and you would have to
    // hunt for who did it. The underscore is the house signal for "mutable twin".
    // (RockyGo follows the same convention, e.g. `_isLoading` / `isLoading`.)

    /** What the user typed in the search box. */
    private val _searchQuery = MutableLiveData("")
    val searchQuery: LiveData<String> get() = _searchQuery

    /** true while a sync is in flight - drives the progress bar. */
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    /**
     * One-shot message for a snackbar/toast.
     *
     * WHY THIS IS A LiveData<String?> AND NOT A CALLBACK: it survives rotation, so
     * a message triggered mid-rotation is still shown. The Activity must call
     * [onMessageShown] after displaying it, otherwise the same message reappears
     * on the next rotation. That "event vs state" problem is exactly why one-shot
     * events are awkward in MVVM; the explicit reset below is the plainest fix at
     * this level. (RockyGo uses the same pattern.)
     */
    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> get() = _message

    /**
     * THE FILTER PATTERN — used in every list screen we ship.
     *
     * `switchMap` says: whenever [_searchQuery] changes, DROP the previous source
     * and switch to the new one. So the screen always observes exactly one live
     * query, and Room cancels the old one for us.
     *
     * Doing this with a plain `observe` + a manual re-query is how you end up with
     * three observers racing and stale results on screen.
     *
     * This property declares its own LiveData instead of a get() accessor: it is
     * created once, at construction, and never changes identity. The Activity can
     * observe it immediately.
     */
    val notes: LiveData<List<Note>> = _searchQuery.switchMap { query ->
        noteRepository.observeNotes(query.trim())
    }

    // ------------------------------------------------------------------------
    //  INTENTS (user actions -> state)
    // ------------------------------------------------------------------------

    fun onSearchQueryChanged(query: String) {
        // The debounce itself lives in the layout/Activity (a TextWatcher chunk)
        // rather than in the DAO, because "how fast should search feel" is a UI
        // decision. Here we simply accept the value.
        if (_searchQuery.value == query) return
        _searchQuery.value = query
    }

    /** Called once from the Activity's onCreate. Guarded by a prefs flag. */
    fun seedSampleDataOnFirstLaunch() {
        viewModelScope.launch {
            noteRepository.seedSampleNotesIfEmpty()
        }
    }

    /**
     * Sync from the network.
     *
     * viewModelScope is the coroutine scope tied to THIS ViewModel's lifetime.
     * When the screen is finished and the ViewModel is cleared, the scope is
     * cancelled - so an in-flight request dies instead of trying to touch a dead
     * Activity. Never use GlobalScope for screen work.
     */
    fun syncFromServer() {
        if (_isLoading.value == true) return   // ignore double taps
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = noteRepository.syncFromServer()) {
                is NetworkResult.Success ->
                    _message.value = "Synced ${result.data} notes from the server."

                is NetworkResult.Error ->
                    _message.value = result.message
            }
            _isLoading.value = false
        }
    }

    /**
     * Delete straight from the list row.
     *
     * No result is returned and no callback is needed: Room re-emits the list
     * LiveData, the XML binding pushes the new list into the adapter, DiffUtil
     * animates the row away. THIS is why we do not hand-roll refresh calls.
     */
    fun deleteNote(note: Note) {
        viewModelScope.launch {
            noteRepository.deleteNote(note.id)
            _message.value = "Deleted \"${note.title}\"."
        }
    }

    /** The Activity calls this right after showing [message]. See the note above. */
    fun onMessageShown() {
        _message.value = null
    }

    // Note what is NOT here: no getAllNotes() refresh, no "reload the list".
    // The list re-emits by itself because it is a LiveData fed by Room. Adding a
    // manual reload would be a step backwards.
}
