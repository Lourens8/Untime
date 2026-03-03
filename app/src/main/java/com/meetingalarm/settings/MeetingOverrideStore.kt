package com.meetingalarm.settings

import android.content.Context

class MeetingOverrideStore(context: Context) {

    private val prefs = context.getSharedPreferences("meeting_overrides", Context.MODE_PRIVATE)

    fun getMinutesBefore(key: String): Int? =
        if (prefs.contains("${key}_minutes_before")) prefs.getInt("${key}_minutes_before", 0) else null

    fun setMinutesBefore(key: String, value: Int?) {
        if (value != null) prefs.edit().putInt("${key}_minutes_before", value).apply()
        else prefs.edit().remove("${key}_minutes_before").apply()
    }

    fun getAutoDismissSeconds(key: String): Int? =
        if (prefs.contains("${key}_auto_dismiss_seconds")) prefs.getInt("${key}_auto_dismiss_seconds", 60) else null

    fun setAutoDismissSeconds(key: String, value: Int?) {
        if (value != null) prefs.edit().putInt("${key}_auto_dismiss_seconds", value).apply()
        else prefs.edit().remove("${key}_auto_dismiss_seconds").apply()
    }

    fun getSnoozeDurationSeconds(key: String): Int? =
        if (prefs.contains("${key}_snooze_duration_seconds")) prefs.getInt("${key}_snooze_duration_seconds", 60) else null

    fun setSnoozeDurationSeconds(key: String, value: Int?) {
        if (value != null) prefs.edit().putInt("${key}_snooze_duration_seconds", value).apply()
        else prefs.edit().remove("${key}_snooze_duration_seconds").apply()
    }

    fun getSnoozeEnabled(key: String): Boolean? =
        if (prefs.contains("${key}_snooze_enabled")) prefs.getBoolean("${key}_snooze_enabled", true) else null

    fun setSnoozeEnabled(key: String, value: Boolean?) {
        if (value != null) prefs.edit().putBoolean("${key}_snooze_enabled", value).apply()
        else prefs.edit().remove("${key}_snooze_enabled").apply()
    }

    fun getAlarmSoundUri(key: String): String? =
        prefs.getString("${key}_alarm_sound_uri", null)

    fun setAlarmSoundUri(key: String, value: String?) {
        if (value != null) prefs.edit().putString("${key}_alarm_sound_uri", value).apply()
        else prefs.edit().remove("${key}_alarm_sound_uri").apply()
    }

    fun hasAnyOverride(title: String): Boolean {
        val key = title.trim().lowercase()
        return getMinutesBefore(key) != null ||
                getAutoDismissSeconds(key) != null ||
                getSnoozeDurationSeconds(key) != null ||
                getSnoozeEnabled(key) != null ||
                getAlarmSoundUri(key) != null
    }
}
