# PrebuildApplication — Android Onboarding Project

A deliberately small, heavily commented Android app for **new developers** joining the team.
It is a working MVVM skeleton (Kotlin + Hilt + DataBinding + Room + Retrofit), built in the same
shape as our production apps, so that the first real ticket you pick up feels familiar.

Read the code top-down. **Every file starts with an `ONBOARDING NOTE` that explains not just
*what* the code does but *why* it is written that way.** If a comment ever contradicts the code,
tell your lead — the comment is the bug.

---

## 1. What the app does

A tiny notes app. It is small on purpose — three screens' worth of concepts in two screens.

- **List screen** (`MainActivity`) — notes from the local database, live search, delete, sync from a server.
- **Editor screen** (`DetailActivity`) — create, edit and delete one note.

It touches every layer you will meet in a real project:

- a Room database (local persistence)
- a Retrofit call to a real public API (`jsonplaceholder.typicode.com`)
- encrypted SharedPreferences
- Hilt dependency injection
- DataBinding (one-way **and** two-way)
- LiveData + coroutines
- a RecyclerView adapter with DiffUtil
- unit tests that run without a device

---

## 2. Getting it running

### Prerequisites

- **Android Studio** (Koala or newer) — it bundles the right JDK and the Android SDK.
- **JDK 17.** The build targets Java 17. `java -version` should report 17.x.
  If Android Studio is installed, it already ships one: point Gradle at it in
  *Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK*.

### Steps

1. Open the project folder in Android Studio and let it sync.
   If it complains `SDK location not found`, copy `local.properties.example` to
   `local.properties` and set `sdk.dir` to your SDK path.
2. Pick the **`app`** run configuration and press Run on an emulator or a real device
   (Android 7.0 / API 24 or newer).
3. You should see three seeded notes. Tap the toolbar overflow → **Sync from server**
   and three more notes arrive from the network.
4. Run the unit tests from the terminal:

   ```bash
   ./gradlew testDebugUnitTest
   ```

   Or right-click a test class in Android Studio → *Run*.

### Build commands worth knowing

```bash
./gradlew assembleDebug            # build a debug APK
./gradlew testDebugUnitTest        # JVM unit tests (fast, no device)
./gradlew connectedDebugAndroidTest # instrumented tests (needs a device/emulator)
./gradlew clean                    # wipe build outputs when something makes no sense
```

---

## 3. Project structure

```
app/src/main/java/com/nrvz/prebuildapplication/
├── PrebuildApplication.kt        @HiltAndroidApp — the process entry point
│
├── constants/                    Values that never change at runtime
│   ├── AppConst.kt               URLs, limits, regex, database/prefs names
│   ├── IntentConst.kt            Keys for data passed between screens
│   └── SharedPrefsConstants.kt   Keys for SharedPreferences
│
├── database/                     LOCAL DATA (Room)
│   ├── AppDatabase.kt            The database + TypeConverters
│   ├── dao/NoteDao.kt            Every SQL statement the app can run
│   └── entity/NoteEntity.kt      The table + entity<->domain mappers
│
├── models/Note.kt                DOMAIN model — what the UI speaks in
│
├── network/                      REMOTE DATA (Retrofit)
│   ├── ApiService.kt             The HTTP API as an interface
│   ├── NetworkResult.kt          safeApiCall + error wrapper
│   └── dto/PostDto.kt            The shape the SERVER sends
│
├── repositories/NoteRepository.kt  The ONE source of truth for notes
│
├── screens/                      UI
│   ├── main/MainActivity.kt      List screen — READ THIS FIRST
│   ├── main/NoteAdapter.kt       RecyclerView + DiffUtil
│   └── detail/DetailActivity.kt  Editor screen
│
├── viewmodels/                   Screen state + decisions
│   ├── MainViewModel.kt
│   └── DetailViewModel.kt
│
└── utilities/
    ├── BindingAdapter.kt         Custom XML attributes (@BindingAdapter)
    ├── di/                       Hilt modules (Database, Network, Prefs)
    ├── extension/                Kotlin extensions for Context / View / String
    └── helpers/                  SharedPrefs, SafeClickListener, click callbacks
```

### Where the same thing lives in our production app

| This project                  | RockyGo (Trackerteer)                                        |
| ----------------------------- | ------------------------------------------------------------ |
| `PrebuildApplication`         | `RockyGoApplication`                                         |
| `constants/`                  | `constants/`                                                 |
| `database/`, `database/entity`| `database/`, `database/entity`                               |
| `models/Note`                 | `domains/` (e.g. `OrderDomain`)                              |
| `network/`                    | `network/` (`AppService`, `ChatService`)                     |
| `repositories/`               | `repositories/`                                              |
| `screens/<feature>/`          | `screens/<feature>/` (~90 activities)                        |
| `viewmodels/`                 | `viewmodels/`                                                |
| `utilities/di/`               | `utilities/di/`                                              |
| `utilities/extension/`        | `utilities/extension/`                                       |
| `utilities/helpers/`          | `utilities/helpers/`                                         |
| `NoteAdapter` (direct)        | `CustomerPromotionItemAdapter` (same style) + `utilities/adapter/SimpleListAdapter` (the shared wrapper) |

The production app additionally has product flavors, Firebase, a chat socket, Stripe/Razorpay,
Google Maps and a `grglib` module. None of that changes the structure you see here — it is the
same skeleton with more rooms added.

---

## 4. The one diagram to remember

```
        USER TAPS "+"
              |
              v
  MainActivity  --->  MainViewModel  --->  NoteRepository  --->  Room / Retrofit / Prefs
   (draw only)        (decide, hold        (the only class
                       screen state)        that knows about
              ^                             data sources)
              |
        LiveData observation
     (the screen redraws itself)
```

Strict rules that follow from that picture:

1. An **Activity** never runs SQL, never makes an HTTP call, never contains a business rule.
2. A **ViewModel** never holds a `Context`, a `View` or an `Activity`.
3. A **Repository** is the only place that knows where data comes from.
4. Nothing above the repository knows whether the data was local or remote.

If you can answer "which layer does this belong in?" before writing the code, you are
already ahead of most first-week commits.

### Trace a single tap, end to end

Open [`NetworkResult.kt`](app/src/main/java/com/nrvz/prebuildapplication/network/NetworkResult.kt)
afterwards and re-trace it yourself:

1. `MainActivity.initEventListener()` wires the toolbar item `action_sync` to `mViewModel.syncFromServer()`.
2. `MainViewModel.syncFromServer()` sets `_isLoading = true`, then calls the repository inside `viewModelScope`.
3. `NoteRepository.syncFromServer()` calls `safeApiCall { apiService.getPosts() }` — a Retrofit call.
4. Every failure mode (no internet, 4xx/5xx, unexpected bug) is converted into `NetworkResult.Error`.
5. On success the DTOs are mapped to entities and written to Room.
6. Room notices the table changed and re-emits the `LiveData` the list is observing.
7. `NoteAdapter.submitList()` diffs old vs new and animates only what changed.

No refresh call. No callback telling the list to reload. No `notifyDataSetChanged()`.

---

## 5. House conventions

These are not preferences — they are the patterns every codebase we own uses, so that anyone can
open any file and know where to look.

- **Field naming:** private fields use an `m` prefix (`mBinding`, `mViewModel`, `mAdapter`).
- **Activity shape:** `onCreate()` calls one private `initConfig()`, which calls, in order,
  `initExtras()` → `initBinding()` → `initAdapter()` → `initEventListener()` → `initRequest()` → `observeViewModel()`.
- **DataBinding:** `DataBindingUtil.setContentView()` and `mBinding.lifecycleOwner = this`.
  Never `findViewById` in a new screen.
- **ViewModel access:** `private val mViewModel: SomeViewModel by viewModels()`.
- **LiveData:** `private val _state` to write, `public val state` to read. The underscore is the signal.
- **Clicks:** `view.setSafeOnClickListener { }` for anything that writes data (blocks double taps).
- **Strings:** never hardcoded — layouts use `@string/`, Kotlin uses `R.string.`.
- **Icons:** vector drawables (`drawable/*.xml`), not PNGs.
- **XML order:** `android:` attributes first, then `app:`, then `tools:`. Ids at the top.
- **Version:** bump `versionName` on **every** push (never `versionCode`) and check it on the
  screen footer. A stale APK has cost this team a full debugging session before.
- **Repos:** every new repository is **private** unless someone explicitly asks otherwise.

---

## 6. Your onboarding exercises

Work up from easy to "this is a real ticket". Each one is deliberately incomplete — the point is
the search, not the copy-paste. Ask questions, but try for 20 minutes first.

**Warm-up — reading**

1. Change the seeded notes in `NoteRepository.seedSampleNotesIfEmpty()` to your own text, run the
   app, and explain why the change only appears if you clear app data (uninstall the app or
   *Settings → Apps → your app → Storage → Clear storage*).
2. Delete a note from the list. Explain, in one sentence, why the list updates without any
   refresh code.

**Intermediate — a small feature**

3. Add a "starred" flag: an `isStarred` column on `NoteEntity`, a star icon on the row, and a
   tap target that toggles it. **You must bump the database version** — see the comment in
   `AppDatabase.kt` about what happens if you forget.
4. Add a `@Query` to `NoteDao` that returns only starred notes, and hook it up to a filter
   (reuse the `switchMap` filter pattern in `MainViewModel`). Do not write SQL by string
   concatenation — use a bound parameter.
5. Make the empty state say something different when a search returns nothing versus when the
   database is genuinely empty. (Hint: two different reasons for an empty list = two states, and
   the ViewModel should expose both.)

**"Real ticket" difficulty**

6. Add a pull-to-refresh gesture to the list using `SwipeRefreshLayout`, wired to
   `syncFromServer()`, with the progress indicator tied to `isLoading`.
7. Add a *sort* option (newest / oldest / title) to the toolbar menu, implemented as another
   `switchMap` over the DAO query.
8. Write an instrumented test in `app/src/androidTest/` that inserts a note through the DAO and
   asserts it comes back from `getById()`. This is the migration-safety test we write for real
   schema changes.
9. Break something on purpose: remove `mBinding.lifecycleOwner = this` from `MainActivity`, run
   the app, and explain the symptom to your lead. This is the single most common data-binding bug
   in the wild, and now you have seen it.

---

## 7. When the build breaks

These are the failures you will actually hit, and what each one means.

| Error message (or symptom) | Cause & fix |
| --- | --- |
| `AttributePrefixUnbound` / `ParseError at [row,col]` | A layout uses an `app:` attribute but the `<layout>` tag does not declare `xmlns:app="http://schemas.android.com/apk/res-auto"`. |
| `The processing instruction target matching "[xX][mM][lL]" is not allowed` | The `<?xml ... ?>` declaration is not on line 1 — usually a comment got inserted above it, or the declaration is duplicated lower in the file. |
| `SDK location not found` | Missing `local.properties`. See `local.properties.example`. |
| `Room cannot verify the data integrity` | A schema changed without bumping `@Database(version = ...)`. Bump it (and write a migration in production). |
| `[Dagger/MissingBinding] NoteDao cannot be provided` | A DAO is being injected somewhere that is not a repository. Add a `@Provides` in `DatabaseModule` — there is a working example there. |
| `does not implement interface GeneratedComponentManagerHolder` | An Activity/ViewModel uses Hilt but the Activity is missing `@AndroidEntryPoint` (or the Application is missing `@HiltAndroidApp`). |
| A layout shows default/empty values | The field is not bound in the row XML, or `executePendingBindings()` is missing at the end of `bind()`. |
| A LiveData-bound view never updates | `mBinding.lifecycleOwner` was not set, **or** the property you bound is read-only `LiveData` while the layout uses `@={...}` two-way binding. |
| A toolbar menu item does nothing | The id in `menu_main.xml` does not match the `R.id` constant in the `when`, or two menu listeners are fighting (see the note in `MainActivity`). |
| `RecyclerView has no LayoutManager` / squashed rows | `inflate(..., parent, false)` was called with `true`. |

---

## 8. Before you push

- [ ] `./gradlew assembleDebug` succeeds — do not push a red build.
- [ ] `./gradlew testDebugUnitTest` passes.
- [ ] You ran the app and clicked through every screen you touched. Twice. Include the back button.
- [ ] `versionName` in `app/build.gradle` is bumped.
- [ ] No hardcoded strings, no hardcoded colors, no `println`, no `Log.d` left behind. (`Timber` or nothing.)
- [ ] You did not commit `local.properties`, `google-services.json` or anything with a keystore.
- [ ] `git pull --rebase` before pushing — someone else may have pushed since you started.
- [ ] The commit message says *why*, not *what*. "Fix crash when note body is empty" beats "changes".

---

## 9. Credits / lineage

This repo started life in Feb 2022 as an empty MVVM prebuild skeleton (Room + secure prefs +
extensions, no ViewModel, `jcenter()` in `settings.gradle`). It was rebuilt as a teaching project:
the skeleton was kept, the broken parts were fixed, and the comments were written so a fresh
graduate can learn the house patterns from the code itself.

Structure modelled on the Trackerteer production app (`RockyGo`, `com.trackerteer.grg`).

---

*Support the author: [patreon.com/c/NRVZ](https://www.patreon.com/c/NRVZ)*
