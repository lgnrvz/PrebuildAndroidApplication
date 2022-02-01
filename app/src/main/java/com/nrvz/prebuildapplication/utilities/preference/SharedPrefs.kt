package com.nrvz.prebuildapplication.utilities.preference

import com.securepreferences.SecurePreferences

class SharedPrefs(private val mSecurePrefs: SecurePreferences) {

    fun getString(key: String): String? {
        return mSecurePrefs.getString(key, null)
    }

    fun getString(key: String, defaultValue: String): String {
        return mSecurePrefs.getString(key, defaultValue)!!
    }

    fun getBoolean(key: String): Boolean {
        return mSecurePrefs.getBoolean(key, false)
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return mSecurePrefs.getBoolean(key, defaultValue)
    }

    fun getLong(key: String): Long {
        return mSecurePrefs.getLong(key, 0L)
    }

    fun getLong(key: String, defaultValue: Long): Long {
        return mSecurePrefs.getLong(key, defaultValue)
    }

    fun getInt(key: String): Int {
        return mSecurePrefs.getInt(key, 0)
    }

    fun getInt(key: String, defaultValue: Int): Int {
        return mSecurePrefs.getInt(key, defaultValue)
    }

    fun save(key: String, value: Any) {
        when (value) {
            is String -> {
                mSecurePrefs.edit().putString(key, value).apply()
            }
            is Int -> {
                mSecurePrefs.edit().putInt(key, value).apply()
            }
            is Long -> {
                mSecurePrefs.edit().putLong(key, value).apply()
            }
            is Boolean -> {
                mSecurePrefs.edit().putBoolean(key, value).apply()
            }
        }
    }

    fun clearAll() {
        mSecurePrefs.edit().clear().apply()
    }

    fun clearKey(key: String) {
        mSecurePrefs.edit().remove(key).apply()
    }
}