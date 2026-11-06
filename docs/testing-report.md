# Section B — testing report

Test date: 6 November 2026. Test device: Samsung Galaxy A55 (SM-A556E). Build: Android Studio Rabbit 2 (2026.2.2), installed over Wi-Fi debugging (no USB cable). Nothing was compiled or installed in the author's workspace (no Android SDK there); every pass below is a result from the real phone.

## Reel Monster

| Check | Result |
|---|---|
| Installs on a real phone | Pass |
| Bubble stays on top of Instagram and YouTube | Pass |
| Manual tap counting (Level 1) | Pass — 12–13 reels counted |
| Level 2 automatic counting | Working — screenshot shows Level 2 on, Today: 13 reels |
| Count survives a phone restart | Not yet run |
| Level 2 accuracy over 20 reels (within 2) | Not yet run with numbers |
| No internet, no login | Pass |

Bug found and fixed during testing: first phone run crashed repeatedly (Device Care). Cause: `MonsterService` called two-argument `startForeground()` with no foreground-service type declared — Android 14+ rejects this for targetSdk 34. Fix: manifest declares `foregroundServiceType="specialUse"` + permission + subtype property; service uses `ServiceCompat.startForeground()` with that type.

## Bill Book

| Check | Result |
|---|---|
| Installs on a real phone | Pass |
| Items add / delete | Pass |
| Totals match hand calculation (5 bills) | Partial — 4 of 5 verified |
| Works in flight mode; old bills stay saved | Not yet run |
| UPI QR scan shows the right amount | Pass — ₹300.00 to meetpatil@okhdfc (dummy), no payment made |
| Share bill as image | Not yet run |
| No internet, no login | Pass |

Hand-checked totals: #101 ₹545.00, #102 ₹120.00, #103 ₹537.70, #104 ₹300.00 (Toor Dal 2 kg × 1 @ ₹300.00, no discount/tax). Daily total ₹1502.70 = 545.00 + 120.00 + 537.70 + 300.00.

## Static checks (author's workspace, 5–6 Nov 2026)

- All XML files parse as well-formed XML.
- All Kotlin files have balanced braces/parens/brackets.
- Gradle files consistent: AGP 8.5.2 + Gradle 8.7, Kotlin 1.9.24, compileSdk/targetSdk 34, minSdk 26.
- Every `@string/`, `@color/`, `@drawable/`, `@layout/`, `@xml/` reference resolves to a file in `res/`.
- No `INTERNET` permission in either manifest.
- UPI link format reviewed: `upi://pay?pa=<url-encoded UPI ID>&pn=<url-encoded shop>&am=<total with 2 decimals>&cu=INR`.

## Screenshots

Genuine phone screenshots, 6 November 2026: `reel-monster/docs/screenshots/reel-monster-level2.png`, `bill-book/docs/screenshots/bill-book-home.png`, `bill-book/docs/screenshots/bill-book-items.png`, `bill-book/docs/screenshots/bill-book-qr.png`.
