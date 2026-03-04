package com.meetingalarm.settings

import android.content.Context

class SettingsStore(context: Context) {

    private val prefs = context.getSharedPreferences("meeting_alarm_settings", Context.MODE_PRIVATE)

    fun getMinutesBefore(): Int = prefs.getInt(KEY_MINUTES_BEFORE, DEFAULT_MINUTES_BEFORE)

    fun setMinutesBefore(minutes: Int) {
        prefs.edit().putInt(KEY_MINUTES_BEFORE, minutes).apply()
    }

    fun getAutoDismissSeconds(): Int = prefs.getInt(KEY_AUTO_DISMISS_SECONDS, DEFAULT_AUTO_DISMISS_SECONDS)

    fun setAutoDismissSeconds(seconds: Int) {
        prefs.edit().putInt(KEY_AUTO_DISMISS_SECONDS, seconds).apply()
    }

    fun getSnoozeDurationSeconds(): Int = prefs.getInt(KEY_SNOOZE_DURATION_SECONDS, DEFAULT_SNOOZE_DURATION_SECONDS)

    fun setSnoozeDurationSeconds(seconds: Int) {
        prefs.edit().putInt(KEY_SNOOZE_DURATION_SECONDS, seconds).apply()
    }

    fun getSnoozeEnabled(): Boolean = prefs.getBoolean(KEY_SNOOZE_ENABLED, DEFAULT_SNOOZE_ENABLED)

    fun setSnoozeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SNOOZE_ENABLED, enabled).apply()
    }

    fun getAlarmSoundUri(): String? = prefs.getString(KEY_ALARM_SOUND_URI, null)

    fun setAlarmSoundUri(uri: String?) {
        if (uri != null) prefs.edit().putString(KEY_ALARM_SOUND_URI, uri).apply()
        else prefs.edit().remove(KEY_ALARM_SOUND_URI).apply()
    }

    fun getSelectedCalendarIds(): Set<String> =
        prefs.getStringSet(KEY_SELECTED_CALENDAR_IDS, emptySet()) ?: emptySet()

    fun setSelectedCalendarIds(ids: Set<String>) {
        prefs.edit().putStringSet(KEY_SELECTED_CALENDAR_IDS, ids).apply()
    }

    fun getNapDurationMinutes(): Int = prefs.getInt(KEY_NAP_DURATION_MINUTES, DEFAULT_NAP_DURATION_MINUTES)

    fun setNapDurationMinutes(minutes: Int) {
        prefs.edit().putInt(KEY_NAP_DURATION_MINUTES, minutes).apply()
    }

    fun isNapActive(): Boolean = prefs.getBoolean(KEY_NAP_ACTIVE, false)

    fun setNapActive(active: Boolean) {
        prefs.edit().putBoolean(KEY_NAP_ACTIVE, active).apply()
    }

    fun getNapEndTimeMillis(): Long = prefs.getLong(KEY_NAP_END_TIME, 0L)

    fun setNapEndTimeMillis(millis: Long) {
        prefs.edit().putLong(KEY_NAP_END_TIME, millis).apply()
    }

    companion object {
        private const val KEY_MINUTES_BEFORE = "minutes_before"
        private const val DEFAULT_MINUTES_BEFORE = 0

        private const val KEY_AUTO_DISMISS_SECONDS = "auto_dismiss_seconds"
        private const val DEFAULT_AUTO_DISMISS_SECONDS = 60

        private const val KEY_SNOOZE_DURATION_SECONDS = "snooze_duration_seconds"
        private const val DEFAULT_SNOOZE_DURATION_SECONDS = 60

        private const val KEY_SNOOZE_ENABLED = "snooze_enabled"
        private const val DEFAULT_SNOOZE_ENABLED = true

        private const val KEY_ALARM_SOUND_URI = "alarm_sound_uri"

        private const val KEY_SELECTED_CALENDAR_IDS = "selected_calendar_ids"

        private const val KEY_NAP_DURATION_MINUTES = "nap_duration_minutes"
        private const val DEFAULT_NAP_DURATION_MINUTES = 30

        private const val KEY_NAP_ACTIVE = "nap_active"
        private const val KEY_NAP_END_TIME = "nap_end_time"
    }
}
