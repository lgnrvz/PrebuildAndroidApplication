package com.nrvz.prebuildapplication.screens.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.nrvz.prebuildapplication.BuildConfig
import com.nrvz.prebuildapplication.R
import com.nrvz.prebuildapplication.constants.AppConst
import com.nrvz.prebuildapplication.constants.IntentConst
import com.nrvz.prebuildapplication.databinding.ActivityMainBinding
import com.nrvz.prebuildapplication.models.Note
import com.nrvz.prebuildapplication.screens.detail.DetailActivity
import com.nrvz.prebuildapplication.utilities.extension.showIf
import com.nrvz.prebuildapplication.utilities.helpers.NoteRecyclerClick
import com.nrvz.prebuildapplication.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ============================================================================
 *  MainActivity — the list screen. READ THIS FILE FIRST, IN THIS ORDER.
 * ============================================================================
 *
 * ONBOARDING NOTE — THE HOUSE ACTIVITY PATTERN
 * --------------------------------------------
 * Every screen we ship follows this shape, so a new teammate can open ANY screen
 * and know where to look:
 *
 *      onCreate()
 *        └── initConfig()          <- private, the only thing onCreate calls
 *              ├── initExtras()      read intent extras (if any)
 *              ├── initBinding()     DataBindingUtil.setContentView + lifecycleOwner
 *              ├── initAdapter()     RecyclerView + adapter + layout manager
 *              ├── initEventListener() clicks, text watchers, swipe, menus
 *              ├── initRequest()     kick off initial loads
 *              └── observeViewModel() observe LiveData, update the views
 *
 * Keep the order. initRequest() before observeViewModel() is deliberate: start the
 * work, THEN start watching, so you cannot miss an emission that arrives
 * immediately. (LiveData replays its last value to a new observer, so this is
 * belt-and-braces - but a consistent order is what makes screens predictable.)
 *
 * WHO DOES WHAT IN MVVM
 * ---------------------
 *   Activity  : draw what the ViewModel says, forward user input back to it.
 *               NOTHING ELSE. No SQL, no HTTP, no business rules.
 *   ViewModel : hold screen state, decide what to do, expose LiveData.
 *   Repository: the only place that knows about Room / Retrofit / prefs.
 *
 * If you can look at onCreate and say "this screen lists notes and syncs them",
 * the split is right.
 *
 * @AndroidEntryPoint is REQUIRED for `by viewModels()` to inject a
 * @HiltViewModel. Forget it and you get a runtime crash:
 *     "Given component holder class ... does not implement interface
 *      GeneratedComponentManagerHolder"
 *
 * RockyGo screens extend a BaseActivity that also handles maintenance/session
 * banners. We start from AppCompatActivity here so you can see the plain pattern
 * first - the extra behaviour is just more finish() logic bolted on top.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    // `m` prefix = house convention for private fields (mBinding, mViewModel,
    // mAdapter). You will see it in every Trackerteer codebase.
    private lateinit var mBinding: ActivityMainBinding
    private lateinit var mAdapter: NoteAdapter

    // `by viewModels()` is a lazy delegate: the ViewModel is created on first
    // access and survives rotation. `private val` + `m` prefix, per convention.
    private val mViewModel: MainViewModel by viewModels()

    /** Holds the in-flight debounce job so a new keystroke can cancel the old one. */
    private var mSearchJob: Job? = null

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initConfig()
    }

    // =========================================================================
    //  Setup
    // =========================================================================

    private fun initConfig() {
        initBinding()
        initAdapter()
        initEventListener()
        initRequest()
        observeViewModel()
    }

    /**
     * DataBindingUtil.setContentView() instead of setContentView().
     *
     * It inflates the layout, generates the binding, AND sets it as the content
     * view. Note that we never call findViewById() in this project - mBinding.xxx
     * is checked at compile time. Rename a view id and the build fails instead of
     * returning null at runtime.
     */
    private fun initBinding() {
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        // MANDATORY for LiveData to be observed from XML. Without it, any
        // @{viewModel.somethingLiveData} expression silently never updates.
        // This is the single most common "data binding does not work" cause.
        mBinding.lifecycleOwner = this

        // Hand the ViewModel to the layout's <variable name="viewModel">.
        // After this line, @{viewModel.xxx} inside activity_main.xml resolves.
        mBinding.viewModel = mViewModel

        setSupportActionBar(mBinding.toolbar)

        // Proves which build is installed. HOUSE RULE: bump versionName on every
        // push and check it here - never trust "I installed the new APK".
        mBinding.textViewVersion.text = getString(R.string.version_format, BuildConfig.VERSION_NAME)

        Timber.tag(TAG).d("MainActivity created, version=%s", BuildConfig.VERSION_NAME)
    }

    private fun initAdapter() {
        // The adapter gets behaviour, not a Context. It fires these lambdas and
        // never learns what a DetailActivity is - that keeps it testable.
        mAdapter = NoteAdapter(
            NoteRecyclerClick(
                // Named arguments, so the mapping is obvious at the call site.
                // The property names are `click` / `deleteClick` (not `onClick`) -
                // see the pitfall note in NoteRecyclerClick.
                click = { note -> openDetail(note) },
                deleteClick = { note -> confirmDelete(note) },
            )
        )
        mBinding.recyclerViewNotes.adapter = mAdapter
        mBinding.recyclerViewNotes.setHasFixedSize(true)
        // LayoutManager is set in XML (app:layoutManager) so the whole list
        // configuration is visible in one place. Setting it here works too - just
        // pick one and stay consistent.
    }

    private fun initEventListener() {
        // FAB -> new note
        mBinding.fabAddNote.setOnClickListener { openDetail(null) }

        // Toolbar menu: inflate + click listener directly on the toolbar. This is
        // the single-toolbar screen pattern. Screens with fragments use
        // onCreateOptionsMenu/onOptionsItemSelected instead - both are in use, just
        // do not mix them on one screen (that is when "my menu item never fires"
        // happens: two listeners, one silently wins).
        mBinding.toolbar.inflateMenu(R.menu.menu_main)
        mBinding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_sync -> {
                    mViewModel.syncFromServer()
                    true
                }
                else -> false
            }
        }

        /**
         * SEARCH DEBOUNCE.
         *
         * Typing "hello" fires this 5 times. Without a debounce, that is 5 queries
         * and 5 list rebuilds (and on a network search, 5 HTTP calls). Instead we
         * cancel the previous job and wait 300ms - only the pause between words
         * survives, so we run one query.
         *
         * lifecycleScope cancels automatically when the Activity is destroyed, so
         * there is no leak. Use lifecycleScope (or viewModelScope) - never
         * GlobalScope.
         */
        mBinding.editTextSearch.doAfterTextChanged { text ->
            mSearchJob?.cancel()
            mSearchJob = lifecycleScope.launch {
                delay(AppConst.SEARCH_DEBOUNCE_MS)
                mViewModel.onSearchQueryChanged(text?.toString().orEmpty())
            }
        }
    }

    private fun initRequest() {
        // First launch only - the repository guards it with a prefs flag + a row
        // count, so calling this on every onCreate is harmless.
        mViewModel.seedSampleDataOnFirstLaunch()
    }

    // =========================================================================
    //  Observation - the only place that touches views with data
    // =========================================================================

    private fun observeViewModel() {

        // The list itself is bound in XML (app:list_data="@{viewModel.notes}"), so
        // there is no submitList() here - see utilities/BindingAdapter.kt. This
        // observer exists purely to drive the EMPTY STATE, which needs a boolean,
        // not a list.
        //
        // Both observers fire on every change; keeping the "what does the user see"
        // decision OUT of the adapter is what lets the empty state live here.
        mViewModel.notes.observe(this) { notes ->
            mBinding.textViewEmpty.showIf(notes.isEmpty())
        }

        mViewModel.isLoading.observe(this) { isLoading ->
            mBinding.progressBar.showIf(isLoading)
        }

        /**
         * One-shot message.
         *
         * The `?: return@observe` guard matters: this LiveData becomes null right
         * after we show it (see onMessageShown below), and without the guard a
         * rotation would replay the value and pop the snackbar again.
         *
         * Do NOT use `observe` for one-shot navigation (finish(), startActivity()).
         * Re-delivery on rotation causes double navigation - a real bug class. Use a
         * channel/SingleLiveEvent for that. Here a snackbar is idempotent enough
         * that the explicit reset is fine.
         */
        mViewModel.message.observe(this) { message ->
            if (message.isNullOrBlank()) return@observe
            Snackbar.make(mBinding.root, message, Snackbar.LENGTH_LONG).show()
            mViewModel.onMessageShown()
        }
    }

    // =========================================================================
    //  Navigation
    // =========================================================================

    /**
     * Same screen, two modes.
     *
     * The id travels in the Intent, keyed by a shared constant. The detail screen
     * reads it with the SAME constant - a typo in either string is a bug where the
     * screen opens empty with no error. That is why IntentConst exists.
     */
    private fun openDetail(note: Note?) {
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra(IntentConst.NOTE_ID, note?.id ?: 0L)
        intent.putExtra(IntentConst.IS_NEW_NOTE, note == null)

        // A "back" affordance the user can see: the toolbar title tells them which
        // mode they are in. (The detail screen shows the real title.)
        startActivity(intent)
    }

    /**
     * Confirm before a destructive action. Every delete in every app we ship asks
     * first - users mis-tap constantly, and an undo implementation is more work
     * than a dialog.
     */
    private fun confirmDelete(note: Note) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_delete_title)
            .setMessage(getString(R.string.dialog_delete_message, note.title))
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.action_delete) { _, _ -> mViewModel.deleteNote(note) }
            .show()
    }

    override fun onDestroy() {
        // Not required (lifecycleScope cancels itself), but explicit is kind to the
        // next reader: this screen owns a running job.
        mSearchJob?.cancel()
        super.onDestroy()
    }
}
