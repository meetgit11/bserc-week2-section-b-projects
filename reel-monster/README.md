# Reel Monster — BSERC Week 2, Section B (Project 6, Levels 1 + 2)

A cute monster that floats on top of Instagram and YouTube and counts every reel you watch.

> **Status (6 Nov 2026):** built in Android Studio on the user's laptop and **installed on his Samsung Galaxy A55 (SM-A556E) over Wi-Fi** — no USB cable. **Level 1 manual counting is verified working on the phone** (user scrolled 12–13 reels, tapped the monster, count stored). One real bug was found and fixed the same day (see "Crash fix" below). **Level 2 automatic counting is built** (code complete, static checks pass) and is waiting for its on-phone test. No APK was built in this VM — it has no Java/Android SDK.

## Crash fix (6 Nov 2026)

On the first phone run the app kept crashing (Device Care reported repeated crashes). Cause: `MonsterService` called two-argument `startForeground()` with no foreground-service type declared, which Android 14+ rejects when the app targets SDK 34. Fix, already in this code: the manifest declares `android:foregroundServiceType="specialUse"` plus the `FOREGROUND_SERVICE_SPECIAL_USE` permission and the required special-use subtype property, and `MonsterService` starts with `ServiceCompat.startForeground(..., FOREGROUND_SERVICE_TYPE_SPECIAL_USE)`. After this fix the app runs on the phone.

## What it does (Level 1, manual — the selected level)

- **Floating monster bubble** (`MonsterService`, `WindowManager.TYPE_APPLICATION_OVERLAY`): a green monster face that stays on top of Instagram, YouTube and every other app.
- **Tap = +1 reel.** Tap the monster after each reel you watch; the counter badge on the bubble goes up.
- **The monster grows** as today's count rises (+0.8% per reel, capped at +80%) and shows a rotating funny line on each tap ("Yum! Your evening tastes great.", "Burp. Your homework says hi.", …), with special lines at 10 / 25 / 50 / 100 reels.
- **Drag** the monster to move it. **Long-press** it to open the app.
- **In-app screen:** today's count (big), estimated time lost (count × seconds-per-reel, default 30 s — clearly labelled as an *estimate*, editable in Settings), and the **last 7 days** with a count per day.
- **Count survives a phone restart:** every tap is written to `SharedPreferences` on disk (`CountStore.kt`). No boot receiver or server is needed.
- **Undo** (−1) and **Reset today** buttons for accidental taps.

## Level 2 — automatic counting (built 6 Nov 2026, on-phone test pending)

- **How:** `ReelDetectionService`, an AccessibilityService that counts a reel each time you scroll to a new one in Instagram (`com.instagram.android`) or YouTube (`com.google.android.youtube`). You switch it on in **Settings → Accessibility → Reel Monster**, then pick Level 2 in the app. The app checks whether the service is actually enabled and tells you if it is not.
- **Counting rule:** one swipe fires a burst of scroll events, so a reel only counts if at least 1.5 s passed since the last counted reel. A reel flicked past in under ~1.5 s is not counted, and the first reel you see when you open Reels/Shorts is not counted either — counting starts with the first swipe.
- **Honest limits:** the count is an estimate, not an exact tally. Horizontal swipes (photo carousels, comment sheets) also fire scroll events, and fast, erratic scrolling drifts the count. Over 20 slow reels it should land within 2 of a careful manual count. If Instagram/YouTube restructure their reel feed, detection can break and need updating.
- **Privacy:** the service sets `canRetrieveWindowContent="false"` — it never reads screen text, messages or passwords, only scroll events from those two apps. Nothing leaves the phone. Counting only happens while Level 2 is selected **and** the monster bubble is running; turn the service off in Accessibility settings and automatic counting stops immediately.

## Privacy

No internet permission, no account, no login. Nothing leaves the phone. Level 2 adds an AccessibilityService (see above) — scroll events only, no screen content, on-device only.

## Project structure

```
Reel Monster/
├── settings.gradle, build.gradle, gradle.properties
├── gradle/wrapper/gradle-wrapper.properties   (Gradle 8.7)
└── app/
    ├── build.gradle                          (minSdk 26, targetSdk 34, AGP 8.5.2, Kotlin 1.9.24)
    ├── proguard-rules.pro
    └── src/main/
        ├── AndroidManifest.xml               (SYSTEM_ALERT_WINDOW, FOREGROUND_SERVICE + FOREGROUND_SERVICE_SPECIAL_USE, POST_NOTIFICATIONS; MonsterService with foregroundServiceType="specialUse"; ReelDetectionService with BIND_ACCESSIBILITY_SERVICE)
        ├── java/com/bserc/reelmonster/
        │   ├── MainActivity.kt               (dashboard, level choice, settings, permissions)
        │   ├── MonsterService.kt             (foreground service + floating bubble)
        │   ├── ReelDetectionService.kt       (Level 2 automatic counting, accessibility service)
        │   ├── CountStore.kt                 (per-day counts in SharedPreferences; level + bubble-active flags)
        │   └── FunnyLines.kt                 (rotating lines + milestones)
        └── res/
            ├── layout/activity_main.xml, layout/bubble_monster.xml
            ├── xml/accessibility_service_config.xml  (Level 2: scroll events, Instagram + YouTube only)
            ├── drawable/ic_monster.xml       (monster face, drawn from shapes)
            ├── drawable/badge_circle.xml
            ├── values/strings.xml, colors.xml, themes.xml
            └── mipmap-anydpi-v26/            (adaptive launcher icon)
```

## Build & install (on your laptop, in Android Studio)

1. Install **Android Studio** (Hedgehog or newer) with **JDK 17** and SDK **API 34**. On first open, let it download the Android SDK.
2. Open this folder (`section-b-reel-monster/`) in Android Studio — it is a complete Gradle project. Let Gradle sync finish (first sync downloads dependencies; needs internet once, for the build tools only — the app itself needs none).
3. Connect your Android phone with a USB cable, turn on **Developer options → USB debugging**, and select the phone as the run target. Or pair it over Wi-Fi (Device Manager → Pair Devices Using Wi-Fi, no cable needed — this is how the app was first installed on 6 Nov 2026). Or create an emulator (API 26+), but the bubble-over-Instagram test needs a real phone.
4. Click **Run ▶**. The app installs and opens.
5. In the app tap **“Start the monster bubble”**. When Android asks, allow **“Display over other apps”** (this is the permission that lets the bubble float on Instagram/YouTube). On Android 13+ also allow notifications when asked.
6. Open **Instagram** or **YouTube**, watch a reel, then tap the monster — the badge should go 1, 2, 3…

If Gradle sync fails, check: JDK 17 is selected (File → Project Structure → SDK Location → JDK), and the SDK has API 34 installed (SDK Manager).

## Test steps (from the PDF "Done when" list)

See `test-checklist.md` for the full list with what is and is not yet verified. In short: install on a real phone → bubble stays on top of Instagram and YouTube → tap counts up → restart the phone, count is still there → app works with mobile data and Wi-Fi off. **Level 2:** enable the Accessibility service, pick Level 2, scroll 20+ reels slowly, and compare with a manual count — it should be within 2.

## Limits

- Level 2 automatic counting is an estimate (see above): 1.5 s minimum gap, first reel not counted, horizontal swipes can add false counts, and Instagram/YouTube layout changes can break detection.
- The "time lost" figure is an estimate (reels are not all 30 seconds); it is labelled as one in the app.
- The bubble is a foreground service, so Android shows a small ongoing notification while it runs — that is how Android keeps the bubble alive.
- Some phones (notably Xiaomi / Oppo / Vivo) add their own "floating window" permission on top of Android's — if the bubble does not appear, also allow floating windows for Reel Monster in the phone's own settings.
