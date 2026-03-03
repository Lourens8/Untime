package com.meetingalarm.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.meetingalarm.calendar.CalendarReader
import com.meetingalarm.exclusion.ExclusionStore
import com.meetingalarm.settings.MeetingOverrideStore
import com.meetingalarm.settings.SettingsStore

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val exclusionStore = ExclusionStore(context)
        val settingsStore = SettingsStore(context)
        val calendarReader = CalendarReader(context, exclusionStore, settingsStore)
        val alarmScheduler = AlarmScheduler(context)
        val overrideStore = MeetingOverrideStore(context)

        val meetings = calendarReader.readTodayMeetings()
        alarmScheduler.scheduleAll(meetings, settingsStore, overrideStore)
    }
}
