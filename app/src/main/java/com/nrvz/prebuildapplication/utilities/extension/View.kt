package com.nrvz.prebuildapplication.utilities.extension

import android.view.View
import com.nrvz.prebuildapplication.utilities.helpers.SafeClickListener

/**
 * ============================================================================
 *  View extensions — visibility and click helpers.
 * ============================================================================
 *
 * ONBOARDING NOTE
 * ---------------
 * VISIBLE vs INVISIBLE vs GONE - know the difference, it comes up in every review:
 *   VISIBLE   : drawn, takes up space
 *   INVISIBLE : not drawn, STILL takes up space
 *   GONE      : not drawn, takes up NO space (the layout reflows)
 *
 * Picking the wrong one is how labels end up with mystery gaps above them.
 */

fun View.gone() {
    visibility = View.GONE
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

/** `view.isVisible = true/false` reads better in conditions. Gone != invisible. */
fun View.showIf(condition: Boolean) {
    visibility = if (condition) View.VISIBLE else View.GONE
}

/**
 * Attaches a [SafeClickListener] instead of a raw OnClickListener.
 * Use this for anything that writes data - see SafeClickListener's docs for why.
 *
 *     mBinding.buttonSave.setSafeOnClickListener { mViewModel.save() }
 */
fun View.setSafeOnClickListener(onSafeClick: (View) -> Unit) {
    setOnClickListener(SafeClickListener { onSafeClick(it) })
}
