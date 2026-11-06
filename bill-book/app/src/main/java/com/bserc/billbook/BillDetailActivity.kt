package com.bserc.billbook

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

/**
 * Shows one saved bill with its UPI QR code, and shares the bill as an
 * image (PNG) through any app — WhatsApp included, via the share sheet.
 *
 * The QR code encodes upi://pay?pa=<UPI ID>&pn=<shop>&am=<exact total>&cu=INR.
 * Scanning it in a UPI app shows the right amount; nothing is paid
 * automatically — the user still has to confirm in their UPI app.
 */
class BillDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_BILL_NUMBER = "bill_number"
    }

    private lateinit var store: BillStore
    private var bill: Bill? = null
    private lateinit var billCard: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bill_detail)
        store = BillStore(this)
        billCard = findViewById(R.id.billCard)

        val number = intent.getIntExtra(EXTRA_BILL_NUMBER, -1)
        val found = store.billByNumber(number)
        if (found == null) {
            Toast.makeText(this, R.string.bill_not_found, Toast.LENGTH_LONG).show()
            finish()
            return
        }
        bill = found
        render(found)

        findViewById<Button>(R.id.shareImageButton).setOnClickListener { shareBillAsImage() }
        findViewById<Button>(R.id.shareTextButton).setOnClickListener { shareBillAsText() }
    }

    private fun render(b: Bill) {
        findViewById<TextView>(R.id.billTitleText).text =
            getString(R.string.bill_title, b.shopName, b.number)
        findViewById<TextView>(R.id.billDateText).text =
            getString(R.string.bill_date, b.dateIso, b.timeIso)

        val linesText = StringBuilder()
        for (line in b.lines) {
            linesText.append(
                getString(
                    R.string.bill_line_row,
                    line.name, line.quantity, line.price.formatRupees(),
                    line.lineTotal().formatRupees()
                )
            ).append("\n")
        }
        findViewById<TextView>(R.id.billLinesText).text = linesText.toString().trim()
        findViewById<TextView>(R.id.billTotalsText).text = getString(
            R.string.totals_breakdown,
            b.subtotal().formatRupees(),
            b.discountAmount().formatRupees(),
            b.taxAmount().formatRupees(),
            b.total().formatRupees()
        )
        findViewById<TextView>(R.id.upiLinkText).text = b.upiLink()
        if (store.isSampleUpiId()) {
            findViewById<TextView>(R.id.sampleUpiWarning).visibility = View.VISIBLE
        }
        val qrBitmap = QrUtil.encodeToBitmap(b.upiLink(), 512)
        findViewById<ImageView>(R.id.qrImage).setImageBitmap(qrBitmap)
    }

    /** Renders the bill card view to a PNG and shares it. */
    private fun shareBillAsImage() {
        val b = bill ?: return
        val bitmap = Bitmap.createBitmap(
            billCard.width, billCard.height, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        billCard.draw(canvas)

        val file = File(cacheDir, "bill-${b.number}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text, b.shopName, b.number, b.total().formatRupees()))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.share_bill)))
    }

    private fun shareBillAsText() {
        val b = bill ?: return
        val text = buildString {
            appendLine("${b.shopName} — Bill #${b.number} (${b.dateIso} ${b.timeIso})")
            for (line in b.lines) {
                appendLine("${line.name} × ${line.quantity} — ${line.lineTotal().formatRupees()}")
            }
            appendLine("Total: ${b.total().formatRupees()}")
            appendLine("Pay via UPI: ${b.upiLink()}")
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.share_bill)))
    }
}
