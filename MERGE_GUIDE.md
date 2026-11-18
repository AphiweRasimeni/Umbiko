# MERGE GUIDE (delete this file before you push to GitHub)

This folder contains **only new and changed files**. Files you already sent me are unchanged and must
stay as they are — copy them into the same paths.

## 1. Unchanged — keep your existing copy
`data/ConnectivityObserver`, `data/local/{AppDatabase, CommentEntity, NotificationDao, NotificationLogEntity, OutboxDao,
OutboxEntity, PostDao, PostEntity, SettingsDao, SettingsEntity}`, `data/remote/{AuthDtos (RegisterRequestDto… ApiErrorDto),
NetworkModule, PostDtos (EventDetailInDto… SyncPullResponseDto), SafeApiCall, TokenAuthenticator}`,
`di/{LocalAppContainer, SimpleViewModelFactory}`, `domain/{ApiResult, NewPostDraft/PostValidation}`, `sync/SyncWorker`,
`ui/components/*`, `ui/nav/*`, `ui/theme/*`, `ui/screens/auth/RegisterScreen`, `ui/screens/create/CreatePostViewModel`,
`ui/screens/events/*`, `ui/screens/home/*`, `ui/screens/notifications/*`, `ui/screens/post/*`.

## 2. Replace your old version with these
`MainActivity`, `UmbikoApplication`, `di/AppContainer`, `data/TokenManager`, `data/local/CommentDao`,
`data/remote/{ApiService, AuthInterceptor}`, `data/repository/UmbikoRepository`, `sync/SyncScheduler`,
`ui/screens/auth/{AuthViewModel, LoginScreen}`, `ui/screens/create/CreatePostScreen`,
`ui/screens/settings/{SettingsScreen, SettingsViewModel}`.

## 3. New
`auth/GoogleSignInHelper`, `data/remote/SsoDtos`, `push/*`, `util/LocaleManager`, all of `res/`, `AndroidManifest.xml`,
Gradle files, tests, `.github/workflows/android.yml`, README, RELEASE_NOTES, `docs/`, `backend_patch/`.

## 4. Things I could not generate
* `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar` — in Android Studio run
  `gradle wrapper --gradle-version 8.9` (or let Studio create them) and **commit them**; GitHub Actions needs them.
* `google-services.json`, your keystore, `local.properties` — secrets; see README.
* If you already have a Manifest, merge it with mine (permissions, FCM service, locale service, `localeConfig`, `allowBackup=false`).
* If your own `build.gradle.kts` differs, keep your versions where they work and just add the dependencies from mine.
