package com.bserc.reelmonster

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import androidx.core.content.ContextCompat

/**
 * Level 2 — automatic reel counting.
 *
 * An AccessibilityService that counts a reel each time the user scrolls to a
 * new one inside Instagram or YouTube. It only receives scroll events from
 * those two apps (see res/xml/accessibility_service_config.xml) and it never
 * reads window content: canRetrieveWindowContent is false, so no screen text,
 * password or message is ever seen. Everything stays on the phone; nothing
 * is sent anywhere.
 *
 * How counting works, honestly:
 * - One swipe fires a burst of scroll events, so a reel is only counted if at
 *   least MIN_GAP_MS have passed since the last counted reel. A reel flicked
 *   past in under ~1.5 s is therefore NOT counted.
 * - The first reel you see when you open Reels/Shorts is not counted either —
 *   counting starts with the first swipe.
 * - Scroll events also fire for horizontal swipes (photo carousels, comment
 *   sheets), so the count is an estimate, not an exact tally. Over 20 reels
 *   it should land within 2 of a careful manual count; fast, erratic
 *   scrolling will drift further.
 * - Counting only happens while Level 2 is selected in the app AND the
 *   monster bubble service is running. If the bubble is stopped, nothing is
 *   counted, so the count never moves without the monster visible.
 *
 * The user turns this on in Settings → Accessibility → Reel Monster. Turning
 * it off there stops all automatic counting immediately.
 */
class ReelDetectionService : AccessibilityService() {

    companion object {
        // Minimum gap between two counted reels (see class doc above).
        private const val MIN_GAP_MS = 1500L
    }

    private var countStore: CountStore? = null
    private var lastCountedAtMs = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        countStore = CountStore(applicationContext)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_VIEW_SCROLLED) return
        val store = countStore ?: return
        if (store.level() != 2) return
        if (!store.isBubbleActive()) return
        val now = System.currentTimeMillis()
        if (now - lastCountedAtMs < MIN_GAP_MS) return
        lastCountedAtMs = now
        store.incrementToday()
        // Ask the bubble to refresh its badge and notification from the new count.
        val refresh = Intent(this, MonsterService::class.java)
            .setAction(MonsterService.ACTION_REFRESH_COUNT)
        ContextCompat.startForegroundService(this, refresh)
    }

    override fun onInterrupt() {
        // Nothing to clean up: no listeners, no views, no network.
    }
}
