package com.meetingalarm.calendar

import android.content.ContentUris
import android.content.Context
import android.provider.CalendarContract
import com.meetingalarm.exclusion.ExclusionStore
import com.meetingalarm.model.Meeting
import com.meetingalarm.settings.SettingsStore
import java.util.Calendar
import java.util.TimeZone

class CalendarReader(
    private val context: Context,
    private val exclusionStore: ExclusionStore,
    private val settingsStore: SettingsStore
) {

    fun readTodayMeetings(): List<Meeting> {
        val tz = TimeZone.getDefault()

        val startOfDay = Calendar.getInstance(tz).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val endOfDay = Calendar.getInstance(tz).apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        val selectedCalendarIds = settingsStore.getSelectedCalendarIds()

        val projection = arrayOf(
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.EVENT_LOCATION,
            CalendarContract.Instances.CALENDAR_ID
        )

        val uri = CalendarContract.Instances.CONTENT_URI.buildUpon().let {
            ContentUris.appendId(it, startOfDay)
            ContentUris.appendId(it, endOfDay)
            it.build()
        }

        val selection = if (selectedCalendarIds.isNotEmpty()) {
            val placeholders = selectedCalendarIds.joinToString(",") { "?" }
            "${CalendarContract.Instances.CALENDAR_ID} IN ($placeholders)"
        } else {
            null
        }

        val selectionArgs = if (selectedCalendarIds.isNotEmpty()) {
            selectedCalendarIds.toTypedArray()
        } else {
            null
        }

        val meetings = mutableListOf<Meeting>()

        context.contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            "${CalendarContract.Instances.BEGIN} ASC"
        )?.use { cursor ->
            val idIdx = cursor.getColumnIndex(CalendarContract.Instances.EVENT_ID)
            val titleIdx = cursor.getColumnIndex(CalendarContract.Instances.TITLE)
            val beginIdx = cursor.getColumnIndex(CalendarContract.Instances.BEGIN)
            val endIdx = cursor.getColumnIndex(CalendarContract.Instances.END)
            val locationIdx = cursor.getColumnIndex(CalendarContract.Instances.EVENT_LOCATION)

            while (cursor.moveToNext()) {
                val eventId = cursor.getLong(idIdx)
                val title = cursor.getString(titleIdx) ?: "(No title)"
                val startTime = cursor.getLong(beginIdx)
                val endTime = cursor.getLong(endIdx)
                val location = cursor.getString(locationIdx)?.takeIf { it.isNotBlank() }

                meetings.add(
                    Meeting(
                        eventId = eventId,
                        title = title,
                        startTimeMillis = startTime,
                        endTimeMillis = endTime,
                        isExcluded = exclusionStore.isExcluded(title),
                        location = location
                    )
                )
            }
        }

        return meetings
    }
}
