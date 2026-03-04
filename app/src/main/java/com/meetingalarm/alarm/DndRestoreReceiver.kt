package com.meetingalarm.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Fires at meeting end time to restore the prior DND state.
 */
class DndRestoreReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val eventId = intent.getLongExtra(EXTRA_EVENT_ID, 0)
        DndManager(context).restoreDnd(eventId)
        if (eventId != 0L) {
            OngoingNotificationManager.cancelMeetingNotification(context, eventId)
        }
    }

    companion object {
        const val EXTRA_EVENT_ID = "event_id"
    }
}
