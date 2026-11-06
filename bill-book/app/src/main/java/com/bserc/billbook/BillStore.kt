package com.bserc.billbook

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * All data lives on the phone: bills in a JSON file in internal storage,
 * shop profile and items in SharedPreferences. No account, no server,
 * no internet — the app works in flight mode and old bills stay saved.
 *
 * Sample data (clearly labelled SAMPLE in the UI) is seeded on first run:
 * shop "Sharma Stores", UPI ID "sharmastores@okhdfc" (an obviously fake
 * sample ID — replace it with your own UPI ID in Shop Profile), six items
 * and three sample bills. The user must replace the sample UPI ID with
 * their own before taking real payments.
 */
class BillStore(private val context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val billsFile: File get() = File(context.filesDir, BILLS_FILE)

    companion object {
        const val PREFS_NAME = "bill_book"
        const val BILLS_FILE = "bills.json"
        const val KEY_SHOP_NAME = "shop_name"
        const val KEY_UPI_ID = "upi_id"
        const val KEY_ITEMS_JSON = "items_json"
        const val KEY_SEEDED = "seeded"

        const val SAMPLE_SHOP_NAME = "Sharma Stores"
        const val SAMPLE_UPI_ID = "sharmastores@okhdfc"

        fun sampleItems(): List<ShopItem> = listOf(
            ShopItem("rice", "Rice 2 kg", "pack", money("120")),
            ShopItem("oil", "Oil 1 L", "bottle", money("165")),
            ShopItem("sugar", "Sugar 1 kg", "pack", money("45")),
            ShopItem("flour", "Wheat Flour 5 kg", "pack", money("210")),
            ShopItem("tea", "Tea 250 g", "pack", money("140")),
            ShopItem("milk", "Milk 1 L", "packet", money("56"))
        )
    }

    init {
        seedIfNeeded()
    }

    private fun seedIfNeeded() {
        if (prefs.getBoolean(KEY_SEEDED, false)) return
        prefs.edit()
            .putString(KEY_SHOP_NAME, SAMPLE_SHOP_NAME)
            .putString(KEY_UPI_ID, SAMPLE_UPI_ID)
            .putString(KEY_ITEMS_JSON, itemsToJson(sampleItems()))
            .putBoolean(KEY_SEEDED, true)
            .apply()
        // Three sample bills, computed from the sample items above.
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val samples = listOf(
            Bill(101, today, "09:12", listOf(
                BillLine("rice", "Rice 2 kg", "pack", money("120"), 2),
                BillLine("oil", "Oil 1 L", "bottle", money("165"), 1),
                BillLine("tea", "Tea 250 g", "pack", money("140"), 1)
            ), money("0"), money("0"), SAMPLE_SHOP_NAME, SAMPLE_UPI_ID),
            Bill(102, today, "10:05", listOf(
                BillLine("rice", "Rice 2 kg", "pack", money("120"), 1)
            ), money("0"), money("0"), SAMPLE_SHOP_NAME, SAMPLE_UPI_ID),
            Bill(103, today, "11:40", listOf(
                BillLine("flour", "Wheat Flour 5 kg", "pack", money("210"), 2),
                BillLine("sugar", "Sugar 1 kg", "pack", money("45"), 2),
                BillLine("milk", "Milk 1 L", "packet", money("56"), 1)
            ), money("5"), money("0"), SAMPLE_SHOP_NAME, SAMPLE_UPI_ID)
        )
        saveBills(samples)
    }

    // ---- Shop profile ----
    fun shopName(): String = prefs.getString(KEY_SHOP_NAME, SAMPLE_SHOP_NAME) ?: SAMPLE_SHOP_NAME
    fun upiId(): String = prefs.getString(KEY_UPI_ID, SAMPLE_UPI_ID) ?: SAMPLE_UPI_ID
    fun isSampleUpiId(): Boolean = upiId() == SAMPLE_UPI_ID

    fun saveProfile(name: String, upi: String) {
        prefs.edit().putString(KEY_SHOP_NAME, name).putString(KEY_UPI_ID, upi).apply()
    }

    // ---- Items ----
    fun items(): List<ShopItem> {
        val json = prefs.getString(KEY_ITEMS_JSON, null) ?: return sampleItems()
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i -> itemFromJson(arr.getJSONObject(i)) }
        } catch (e: Exception) {
            sampleItems()
        }
    }

    fun saveItems(items: List<ShopItem>) {
        prefs.edit().putString(KEY_ITEMS_JSON, itemsToJson(items)).apply()
    }

    fun addItem(item: ShopItem) = saveItems(items() + item)
    fun deleteItem(id: String) = saveItems(items().filter { it.id != id })

    // ---- Bills ----
    fun bills(): List<Bill> {
        if (!billsFile.exists()) return emptyList()
        return try {
            val arr = JSONArray(billsFile.readText())
            (0 until arr.length()).map { i -> billFromJson(arr.getJSONObject(i)) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun nextBillNumber(): Int = (bills().maxOfOrNull { it.number } ?: 100) + 1

    fun saveBill(bill: Bill) {
        saveBills(bills() + bill)
    }

    fun billByNumber(number: Int): Bill? = bills().find { it.number == number }

    /** Total sales for a given day (default: today). */
    fun dailyTotal(dateIso: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)): java.math.BigDecimal =
        bills().filter { it.dateIso == dateIso }
            .fold(java.math.BigDecimal.ZERO) { acc, bill -> acc.add(bill.total()) }

    fun todayIso(): String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    fun nowTime(): String = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))

    private fun saveBills(bills: List<Bill>) {
        val arr = JSONArray()
        bills.forEach { arr.put(billToJson(it)) }
        billsFile.writeText(arr.toString())
    }

    // ---- JSON helpers (org.json is built into Android — no extra dependency) ----
    private fun itemsToJson(items: List<ShopItem>): String {
        val arr = JSONArray()
        items.forEach { item ->
            arr.put(JSONObject().apply {
                put("id", item.id); put("name", item.name)
                put("unit", item.unit); put("price", item.price.toPlainString())
            })
        }
        return arr.toString()
    }

    private fun itemFromJson(o: JSONObject) = ShopItem(
        o.getString("id"), o.getString("name"), o.getString("unit"), money(o.getString("price"))
    )

    private fun billToJson(b: Bill): JSONObject = JSONObject().apply {
        put("number", b.number); put("date", b.dateIso); put("time", b.timeIso)
        put("discount", b.discountPercent.toPlainString())
        put("tax", b.taxPercent.toPlainString())
        put("shopName", b.shopName); put("upiId", b.upiId)
        val lines = JSONArray()
        b.lines.forEach { line ->
            lines.put(JSONObject().apply {
                put("itemId", line.itemId); put("name", line.name); put("unit", line.unit)
                put("price", line.price.toPlainString()); put("qty", line.quantity)
            })
        }
        put("lines", lines)
    }

    private fun billFromJson(o: JSONObject): Bill {
        val linesArr = o.getJSONArray("lines")
        val lines = (0 until linesArr.length()).map { i ->
            val l = linesArr.getJSONObject(i)
            BillLine(l.getString("itemId"), l.getString("name"), l.getString("unit"),
                money(l.getString("price")), l.getInt("qty"))
        }
        return Bill(
            o.getInt("number"), o.getString("date"), o.getString("time"), lines,
            money(o.getString("discount")), money(o.getString("tax")),
            o.getString("shopName"), o.getString("upiId")
        )
    }
}
