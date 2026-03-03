package com.meetingalarm.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(EXTRA_MEETING_TITLE) ?: "Meeting"
        val eventId = intent.getLongExtra(EXTRA_EVENT_ID, 0)
        val location = intent.getStringExtra(EXTRA_LOCATION)
        val autoDismissSeconds = intent.getIntExtra(EXTRA_AUTO_DISMISS_SECONDS, 60)
        val snoozeDurationSeconds = intent.getIntExtra(EXTRA_SNOOZE_DURATION_SECONDS, 60)
        val snoozeEnabled = intent.getBooleanExtra(EXTRA_SNOOZE_ENABLED, true)
        val alarmSoundUri = intent.getStringExtra(EXTRA_ALARM_SOUND_URI)

        // Start foreground service to play sound and launch alarm UI
        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtra(AlarmService.EXTRA_MEETING_TITLE, title)
            putExtra(AlarmService.EXTRA_EVENT_ID, eventId)
            putExtra(AlarmService.EXTRA_LOCATION, location)
            putExtra(AlarmService.EXTRA_AUTO_DISMISS_SECONDS, autoDismissSeconds)
            putExtra(AlarmService.EXTRA_SNOOZE_DURATION_SECONDS, snoozeDurationSeconds)
            putExtra(AlarmService.EXTRA_SNOOZE_ENABLED, snoozeEnabled)
            putExtra(AlarmService.EXTRA_ALARM_SOUND_URI, alarmSoundUri)
        }
        ContextCompat.startForegroundService(context, serviceIntent)
    }

    companion object {
        const val EXTRA_MEETING_TITLE = "meeting_title"
        const val EXTRA_EVENT_ID = "event_id"
        const val EXTRA_END_TIME = "end_time"
        const val EXTRA_LOCATION = "location"
        const val EXTRA_AUTO_DISMISS_SECONDS = "auto_dismiss_seconds"
        const val EXTRA_SNOOZE_DURATION_SECONDS = "snooze_duration_seconds"
        const val EXTRA_SNOOZE_ENABLED = "snooze_enabled"
        const val EXTRA_ALARM_SOUND_URI = "alarm_sound_uri"
    }
}
