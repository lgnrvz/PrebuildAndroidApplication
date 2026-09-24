package com.nrvz.prebuildapplication.constants

/**
 * ============================================================================
 *  IntentConst — the keys you use to pass data between screens.
 * ============================================================================
 *
 * ONBOARDING NOTE
 * ---------------
 * Sender  : intent.putExtra(IntentConst.NOTE_ID, note.id)
 * Receiver: intent.getLongExtra(IntentConst.NOTE_ID, 0L)
 *
 * RULE: both sides use the SAME constant. Hand-typing "note_id" in two files is
 * how you get a bug where a screen silently opens with empty data - the sender
 * and receiver never actually agreed on the key, and nothing crashes to tell you.
 *
 * RockyGo's equivalent file has ~90 of these. This is where they go.
 */
object IntentConst {
    const val NOTE_ID = "NOTE_ID"

    /** true when the detail screen is opened to CREATE a note instead of edit one. */
    const val IS_NEW_NOTE = "IS_NEW_NOTE"

    /** Generic keys you will reuse on almost every screen. */
    const val TITLE = "TITLE"
    const val MESSAGE = "MESSAGE"
}
