package com.meetingalarm.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.meetingalarm.model.Meeting
import com.meetingalarm.settings.EffectiveSettings
import com.meetingalarm.settings.MeetingOverrideStore
import com.meetingalarm.settings.SettingsStore

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(meeting: Meeting, settings: EffectiveSettings) {
        val triggerTime = meeting.startTimeMillis - settings.minutesBefore * 60_000L
        // Schedule start alarm
        if (triggerTime > System.currentTimeMillis()) {
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra(AlarmReceiver.EXTRA_MEETING_TITLE, meeting.title)
                putExtra(AlarmReceiver.EXTRA_EVENT_ID, meeting.eventId)
                putExtra(AlarmReceiver.EXTRA_END_TIME, meeting.endTimeMillis)
                putExtra(AlarmReceiver.EXTRA_LOCATION, meeting.location)
                putExtra(AlarmReceiver.EXTRA_AUTO_DISMISS_SECONDS, settings.autoDismissSeconds)
                putExtra(AlarmReceiver.EXTRA_SNOOZE_DURATION_SECONDS, settings.snoozeDurationSeconds)
                putExtra(AlarmReceiver.EXTRA_SNOOZE_ENABLED, settings.snoozeEnabled)
                putExtra(AlarmReceiver.EXTRA_ALARM_SOUND_URI, settings.alarmSoundUri)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                meeting.eventId.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }

        // Schedule DND restore at meeting end
        if (meeting.endTimeMillis > System.currentTimeMillis()) {
            scheduleDndRestore(meeting)
        }
    }

    private fun scheduleDndRestore(meeting: Meeting) {
        val intent = Intent(context, DndRestoreReceiver::class.java).apply {
            putExtra(DndRestoreReceiver.EXTRA_EVENT_ID, meeting.eventId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            dndRequestCode(meeting.eventId),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            meeting.endTimeMillis,
            pendingIntent
        )
    }

    fun cancel(meeting: Meeting) {
        // Cancel start alarm
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            meeting.eventId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)

        // Cancel DND restore alarm
        val dndIntent = Intent(context, DndRestoreReceiver::class.java)
        val dndPendingIntent = PendingIntent.getBroadcast(
            context,
            dndRequestCode(meeting.eventId),
            dndIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(dndPendingIntent)

        // If the alarm already fired (meeting is in progress), DND was enabled for it.
        // Restore DND now since the DND restore alarm was just cancelled.
        val now = System.currentTimeMillis()
        if (now >= meeting.startTimeMillis && now < meeting.endTimeMillis) {
            DndManager(context).restoreDnd(meeting.eventId)
        }
    }

    fun scheduleAll(
        meetings: List<Meeting>,
        settingsStore: SettingsStore,
        overrideStore: MeetingOverrideStore
    ) {
        for (meeting in meetings) {
            if (meeting.isExcluded) {
                cancel(meeting)
            } else {
                val settings = EffectiveSettings.resolve(meeting.title, settingsStore, overrideStore)
                schedule(meeting, settings)
            }
        }

        // Clean up DND/notifications for meetings deleted from the calendar
        val currentEventIds = meetings.map { it.eventId }.toSet()
        val dndManager = DndManager(context)
        for (activeId in dndManager.getActiveEventIds()) {
            if (activeId !in currentEventIds) {
                cleanupDeletedMeeting(activeId)
            }
        }
    }

    /** Cancel DND restore alarm, restore DND state, and dismiss notification for a deleted meeting. */
    fun cleanupDeletedMeeting(eventId: Long) {
        // Cancel the scheduled DND restore alarm
        val dndIntent = Intent(context, DndRestoreReceiver::class.java)
        val dndPendingIntent = PendingIntent.getBroadcast(
            context,
            dndRequestCode(eventId),
            dndIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(dndPendingIntent)

        // Restore DND and remove from active set
        DndManager(context).restoreDnd(eventId)

        // Dismiss the ongoing notification
        OngoingNotificationManager.cancelMeetingNotification(context, eventId)
    }

    fun scheduleSnooze(
        title: String,
        eventId: Long,
        location: String?,
        delayMillis: Long,
        autoDismissSeconds: Int,
        snoozeDurationSeconds: Int,
        snoozeEnabled: Boolean,
        alarmSoundUri: String?
    ) {
        val triggerTime = System.currentTimeMillis() + delayMillis

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_MEETING_TITLE, title)
            putExtra(AlarmReceiver.EXTRA_EVENT_ID, eventId)
            putExtra(AlarmReceiver.EXTRA_LOCATION, location)
            putExtra(AlarmReceiver.EXTRA_AUTO_DISMISS_SECONDS, autoDismissSeconds)
            putExtra(AlarmReceiver.EXTRA_SNOOZE_DURATION_SECONDS, snoozeDurationSeconds)
            putExtra(AlarmReceiver.EXTRA_SNOOZE_ENABLED, snoozeEnabled)
            putExtra(AlarmReceiver.EXTRA_ALARM_SOUND_URI, alarmSoundUri)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            eventId.toInt() + SNOOZE_REQUEST_CODE_OFFSET,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
    }

    fun scheduleNapAlarm(durationMinutes: Int, alarmSoundUri: String?) {
        val triggerTime = System.currentTimeMillis() + durationMinutes * 60_000L

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_MEETING_TITLE, "Nap Over!")
            putExtra(AlarmReceiver.EXTRA_EVENT_ID, NAP_REQUEST_CODE.toLong())
            putExtra(AlarmReceiver.EXTRA_AUTO_DISMISS_SECONDS, 60)
            putExtra(AlarmReceiver.EXTRA_SNOOZE_ENABLED, false)
            putExtra(AlarmReceiver.EXTRA_ALARM_SOUND_URI, alarmSoundUri)
            putExtra(EXTRA_IS_NAP_ALARM, true)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            NAP_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
    }

    fun cancelNapAlarm() {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            NAP_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    /** Offset request code for DND restore to avoid collision with start alarm. */
    private fun dndRequestCode(eventId: Long): Int = eventId.toInt() + DND_REQUEST_CODE_OFFSET

    companion object {
        private const val DND_REQUEST_CODE_OFFSET = 100_000
        private const val SNOOZE_REQUEST_CODE_OFFSET = 200_000
        private const val NAP_REQUEST_CODE = 300_000
        const val EXTRA_IS_NAP_ALARM = "is_nap_alarm"

        fun canScheduleExactAlarms(context: Context): Boolean {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            return am.canScheduleExactAlarms()
        }
    }
}
