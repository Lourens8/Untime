package com.meetingalarm.alarm

import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences

/**
 * Manages Do Not Disturb mode for active meetings.
 * Saves the prior DND state so it can be restored correctly when the last meeting ends.
 * Tracks active meeting count to handle overlapping meetings.
 */
class DndManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("dnd_state", Context.MODE_PRIVATE)
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun hasAccess(): Boolean = notificationManager.isNotificationPolicyAccessGranted

    fun enableDnd(eventId: Long = 0L) {
        if (!hasAccess()) return

        val activeCount = prefs.getInt(KEY_ACTIVE_COUNT, 0)

        // Save the prior filter only when no meetings are currently active
        if (activeCount == 0) {
            prefs.edit()
                .putInt(KEY_PRIOR_FILTER, notificationManager.currentInterruptionFilter)
                .apply()
        }

        prefs.edit().putInt(KEY_ACTIVE_COUNT, activeCount + 1).apply()

        if (eventId != 0L) {
            val ids = prefs.getStringSet(KEY_ACTIVE_EVENT_IDS, emptySet())!!.toMutableSet()
            ids.add(eventId.toString())
            prefs.edit().putStringSet(KEY_ACTIVE_EVENT_IDS, ids).apply()
        }

        notificationManager.setInterruptionFilter(
            NotificationManager.INTERRUPTION_FILTER_ALARMS
        )
    }

    /**
     * Enable DND with a known end time. Same as enableDnd() — the end time is
     * tracked separately for notifications/UI, not by the DND system itself.
     */
    fun enableDndUntil(endTimeMillis: Long, eventId: Long = 0L) {
        enableDnd(eventId)
    }

    fun restoreDnd(eventId: Long = 0L) {
        if (!hasAccess()) return

        val activeCount = prefs.getInt(KEY_ACTIVE_COUNT, 0)
        val newCount = (activeCount - 1).coerceAtLeast(0)
        prefs.edit().putInt(KEY_ACTIVE_COUNT, newCount).apply()

        if (eventId != 0L) {
            val ids = prefs.getStringSet(KEY_ACTIVE_EVENT_IDS, emptySet())!!.toMutableSet()
            ids.remove(eventId.toString())
            prefs.edit().putStringSet(KEY_ACTIVE_EVENT_IDS, ids).apply()
        }

        // Only restore when the last active meeting ends
        if (newCount == 0) {
            val priorFilter = prefs.getInt(
                KEY_PRIOR_FILTER,
                NotificationManager.INTERRUPTION_FILTER_ALL
            )
            notificationManager.setInterruptionFilter(priorFilter)
            prefs.edit()
                .remove(KEY_PRIOR_FILTER)
                .remove(KEY_ACTIVE_COUNT)
                .remove(KEY_ACTIVE_EVENT_IDS)
                .apply()
        }
    }

    /** Returns the set of event IDs that currently have DND active. */
    fun getActiveEventIds(): Set<Long> {
        return prefs.getStringSet(KEY_ACTIVE_EVENT_IDS, emptySet())!!
            .mapNotNull { it.toLongOrNull() }
            .toSet()
    }

    /**
     * Update the DND countdown end time. No-op since we can't show a system
     * DND timer from a third-party app.
     */
    fun updateDndEndTime(newEndTimeMillis: Long) {
        // No-op — DND timer on lock screen requires hidden system API
    }

    companion object {
        private const val KEY_PRIOR_FILTER = "prior_interruption_filter"
        private const val KEY_ACTIVE_COUNT = "active_meeting_count"
        private const val KEY_ACTIVE_EVENT_IDS = "active_event_ids"
    }
}
