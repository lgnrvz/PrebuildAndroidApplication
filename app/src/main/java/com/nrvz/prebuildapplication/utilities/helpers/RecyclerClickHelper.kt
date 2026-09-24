package com.nrvz.prebuildapplication.utilities.helpers

import com.nrvz.prebuildapplication.models.Note

/**
 * ============================================================================
 *  RecyclerClickHelper — how a RecyclerView talks back to the Activity.
 * ============================================================================
 *
 * ONBOARDING NOTE
 * ---------------
 * THE PROBLEM: a RecyclerView adapter must not hold a reference to the Activity,
 * or it leaks it, and it must not know about navigation, or it becomes untestable.
 *
 * THE PATTERN: the adapter takes a small holder object of lambdas. The Activity
 * constructs it and decides what a tap MEANS. The adapter only decides when a tap
 * HAPPENED. Swapping "go to detail" for "show a bottom sheet" is then a one-line
 * change in the Activity, with zero adapter edits.
 *
 * `(Note) -> Unit` is a function type: "something that accepts a Note and returns
 * nothing". The Activity passes a lambda; the adapter calls it.
 *
 * WHY A CLASS AND NOT JUST TWO LAMBDAS? Because data binding, and because it
 * names the callbacks. `clickListener.onDeleteClick(note)` reads better than
 * `deleteListener(note)`, and more callbacks can be added without changing the
 * adapter's constructor everywhere.
 *
 * RockyGo has a whole file of these (FoodRecyclerClick, CartRecyclerClick,
 * AddressRecyclerClick...) - this is the trimmed-down version.
 */
class NoteRecyclerClick(
    /** Row tapped -> open the detail screen. */
    val onClick: (Note) -> Unit,
    /** Delete icon tapped -> remove the row. */
    val onDeleteClick: (Note) -> Unit,
) {
    fun onClick(model: Note) = onClick(model)
    fun onDeleteClick(model: Note) = onDeleteClick(model)
}
