package com.meetingalarm.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.meetingalarm.calendar.CalendarReader
import com.meetingalarm.exclusion.ExclusionStore

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val exclusionStore = ExclusionStore(context)
        val calendarReader = CalendarReader(context, exclusionStore)
        val alarmScheduler = AlarmScheduler(context)

        val meetings = calendarReader.readTodayMeetings()
        alarmScheduler.scheduleAll(meetings)
    }
}
