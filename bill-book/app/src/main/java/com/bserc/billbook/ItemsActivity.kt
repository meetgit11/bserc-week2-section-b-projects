package com.bserc.billbook

import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/** Item list with prices: add a new item, or delete an existing one. */
class ItemsActivity : AppCompatActivity() {

    private lateinit var store: BillStore
    private lateinit var itemsLayout: LinearLayout
    private lateinit var nameInput: EditText
    private lateinit var unitInput: EditText
    private lateinit var priceInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_items)
        store = BillStore(this)

        itemsLayout = findViewById(R.id.itemsLayout)
        nameInput = findViewById(R.id.newItemName)
        unitInput = findViewById(R.id.newItemUnit)
        priceInput = findViewById(R.id.newItemPrice)

        findViewById<Button>(R.id.addItemButton).setOnClickListener { addItem() }
        refresh()
    }

    private fun refresh() {
        itemsLayout.removeAllViews()
        for (item in store.items()) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, 8, 0, 8)
            }
            val label = TextView(this).apply {
                text = getString(R.string.item_row_label, item.name, item.price.formatRupees())
                textSize = 16f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            val delete = Button(this).apply {
                text = getString(R.string.delete)
                setOnClickListener {
                    store.deleteItem(item.id)
                    refresh()
                }
            }
            row.addView(label)
            row.addView(delete)
            itemsLayout.addView(row)
        }
    }

    private fun addItem() {
        val name = nameInput.text.toString().trim()
        val unit = unitInput.text.toString().trim().ifEmpty { "item" }
        val price = priceInput.text.toString().toBigDecimalOrNull()
        if (name.isEmpty() || price == null || price <= java.math.BigDecimal.ZERO) {
            Toast.makeText(this, R.string.item_invalid, Toast.LENGTH_SHORT).show()
            return
        }
        store.addItem(ShopItem("item-" + System.currentTimeMillis(), name, unit, money(price.toPlainString())))
        nameInput.text.clear()
        unitInput.text.clear()
        priceInput.text.clear()
        refresh()
    }
}
