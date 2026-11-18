# Umbiko — community noticeboard (Android)

Umbiko lets people in a neighbourhood share **notices** and **events**, RSVP, comment and receive
**safety alerts** — and it keeps working with no data: everything you do offline is saved on the phone
and synchronised automatically when you reconnect.

> 🎥 **Demo video:** _add your unlisted YouTube link here_
> 📸 **Screenshots:** _add 4–6 images to `docs/screenshots/` and embed them here_

## Features (Part 3 brief → where it lives)

| Requirement | Implementation |
|---|---|
| Register & log in (password protected) | `AuthViewModel`, `LoginScreen`, `RegisterScreen`. Passwords go to the API over HTTPS and are hashed server-side; the device stores only JWTs, in `EncryptedSharedPreferences` (`TokenManager`) |
| Single sign-on | Sign in with Google via Credential Manager (`GoogleSignInHelper`) → `POST /auth/google` |
| Settings | Language, radius, push, biometrics, Wi-Fi-only sync; written to Room first, then synced (`SettingsViewModel`) |
| REST API + database | Retrofit `ApiService` → FastAPI on Render with a hosted database |
| Offline mode with sync | Room cache + outbox, `SyncWorker` (WorkManager), reconnect trigger in `UmbikoApplication` |
| Real-time notifications | Firebase Cloud Messaging (`UmbikoFirebaseMessagingService`) + system notifications (`NotificationHelper`) + in-app log |
| Multi-language | English, isiZulu, Sesotho, Afrikaans (`res/values-*`), switched live with per-app locales (`LocaleManager`) |
| Biometric unlock | `MainActivity` + AndroidX Biometric |
| Data export | Settings → "Export my data" (JSON share sheet) |
| Play Store ready | Signed release config, R8, adaptive icon, `docs/PLAY_STORE_PREP.md` |

## Architecture

```
UI (Jetpack Compose screens)  →  ViewModels  →  UmbikoRepository  →  Room (single source of truth)
                                                       │                    ▲
                                                       └── Retrofit/OkHttp ─┘  (only to refresh / push the outbox)
```

* **Offline-first:** every screen reads from Room. Writes go to Room + an `outbox` table; `SyncWorker`
  drains the outbox in one `POST /sync/push` call.
* **Design:** navy for structure, teal for actions, amber reserved for alerts/offline status.

## Setup

1. Open the project in **Android Studio** (Koala or newer, JDK 17).
2. Create `local.properties` (it is git-ignored) and add:
   ```
   API_BASE_URL=https://your-api.onrender.com/
   GOOGLE_WEB_CLIENT_ID=xxxxxxxx.apps.googleusercontent.com   # optional: enables the Google button
   ```
3. **Push (optional):** create a Firebase project, add the Android app `com.umbiko.app`, download
   `google-services.json` into `app/`. Without it the app still builds; push is simply inactive.
4. Backend changes are listed in `backend_patch/BACKEND_CHANGES.md`.
5. Run on a **physical phone** (the brief does not allow an emulator for the final demo).

## GitHub & GitHub Actions

* Source is committed regularly to this repository (no zip files).
* `.github/workflows/android.yml` runs on every push/PR: it sets up JDK 17, runs the unit tests
  (`AuthValidationTest`, `PostValidationTest`) and builds the debug APK, which is uploaded as an artifact.
* The final commit is tagged: `git tag -a v2.0.0-final-poe -m "Final PoE" && git push origin v2.0.0-final-poe`

## Build a release

```
# keystore.properties (git-ignored): storeFile, storePassword, keyAlias, keyPassword
./gradlew bundleRelease     # AAB for the Play Console
./gradlew assembleRelease   # signed APK as evidence
```

## Release notes

See [RELEASE_NOTES.md](RELEASE_NOTES.md).

## References

* Android Developers (2025) *Per-app language support*. https://developer.android.com/guide/topics/resources/app-languages [Accessed 5 November 2026].
* Android Developers (2025) *Sign in your user with Credential Manager*. https://developer.android.com/identity/sign-in/credential-manager-siwg [Accessed 5 November 2026].
* Android Developers (2025) *Show a biometric authentication dialog*. https://developer.android.com/identity/sign-in/biometric-auth [Accessed 5 November 2026].
* Firebase (2025) *Firebase Cloud Messaging*. https://firebase.google.com/docs/cloud-messaging [Accessed 5 November 2026].
