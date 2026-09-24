package com.nrvz.prebuildapplication.utilities.di

import android.content.Context
import androidx.room.Room
import com.nrvz.prebuildapplication.constants.AppConst
import com.nrvz.prebuildapplication.database.AppDatabase
import com.nrvz.prebuildapplication.database.dao.NoteDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * ============================================================================
 *  DatabaseModule — how Hilt builds the database.
 * ============================================================================
 *
 * ONBOARDING NOTE
 * ---------------
 * ANATOMY OF A MODULE:
 *   @Module                 -> a bag of instructions for Hilt
 *   @InstallIn(SingletonComponent::class) -> the bindings live as long as the app
 *                            process does. Alternatives: ActivityComponent,
 *                            FragmentComponent, ViewModelComponent.
 *   @Provides fun x(): X    -> "when someone asks for an X, run this method"
 *   @Singleton              -> "run it ONCE, then reuse the result"
 *
 * @ApplicationContext is not decoration. Injecting an Activity Context into a
 * @Singleton leaks the Activity (it lives forever). Hilt gives you the
 * application-scoped Context here and that is the only safe option at this scope.
 *
 * TWO LESSONS BAKED INTO THIS FILE
 * --------------------------------
 * 1. ONE AppDatabase instance, always. Two Room builders on the same file cause
 *    SQLite locking errors that look random and are miserable to debug.
 *
 * 2. `fallbackToDestructiveMigration()` is deliberate. Without it, a device whose
 *    database is on a version with no migration path throws
 *    IllegalStateException on launch - the app dies on open, for real users, on
 *    an upgrade you shipped. The fallback recreates the database instead (DATA IS
 *    LOST, which is why it is only acceptable for local/cache data).
 *    When you DO write a real migration, keep this as the safety net:
 *       .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
 *       .fallbackToDestructiveMigration()
 *    RockyGo does exactly that with ~28 registered migrations.
 */
@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase =
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            AppConst.DATABASE_NAME,
        )
            .fallbackToDestructiveMigration()
            .build()

    /**
     * The DAO is provided for cases where something other than a Repository needs
     * it (a ViewModel, a Worker, a Service). NoteRepository injects AppDatabase
     * and calls `database.noteDao()`, so it does NOT need this method - but
     * without a @Provides here, `@Inject lateinit var noteDao: NoteDao` anywhere
     * else fails with:
     *     [Dagger/MissingBinding] NoteDao cannot be provided without an @Provides
     * That error means: add a method exactly like this one.
     */
    @Provides
    fun provideNoteDao(database: AppDatabase): NoteDao = database.noteDao()
}
