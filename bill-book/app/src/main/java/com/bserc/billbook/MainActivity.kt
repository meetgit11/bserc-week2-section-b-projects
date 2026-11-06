package com.bserc.billbook

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * Home screen: shop name, today's total sales, bill history
 * (newest first), and buttons to make a bill, manage items and
 * edit the shop profile. Everything works offline.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var store: BillStore
    private lateinit var shopNameText: TextView
    private lateinit var todayTotalText: TextView
    private lateinit var sampleWarningText: TextView
    private lateinit var historyLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        store = BillStore(this)

        shopNameText = findViewById(R.id.shopNameText)
        todayTotalText = findViewById(R.id.todayTotalText)
        sampleWarningText = findViewById(R.id.sampleWarningText)
        historyLayout = findViewById(R.id.historyLayout)

        findViewById<Button>(R.id.newBillButton).setOnClickListener {
            startActivity(Intent(this, NewBillActivity::class.java))
        }
        findViewById<Button>(R.id.itemsButton).setOnClickListener {
            startActivity(Intent(this, ItemsActivity::class.java))
        }
        findViewById<Button>(R.id.profileButton).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        shopNameText.text = store.shopName()
        todayTotalText.text = getString(R.string.today_total, store.dailyTotal().formatRupees())
        sampleWarningText.visibility =
            if (store.isSampleUpiId()) android.view.View.VISIBLE else android.view.View.GONE

        historyLayout.removeAllViews()
        val bills = store.bills().sortedByDescending { it.number }
        if (bills.isEmpty()) {
            historyLayout.addView(TextView(this).apply {
                text = getString(R.string.no_bills_yet)
                textSize = 16f
            })
        }
        for (bill in bills) {
            val row = Button(this).apply {
                text = getString(
                    R.string.history_row,
                    bill.number, bill.dateIso, bill.timeIso, bill.total().formatRupees()
                )
                setOnClickListener {
                    startActivity(
                        Intent(this@MainActivity, BillDetailActivity::class.java)
                            .putExtra(BillDetailActivity.EXTRA_BILL_NUMBER, bill.number)
                    )
                }
            }
            historyLayout.addView(row)
        }
    }
}
