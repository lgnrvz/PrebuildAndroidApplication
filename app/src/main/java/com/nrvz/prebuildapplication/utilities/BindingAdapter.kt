package com.nrvz.prebuildapplication.utilities

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.google.android.material.textfield.TextInputLayout
import androidx.recyclerview.widget.RecyclerView
import com.nrvz.prebuildapplication.models.Note
import com.nrvz.prebuildapplication.screens.main.NoteAdapter
import com.nrvz.prebuildapplication.utilities.extension.asTagText

/**
 * ============================================================================
 *  BindingAdapter — custom attributes you can use from XML.
 * ============================================================================
 *
 * ONBOARDING NOTE
 * ---------------
 * Data binding lets XML call Kotlin. You declare a function with @BindingAdapter
 * and the attribute name, then use that name in a layout:
 *
 *     @BindingAdapter("tagText")
 *     fun bindTagText(textView: TextView, tags: List<String>?)
 *
 *     <!-- layout -->
 *     <TextView app:tagText="@{note.tags}" />
 *
 * WHY THIS IS USEFUL: layout XML has no loops, no if/else worth using, and no
 * string formatting. A binding adapter moves that logic into Kotlin where it can
 * be tested - and keeps the layout declarative.
 *
 * RULES THAT WILL BITE YOU
 * ------------------------
 * 1. Declare `xmlns:app="http://schemas.android.com/apk/res-auto"` on the <layout>
 *    tag. Using an `app:` attribute without it fails the resource compile with
 *    "AttributePrefixUnbound".
 * 2. A binding adapter runs when the bound value CHANGES. It will not re-run just
 *    because the object's internals mutated - LiveData/observable fields are what
 *    trigger it. If a view refuses to update, this is the first thing to check.
 * 3. Keep them dumb. An adapter that starts a network call is a bug generator.
 *
 * RockyGo's equivalent has ~30 of these (`list_data`, `set_notification_count`,
 * `set_discount`, ...). Same idea, much bigger file.
 */

/**
 * Renders a note's tags as one line: ["room","livedata"] -> "room, livedata".
 * Hides the view entirely when there are no tags, so the row does not reserve
 * blank space (GONE, not INVISIBLE - see View.kt for the difference).
 */
@BindingAdapter("tagText")
fun bindTagText(textView: TextView, tags: List<String>?) {
    if (tags.isNullOrEmpty()) {
        textView.visibility = TextView.GONE
    } else {
        textView.visibility = TextView.VISIBLE
        textView.text = tags.asTagText()
    }
}

/**
 * Feeds a list straight into a RecyclerView from XML:
 *
 *     <androidx.recyclerview.widget.RecyclerView
 *         app:list_data="@{viewModel.notes}" />
 *
 * Data binding unwraps the LiveData for you - it passes the CURRENT
 * List<Note> - and calls this again every time the LiveData emits.
 *
 * Two things to notice:
 *   - `as?` : the adapter must already be set (in initAdapter()) or this does
 *     nothing. Null-cast instead of `!!` so a wiring mistake shows an empty list
 *     rather than crashing.
 *   - submitList + DiffUtil means the RecyclerView diffs the old and new lists and
 *     animates only real changes. No notifyDataSetChanged(), ever.
 */
@BindingAdapter("list_data")
fun bindListData(recyclerView: RecyclerView, data: List<Note>?) {
    (recyclerView.adapter as? NoteAdapter)?.submitList(data.orEmpty())
}

/**
 * Shows/hides the error text under a form field:
 *
 *     <TextInputLayout app:errorText="@{viewModel.titleError}">
 *
 * Material's TextInputLayout has setError(), but binding to `app:error` is not
 * reliable across versions - a custom adapter with an explicitly-named attribute
 * is the version-proof way, and it is what you should reach for when a widget's
 * setter has no matching declared XML attribute.
 *
 * Passing null clears the error. Note the field must have `app:errorEnabled="true"`
 * (or have shown an error once) or the layout will not have room for it.
 */
@BindingAdapter("errorText")
fun bindErrorText(textInputLayout: TextInputLayout, error: String?) {
    textInputLayout.error = error
}
