package com.nrvz.prebuildapplication.utilities.helpers

import com.securepreferences.SecurePreferences
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ============================================================================
 *  SharedPrefs — the app's only way to read/write preferences.
 * ============================================================================
 *
 * ONBOARDING NOTE
 * ---------------
 * WHY WRAP IT AT ALL? Because `prefs.getString("KEY", null)` scattered across 30
 * files is unmaintainable, and because the wrapper gives you:
 *   - one place to change HOW preferences are stored (plain vs encrypted)
 *   - typed getters with sensible defaults, so no `!!` at the call site
 *   - a testable seam: inject a fake SharedPrefs in a unit test
 *
 * @Singleton makes Hilt create it once and hand the same instance to every
 * Repository and ViewModel that asks for it. Compare that to the old pattern in
 * this repo (a `lateinit var` + `synchronized` block in the file) - DI does the
 * same job with less code and no global mutable state.
 *
 * Backed by SecurePreferences (com.scottyab:secure-preferences-lib), which
 * encrypts values before writing them to disk. The password comes from the DI
 * module - read the warning there about a very common mistake.
 *
 * RockyGo equivalent: utilities/helpers/SharedPrefs.kt (identical API).
 */
@Singleton
class SharedPrefs @Inject constructor(
    private val mSecurePrefs: SecurePreferences,
) {

    fun getString(key: String): String? = mSecurePrefs.getString(key, null)

    fun getString(key: String, defaultValue: String): String =
        mSecurePrefs.getString(key, defaultValue) ?: defaultValue

    fun getBoolean(key: String): Boolean = mSecurePrefs.getBoolean(key, false)

    fun getBoolean(key: String, defaultValue: Boolean): Boolean =
        mSecurePrefs.getBoolean(key, defaultValue)

    fun getInt(key: String): Int = mSecurePrefs.getInt(key, 0)

    fun getInt(key: String, defaultValue: Int): Int =
        mSecurePrefs.getInt(key, defaultValue)

    fun getLong(key: String): Long = mSecurePrefs.getLong(key, 0L)

    fun getLong(key: String, defaultValue: Long): Long =
        mSecurePrefs.getLong(key, defaultValue)

    /**
     * Save any supported value.
     *
     * Pitfall to be aware of: the `when` below has NO `else`, so passing a Double
     * or a List SILENTLY does nothing - the call compiles and your data quietly
     * never saves. If you need another type, add a branch (and a getter above).
     * RockyGo has the same signature; treat it as a known edge, not a bug to
     * "fix" on your first day.
     */
    fun save(key: String, value: Any) {
        when (value) {
            is String -> mSecurePrefs.edit().putString(key, value).apply()
            is Int -> mSecurePrefs.edit().putInt(key, value).apply()
            is Long -> mSecurePrefs.edit().putLong(key, value).apply()
            is Boolean -> mSecurePrefs.edit().putBoolean(key, value).apply()
        }
    }

    /** Wipes everything. Careful - this is the app's "factory reset". */
    fun clearAll() = mSecurePrefs.edit().clear().apply()

    /** Removes a single key. Use this when they log out, not clearAll(). */
    fun clearKey(key: String) = mSecurePrefs.edit().remove(key).apply()
}
