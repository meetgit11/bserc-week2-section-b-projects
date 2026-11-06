package com.bserc.billbook

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.math.BigDecimal

/**
 * Make a new bill: tap + / − on any item to set its quantity,
 * enter a discount % and a tax %, watch the totals update live,
 * then save. Totals are computed with BigDecimal (see Models.kt),
 * so they match a hand calculation.
 */
class NewBillActivity : AppCompatActivity() {

    private lateinit var store: BillStore
    private lateinit var itemsLayout: LinearLayout
    private lateinit var discountInput: EditText
    private lateinit var taxInput: EditText
    private lateinit var totalsText: TextView

    /** itemId -> quantity chosen for this bill. */
    private val quantities = mutableMapOf<String, Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_bill)
        store = BillStore(this)

        itemsLayout = findViewById(R.id.itemsLayout)
        discountInput = findViewById(R.id.discountInput)
        taxInput = findViewById(R.id.taxInput)
        totalsText = findViewById(R.id.totalsText)

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) = updateTotals()
        }
        discountInput.addTextChangedListener(watcher)
        taxInput.addTextChangedListener(watcher)

        findViewById<Button>(R.id.saveBillButton).setOnClickListener { saveBill() }
        buildItemRows()
        updateTotals()
    }

    private fun buildItemRows() {
        itemsLayout.removeAllViews()
        for (item in store.items()) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, 8, 0, 8)
            }
            val name = TextView(this).apply {
                text = getString(R.string.item_row_label, item.name, item.price.formatRupees())
                textSize = 16f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            val minus = Button(this).apply { text = "−" }
            val qtyText = TextView(this).apply {
                text = "0"
                textSize = 18f
                gravity = Gravity.CENTER
                setPadding(16, 0, 16, 0)
            }
            val plus = Button(this).apply { text = "+" }
            minus.setOnClickListener {
                val q = (quantities[item.id] ?: 0).let { if (it > 0) it - 1 else 0 }
                quantities[item.id] = q
                qtyText.text = q.toString()
                updateTotals()
            }
            plus.setOnClickListener {
                val q = (quantities[item.id] ?: 0) + 1
                quantities[item.id] = q
                qtyText.text = q.toString()
                updateTotals()
            }
            row.addView(name)
            row.addView(minus)
            row.addView(qtyText)
            row.addView(plus)
            itemsLayout.addView(row)
        }
    }

    private fun currentBill(): Bill {
        val lines = store.items()
            .filter { (quantities[it.id] ?: 0) > 0 }
            .map { item ->
                BillLine(item.id, item.name, item.unit, item.price, quantities[item.id] ?: 0)
            }
        val discount = discountInput.text.toString().toBigDecimalOrNull() ?: BigDecimal.ZERO
        val tax = taxInput.text.toString().toBigDecimalOrNull() ?: BigDecimal.ZERO
        return Bill(
            number = store.nextBillNumber(),
            dateIso = store.todayIso(),
            timeIso = store.nowTime(),
            lines = lines,
            discountPercent = discount,
            taxPercent = tax,
            shopName = store.shopName(),
            upiId = store.upiId()
        )
    }

    private fun updateTotals() {
        val bill = currentBill()
        totalsText.text = getString(
            R.string.totals_breakdown,
            bill.subtotal().formatRupees(),
            bill.discountAmount().formatRupees(),
            bill.taxAmount().formatRupees(),
            bill.total().formatRupees()
        )
    }

    private fun saveBill() {
        val bill = currentBill()
        if (bill.lines.isEmpty()) {
            Toast.makeText(this, R.string.bill_empty, Toast.LENGTH_SHORT).show()
            return
        }
        if (bill.discountPercent < BigDecimal.ZERO || bill.discountPercent > BigDecimal(100) ||
            bill.taxPercent < BigDecimal.ZERO || bill.taxPercent > BigDecimal(100)
        ) {
            Toast.makeText(this, R.string.percent_invalid, Toast.LENGTH_SHORT).show()
            return
        }
        store.saveBill(bill)
        Toast.makeText(this, getString(R.string.bill_saved, bill.number), Toast.LENGTH_SHORT).show()
        startActivity(
            Intent(this, BillDetailActivity::class.java)
                .putExtra(BillDetailActivity.EXTRA_BILL_NUMBER, bill.number)
        )
        finish()
    }
}
