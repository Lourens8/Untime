package com.meetingalarm

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.AudioAttributes
import android.provider.CalendarContract
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.meetingalarm.alarm.CalendarContentObserver
import com.meetingalarm.alarm.CalendarSyncWorker
import java.util.concurrent.TimeUnit

class MeetingAlarmApp : Application() {

    private var calendarObserver: CalendarContentObserver? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        registerCalendarObserver()
        enqueueCalendarSyncWorker()
    }

    override fun onTerminate() {
        super.onTerminate()
        calendarObserver?.let { contentResolver.unregisterContentObserver(it) }
    }

    private fun registerCalendarObserver() {
        val observer = CalendarContentObserver(this)
        contentResolver.registerContentObserver(
            CalendarContract.Events.CONTENT_URI,
            true,
            observer
        )
        calendarObserver = observer
    }

    private fun enqueueCalendarSyncWorker() {
        val request = PeriodicWorkRequestBuilder<CalendarSyncWorker>(
            15, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            CalendarSyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    private fun createNotificationChannel() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Untime Alarms",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alarms for upcoming meetings"
            setSound(null, audioAttributes) // Sound handled by AlarmActivity
            enableVibration(true)
            setBypassDnd(true)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
        }

        val statusChannel = NotificationChannel(
            STATUS_CHANNEL_ID,
            "Active Timers",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Shows active meeting and nap timers on lock screen and AOD"
            setSound(null, null)
            enableVibration(false)
            setBypassDnd(true)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
        }

        val nm = getSystemService(NotificationManager::class.java)
        // Delete old channels so the new one takes effect
        nm.deleteNotificationChannel("meeting_status_channel")
        nm.deleteNotificationChannel("meeting_status_channel_v2")
        nm.createNotificationChannel(channel)
        nm.createNotificationChannel(statusChannel)
    }

    companion object {
        const val CHANNEL_ID = "meeting_alarm_channel"
        const val STATUS_CHANNEL_ID = "meeting_status_channel_v3"
    }
}
