package com.meetingalarm.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.meetingalarm.calendar.CalendarReader
import com.meetingalarm.exclusion.ExclusionStore
import com.meetingalarm.settings.MeetingOverrideStore
import com.meetingalarm.settings.SettingsStore

class NapActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_END_NAP -> endNap(context)
            ACTION_END_MEETING_DND -> {
                val eventId = intent.getLongExtra(EXTRA_EVENT_ID, 0)
                endMeetingDnd(context, eventId)
            }
        }
    }

    private fun endNap(context: Context) {
        AlarmScheduler(context).cancelNapAlarm()
        DndManager(context).restoreDnd()
        OngoingNotificationManager.cancelNapNotification(context)

        val settingsStore = SettingsStore(context)
        settingsStore.setNapActive(false)
        settingsStore.setNapEndTimeMillis(0)

        // Re-schedule meeting alarms
        val exclusionStore = ExclusionStore(context)
        val overrideStore = MeetingOverrideStore(context)
        val calendarReader = CalendarReader(context, exclusionStore, settingsStore)
        val meetings = calendarReader.readTodayMeetings()
        AlarmScheduler(context).scheduleAll(meetings, settingsStore, overrideStore)
    }

    private fun endMeetingDnd(context: Context, eventId: Long) {
        DndManager(context).restoreDnd(eventId)
        OngoingNotificationManager.cancelMeetingNotification(context, eventId)

        // Cancel the scheduled DND restore alarm for this event
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val dndIntent = Intent(context, DndRestoreReceiver::class.java)
        val dndPendingIntent = PendingIntent.getBroadcast(
            context,
            eventId.toInt() + DND_REQUEST_CODE_OFFSET,
            dndIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(dndPendingIntent)
    }

    companion object {
        const val ACTION_END_NAP = "com.meetingalarm.ACTION_END_NAP"
        const val ACTION_END_MEETING_DND = "com.meetingalarm.ACTION_END_MEETING_DND"
        const val EXTRA_EVENT_ID = "event_id"
        private const val DND_REQUEST_CODE_OFFSET = 100_000
    }
}
