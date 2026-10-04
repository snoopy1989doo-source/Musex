# Implementation status

Updated: 4 October 2026

## Complete in the current foundation

- Native Android project and Compose visual system.
- Room v1 local schema with foreign keys and globally unique video IDs.
- URL validation and canonicalization for supported YouTube hosts.
- Pending-first Quick Add and share intent entry.
- Deduplicated add with additional playlist/tag organization.
- Local playlists, Favorites, notes, overrides, play/open counters and literal search.
- Empty/error/configuration states that do not impersonate provider success.
- Unit tests for URL parsing and host rejection.

## Next implementation phases

1. Persisted WorkManager metadata queue with bounded thumbnail cache and retry/edit UI.
2. Playlist detail, atomic Move versus Add, drag custom order, all view sorts, smart rules, custom cover storage, richer Can't Remember filters and speech-recognition fallback.
3. Google authorization, paginated import and writable-owner verification using real credentials and test accounts.
4. Durable sync baseline/operations, conflict preview and incremental reconciliation.
5. Versioned `.musex` backup/restore with atomic validation, reminders, device QA and official embedded-player feasibility.

## External requirements / blockers

- Google Cloud project, enabled YouTube Data API, configured consent screen and Android OAuth client.
- Final Android package/signing certificate identity.
- A physical target device for share sheet, YouTube/YouTube Music intent, speech recognition and player-policy QA.
- Release signing material for a distributable release APK. Secrets must remain outside Git.

