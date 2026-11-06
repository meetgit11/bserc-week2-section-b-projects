# prompts.md — BSERC Week 2, Section B

Prompts used, in order, as required by the Week 2 PDF. One entry per prompt, with who asked it and what it produced. The per-app `prompts.md` files (inside `reel-monster/` and `bill-book/`) hold the full builder prompts; this file is the chronological record for the section.

## 1. User → Muse (5 Nov 2026)
> Shared the Week 2 projects PDF (19 pages) and confirmed the final selection: Section A — Form Photo Fixer, Resume Builder; Section B — Reel Monster (Level 1, manual), Bill Book; Section C — Chat Saver, Style Spy; Section D — Your Own Skill (/viva), Same Page, New Design. Working model: "me and chatgpt will be giving and planning along with you, but your work mainly will be to build along side connected tools like Vercel, Render, etc." Build order: Section A → D.

Result: `plan.md` written with per-project specs and "Done when" lists from the PDF.

## 2. User → Muse (5 Nov 2026, ~21:06 IST)
> "why did u start building the projects?"

Result: correction recorded — Muse had read a general direction as a go-ahead and built Sections A, C, D (and started B) before the user answered the order/split question. Rule from then on: user + ChatGPT plan first; Muse builds only the specific project the user names. All building paused. Section B files were left on disk as-is (both apps substantially written, not verified, no APK built).

## 3. User → Muse (6 Nov 2026)
> "today we will cover 2 sections that is Section B and C, so as i had downloaded the android studio, tell me now what setup should i choose, the default one or custom setup? tell me"

Result: the Nov 5 pause was lifted for Sections B and C only. Advice: Standard (default) setup. Section B resumed: the two apps were reviewed, and the user was guided through opening them in Android Studio (Rabbit 2, 2026.2.2) on his laptop.

## 4. User → Muse (6 Nov 2026)
> "actually i cannot test the section B on mmy phone as i dont have the cable for the connection, i have installed the zip file on this pc, but in mmy opinion what to do now? then?"

Result: plan changed to wireless debugging over the same Wi-Fi (Device Manager → Pair Devices Using Wi-Fi) as the first choice, emulator as fallback. The user's Samsung Galaxy A55 (SM-A556E) was paired over Wi-Fi the same day.

## 5. User → Muse (6 Nov 2026)
> "my phone's device care says that the reelmonster keeps crashing, and also it really is getting crashed and not working... now what to do"

Result: diagnosed from reading `MonsterService.kt` — the service called two-argument `startForeground()` with no foreground-service type declared, which Android 14+ rejects for targetSdk 34. Fixed in the code (manifest declares `foregroundServiceType="specialUse"` + permission + subtype property; service uses `ServiceCompat.startForeground()` with that type). The user applied the same fix on his PC copy and re-ran: manual counting verified working (12–13 reels).

## 6. User → Muse (6 Nov 2026)
> "yupp, it is working, and also i need to click the monster manually and count increases, i scrolled some reels around 12-13 reels and the floating monster stores the count, it is good, but if possible we will create level 2, automatic count... is it possible?" → then "Yes, build Level 2"

Result: Level 2 built the same day — `ReelDetectionService` (AccessibilityService: scroll events in Instagram/YouTube only, 1.5 s minimum gap between counted reels, only while Level 2 is selected and the bubble is running; `canRetrieveWindowContent="false"`), config XML, manifest declaration, `CountStore` level + bubble-active flags, MainActivity wiring. Static checks passed (XML well-formed, all references resolve, braces balanced). The user reported Level 2 working on his phone the same day (screenshot: Level 2 on, Today: 13 reels).

## 7. User → Muse (6 Nov 2026)
> "ok, both the apps are working properly, i will provide you some images which is essential for including in READ.md and also for the Report.. so is it ok from your side? that we are wrapping up the section B now?"

Result: both apps confirmed working on the phone (Bill Book: items, home with 4 bills totalling ₹1502.70, bill #104 with UPI QR). Three quick checks were named as still open before an honest wrap-up (Reel Monster restart test; Bill Book flight-mode test and UPI QR scan; one more hand-checked bill). The user then sent the Bill #104 QR screenshot; the QR was scanned and showed a ₹300.00 payment to meetpatil@okhdfc (dummy), no payment made. A Section B mini report (8 pages) was produced with the four screenshots.

## 8. User → Muse (6 Nov 2026)
> "do you think we need to commit this all on my github?" → "yes do it"

Result: this repository — `bserc-week2-section-b-projects` — created (public, MIT), both Android Studio projects pushed with their READMEs, prompts and test checklists, docs, and the four genuine phone screenshots.
