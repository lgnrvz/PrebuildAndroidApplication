package com.nrvz.prebuildapplication.screens.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nrvz.prebuildapplication.databinding.ItemNoteBinding
import com.nrvz.prebuildapplication.models.Note
import com.nrvz.prebuildapplication.utilities.extension.setSafeOnClickListener
import com.nrvz.prebuildapplication.utilities.helpers.NoteRecyclerClick

/**
 * ============================================================================
 *  NoteAdapter — turns a List<Note> into RecyclerView rows.
 * ============================================================================
 *
 * ONBOARDING NOTE
 * ---------------
 * WE EXTEND ListAdapter, NOT RecyclerView.Adapter. ListAdapter brings:
 *   - an internal copy of the list (so external mutation cannot desync the UI)
 *   - submitList(); it diffs old vs new on a background thread
 *   - automatic, correctly-animated row changes
 *
 * The consequence: NEVER call notifyDataSetChanged(). If you feel the urge, you
 * probably passed a mutated list instead of a new one - DiffUtil compares by
 * `equals`, so it cannot see a change that happened inside the same object.
 *
 * THE DiffUtil CONTRACT - get these two right and everything else follows:
 *   areItemsTheSame()     -> same VISUAL ROW? Usually `id == id`.
 *                            false = "this is a different row" (insert/remove)
 *   areContentsTheSame()  -> same CONTENT? Usually `oldItem == newItem`
 *                            (data classes give you equals() for free).
 *                            false = "same row, redraw it" (content change)
 * Mixing them up produces the classic bug where an edited row does not repaint.
 *
 * The click callbacks arrive as a [NoteRecyclerClick] holder. The adapter knows
 * WHEN something was tapped; the Activity decides what that MEANS. See
 * utilities/helpers/RecyclerClickHelper.kt.
 *
 * RockyGo has both this direct style (CustomerPromotionItemAdapter) and a shared
 * generic wrapper (utilities/adapter/SimpleListAdapter). Read the direct version
 * first - the wrapper is just this class with the copying factored out.
 */
class NoteAdapter(
    private val clickListener: NoteRecyclerClick,
) : ListAdapter<Note, NoteAdapter.ViewHolder>(DIFF_CALLBACK) {

    companion object {
        /**
         * Kept in a companion object so the object is created once per class, not
         * once per adapter instance. DiffUtil callbacks are stateless; there is no
         * reason to allocate a new one every time a list screen is built.
         */
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Note>() {
            override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean =
                oldItem == newItem   // data class equals(): every property compared
        }
    }

    /**
     * `inner class` (not a nested class) because it needs `clickListener` from the
     * enclosing adapter instance. That is the one case where inner is correct -
     * otherwise prefer a normal nested class, which holds no outer reference.
     */
    inner class ViewHolder(
        private val binding: ItemNoteBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Note) {
            // Hand the row's data to the layout. `note` is the <variable> declared
            // in item_note.xml; every @{note.xxx} expression in that file now
            // resolves against this object.
            binding.note = item

            // Forces pending binding expressions to be applied NOW. Without it the
            // values land at the end of the frame, which causes a visible flicker
            // on recycled rows. Always call it at the end of bind().
            binding.executePendingBindings()

            // setSafeOnClickListener (not setOnClickListener) so a double tap on a
            // row cannot open two detail screens.
            binding.root.setSafeOnClickListener { clickListener.onClick(item) }
            binding.imageButtonDelete.setSafeOnClickListener {
                clickListener.onDeleteClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // The 3-arg inflate signature: (inflater, parent, attachToRoot = FALSE).
        // Passing `true` would attach the row to the RecyclerView before it is
        // measured - the classic "RecyclerView has no LayoutManager" / squashed-row
        // bug. Always false here.
        val binding = ItemNoteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
