package com.meetingalarm.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Fires at meeting end time to restore the prior DND state.
 */
class DndRestoreReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        DndManager(context).restoreDnd()
    }

    companion object {
        const val EXTRA_EVENT_ID = "event_id"
    }
}
