package com.meetingalarm.alarm

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import com.meetingalarm.calendar.CalendarReader
import com.meetingalarm.exclusion.ExclusionStore
import com.meetingalarm.settings.MeetingOverrideStore
import com.meetingalarm.settings.SettingsStore

class CalendarContentObserver(
    private val context: Context,
    private val debounceHandler: Handler = Handler(Looper.getMainLooper())
) : ContentObserver(debounceHandler) {

    private val syncRunnable = Runnable { syncAlarms() }

    override fun onChange(selfChange: Boolean) {
        // Debounce: cancel any pending sync and schedule a new one in 2 seconds
        debounceHandler.removeCallbacks(syncRunnable)
        debounceHandler.postDelayed(syncRunnable, DEBOUNCE_MILLIS)
    }

    private fun syncAlarms() {
        val settingsStore = SettingsStore(context)
        val exclusionStore = ExclusionStore(context)
        val overrideStore = MeetingOverrideStore(context)
        val calendarReader = CalendarReader(context, exclusionStore, settingsStore)
        val alarmScheduler = AlarmScheduler(context)

        val meetings = calendarReader.readTodayMeetings()
        alarmScheduler.scheduleAll(meetings, settingsStore, overrideStore)
    }

    companion object {
        private const val DEBOUNCE_MILLIS = 2000L
    }
}
