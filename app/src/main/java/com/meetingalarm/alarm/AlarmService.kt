package com.meetingalarm.alarm

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.meetingalarm.MeetingAlarmApp
import com.meetingalarm.R

class AlarmService : Service() {

    private var mediaPlayer: MediaPlayer? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val title = intent?.getStringExtra(EXTRA_MEETING_TITLE) ?: "Meeting"
        val eventId = intent?.getLongExtra(EXTRA_EVENT_ID, 0) ?: 0
        val endTime = intent?.getLongExtra(EXTRA_END_TIME, 0) ?: 0
        val location = intent?.getStringExtra(EXTRA_LOCATION)
        val autoDismissSeconds = intent?.getIntExtra(EXTRA_AUTO_DISMISS_SECONDS, 60) ?: 60
        val snoozeDurationSeconds = intent?.getIntExtra(EXTRA_SNOOZE_DURATION_SECONDS, 60) ?: 60
        val snoozeEnabled = intent?.getBooleanExtra(EXTRA_SNOOZE_ENABLED, true) ?: true
        val alarmSoundUri = intent?.getStringExtra(EXTRA_ALARM_SOUND_URI)
        val isNapAlarm = intent?.getBooleanExtra(EXTRA_IS_NAP_ALARM, false) ?: false

        // Build full-screen intent for the foreground notification
        val activityIntent = Intent(this, AlarmActivity::class.java).apply {
            putExtra(AlarmActivity.EXTRA_MEETING_TITLE, title)
            putExtra(AlarmActivity.EXTRA_EVENT_ID, eventId)
            putExtra(AlarmActivity.EXTRA_END_TIME, endTime)
            putExtra(AlarmActivity.EXTRA_LOCATION, location)
            putExtra(AlarmActivity.EXTRA_AUTO_DISMISS_SECONDS, autoDismissSeconds)
            putExtra(AlarmActivity.EXTRA_SNOOZE_DURATION_SECONDS, snoozeDurationSeconds)
            putExtra(AlarmActivity.EXTRA_SNOOZE_ENABLED, snoozeEnabled)
            putExtra(AlarmActivity.EXTRA_ALARM_SOUND_URI, alarmSoundUri)
            putExtra(AlarmActivity.EXTRA_IS_NAP_ALARM, isNapAlarm)
            this.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            eventId.toInt(),
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationTitle = if (isNapAlarm) "Nap Over!" else "Meeting Starting"
        val notification = NotificationCompat.Builder(this, MeetingAlarmApp.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentTitle(notificationTitle)
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)

        // Start alarm sound immediately
        startAlarmSound(alarmSoundUri)

        // Directly launch AlarmActivity to take over the screen
        startActivity(activityIntent)

        return START_NOT_STICKY
    }

    private fun startAlarmSound(customSoundUri: String?) {
        val alarmUri = if (customSoundUri != null) {
            Uri.parse(customSoundUri)
        } else {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        }

        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            setDataSource(this@AlarmService, alarmUri)
            isLooping = true
            prepare()
            start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.let {
            if (it.isPlaying) it.stop()
            it.release()
        }
        mediaPlayer = null
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
        const val EXTRA_IS_NAP_ALARM = "is_nap_alarm"
        private const val NOTIFICATION_ID = 9999

        fun stop(context: Context) {
            context.stopService(Intent(context, AlarmService::class.java))
        }
    }
}
