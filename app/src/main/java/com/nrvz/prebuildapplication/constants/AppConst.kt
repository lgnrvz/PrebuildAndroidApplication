package com.nrvz.prebuildapplication.constants

/**
 * ============================================================================
 *  AppConst — magic values that never change at runtime.
 * ============================================================================
 *
 * ONBOARDING NOTE
 * ---------------
 * RULE: if you are about to type a literal more than once, it belongs here.
 * Never scatter "https://..." or "note_id" strings through the codebase.
 *
 * What goes where (house convention, same as RockyGo):
 *   AppConst                -> app-wide constants, regex, URL paths, limits
 *   IntentConst             -> keys used when passing data between screens
 *   SharedPrefsConstants    -> keys used for SharedPreferences
 *
 * `object` in Kotlin = a singleton. There is exactly one AppConst, and you use
 * it as AppConst.EMAIL_REGEX - no instance, no `new`.
 */
object AppConst {

    /** Retrofit needs a base URL ending in "/". Comes from build.gradle BuildConfig. */
    const val BASE_API = "posts/"

    /** Room database file name. Changing this orphans all existing user data. */
    const val DATABASE_NAME = "prebuild_database"

    /** SecurePreferences file name. */
    const val SECURE_PREFS_NAME = "secure_preference"

    /**
     * Password used to encrypt SharedPreferences.
     *
     * !! TEACHING POINT, READ THIS !!
     * RockyGo passes an EMPTY STRING ("") here. That silently disables the
     * encryption - the "secure" prefs are then readable by anyone with a rooted
     * device or an unencrypted backup. Do not copy that. In a real app this
     * value comes from a keystore-backed secret, not from source code.
     * It lives in AppConst here ONLY because this is a tutorial app with no
     * real user data.
     */
    const val SECURE_PREFS_PASSWORD = "onboarding-demo-key"

    /** Delay before we send a search query, so we don't hit the DB on every keystroke. */
    const val SEARCH_DEBOUNCE_MS = 300L

    /** Simple email check. Ported from RockyGo's AppConst - same regex, same caveat. */
    const val EMAIL_REGEX = "^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})"

    /** How many sample rows we insert the very first time the app runs. */
    const val SAMPLE_NOTE_COUNT = 3
}
