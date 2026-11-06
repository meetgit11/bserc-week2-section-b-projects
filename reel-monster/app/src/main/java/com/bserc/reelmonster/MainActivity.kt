package com.bserc.reelmonster

import android.Manifest
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.accessibility.AccessibilityManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.time.format.DateTimeFormatter

/**
 * Main screen: today's count, estimated time lost, last 7 days,
 * level choice (Level 1 manual taps; Level 2 automatic counting via the
 * ReelDetectionService accessibility service), and the Start / Stop
 * monster controls.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var countStore: CountStore
    private lateinit var todayCountText: TextView
    private lateinit var timeLostText: TextView
    private lateinit var lastSevenDaysLayout: LinearLayout
    private lateinit var monsterStatusText: TextView
    private lateinit var levelStatusText: TextView
    private lateinit var secondsPerReelInput: EditText
    private lateinit var levelGroup: RadioGroup

    private val overlayPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (Settings.canDrawOverlays(this)) startMonsterService() else {
                Toast.makeText(this, R.string.overlay_denied, Toast.LENGTH_LONG).show()
            }
        }

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* optional */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        countStore = CountStore(this)

        todayCountText = findViewById(R.id.todayCountText)
        timeLostText = findViewById(R.id.timeLostText)
        lastSevenDaysLayout = findViewById(R.id.lastSevenDaysLayout)
        monsterStatusText = findViewById(R.id.monsterStatusText)
        levelStatusText = findViewById(R.id.levelStatusText)
        secondsPerReelInput = findViewById(R.id.secondsPerReelInput)
        levelGroup = findViewById(R.id.levelGroup)

        secondsPerReelInput.setText(countStore.secondsPerReel().toString())
        findViewById<Button>(R.id.saveSecondsButton).setOnClickListener {
            val seconds = secondsPerReelInput.text.toString().toIntOrNull()
            if (seconds == null || seconds < 5 || seconds > 300) {
                Toast.makeText(this, R.string.seconds_invalid, Toast.LENGTH_SHORT).show()
            } else {
                countStore.setSecondsPerReel(seconds)
                refresh()
                Toast.makeText(this, R.string.seconds_saved, Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<RadioButton>(R.id.level1Radio).setOnClickListener {
            countStore.setLevel(CountStore.LEVEL_MANUAL)
            refresh()
        }

        findViewById<RadioButton>(R.id.level2Radio).setOnClickListener {
            if (isLevel2ServiceEnabled()) {
                countStore.setLevel(CountStore.LEVEL_AUTOMATIC)
                refresh()
                Toast.makeText(this, R.string.level2_enabled, Toast.LENGTH_LONG).show()
            } else {
                // Keep Level 1 selected until the service is actually enabled.
                levelGroup.check(R.id.level1Radio)
                Toast.makeText(this, R.string.level2_enable_prompt, Toast.LENGTH_LONG).show()
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        }

        findViewById<Button>(R.id.startMonsterButton).setOnClickListener { onStartMonsterClicked() }
        findViewById<Button>(R.id.stopMonsterButton).setOnClickListener { stopMonsterService() }
        findViewById<Button>(R.id.undoButton).setOnClickListener {
            countStore.decrementToday()
            refresh()
        }
        findViewById<Button>(R.id.resetTodayButton).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(R.string.reset_confirm_title)
                .setMessage(R.string.reset_confirm_message)
                .setPositiveButton(R.string.reset) { _, _ ->
                    countStore.resetToday()
                    refresh()
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }
        refresh()
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        val today = countStore.todayCount()
        todayCountText.text = getString(R.string.today_reels, today)
        val minutes = countStore.estimatedMinutesLostToday()
        timeLostText.text = getString(
            R.string.time_lost_estimate,
            minutes,
            countStore.secondsPerReel()
        )
        lastSevenDaysLayout.removeAllViews()
        val dayFormat = DateTimeFormatter.ofPattern("EEE d MMM")
        for ((date, count) in countStore.lastSevenDays()) {
            val row = TextView(this).apply {
                text = getString(R.string.day_row, date.format(dayFormat), count)
                textSize = 16f
                setPadding(0, 6, 0, 6)
            }
            lastSevenDaysLayout.addView(row)
        }
        monsterStatusText.text = getString(
            if (Settings.canDrawOverlays(this)) R.string.monster_ready else R.string.monster_needs_permission
        )
        if (countStore.level() == CountStore.LEVEL_AUTOMATIC) {
            levelGroup.check(R.id.level2Radio)
            levelStatusText.text = getString(
                if (isLevel2ServiceEnabled()) R.string.level2_status_on else R.string.level2_status_off
            )
        } else {
            levelGroup.check(R.id.level1Radio)
            levelStatusText.text = getString(
                if (isLevel2ServiceEnabled()) R.string.level1_status_service_on else R.string.level1_status
            )
        }
    }

    /** True when the user has switched on Reel Monster in Accessibility settings. */
    private fun isLevel2ServiceEnabled(): Boolean {
        val manager = getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager
        return manager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            .any {
                it.resolveInfo.serviceInfo.packageName == packageName &&
                    it.resolveInfo.serviceInfo.name == ReelDetectionService::class.java.name
            }
    }

    private fun onStartMonsterClicked() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            overlayPermissionLauncher.launch(intent)
        } else {
            startMonsterService()
        }
    }

    private fun startMonsterService() {
        val intent = Intent(this, MonsterService::class.java)
        ContextCompat.startForegroundService(this, intent)
        Toast.makeText(this, R.string.monster_started, Toast.LENGTH_SHORT).show()
    }

    private fun stopMonsterService() {
        val intent = Intent(this, MonsterService::class.java).apply {
            action = MonsterService.ACTION_STOP
        }
        startService(intent)
        Toast.makeText(this, R.string.monster_stopped, Toast.LENGTH_SHORT).show()
    }
}
