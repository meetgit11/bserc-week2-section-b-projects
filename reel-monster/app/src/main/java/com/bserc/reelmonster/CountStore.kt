package com.bserc.reelmonster

import android.content.Context
import android.content.SharedPreferences
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Persists one counter per day in SharedPreferences.
 *
 * The count survives a phone restart because it is written to disk
 * (SharedPreferences) on every tap — no boot receiver or server is needed.
 * Nothing here uses the internet or any account.
 */
class CountStore(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        const val PREFS_NAME = "reel_monster_counts"
        const val KEY_SECONDS_PER_REEL = "seconds_per_reel"
        const val DEFAULT_SECONDS_PER_REEL = 30
        const val KEY_LEVEL = "level"
        const val LEVEL_MANUAL = 1
        const val LEVEL_AUTOMATIC = 2
        const val KEY_BUBBLE_ACTIVE = "bubble_active"
        private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    }

    private fun keyFor(date: LocalDate): String = "count_" + date.format(DATE_FORMAT)

    fun todayCount(): Int = countFor(LocalDate.now())

    fun countFor(date: LocalDate): Int = prefs.getInt(keyFor(date), 0)

    /** Adds one reel to today's count and returns the new total. */
    fun incrementToday(): Int {
        val today = LocalDate.now()
        val next = countFor(today) + 1
        prefs.edit().putInt(keyFor(today), next).apply()
        return next
    }

    /** Removes one reel from today (undo an accidental tap). Never goes below 0. */
    fun decrementToday(): Int {
        val today = LocalDate.now()
        val next = (countFor(today) - 1).coerceAtLeast(0)
        prefs.edit().putInt(keyFor(today), next).apply()
        return next
    }

    fun resetToday() {
        prefs.edit().remove(keyFor(LocalDate.now())).apply()
    }

    /** Counts for today and the 6 days before it, newest first. */
    fun lastSevenDays(): List<Pair<LocalDate, Int>> {
        val today = LocalDate.now()
        return (0..6).map { offset ->
            val date = today.minusDays(offset.toLong())
            date to countFor(date)
        }
    }

    /** Estimated seconds lost per reel. Editable in Settings; default 30 s. */
    fun secondsPerReel(): Int =
        prefs.getInt(KEY_SECONDS_PER_REEL, DEFAULT_SECONDS_PER_REEL)

    fun setSecondsPerReel(seconds: Int) {
        prefs.edit().putInt(KEY_SECONDS_PER_REEL, seconds.coerceIn(5, 300)).apply()
    }

    /** Estimated time lost today, in whole minutes. Labelled as an estimate in the UI. */
    fun estimatedMinutesLostToday(): Int =
        todayCount() * secondsPerReel() / 60

    /** Counting level: 1 = manual taps, 2 = automatic (accessibility service). */
    fun level(): Int = prefs.getInt(KEY_LEVEL, LEVEL_MANUAL)

    fun setLevel(level: Int) {
        prefs.edit().putInt(KEY_LEVEL, if (level == LEVEL_AUTOMATIC) LEVEL_AUTOMATIC else LEVEL_MANUAL).apply()
    }

    /**
     * True while the monster bubble service is running. Level 2 only counts
     * while this is true, so the count never moves without the monster visible.
     */
    fun isBubbleActive(): Boolean = prefs.getBoolean(KEY_BUBBLE_ACTIVE, false)

    fun setBubbleActive(active: Boolean) {
        prefs.edit().putBoolean(KEY_BUBBLE_ACTIVE, active).apply()
    }
}
