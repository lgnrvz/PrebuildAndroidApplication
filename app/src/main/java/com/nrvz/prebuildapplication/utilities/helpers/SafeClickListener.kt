package com.nrvz.prebuildapplication.utilities.helpers

import android.os.SystemClock
import android.view.View

/**
 * ============================================================================
 *  SafeClickListener — blocks accidental double taps.
 * ============================================================================
 *
 * ONBOARDING NOTE
 * ---------------
 * THE PROBLEM: users tap buttons twice. Two taps on "Save" = two rows. Two taps
 * on "Submit order" = two orders. On cheap touch panels a single press fires
 * onClick multiple times on its own.
 *
 * THE FIX: record the time of the last accepted click and ignore anything within
 * [defaultInterval] ms.
 *
 * SystemClock.elapsedRealtime() counts milliseconds since BOOT, including time in
 * sleep. That makes it the correct clock for measuring intervals. Do NOT use
 * System.currentTimeMillis() here: it is wall-clock time, and a user changing
 * their timezone/time can make it jump forwards or backwards.
 *
 * USAGE: myView.setSafeOnClickListener { doTheThing() }
 *        (the extension function lives in utilities/extension/View.kt)
 *
 * RockyGo has the same class plus a `RecyclerClickCoolDown` variant for list rows.
 */
class SafeClickListener(
    private var defaultInterval: Int = 1000,
    private val onSafeClick: (View) -> Unit,
) : View.OnClickListener {

    private var lastTimeClicked: Long = 0L

    override fun onClick(v: View) {
        if (SystemClock.elapsedRealtime() - lastTimeClicked < defaultInterval) {
            // Too soon - this is the tail of a double tap. Swallow it.
            return
        }
        lastTimeClicked = SystemClock.elapsedRealtime()
        onSafeClick(v)
    }
}
