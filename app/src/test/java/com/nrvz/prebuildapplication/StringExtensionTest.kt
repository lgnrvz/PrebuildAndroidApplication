package com.nrvz.prebuildapplication

import com.nrvz.prebuildapplication.utilities.extension.asTagList
import com.nrvz.prebuildapplication.utilities.extension.asTagText
import com.nrvz.prebuildapplication.utilities.extension.capitalizeFirst
import com.nrvz.prebuildapplication.utilities.extension.isEmailValid
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * ============================================================================
 *  StringExtensionTest — testing the utilities layer.
 * ============================================================================
 *
 * ONBOARDING NOTE
 * ---------------
 * This file exists to show you the SHAPE of a good test: arrange, act, assert,
 * one behaviour per test, and a name that reads as a sentence.
 *
 * It also documents a real limitation instead of hiding it. Look at
 * `email validation is intentionally loose` below - the loose cases are asserted
 * ON PURPOSE, so nobody "fixes" the regex and breaks a screen that relied on it.
 * Encode what the code DOES, then argue about whether it should.
 *
 * YOUR TURN
 * ---------
 * Add a case for a tag string with a trailing comma ("work,") and see what
 * asTagList() does with it. Is the result what a user would expect?
 */
class StringExtensionTest {

    // ---- tags: the round-trip pair ----

    @Test
    fun `asTagText joins tags with a comma and a space`() {
        assertEquals("room, livedata", listOf("room", "livedata").asTagText())
    }

    @Test
    fun `asTagText on an empty list is an empty string`() {
        assertEquals("", emptyList<String>().asTagText())
    }

    @Test
    fun `asTagList trims each entry and drops blanks`() {
        assertEquals(listOf("work", "urgent"), " work , urgent ,, ".asTagList())
    }

    @Test
    fun `asTagList on an empty string returns an empty list`() {
        assertTrue("".asTagList().isEmpty())
    }

    @Test
    fun `tags survive a full round trip`() {
        val original = listOf("work", "urgent")

        assertEquals(original, original.asTagText().asTagList())
    }

    // ---- capitalization ----

    @Test
    fun `capitalizeFirst upper-cases only the first character`() {
        assertEquals("Hello world", "hello world".capitalizeFirst())
    }

    @Test
    fun `capitalizeFirst does not throw on an empty string`() {
        // replaceFirstChar on "" is safe in Kotlin, but this pins the guarantee so a
        // future rewrite to `this[0].uppercase() + substring(1)` fails loudly here
        // instead of crashing in front of a user.
        assertEquals("", "".capitalizeFirst())
    }

    // ---- email ----

    @Test
    fun `email validation accepts a normal address`() {
        assertTrue("louigie.narvaez@trackerteer.com".isEmailValid())
    }

    @Test
    fun `email validation rejects a missing at sign`() {
        assertFalse("not-an-email".isEmailValid())
    }

    @Test
    fun `email validation is intentionally loose`() {
        // DOCUMENTED LIMITATION, not a bug. The regex is ported verbatim from
        // RockyGo's AppConst and only checks the rough shape. The server is the
        // real authority. If you tighten this, tell your lead first.
        assertFalse("a@b".isEmailValid())            // no dot
        assertFalse("@b.com".isEmailValid())         // must START with a letter
    }
}
