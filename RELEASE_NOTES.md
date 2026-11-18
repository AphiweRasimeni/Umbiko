# Release notes

## v2.0.0 — Final PoE (since the Part 2 prototype)

### New features
* **Multi-language:** English, isiZulu, Sesotho and Afrikaans, switchable in Settings and applied instantly.
* **Single sign-on:** "Continue with Google" on the login screen.
* **Push notifications:** Firebase Cloud Messaging delivers safety alerts and updates; they appear as system
  notifications and in the in-app Notifications screen. A "Push notifications" switch in Settings controls them.
* **Biometric unlock:** optional fingerprint/face/device-PIN lock when opening the app.
* **Wi-Fi-only sync:** the setting now genuinely controls when queued changes are uploaded.
* **Categories on posts:** choose a category (e.g. safety alerts) when creating a post.
* **Export my data:** share a JSON copy of your profile, settings and posts.

### Improvements and fixes
* After a sync, local drafts and comments are reconciled with the server copy (no duplicates).
* Posts the server rejects are removed from the feed and you are told why.
* Logging out now clears all local data (and tries to upload unsynced work first).
* Notification when a sync finishes; no "0 items published" noise.
* Release build: R8 shrinking, adaptive icon, signing configuration, backups disabled for encrypted storage.

### Engineering
* Unit tests for registration and post validation; GitHub Actions builds and tests every push.

> **Edit before submitting:** list which of your Part 1 "innovative / user-defined" features
> (e.g. offline-first sync, safety alerts, biometric unlock) were added in this version.
