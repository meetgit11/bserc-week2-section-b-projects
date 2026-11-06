package com.bserc.reelmonster

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import kotlin.math.abs

/**
 * Foreground service that shows the floating monster bubble on top of
 * Instagram, YouTube and every other app.
 *
 * Level 1 (manual): tap the monster after each reel and the counter goes up by 1.
 * The monster grows as today's count rises. Drag it to move it.
 * Long-press it to open the app. Tapping it also shows a funny line.
 *
 * Requires the "Display over other apps" permission, granted by the user
 * from the app's Start button (Settings.ACTION_MANAGE_OVERLAY_PERMISSION).
 */
class MonsterService : Service() {

    companion object {
        const val CHANNEL_ID = "monster_bubble"
        const val NOTIFICATION_ID = 1
        const val ACTION_STOP = "com.bserc.reelmonster.STOP"
        const val ACTION_REFRESH_COUNT = "com.bserc.reelmonster.REFRESH_COUNT"
        private const val BASE_SIZE_DP = 72
    }

    private lateinit var windowManager: WindowManager
    private lateinit var countStore: CountStore
    private var bubble: FrameLayout? = null
    private var countBadge: TextView? = null
    private var params: WindowManager.LayoutParams? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        countStore = CountStore(applicationContext)
        countStore.setBubbleActive(true)
        createNotificationChannel()
        // Android 14+ requires a foreground service type; "specialUse" fits the
        // floating bubble, and the type is declared in the manifest.
        ServiceCompat.startForeground(
            this, NOTIFICATION_ID, buildNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            removeBubble()
            stopSelf()
            return START_NOT_STICKY
        }
        if (intent?.action == ACTION_REFRESH_COUNT) {
            // Level 2 counted a reel; refresh the badge and notification.
            updateBadge()
            return START_STICKY
        }
        if (bubble == null) showBubble()
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            )
            channel.description = getString(R.string.notification_channel_description)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val openIntent = Intent(this, MainActivity::class.java)
        val pending = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_monster)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text, countStore.todayCount()))
            .setContentIntent(pending)
            .setOngoing(true)
            .build()
    }

    private fun showBubble() {
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.bubble_monster, null) as FrameLayout
        countBadge = view.findViewById(R.id.bubbleCount)

        val sizePx = bubbleSizePx(countStore.todayCount())
        params = WindowManager.LayoutParams(
            sizePx,
            sizePx,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 40
            y = 300
        }

        var downX = 0f
        var downY = 0f
        var startX = 0
        var startY = 0
        var moved = false
        var downTime = 0L
        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.rawX
                    downY = event.rawY
                    startX = params!!.x
                    startY = params!!.y
                    downTime = System.currentTimeMillis()
                    moved = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - downX).toInt()
                    val dy = (event.rawY - downY).toInt()
                    if (abs(dx) > 10 || abs(dy) > 10) moved = true
                    params!!.x = startX + dx
                    params!!.y = startY + dy
                    windowManager.updateViewLayout(view, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val heldMs = System.currentTimeMillis() - downTime
                    if (!moved && heldMs < 300) {
                        onMonsterTapped()
                    } else if (!moved && heldMs >= 500) {
                        openApp()
                    }
                    true
                }
                else -> false
            }
        }
        // NOTE: no click listener on monsterImage — it must stay non-clickable
        // so touch events reach the FrameLayout's drag/tap listener above.
        bubble = view
        windowManager.addView(view, params)
        updateBadge()
    }

    /** One tap = one reel watched. */
    private fun onMonsterTapped() {
        val count = countStore.incrementToday()
        updateBadge()
        Toast.makeText(this, FunnyLines.forCount(count), Toast.LENGTH_SHORT).show()
    }

    private fun updateBadge() {
        val count = countStore.todayCount()
        countBadge?.text = count.toString()
        // The monster grows with today's count: +0.8% per reel, capped at +80%.
        val sizePx = bubbleSizePx(count)
        params?.let {
            it.width = sizePx
            it.height = sizePx
            bubble?.let { view -> windowManager.updateViewLayout(view, it) }
        }
        // Keep the notification count in sync.
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, buildNotification())
    }

    private fun bubbleSizePx(count: Int): Int {
        val density = resources.displayMetrics.density
        val growth = 1f + minOf(count, 100) * 0.008f
        return (BASE_SIZE_DP * growth * density).toInt()
    }

    private fun openApp() {
        val intent = Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun removeBubble() {
        bubble?.let { windowManager.removeView(it) }
        bubble = null
        countBadge = null
    }

    override fun onDestroy() {
        countStore.setBubbleActive(false)
        removeBubble()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
