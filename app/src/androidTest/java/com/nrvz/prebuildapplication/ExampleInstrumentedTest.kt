package com.nrvz.prebuildapplication

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * ============================================================================
 *  ExampleInstrumentedTest — the template for tests that need a DEVICE.
 * ============================================================================
 *
 * ONBOARDING NOTE
 * ---------------
 * COMPARE WITH app/src/test/: those tests run on your laptop's JVM in
 * milliseconds. These run on an emulator or a real phone, and are 100x slower.
 *
 * USE THIS FOLDER ONLY WHEN YOU ACTUALLY NEED THE DEVICE:
 *   - Room migrations (needs a real SQLite file)
 *   - Espresso / Compose UI assertions
 *   - permissions, notifications, sensors, hardware
 *
 * RUN IT:
 *     ./gradlew connectedDebugAndroidTest      (device must be attached)
 *
 * The check below is the standard sanity test: it asserts the app is running
 * under the applicationId we expect. If this fails, your test APK and app APK are
 * built from different flavor/applicationId settings - useful to know BEFORE you
 * spend an hour on a mysterious UI failure.
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @Test
    fun useAppContext() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.nrvz.prebuildapplication", appContext.packageName)
    }
}
