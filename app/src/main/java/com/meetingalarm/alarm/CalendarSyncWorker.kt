package com.meetingalarm.alarm

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.meetingalarm.calendar.CalendarReader
import com.meetingalarm.exclusion.ExclusionStore
import com.meetingalarm.settings.MeetingOverrideStore
import com.meetingalarm.settings.SettingsStore

class CalendarSyncWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        val context = applicationContext
        val settingsStore = SettingsStore(context)
        val exclusionStore = ExclusionStore(context)
        val overrideStore = MeetingOverrideStore(context)
        val calendarReader = CalendarReader(context, exclusionStore, settingsStore)
        val alarmScheduler = AlarmScheduler(context)

        val meetings = calendarReader.readTodayMeetings()
        alarmScheduler.scheduleAll(meetings, settingsStore, overrideStore)

        return Result.success()
    }

    companion object {
        const val WORK_NAME = "calendar_sync"
    }
}
