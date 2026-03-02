package com.meetingalarm.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.meetingalarm.model.Meeting

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(meeting: Meeting) {
        // Schedule start alarm
        if (meeting.startTimeMillis > System.currentTimeMillis()) {
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra(AlarmReceiver.EXTRA_MEETING_TITLE, meeting.title)
                putExtra(AlarmReceiver.EXTRA_EVENT_ID, meeting.eventId)
                putExtra(AlarmReceiver.EXTRA_END_TIME, meeting.endTimeMillis)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                meeting.eventId.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                meeting.startTimeMillis,
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
    }

    fun scheduleAll(meetings: List<Meeting>) {
        for (meeting in meetings) {
            if (meeting.isExcluded) {
                cancel(meeting)
            } else {
                schedule(meeting)
            }
        }
    }

    /** Offset request code for DND restore to avoid collision with start alarm. */
    private fun dndRequestCode(eventId: Long): Int = eventId.toInt() + DND_REQUEST_CODE_OFFSET

    companion object {
        private const val DND_REQUEST_CODE_OFFSET = 100_000

        fun canScheduleExactAlarms(context: Context): Boolean {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            return am.canScheduleExactAlarms()
        }
    }
}
