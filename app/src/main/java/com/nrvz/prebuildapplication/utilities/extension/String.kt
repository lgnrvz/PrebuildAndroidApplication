package com.nrvz.prebuildapplication.utilities.extension

import com.nrvz.prebuildapplication.constants.AppConst

/**
 * ============================================================================
 *  String extensions.
 * ============================================================================
 *
 * ONBOARDING NOTE
 * ---------------
 * This regex is copied verbatim from RockyGo's AppConst. It is LOOSE on purpose:
 * it only checks the rough shape `something@something.something`, which is all
 * client-side validation should ever do. The server is the real authority on
 * whether an address exists - never block a user because your regex is stricter
 * than reality.
 *
 * YOUR TURN
 * ---------
 * Write a unit test for this in app/src/test (see NoteMapperTest.kt for the
 * pattern) and try to make it fail. `a@b.c` passes, and so does `a@ @ .`. Is that
 * acceptable? That is the conversation to have with your lead.
 */
fun String.isEmailValid(): Boolean = AppConst.EMAIL_REGEX.toRegex().matches(this)

/** "hello world" -> "Hello world". Guarded so it never throws on an empty string. */
fun String.capitalizeFirst(): String =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

/** Comma-joined tags for display: ["work","urgent"] -> "work, urgent". */
fun List<String>.asTagText(): String = joinToString(", ")

/**
 * "work, urgent" -> ["work","urgent"].
 * Splits, trims, drops empties. This is the inverse of asTagText() and the reason
 * the two live next to each other: round-tripping user input is where bugs hide.
 */
fun String.asTagList(): List<String> =
    split(",").map { it.trim() }.filter { it.isNotEmpty() }
