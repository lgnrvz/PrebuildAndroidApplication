package com.nrvz.prebuildapplication.utilities.di

import android.content.Context
import com.nrvz.prebuildapplication.constants.AppConst
import com.securepreferences.SecurePreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * ============================================================================
 *  SharedPreferenceModule — provides the encrypted preferences instance.
 * ============================================================================
 *
 * ONBOARDING NOTE
 * ---------------
 * ⚠️ THE PASS-WORD PARAMETER IS THE WHOLE POINT OF THIS FILE.
 *
 * SecurePreferences(context, password, fileName) encrypts values with `password`.
 * RockyGo's SharedPreferenceModule passes an EMPTY STRING:
 *
 *     return SecurePreferences(context, "", "secure_preference")   // RockyGo
 *
 * With an empty password the encryption is effectively a no-op - the file is
 * readable, and "secure" is a false comfort. This project passes a real
 * (if dummy) value instead, and you should flag this pattern if you ever copy a
 * module that passes "".
 *
 * Where SHOULD the password come from in a real app?
 *   1. Android Keystore, generated on first run and never stored in the APK, or
 *   2. a build-time secret from a gitignored secrets.properties / CI variable
 *      (see BuildConfig.BASE_API_URL for the mechanism).
 * Never a literal you committed, and never "".
 */
@Module
@InstallIn(SingletonComponent::class)
class SharedPreferenceModule {

    @Provides
    @Singleton
    fun provideSecurePreferences(
        @ApplicationContext context: Context,
    ): SecurePreferences =
        SecurePreferences(
            context,
            AppConst.SECURE_PREFS_PASSWORD,
            AppConst.SECURE_PREFS_NAME,
        )
}
