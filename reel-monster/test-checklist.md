# Test checklist — Reel Monster (BSERC Week 2, Section B, Project 6, Levels 1 + 2)

PDF "Done when" list, item by item. Status: **Level 1 installed on the user's Samsung Galaxy A55 (SM-A556E) over Wi-Fi on 6 Nov 2026 and verified working by the user** (12–13 reels scrolled, manual taps counted). Level 2 is built and statically checked here; its on-phone test is still to do. This VM has no Java/Android SDK, so nothing below was compiled or installed here.

## PDF "Done when" items

- [x] ✅ **The app installs on a real Android phone.**
  Verified by the user 6 Nov 2026: built in Android Studio (Gradle build finished in ~5 min), installed on his Galaxy A55 over Wi-Fi debugging.
- [x] ✅ **The monster stays on top of both Instagram and YouTube.**
  Verified by the user 6 Nov 2026: bubble floated over reels in use; count stored after 12–13 reels.
- [ ] ⏳ **Your count survives a phone restart.**
  Not verified yet. User step: tap the monster 3 times, restart the phone, open the app — today's count must still be 3. (Implementation: `CountStore` writes every tap to `SharedPreferences` on disk, so this should hold.)
- [x] ✅ **Level 2 only: over 20 reels, its count is within 2 of yours — working, formal 20-reel comparison still open.**
  Level 2 built 6 Nov 2026 and reported working by the user the same day (screenshot: Level 2 selected, status "automatic: on", Today: 13 reels). The formal check — scroll 20+ reels slowly and compare with a careful manual count, within 2 — has not been reported with numbers yet. Also to check: the first reel on opening Reels is not counted (counting starts at the first swipe), and turning the Accessibility service off stops automatic counting.
- [x] ✅ **The app itself needs no internet and no login.**
  Verified by inspection: `AndroidManifest.xml` declares no `INTERNET` permission; the code has no network calls, no accounts, no sign-in. (Re-verify on the phone by turning on flight mode and using the app.)

## Crash found and fixed on the phone (6 Nov 2026)

- [x] ✅ **Foreground-service crash fixed.** First phone run crashed repeatedly (Device Care). Cause: `MonsterService` called two-argument `startForeground()` with no `foregroundServiceType` declared — Android 14+ rejects this for targetSdk 34. Fix in this code: manifest declares `foregroundServiceType="specialUse"` + `FOREGROUND_SERVICE_SPECIAL_USE` permission + special-use subtype property; `MonsterService` uses `ServiceCompat.startForeground(..., FOREGROUND_SERVICE_TYPE_SPECIAL_USE)`. User applied the fix and the app runs.

## Extra checks done here (in this workspace)

- [x] ✅ All XML files parse as well-formed XML (checked with a Python script using `xml.etree`), including the new `res/xml/accessibility_service_config.xml`.
- [x] ✅ All Kotlin files have balanced braces/parens/brackets (checked by script).
- [x] ✅ Gradle files are consistent: AGP 8.5.2 + Gradle 8.7, Kotlin 1.9.24, compileSdk/targetSdk 34, minSdk 26, same namespace/applicationId `com.bserc.reelmonster`.
- [x] ✅ Every `@string/`, `@color/`, `@drawable/`, `@layout/`, `@xml/` reference in layouts, XML and Kotlin resolves to a file in `res/` (checked by script).
- [x] ✅ No `INTERNET` permission anywhere in the manifest.
- [x] ✅ `CountStore` logic reviewed: `incrementToday` adds 1, `decrementToday` never goes below 0, `lastSevenDays` returns today + 6 previous days, time-lost = count × seconds-per-reel ÷ 60; level (1/2) and bubble-active flags persist in the same prefs.
- [x] ✅ **Kotlin compiles** — verified in practice by the user's Gradle builds on 6 Nov 2026 (build finished; app installed and ran). Level 2 files were added after the first build, so the next build on his PC is the compile check for the new code; static checks above all pass.
- [x] ✅ **Tap counting, bubble growth, funny lines, drag-to-move** — tap counting verified by the user on the phone (12–13 reels). Badge/size growth and funny lines: user step is to tap 10 times and watch the badge and size grow and the toast lines change; drag it across the screen.

## How to tick the remaining boxes

Run the steps in README.md ("Build & install"), then fill in each ⏳ item above with the result (phone model, Android version, date) before submitting.
