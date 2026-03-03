package com.meetingalarm.settings

data class EffectiveSettings(
    val minutesBefore: Int,
    val autoDismissSeconds: Int,
    val snoozeDurationSeconds: Int,
    val snoozeEnabled: Boolean,
    val alarmSoundUri: String?
) {
    companion object {
        fun resolve(
            title: String,
            settingsStore: SettingsStore,
            overrideStore: MeetingOverrideStore
        ): EffectiveSettings {
            val key = title.trim().lowercase()
            return EffectiveSettings(
                minutesBefore = overrideStore.getMinutesBefore(key)
                    ?: settingsStore.getMinutesBefore(),
                autoDismissSeconds = overrideStore.getAutoDismissSeconds(key)
                    ?: settingsStore.getAutoDismissSeconds(),
                snoozeDurationSeconds = overrideStore.getSnoozeDurationSeconds(key)
                    ?: settingsStore.getSnoozeDurationSeconds(),
                snoozeEnabled = overrideStore.getSnoozeEnabled(key)
                    ?: settingsStore.getSnoozeEnabled(),
                alarmSoundUri = overrideStore.getAlarmSoundUri(key)
                    ?: settingsStore.getAlarmSoundUri()
            )
        }
    }
}
