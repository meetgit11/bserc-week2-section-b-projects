package com.bserc.billbook

import java.math.BigDecimal
import java.math.RoundingMode

/** Money is kept in rupees as BigDecimal, rounded to 2 places, so totals
 *  always match a hand calculation (no floating-point surprises). */
fun money(value: Double): BigDecimal =
    BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP)

fun money(value: String): BigDecimal =
    BigDecimal(value).setScale(2, RoundingMode.HALF_UP)

fun BigDecimal.formatRupees(): String = "₹" + setScale(2, RoundingMode.HALF_UP).toPlainString()

fun BigDecimal.formatUpiAmount(): String = setScale(2, RoundingMode.HALF_UP).toPlainString()

data class ShopItem(
    val id: String,
    val name: String,
    val unit: String,
    val price: BigDecimal
)

data class BillLine(
    val itemId: String,
    val name: String,
    val unit: String,
    val price: BigDecimal,
    val quantity: Int
) {
    /** Line total = price × quantity. */
    fun lineTotal(): BigDecimal = price.multiply(BigDecimal(quantity))
}

data class Bill(
    val number: Int,
    val dateIso: String,          // yyyy-MM-dd, local date
    val timeIso: String,          // HH:mm
    val lines: List<BillLine>,
    val discountPercent: BigDecimal,
    val taxPercent: BigDecimal,
    val shopName: String,
    val upiId: String
) {
    /** Subtotal = sum of all line totals. */
    fun subtotal(): BigDecimal =
        lines.fold(BigDecimal.ZERO) { acc, line -> acc.add(line.lineTotal()) }
            .setScale(2, RoundingMode.HALF_UP)

    /** Discount is a percentage of the subtotal. */
    fun discountAmount(): BigDecimal =
        subtotal().multiply(discountPercent)
            .divide(BigDecimal(100), 2, RoundingMode.HALF_UP)

    /** Tax is a percentage of the subtotal after discount. */
    fun taxAmount(): BigDecimal =
        subtotal().subtract(discountAmount()).multiply(taxPercent)
            .divide(BigDecimal(100), 2, RoundingMode.HALF_UP)

    /** Total = subtotal − discount + tax. */
    fun total(): BigDecimal =
        subtotal().subtract(discountAmount()).add(taxAmount())
            .setScale(2, RoundingMode.HALF_UP)

    /** The UPI payment link for exactly this bill's total. */
    fun upiLink(): String {
        val pa = java.net.URLEncoder.encode(upiId, "UTF-8")
        val pn = java.net.URLEncoder.encode(shopName, "UTF-8")
        return "upi://pay?pa=$pa&pn=$pn&am=${total().formatUpiAmount()}&cu=INR"
    }
}
