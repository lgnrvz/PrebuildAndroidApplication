package com.nrvz.prebuildapplication.constants

/**
 * ============================================================================
 *  SharedPrefsConstants — keys for SharedPreferences.
 * ============================================================================
 *
 * ONBOARDING NOTE
 * ---------------
 * Keep ALL preference keys in one file. When you delete a feature, this file is
 * how you find the orphaned keys you should also delete.
 *
 * Naming convention: SP_ prefix, SCREAMING_SNAKE_CASE, same string as the name.
 * The duplicated string looks redundant but it means a typo is a compile error
 * (SP_USER_NAME) rather than a silent read of a key that does not exist
 * ("SP_USERNAME" -> returns null -> you chase a phantom bug).
 */
object SharedPrefsConstants {
    const val SP_IS_FIRST_LAUNCH = "SP_IS_FIRST_LAUNCH"
    const val SP_LAST_SYNC = "SP_LAST_SYNC"
    const val SP_USER_NAME = "SP_USER_NAME"
    const val SP_NOTES_ADDED_COUNT = "SP_NOTES_ADDED_COUNT"
}
