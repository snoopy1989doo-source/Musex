# Musex

Musex is a native Android, local-first personal music library for remembering and reopening songs from YouTube and YouTube Music. It is built with Kotlin, Jetpack Compose, Room, coroutines and WorkManager. Musex does not download audio and does not pretend to know external playback progress.

## Current delivery: Phase 1 foundation

- Steam-Mobile-inspired dark navy Compose UI with large cover treatments and blue accents.
- Five primary destinations: Home, Library, Search, Playlists and Settings.
- Local Room schema for songs, playlists, unique playlist membership, tags and notes.
- One song per YouTube video ID, regardless of supported URL variant.
- Local title/artist overrides, Memory Note, Favorite, Last Played and Play Count.
- Manual playlist creation and multi-playlist assignment from Quick Add.
- Playlist detail with safe membership removal (the Song remains in Library).
- Local title/artist/Memory Note editing and playlist membership editing.
- Library views for Recently Added, Song A–Z, Artist A–Z, Recently Played and Most Played.
- `ACTION_SEND` text sharing into the Quick Add dialog.
- Literal Thai/English search across local title, artist, notes and tags.
- Car Mode foundation with simplified large controls.
- Explicit `Pending` metadata and `Not configured` YouTube states; no fabricated provider success.

Provider metadata jobs, thumbnails, custom covers, smart playlist rules, Google authorization/import/sync, backup/restore and final embedded-player feasibility require further implementation and, where applicable, real Google/OAuth configuration. A visible button is not used to claim those integrations are complete.

## Device smoke test

Tested on 4 October 2026 using a Samsung SM-A156E running Android 16 / API 36 at 1080×2340. Installation, cold launch, playlist creation, share Quick Add, process-death persistence, URL-variant dedupe, playlist membership display, local-title editing and local search passed. No AndroidRuntime fatal entry was present in the captured post-test log window. See [`docs/DEVICE_QA.md`](docs/DEVICE_QA.md).

## Build

Prerequisites:

- Android Studio with Android SDK 35 installed.
- JDK 17 or newer (AGP 8.13 requires a modern JDK; JDK 21 is also supported by the local setup).
- Network access for the first Gradle dependency resolution.

Open this directory in Android Studio, allow Gradle sync to complete, then run the `app` configuration. From a terminal with Gradle available:

```powershell
./gradlew.bat testDebugUnitTest assembleDebug
```

The debug APK is produced at `app/build/outputs/apk/debug/app-debug.apk`.

No release keystore or OAuth credential is committed. Before any connected YouTube feature is enabled, create a Google Cloud project, enable YouTube Data API v3, configure the OAuth consent screen, and register an Android OAuth client matching `com.snoopy.musex` plus the actual signing certificate SHA-1. Do not put a client secret in the APK.

## Integration boundaries

- Local features work without a Google account.
- YouTube Data API is for metadata and playlist operations, not audio streaming.
- External launches are counted as actions, not verified listening or completion.
- A future in-app player must use an official, visible YouTube player and comply with player requirements.
- Imported remote links must be reverified against the connected account after restore.

Official references reviewed for this baseline:

- [Android Compose navigation](https://developer.android.com/develop/ui/compose/navigation)
- [Room](https://developer.android.com/training/data-storage/room)
- [WorkManager constraints](https://developer.android.com/develop/background-work/background-tasks/persistent/getting-started/define-work)
- [Google Authorization for Android](https://developers.google.com/identity/authorization/android)
- [YouTube Data API Android quickstart](https://developers.google.com/youtube/v3/quickstart/android)
- [YouTube OAuth for installed apps](https://developers.google.com/youtube/v3/guides/auth/installed-apps)

## Data and backup policy

The current database is local and excluded from Android Auto Backup until the versioned `.musex` export/restore path is implemented. The planned archive contains `manifest.json`, `library.json`, and uploaded covers. It excludes OAuth tokens, passwords, audio and thumbnail cache.
