# Test checklist — Bill Book (BSERC Week 2, Section B, Project 7)

PDF "Done when" list, item by item. Status: **installed on the user's Samsung Galaxy A55 (SM-A556E) over Wi-Fi on 6 Nov 2026 and verified working by the user** (screenshots: home, items, bill with QR). This VM has no Java/Android SDK, so nothing below was compiled or installed here.

## PDF "Done when" items

- [x] ✅ **Totals match your hand calculation on 5 bills — 4 of 5 verified.**
  Verified on the phone 6 Nov 2026 for 4 bills: seeded bills #101 (₹545.00), #102 (₹120.00), #103 (₹537.70) match the hand calculations in the table below exactly, and user-made bill #104 (Toor Dal 2 kg × 1 @ ₹300.00, no discount/tax) shows ₹300.00 as hand-calculated. The home screen's "Today — total sales: ₹1502.70" equals the sum of the four bills (545.00 + 120.00 + 537.70 + 300.00). One more bill with a different quantity/discount/tax still to hand-check for the full 5.
- [ ] ⏳ **It works in flight mode, and old bills stay saved.**
  Not verified on a phone yet. User step: turn on flight mode, make a bill, force-close the app, reopen it — the bill must still be there. (Implementation: bills are stored in a JSON file in internal storage; nothing needs the network.)
- [x] ✅ **A UPI app scans the QR and shows the right amount. You don't need to pay.**
  Verified on the phone 6 Nov 2026: bill #104's QR scanned with a UPI app showed a ₹300.00 payment to meetpatil@okhdfc (user-described dummy UPI ID); no payment was made.
- [x] ✅ **It installs on a real phone.**
  Verified by the user 6 Nov 2026: built in Android Studio, installed on his Galaxy A55 over Wi-Fi debugging; screenshots show the home, items and bill screens.

## Totals checked by hand (sample bills seeded on first run)

Formula: subtotal = Σ(price × qty); discount = % of subtotal; tax = % of (subtotal − discount); total = subtotal − discount + tax.

| Bill | Lines | Subtotal | Discount | Tax | Total (app must show) |
|------|-------|----------|----------|-----|-----------------------|
| #101 | Rice 2 kg ×2 (₹240) + Oil 1 L ×1 (₹165) + Tea 250 g ×1 (₹140) | ₹545.00 | 0% → ₹0.00 | 0% → ₹0.00 | **₹545.00** |
| #102 | Rice 2 kg ×1 | ₹120.00 | 0% → ₹0.00 | 0% → ₹0.00 | **₹120.00** |
| #103 | Wheat Flour 5 kg ×2 (₹420) + Sugar 1 kg ×2 (₹90) + Milk 1 L ×1 (₹56) | ₹566.00 | 5% → ₹28.30 | 0% → ₹0.00 | **₹537.70** |

These three totals were computed by hand above from the seeded items. They were NOT run through the app (no Android runtime here) — the first on-phone run must confirm the app shows exactly these three totals.

## Extra checks done here (in this workspace)

- [x] ✅ All XML files parse as well-formed XML (checked with a Python script using `xml.etree`).
- [x] ✅ All Kotlin files have balanced braces/parens/brackets (checked by script).
- [x] ✅ Gradle files are consistent: AGP 8.5.2 + Gradle 8.7, Kotlin 1.9.24, compileSdk/targetSdk 34, minSdk 26, same namespace/applicationId `com.bserc.billbook`; ZXing core 3.5.3 declared once in `app/build.gradle`.
- [x] ✅ Every `@string/`, `@color/`, `@drawable/`, `@layout/`, `@xml/` reference in layouts, manifest and Kotlin resolves to a file in `res/` (checked by script).
- [x] ✅ No `INTERNET` permission anywhere in the manifest; the only declared provider is the `FileProvider` used for sharing bill images.
- [x] ✅ UPI link format reviewed in `Models.kt`: `upi://pay?pa=<url-encoded UPI ID>&pn=<url-encoded shop>&am=<total with 2 decimals>&cu=INR`.
- [x] ✅ Sample data is fake and labelled: shop "Sharma Stores", UPI ID `sharmastores@okhdfc` (sample), items and bills as above; the app warns the user to replace the UPI ID before real payments.
- [x] ✅ **Kotlin compiles** — verified in practice by the user's Gradle build on 6 Nov 2026 (build finished; app installed and ran on his phone).
- [ ] ⏳ **Share-as-image** — NOT verified here. User step: open a bill, tap "Share bill as image", pick WhatsApp, check the bill image arrives readable.

## How to tick the remaining boxes

Run the steps in README.md ("Build & install"), then fill in each ⏳ item above with the result (phone model, Android version, date) before submitting.
