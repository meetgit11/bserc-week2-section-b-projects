# Section B — project overview

Two native Android applications, built for the BSERC GenAI Internship Week 2 (Section B: phone apps and games / real Android install required), and tested on a real phone.

## Reel Monster

A floating monster bubble that counts the reels you watch on Instagram and YouTube.

- **Level 1 (manual):** tap the monster after each reel; the counter goes up. The monster grows with today's count and shows funny lines.
- **Level 2 (automatic):** an AccessibilityService counts a reel each time you scroll to a new one in Instagram or YouTube (1.5 s minimum gap between counted reels; only while Level 2 is selected and the bubble is running; never reads window content).
- **Storage:** per-day counts in SharedPreferences (`CountStore.kt`); no server, no account.
- **Key files:** `app/src/main/java/com/bserc/reelmonster/` — `MainActivity.kt`, `MonsterService.kt`, `ReelDetectionService.kt`, `CountStore.kt`, `FunnyLines.kt`.

## Bill Book

Billing for a small general store.

- **Shop profile** (name, UPI ID) and **item list** with prices.
- **Bills** with quantity, discount and tax; **UPI QR code** for the exact total (ZXing); share a bill as an image or text; bill history and daily total.
- **Storage:** bills in a JSON file in internal storage; no server, no account.
- **Key files:** `app/src/main/java/com/bserc/billbook/` — `MainActivity.kt`, `NewBillActivity.kt`, `ItemsActivity.kt`, `BillDetailActivity.kt`, `ProfileActivity.kt`, `BillStore.kt`, `Models.kt`, `QrUtil.kt`.

## Shared facts

- **Stack:** Kotlin, classic Views/XML, Gradle Groovy DSL, AGP 8.5.2, Gradle 8.7, Kotlin 1.9.24, compileSdk/targetSdk 34, minSdk 26, Java 17.
- **Test device:** Samsung Galaxy A55 (SM-A556E), installed from Android Studio Rabbit 2 (2026.2.2) over Wi-Fi debugging (no USB cable).
- **Privacy:** neither app declares the `INTERNET` permission; neither has an account or login; all testing used fake/sample data only.
- **Status (6 Nov 2026):** both apps run on the phone and their main flows are verified. Open checks: Reel Monster restart test and formal Level 2 accuracy check; Bill Book flight-mode test, fifth hand-checked bill, share-as-image check. See `testing-report.md`.
