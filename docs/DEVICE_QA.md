# Device QA

Date: 4 October 2026  
Device: Samsung SM-A156E  
Android: 16 (API 36)  
Display: 1080×2340  
Build: Musex 0.1.0, versionCode 1, debug signed

## Passed checks

- APK streamed installation succeeded.
- MainActivity cold launch completed and became the top resumed activity.
- Home rendered correctly with the five bottom-navigation destinations and Thai text.
- Manual playlist creation persisted and updated the Playlists screen.
- `ACTION_SEND text/plain` opened Quick Add with a parsed YouTube video ID.
- Saving a video produced a Pending local Song and selected playlist membership.
- Force-stop followed by cold launch retained the Song in Room.
- Sharing the same video through a `/shorts/{id}` URL left exactly one visible Song row.
- Playlist detail displayed the retained Song and the safe “Remove from playlist” action.
- Editing the local title to `NightSong` saved successfully and literal Search returned it.
- Recent `AndroidRuntime` log inspection contained no fatal entries after the test run.

## Not verified / external configuration required

- Google authorization, YouTube playlist import and incremental sync require a configured Google Cloud project, consent screen and Android OAuth client tied to the final signing certificate.
- Official embedded-player policy/behavior and YouTube Music app handoff require dedicated player implementation and acceptance testing.
- Release signing, backup/restore, speech recognition and notification permission paths remain outside this smoke run.

The test data (`NightSong` and the test playlist) remains only on the connected device’s debug-app data and is not committed to Git.

