package com.meetingalarm

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.AudioAttributes

class MeetingAlarmApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Meeting Alarms",
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
