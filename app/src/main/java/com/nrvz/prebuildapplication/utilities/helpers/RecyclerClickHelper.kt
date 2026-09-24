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
    /**
     * Row tapped -> open the detail screen.
     *
     * PITFALL, AND IT BIT US HERE: do NOT name this property `onClick` when there
     * is also an `onClick(...)` METHOD below. `fun onClick(model: Note) = onClick(model)`
     * then resolves to itself, and the compiler reports
     *     "type checking has run into a recursive problem"
     * which does not sound like a naming problem at all. RockyGo names the
     * property `click` and the method `onClick` - that is why. Keep it that way.
     */
    val click: (Note) -> Unit,
    /** Delete icon tapped -> remove the row. */
    val deleteClick: (Note) -> Unit,
) {
    fun onClick(model: Note) = click(model)
    fun onDeleteClick(model: Note) = deleteClick(model)
}
