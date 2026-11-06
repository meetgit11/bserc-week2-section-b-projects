# Reel Monster — requirements and verification

Source: BSERC Week 2 PDF, Section B, Project 6 (Reel Monster). Level 1 (manual) was the selected level; Level 2 (automatic) was added on 6 Nov 2026 at the user's request.

## Requirements (from the PDF)

- Real Android app, installable on a real phone.
- Cute monster bubble floating on top of Instagram and YouTube.
- Level 1 manual: tap the monster after each reel; counter rises. In-app: today's count, time lost, last 7 days. Monster grows and says funny lines as the count rises.
- Level 2 automatic: the monster counts each reel itself (accessibility service).
- Done when: installs on a real Android phone; monster stays on top of both Instagram and YouTube; count survives a phone restart; app needs no internet and no login. Level 2 only: over 20 reels, its count is within 2 of a manual count.

## What was built

- `MonsterService` — foreground service + floating bubble (`WindowManager.TYPE_APPLICATION_OVERLAY`), tap +1, drag to move, long-press to open the app, badge + growth (+0.8% per reel, capped at +80%), funny lines with milestones at 10/25/50/100.
- `ReelDetectionService` — Level 2 AccessibilityService: counts a reel per scroll event in Instagram (`com.instagram.android`) and YouTube (`com.google.android.youtube`), 1.5 s minimum gap, only while Level 2 is selected and the bubble is running; `canRetrieveWindowContent="false"`.
- `CountStore` — per-day counts, seconds-per-reel setting, level (1/2) and bubble-active flags in SharedPreferences.
- `MainActivity` — dashboard, level choice with Accessibility on/off detection and status line, settings, permissions.

## Verification (Samsung Galaxy A55, 6 Nov 2026)

| Check | Result |
|---|---|
| Installs on a real phone | Pass |
| Bubble stays on top of Instagram and YouTube | Pass |
| Manual tap counting | Pass — 12–13 reels counted |
| Level 2 automatic counting | Working — screenshot shows Level 2 on, Today: 13 reels |
| Count survives a phone restart | Not yet run |
| Level 2 accuracy over 20 reels (within 2) | Not yet run with numbers |
| No internet, no login | Pass (no INTERNET permission) |

## Bug found on the phone and fixed

First phone run crashed repeatedly (Device Care). Cause: `MonsterService` called two-argument `startForeground()` with no foreground-service type declared — Android 14+ rejects this for targetSdk 34. Fix: manifest declares `foregroundServiceType="specialUse"` + `FOREGROUND_SERVICE_SPECIAL_USE` permission + special-use subtype property; the service uses `ServiceCompat.startForeground()` with that type.
