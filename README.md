# BSERC Week 2 — Section B Projects

Two native Android applications developed as part of the Bharat Space Education Research Centre (BSERC) GenAI Internship Week 2: **Reel Monster** and **Bill Book**. Built with AI-assisted development, focusing on practical functionality, privacy, responsive UI/UX and software engineering fundamentals.

> This is an independently developed student project. **BSERC does not endorse it.** Everything below states what was actually built and tested; anything not yet tested is marked as such.

## The two apps

### 1. Reel Monster — a monster that counts the reels you watch

A cute monster bubble floats on top of Instagram, YouTube and every other app and counts every reel watched.

- **Level 1 (manual):** tap the monster after each reel; the counter badge goes up. The monster grows as today's count rises and shows a rotating funny line on each tap.
- **Level 2 (automatic):** an AccessibilityService counts a reel each time you scroll to a new one in Instagram or YouTube. It only receives scroll events from those two apps and never reads window content. Counting only happens while Level 2 is selected and the bubble is running.
- In-app screen: today's count, estimated time lost (labelled as an estimate), last 7 days, Undo and Reset today. Counts are stored in SharedPreferences, so they should survive a phone restart.
- **Verified on a real phone (Samsung Galaxy A55, 6 Nov 2026):** installs, bubble stays on top of Instagram and YouTube, manual counting works (12–13 reels), Level 2 automatic counting works (screenshot shows Level 2 on, Today: 13 reels). One real bug was found and fixed on the phone: the bubble service crashed on Android 14+ because it declared no foreground-service type; the manifest now declares `foregroundServiceType="specialUse"` and the service starts with that type.
- **Still open:** the restart test (tap 3, restart, count still 3) and the formal Level 2 check (20+ slow reels, count within 2 of a manual count).

See [`reel-monster/README.md`](reel-monster/README.md) and [`reel-monster/test-checklist.md`](reel-monster/test-checklist.md).

### 2. Bill Book — billing for a small general store

Shop profile and item list with prices; bills with quantity, discount and tax; a UPI QR code for the exact bill total; share a bill as an image or text; bill history and a daily total.

- **Verified on a real phone (Samsung Galaxy A55, 6 Nov 2026):** installs; items add/delete; totals match hand calculation on 4 of 5 bills (seeded bills #101 ₹545.00, #102 ₹120.00, #103 ₹537.70, plus user-made bill #104 ₹300.00 — and the home screen's "Today — total sales: ₹1502.70" equals their sum); the UPI QR for bill #104 scanned in a UPI app and showed a ₹300.00 payment to meetpatil@okhdfc (a dummy UPI ID entered for testing — no payment made).
- **Still open:** the fifth bill hand-check (different quantity/discount/tax), the flight-mode test (old bills must stay saved), and the share-as-image check.

See [`bill-book/README.md`](bill-book/README.md) and [`bill-book/test-checklist.md`](bill-book/test-checklist.md).

## Screenshots

Genuine screenshots from the test phone, 6 November 2026:

| App | Screenshot |
|-----|-----------|
| Reel Monster, Level 2 on | [`reel-monster/docs/screenshots/reel-monster-level2.png`](reel-monster/docs/screenshots/reel-monster-level2.png) |
| Bill Book, home | [`bill-book/docs/screenshots/bill-book-home.png`](bill-book/docs/screenshots/bill-book-home.png) |
| Bill Book, items | [`bill-book/docs/screenshots/bill-book-items.png`](bill-book/docs/screenshots/bill-book-items.png) |
| Bill Book, bill with UPI QR | [`bill-book/docs/screenshots/bill-book-qr.png`](bill-book/docs/screenshots/bill-book-qr.png) |

## Repository structure

```
bserc-week2-section-b-projects/
├── README.md, prompts.md, LICENSE (MIT)
├── reel-monster/                 # full Android Studio project (Kotlin)
│   ├── app/                      # manifest, Kotlin sources, resources
│   ├── README.md, prompts.md, test-checklist.md
│   └── docs/screenshots/         # genuine phone screenshots
├── bill-book/                    # full Android Studio project (Kotlin)
│   ├── app/                      # manifest, Kotlin sources, resources
│   ├── README.md, prompts.md, test-checklist.md
│   └── docs/screenshots/         # genuine phone screenshots
└── docs/
    ├── project-overview.md
    ├── reel-monster-requirements.md
    ├── bill-book-requirements.md
    └── testing-report.md
```

## Build & run (on your laptop, in Android Studio)

1. Install **Android Studio** (Hedgehog or newer) with **JDK 17** and SDK **API 34**.
2. Open one of the project folders (`reel-monster/` or `bill-book/`) in Android Studio — each is a complete Gradle project. Let Gradle sync finish (first sync downloads dependencies; needs internet once, for the build tools only — the apps themselves need none).
3. Connect your Android phone (USB debugging, or pair over Wi-Fi from Device Manager — no cable needed) and select it as the run target. Or create an emulator (API 26+), but the bubble-over-Instagram test needs a real phone.
4. Click **Run ▶**. The app installs and opens.

Tech notes: Kotlin, classic Views/XML (no Compose), Gradle Groovy DSL, AGP 8.5.2, Gradle 8.7, Kotlin 1.9.24, compileSdk/targetSdk 34, minSdk 26, Java 17. Bill Book uses ZXing core 3.5.3 for QR codes. Neither app declares the `INTERNET` permission; neither has an account or login. All testing used fake/sample data only.

## Testing

Full per-app checklists with honest pass/open status: [`reel-monster/test-checklist.md`](reel-monster/test-checklist.md), [`bill-book/test-checklist.md`](bill-book/test-checklist.md), and the consolidated [`docs/testing-report.md`](docs/testing-report.md). A mini report for Section B (8 pages, with screenshots) was also produced during the internship; it is not in this repo.

## Prompts

Every prompt used to build these projects, in order: [`prompts.md`](prompts.md) (plus the per-app `prompts.md` files inside each project folder).

## License

MIT — see [`LICENSE`](LICENSE). It covers the original code only.
