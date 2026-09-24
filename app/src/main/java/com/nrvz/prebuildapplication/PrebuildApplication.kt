package com.nrvz.prebuildapplication

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * ============================================================================
 *  PrebuildApplication — the process-wide entry point.
 * ============================================================================
 *
 * ONBOARDING NOTE
 * ---------------
 * This class runs BEFORE any Activity. It is the right place for one-time,
 * app-wide setup: logging, crash reporting, theme defaults, DI bootstrapping.
 *
 * `@HiltAndroidApp` is what starts Hilt. Without it every `@Inject` in the
 * project fails to compile with "cannot be provided without an @Provides".
 * RockyGo's equivalent is `RockyGoApplication`.
 *
 * Declared in AndroidManifest.xml as android:name=".PrebuildApplication".
 *
 * YOUR TURN
 * ---------
 * Add a `Timber.plant()` call for a release build type that reports to
 * Crashlytics instead of Logcat. (RockyGo does exactly this.)
 */
@HiltAndroidApp
class PrebuildApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Never ship debug logging to production users. BuildConfig.DEBUG is
        // false in release builds automatically.
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
