# Bill Book — BSERC Week 2, Section B (Project 7)

A billing app for a small shop. Make a bill, show a UPI QR code and share it on WhatsApp.

> **Honest limit — read first:** this project was written on a machine with **no Java and no Android SDK**, so **no APK was built here and the app has not been installed on a phone yet**. Everything below is complete, buildable source. You must build the APK in Android Studio on your laptop and test it on your real Android phone (steps in "Build & install"). Do not claim the app is installed until that test passes.

## What it does

- **Shop profile** (name + UPI ID, editable). Seeded with the sample shop **"Sharma Stores"** and the obviously fake sample UPI ID **`sharmastores@okhdfc`** — clearly labelled SAMPLE everywhere. **Replace it with your own UPI ID in Shop profile before taking real payments.**
- **Item list with prices** — add, delete items. Sample items preloaded (all fake sample data): Rice 2 kg ₹120, Oil 1 L ₹165, Sugar 1 kg ₹45, Wheat Flour 5 kg ₹210, Tea 250 g ₹140, Milk 1 L ₹56.
- **Bills with quantity, discount and tax.** Tap + / − on each item, enter discount % and tax %, watch the totals update live. Money is computed with `BigDecimal` (2 decimal places), so totals match a hand calculation: Total = (subtotal − discount) + tax on the discounted subtotal.
- **UPI QR code for the exact total.** The app builds `upi://pay?pa=<UPI ID>&pn=<shop name>&am=<amount>&cu=INR` and renders it as a QR code with ZXing (`com.google.zxing:core:3.5.3`, the only dependency besides AndroidX). Scan it in any UPI app — it shows the right amount. **Nothing is paid automatically**; you still confirm in your UPI app.
- **Share a bill as an image** — the bill is rendered to a PNG and shared through the Android share sheet (WhatsApp appears there). A text-share option is included too.
- **Bill history and a daily total** on the home screen, newest first. Three sample bills are preloaded on first run (computed from the sample items: #101 ₹545.00, #102 ₹120.00, #103 ₹537.70 after a 5% discount).
- **Fully offline:** bills are stored in a JSON file in internal storage, profile/items in `SharedPreferences`. No internet permission, no account, no login. Works in flight mode; old bills stay saved.

## Project structure

```
Bill Book/
├── settings.gradle, build.gradle, gradle.properties
├── gradle/wrapper/gradle-wrapper.properties   (Gradle 8.7)
└── app/
    ├── build.gradle                          (minSdk 26, targetSdk 34, AGP 8.5.2, Kotlin 1.9.24, ZXing core 3.5.3)
    ├── proguard-rules.pro
    └── src/main/
        ├── AndroidManifest.xml               (no INTERNET permission; FileProvider for sharing)
        ├── java/com/bserc/billbook/
        │   ├── MainActivity.kt               (shop name, today's total, bill history)
        │   ├── NewBillActivity.kt            (pick items, discount/tax, live totals, save)
        │   ├── BillDetailActivity.kt         (bill view, QR code, share as image/text)
        │   ├── ItemsActivity.kt              (add/delete items)
        │   ├── ProfileActivity.kt            (shop name + UPI ID)
        │   ├── Models.kt                     (ShopItem, BillLine, Bill — BigDecimal totals, UPI link)
        │   ├── BillStore.kt                  (JSON file + SharedPreferences storage, sample data)
        │   └── QrUtil.kt                     (ZXing QR rendering)
        └── res/
            ├── layout/activity_main.xml, activity_new_bill.xml, activity_bill_detail.xml,
            │   activity_items.xml, activity_profile.xml
            ├── values/strings.xml, colors.xml, themes.xml
            ├── xml/file_paths.xml            (FileProvider cache path)
            └── mipmap-anydpi-v26/            (adaptive launcher icon)
```

## Build & install (on your laptop, in Android Studio)

1. Install **Android Studio** (Hedgehog or newer) with **JDK 17** and SDK **API 34**.
2. Open this folder (`section-b-bill-book/`) in Android Studio. Let Gradle sync finish — the first sync downloads the AndroidX and ZXing dependencies (needs internet once, for the build only; the app itself needs none).
3. Connect your Android phone (USB debugging on) and click **Run ▶**, or use an emulator (API 26+).
4. The app opens with the sample shop and sample bills. First go to **Shop profile** and replace the sample UPI ID with your own if you want to take real payments.

If Gradle sync fails, check: JDK 17 is selected (File → Project Structure → SDK Location → JDK), and API 34 is installed (SDK Manager).

## Test steps (from the PDF "Done when" list)

See `test-checklist.md`. In short: make 5 bills and check every total by hand → turn on flight mode, close and reopen the app, old bills are still there → scan a bill's QR with a UPI app (GPay/PhonePe/Paytm) and check it shows the exact total (do **not** pay) → share a bill as an image to WhatsApp.

## Limits

- The seeded shop, UPI ID, items and bills are **sample data** — replace the UPI ID before real use.
- Totals use discount on the subtotal, then tax on the discounted subtotal. If your shop taxes differently, say so and the formula can be changed in `Models.kt`.
- Sharing needs an app that accepts images (WhatsApp, Gmail, Drive…) — the share sheet lists whatever is installed.
