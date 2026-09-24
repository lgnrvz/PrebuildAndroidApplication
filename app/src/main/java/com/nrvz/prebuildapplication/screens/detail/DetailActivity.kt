package com.nrvz.prebuildapplication.screens.detail

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nrvz.prebuildapplication.R
import com.nrvz.prebuildapplication.constants.IntentConst
import com.nrvz.prebuildapplication.databinding.ActivityDetailBinding
import com.nrvz.prebuildapplication.utilities.extension.setSafeOnClickListener
import com.nrvz.prebuildapplication.utilities.extension.showIf
import com.nrvz.prebuildapplication.viewmodels.DetailViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * ============================================================================
 *  DetailActivity — one screen, two jobs: create a note and edit a note.
 * ============================================================================
 *
 * ONBOARDING NOTE
 * ---------------
 * Notice how little this Activity does. It reads an id from the Intent, hands it
 * to the ViewModel, and reacts to two LiveData signals. Everything else -
 * validation, mapping, insert vs update, "the row disappeared" - is in
 * DetailViewModel. That is the point of the pattern: this file is short enough to
 * read in 60 seconds, and its logic is unit-testable without an emulator.
 *
 * THE TWO-WAY BINDING IN THIS SCREEN
 * ----------------------------------
 * activity_detail.xml binds the form fields with @={viewModel.title}. Typing in
 * the field writes straight into the ViewModel - no TextWatcher, no reading
 * EditTexts on save. When the ViewModel loads an existing note, the field updates
 * itself. One binding, both directions.
 *
 * This is exactly why DetailViewModel exposes MutableLiveData for title/body/tags
 * instead of the usual private+public pair. Re-read that comment if the binding
 * ever stops updating - a read-only LiveData silently breaks two-way binding.
 *
 * The same @AndroidEntryPoint / initConfig() / observeViewModel() structure as
 * MainActivity. Same shape, different screen - that predictability is the point.
 */
@AndroidEntryPoint
class DetailActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityDetailBinding
    private val mViewModel: DetailViewModel by viewModels()

    /** 0L = create mode. Ids from Room start at 1. */
    private var mNoteId = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initConfig()
    }

    private fun initConfig() {
        initExtras()
        initBinding()
        initEventListener()
        initRequest()
        observeViewModel()
    }

    /**
     * Read what the caller passed. ALWAYS supply a default:
     * getLongExtra(key, 0L) returns 0L when the key is missing, so a wiring
     * mistake degrades to "new note" instead of crashing.
     */
    private fun initExtras() {
        mNoteId = intent.getLongExtra(IntentConst.NOTE_ID, 0L)
    }

    private fun initBinding() {
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_detail)
        mBinding.lifecycleOwner = this
        mBinding.viewModel = mViewModel

        // Required before supportActionBar is usable, and it is what makes the
        // Toolbar show the title + up arrow. The theme is NoActionBar, so this
        // toolbar IS the action bar for this screen.
        setSupportActionBar(mBinding.toolbar)

        // Title reflects the mode. Small thing, but an editor that says "Edit note"
        // when it is creating one is how users lose confidence in a build.
        val isNew = intent.getBooleanExtra(IntentConst.IS_NEW_NOTE, true)
        supportActionBar?.title =
            getString(if (isNew) R.string.title_new_note else R.string.title_edit_note)

        // Deleting a note that does not exist yet is meaningless - hide it in
        // create mode.
        mBinding.buttonDelete.showIf(!isNew)

        // Toolbar back arrow.
        mBinding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun initEventListener() {
        mBinding.buttonSave.setSafeOnClickListener { mViewModel.save() }

        mBinding.buttonDelete.setSafeOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_delete_title)
                .setMessage(R.string.dialog_delete_message_simple)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_delete) { _, _ -> mViewModel.delete() }
                .show()
        }
    }

    private fun initRequest() {
        // Hand the id to the ViewModel. It decides whether that means "load" or
        // "leave the form empty". The Activity does NOT branch on that - branching
        // on data is the ViewModel's job.
        mViewModel.loadNote(mNoteId)
    }

    private fun observeViewModel() {

        // Validation errors are bound in XML via app:errorText="@{viewModel.titleError}"
        // (see utilities/BindingAdapter.kt), so there is no observer for it here.
        // Worth comparing the two styles:
        //   XML binding  -> declarative, the layout says what it needs
        //   observer     -> imperative, easier to debug with a breakpoint
        // Both are correct. Pick per screen, not per mood.

        /**
         * Navigation on a state change.
         *
         * This IS a legitimate use of observe() for navigation, because the flag is
         * only ever set by a completed write and the screen is finished immediately
         * after - there is no second render to double-fire into.
         *
         * If you add more outcomes to a screen, switch to a SingleLiveEvent/Channel
         * instead. See the warning in MainActivity.observeViewModel().
         */
        mViewModel.isFinished.observe(this) { isFinished ->
            if (isFinished) finish()
        }
    }

    /**
     * Optional: return the saved note to the caller instead of relying on Room's
     * LiveData to refresh the list.
     *
     * We deliberately do NOT - the list re-emits by itself. This is here so you can
     * see what the alternative looks like, because you will meet codebases that use
     * startActivityForResult + setResult for exactly this.
     */
    @Suppress("unused")
    private fun sendResultBack(noteId: Long) {
        setResult(RESULT_OK, Intent().putExtra(IntentConst.NOTE_ID, noteId))
    }
}
