# Google Play preparation (evidence for the rubric)

## Checklist
- [ ] Create a keystore (Android Studio → Build → Generate Signed Bundle) and fill `keystore.properties`
- [ ] `./gradlew bundleRelease` → upload `app-release.aab` to Play Console **Internal testing** (screenshot it)
- [ ] `./gradlew assembleRelease` → signed APK (evidence)
- [ ] **App icon 512×512 PNG** (Android Studio → Image Asset → export) — the adaptive icon in the project is the launcher icon
- [ ] **Feature graphic 1024×500 PNG**
- [ ] **≥ 2 phone screenshots** (take 4–8 on your physical phone: login, feed, offline banner, create post, settings languages)
- [ ] **Privacy policy URL** (required — the app handles names, emails and phone numbers). Host a page on GitHub Pages
- [ ] Data safety form: collects name, email, phone, user content; encrypted in transit; user can request deletion
- [ ] Content rating questionnaire; target audience 18+ (or 13+)
- [ ] Target API 35 ✔ (set in `app/build.gradle.kts`); `versionCode`/`versionName` bumped for each upload
- [ ] Screenshot of the app in Play Console (and the store listing, if published)

## Draft store listing
**Title:** Umbiko — Community Noticeboard

**Short description (80):** Local notices, events and safety alerts — even when you're offline.

**Full description:**
Umbiko keeps your neighbourhood connected. Post notices, organise events, RSVP and comment, and get safety
alerts from people nearby. Write a post with no data and Umbiko saves it and publishes it automatically when you
reconnect. Available in English, isiZulu, Sesotho and Afrikaans. Sign in with email or Google and protect the app
with your fingerprint.
