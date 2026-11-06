package com.bserc.billbook

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * Shop profile: shop name and UPI ID used on every bill and in every QR code.
 * The seeded UPI ID is an obviously fake sample — replace it with your own
 * UPI ID before taking real payments.
 */
class ProfileActivity : AppCompatActivity() {

    private lateinit var store: BillStore
    private lateinit var nameInput: EditText
    private lateinit var upiInput: EditText
    private lateinit var sampleWarning: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        store = BillStore(this)

        nameInput = findViewById(R.id.shopNameInput)
        upiInput = findViewById(R.id.upiIdInput)
        sampleWarning = findViewById(R.id.sampleUpiWarning)

        nameInput.setText(store.shopName())
        upiInput.setText(store.upiId())
        sampleWarning.visibility =
            if (store.isSampleUpiId()) View.VISIBLE else View.GONE

        findViewById<Button>(R.id.saveProfileButton).setOnClickListener {
            val name = nameInput.text.toString().trim()
            val upi = upiInput.text.toString().trim()
            if (name.isEmpty() || upi.isEmpty() || !upi.contains("@")) {
                Toast.makeText(this, R.string.profile_invalid, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            store.saveProfile(name, upi)
            sampleWarning.visibility =
                if (store.isSampleUpiId()) View.VISIBLE else View.GONE
            Toast.makeText(this, R.string.profile_saved, Toast.LENGTH_SHORT).show()
        }
    }
}
