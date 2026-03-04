package com.meetingalarm.alarm

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.meetingalarm.MainActivity
import com.meetingalarm.MeetingAlarmApp
import com.meetingalarm.R

object OngoingNotificationManager {

    private const val NAP_NOTIFICATION_ID = 8000
    private const val MEETING_NOTIFICATION_ID_BASE = 8100
    private const val NAP_ACTION_REQUEST_CODE = 400_000
    private const val MEETING_ACTION_REQUEST_CODE_BASE = 410_000

    fun showNapNotification(context: Context, endTimeMillis: Long) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, NAP_NOTIFICATION_ID, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val endNapIntent = Intent(context, NapActionReceiver::class.java).apply {
            action = NapActionReceiver.ACTION_END_NAP
        }
        val endNapPendingIntent = PendingIntent.getBroadcast(
            context, NAP_ACTION_REQUEST_CODE, endNapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, MeetingAlarmApp.STATUS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentTitle("Nap active")
            .setContentText("DND enabled")
            .setOngoing(true)
            .setUsesChronometer(true)
            .setChronometerCountDown(true)
            .setWhen(endTimeMillis)
            .setShowWhen(true)
            .setContentIntent(pendingIntent)
            .addAction(0, "End Nap", endNapPendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSilent(true)
            .build()

        nm.notify(NAP_NOTIFICATION_ID, notification)
    }

    fun cancelNapNotification(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(NAP_NOTIFICATION_ID)
    }

    fun showMeetingNotification(context: Context, title: String, endTimeMillis: Long, eventId: Long) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val notifId = MEETING_NOTIFICATION_ID_BASE + eventId.toInt()
        val pendingIntent = PendingIntent.getActivity(
            context, notifId, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val endDndIntent = Intent(context, NapActionReceiver::class.java).apply {
            action = NapActionReceiver.ACTION_END_MEETING_DND
            putExtra(NapActionReceiver.EXTRA_EVENT_ID, eventId)
        }
        val endDndPendingIntent = PendingIntent.getBroadcast(
            context, MEETING_ACTION_REQUEST_CODE_BASE + eventId.toInt(), endDndIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, MeetingAlarmApp.STATUS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentTitle("In meeting: $title")
            .setContentText("DND enabled")
            .setOngoing(true)
            .setUsesChronometer(true)
            .setChronometerCountDown(true)
            .setWhen(endTimeMillis)
            .setShowWhen(true)
            .setContentIntent(pendingIntent)
            .addAction(0, "End DND", endDndPendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setSilent(true)
            .build()

        nm.notify(notifId, notification)
    }

    fun cancelMeetingNotification(context: Context, eventId: Long) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(MEETING_NOTIFICATION_ID_BASE + eventId.toInt())
    }
}
