package com.meetingalarm.calendar

import android.content.Context
import android.provider.CalendarContract

data class CalendarInfo(
    val id: String,
    val displayName: String,
    val accountName: String,
    val color: Int
)

object CalendarStore {

    fun getAvailableCalendars(context: Context): List<CalendarInfo> {
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.ACCOUNT_NAME,
            CalendarContract.Calendars.CALENDAR_COLOR
        )

        val calendars = mutableListOf<CalendarInfo>()

        context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            null,
            null,
            "${CalendarContract.Calendars.CALENDAR_DISPLAY_NAME} ASC"
        )?.use { cursor ->
            val idIdx = cursor.getColumnIndex(CalendarContract.Calendars._ID)
            val nameIdx = cursor.getColumnIndex(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)
            val accountIdx = cursor.getColumnIndex(CalendarContract.Calendars.ACCOUNT_NAME)
            val colorIdx = cursor.getColumnIndex(CalendarContract.Calendars.CALENDAR_COLOR)

            while (cursor.moveToNext()) {
                calendars.add(
                    CalendarInfo(
                        id = cursor.getLong(idIdx).toString(),
                        displayName = cursor.getString(nameIdx) ?: "(Unknown)",
                        accountName = cursor.getString(accountIdx) ?: "",
                        color = cursor.getInt(colorIdx)
                    )
                )
            }
        }

        return calendars
    }
}
