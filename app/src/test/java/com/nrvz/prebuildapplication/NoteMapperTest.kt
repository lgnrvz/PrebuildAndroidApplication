package com.nrvz.prebuildapplication

import com.nrvz.prebuildapplication.database.entity.NoteEntity
import com.nrvz.prebuildapplication.database.entity.toDomain
import com.nrvz.prebuildapplication.database.entity.toEntity
import com.nrvz.prebuildapplication.models.Note
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * ============================================================================
 *  NoteMapperTest — a real unit test, runnable WITHOUT a device or emulator.
 * ============================================================================
 *
 * ONBOARDING NOTE
 * ---------------
 * WHY THIS IS POSSIBLE: Note, NoteEntity and the mappers between them use
 * nothing from the Android framework. No Context, no View, no SQLite handle.
 * They are plain Kotlin objects, so a JVM test can exercise them.
 *
 * THAT IS THE REWARD FOR THE LAYERING. Every piece of business logic you move out
 * of an Activity and into a ViewModel / model / mapper becomes testable here in
 * milliseconds instead of seconds on an emulator. If a class is hard to test, the
 * design is usually the problem - not the test.
 *
 * RUN IT:
 *     ./gradlew testDebugUnitTest
 * Reports land in app/build/reports/tests/testDebugUnitTest/index.html
 *
 * WHAT BELONGS IN app/src/test/  -> pure Kotlin/JVM logic (THIS file)
 * WHAT BELONGS IN app/src/androidTest/ -> anything needing a device, DB or UI
 *                                         (instrumented tests, Espresso)
 *
 * Keep tests small and named for the BEHAVIOUR, not the method. When one fails,
 * the name should already tell you what broke.
 */
class NoteMapperTest {

    private val entity = NoteEntity(
        id = 7L,
        title = "Onboarding",
        body = "Read the comments.",
        tags = listOf("room", "livedata"),
        isSynced = true,
        createdAt = 1_700_000_000_000L,
    )

    @Test
    fun `entity maps to domain with every field preserved`() {
        val note = entity.toDomain()

        assertEquals(7L, note.id)
        assertEquals("Onboarding", note.title)
        assertEquals("Read the comments.", note.body)
        assertEquals(listOf("room", "livedata"), note.tags)
        assertTrue(note.isSynced)
        assertEquals(1_700_000_000_000L, note.createdAt)
    }

    @Test
    fun `domain maps back to entity unchanged`() {
        // Round-tripping is the bug you will actually hit: someone adds a field to
        // the domain model, updates one mapper, and forgets the other. Then a save
        // silently drops the new field. This test pins both directions.
        val roundTripped = entity.toDomain().toEntity()

        assertEquals(entity, roundTripped)
    }

    @Test
    fun `preview takes the first non-blank line and trims it`() {
        val note = Note(
            title = "t",
            body = "\n\n   \nFirst real line\nSecond line that should not appear",
        )

        assertEquals("First real line", note.preview)
    }

    @Test
    fun `preview truncates long bodies to 120 characters`() {
        val note = Note(title = "t", body = "x".repeat(500))

        assertEquals(120, note.preview.length)
    }

    @Test
    fun `preview is empty when the body is blank`() {
        val note = Note(title = "t", body = "\n\n")

        assertEquals("", note.preview)
    }
}
