# Bill Book — requirements and verification

Source: BSERC Week 2 PDF, Section B, Project 7 (Bill Book).

## Requirements (from the PDF)

- Shop profile and item list with prices.
- Bills with quantity, discount and tax.
- UPI QR code for the exact total.
- Share a bill as an image.
- Bill history and daily total.
- Done when: totals match hand calculation on 5 bills; works in flight mode and old bills stay saved; a UPI app scans the QR and shows the right amount (no payment needed); installs on a real phone.

## What was built

- `MainActivity` — shop name, today's total sales, bill history, navigation.
- `ItemsActivity` — item list with prices, add/delete items.
- `NewBillActivity` — build a bill from items with quantities, discount %, tax %.
- `BillDetailActivity` — bill lines, subtotal/discount/tax/total, UPI QR code, share as image/text.
- `ProfileActivity` — shop profile (name, UPI ID).
- `BillStore` / `Models` — bills persisted as JSON in internal storage; totals with `BigDecimal` (total = (subtotal − discount) + tax on the discounted subtotal).
- `QrUtil` — UPI link `upi://pay?pa=<UPI ID>&pn=<shop>&am=<total>&cu=INR` turned into a QR code with ZXing.

## Verification (Samsung Galaxy A55, 6 Nov 2026)

| Check | Result |
|---|---|
| Installs on a real phone | Pass |
| Items add / delete | Pass |
| Totals match hand calculation (5 bills) | Partial — 4 of 5 verified: #101 ₹545.00, #102 ₹120.00, #103 ₹537.70, #104 ₹300.00; daily total ₹1502.70 equals their sum |
| Works in flight mode; old bills stay saved | Not yet run |
| UPI app scans the QR, shows the right amount | Pass — bill #104's QR showed ₹300.00 to meetpatil@okhdfc (dummy), no payment made |
| Share bill as image | Not yet run |
| No internet, no login | Pass (no INTERNET permission) |

## Sample data

The seeded shop is "Sharma Stores" with the sample UPI ID `sharmastores@okhdfc`, and seeded items/bills are fake. On the test phone the profile was changed to the user's own name with a dummy UPI ID (`meetpatil@okhdfc`) for the QR test. Replace the UPI ID with a real one before any real payment.
