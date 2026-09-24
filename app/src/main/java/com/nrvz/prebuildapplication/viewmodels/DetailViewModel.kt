package com.nrvz.prebuildapplication.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nrvz.prebuildapplication.models.Note
import com.nrvz.prebuildapplication.repositories.NoteRepository
import com.nrvz.prebuildapplication.utilities.extension.asTagList
import com.nrvz.prebuildapplication.utilities.extension.asTagText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ============================================================================
 *  DetailViewModel — the editor screen (create / edit / delete one note).
 * ============================================================================
 *
 * ONBOARDING NOTE — WHY ARE THESE `MutableLiveData` PUBLIC?
 * --------------------------------------------------------
 * Everywhere else in this project state is `private _x` + `public x` so only the
 * ViewModel can write it. Here it is deliberately different, because the layout
 * uses TWO-WAY data binding:
 *
 *     <TextInputEditText android:text="@={viewModel.title}" />
 *                                                  ^ the "=" means "write back"
 *
 * Two-way binding needs to be able to SET the value when the user types, so the
 * property it binds to has to be writable. Exposing MutableLiveData is the
 * standard way to do that, and it removes all the `addTextChangedListener`
 * boilerplate you would otherwise need for three fields.
 *
 * The trade-off: any observer could also write these. Keep two-way-bound fields
 * to simple form inputs, and never two-way bind something the ViewModel also
 * computes - the two writers will fight.
 *
 * (This is also why `title` and `body` here are MutableLiveData while `message`
 * below stays private.)
 */
@HiltViewModel
class DetailViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
) : ViewModel() {

    /** Two-way bound to the title field. */
    val title = MutableLiveData("")

    /** Two-way bound to the body field. */
    val body = MutableLiveData("")

    /** Two-way bound to the tags field. Comma-separated text, converted on save. */
    val tags = MutableLiveData("")

    /** Error string shown under the title field. null = no error. */
    private val _titleError = MutableLiveData<String?>(null)
    val titleError: LiveData<String?> get() = _titleError

    /** true -> the Activity should finish() (save or delete succeeded). */
    private val _isFinished = MutableLiveData(false)
    val isFinished: LiveData<Boolean> get() = _isFinished

    /** 0L means "we are creating a new note" - Room hands out real ids starting at 1. */
    private var mNoteId: Long = 0L

    val isEditing: Boolean get() = mNoteId > 0L

    /**
     * Loads an existing note into the form.
     *
     * The id is passed IN from the Activity rather than read from a SavedStateHandle.
     * Both work. A SavedStateHandle (injected as a constructor parameter) reads the
     * Activity's intent extras automatically and survives process death - worth
     * using once you are comfortable; the explicit version below is easier to trace
     * while you are learning.
     */
    fun loadNote(id: Long) {
        mNoteId = id
        if (id <= 0L) return   // new note: leave the form empty

        viewModelScope.launch {
            noteRepository.getNote(id)?.let { note ->
                // LiveData.setValue must run on the main thread. viewModelScope
                // defaults to Dispatchers.Main.immediate, so we are already there.
                // (From a background thread you would use postValue, which is
                // asynchronous - and that is why mixing the two is a classic bug.)
                title.value = note.title
                body.value = note.body
                tags.value = note.tags.asTagText()
            }
        }
    }

    /**
     * Insert or update, then finish.
     *
     * VALIDATION LIVES HERE, not in the Activity. The rule "a note needs a title"
     * is a business rule: it must hold no matter which screen calls save(), and it
     * should be unit-testable without an Activity.
     */
    fun save() {
        val titleText = title.value.orEmpty().trim()
        val bodyText = body.value.orEmpty().trim()

        if (titleText.isEmpty()) {
            _titleError.value = "A title is required."
            return
        }
        _titleError.value = null

        viewModelScope.launch {
            if (isEditing) {
                val updated = noteRepository.updateNote(
                    Note(
                        id = mNoteId,
                        title = titleText,
                        body = bodyText,
                        tags = tags.value.orEmpty().asTagList(),
                        // Preserve the "came from the server" flag: an edit does
                        // not make a server row locally-created.
                        isSynced = false,
                    )
                )
                // A false here means the row vanished (deleted on another screen
                // while this editor was open). Surfacing that is better than
                // silently closing as if the save worked.
                if (!updated) _titleError.value = "This note no longer exists."
                else _isFinished.value = true
            } else {
                noteRepository.addNote(
                    title = titleText,
                    body = bodyText,
                    tags = tags.value.orEmpty().asTagList(),
                )
                _isFinished.value = true
            }
        }
    }

    fun delete() {
        if (!isEditing) {
            _isFinished.value = true
            return
        }
        viewModelScope.launch {
            noteRepository.deleteNote(mNoteId)
            _isFinished.value = true
        }
    }
}
