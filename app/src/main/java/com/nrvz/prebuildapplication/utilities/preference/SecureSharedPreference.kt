package com.nrvz.prebuildapplication.utilities.preference

import android.content.Context
import com.securepreferences.SecurePreferences

private lateinit var SECURED_INSTANCE: SecurePreferences

fun getSecurePrefs(context: Context): SecurePreferences {
    synchronized(SecurePreferences::class.java) {
        if (!::SECURED_INSTANCE.isInitialized) {
            SECURED_INSTANCE = SecurePreferences(context, "", "secure_preference.xml")
        }
    }
    return SECURED_INSTANCE
}