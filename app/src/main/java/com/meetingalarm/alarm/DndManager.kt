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

    fun enableDnd() {
        if (!hasAccess()) return

        val activeCount = prefs.getInt(KEY_ACTIVE_COUNT, 0)

        // Save the prior filter only when no meetings are currently active
        if (activeCount == 0) {
            prefs.edit()
                .putInt(KEY_PRIOR_FILTER, notificationManager.currentInterruptionFilter)
                .apply()
        }

        prefs.edit().putInt(KEY_ACTIVE_COUNT, activeCount + 1).apply()

        notificationManager.setInterruptionFilter(
            NotificationManager.INTERRUPTION_FILTER_NONE
        )
    }

    fun restoreDnd() {
        if (!hasAccess()) return

        val activeCount = prefs.getInt(KEY_ACTIVE_COUNT, 0)
        val newCount = (activeCount - 1).coerceAtLeast(0)
        prefs.edit().putInt(KEY_ACTIVE_COUNT, newCount).apply()

        // Only restore when the last active meeting ends
        if (newCount == 0) {
            val priorFilter = prefs.getInt(
                KEY_PRIOR_FILTER,
                NotificationManager.INTERRUPTION_FILTER_ALL
            )
            notificationManager.setInterruptionFilter(priorFilter)
            prefs.edit().remove(KEY_PRIOR_FILTER).remove(KEY_ACTIVE_COUNT).apply()
        }
    }

    companion object {
        private const val KEY_PRIOR_FILTER = "prior_interruption_filter"
        private const val KEY_ACTIVE_COUNT = "active_meeting_count"
    }
}
