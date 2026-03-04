# Untime

Android app that automatically enables Do Not Disturb mode for your calendar meetings. Get an alarm before each meeting starts, with options to join, snooze, or dismiss — then DND activates until the meeting ends.

## Features

- Calendar-synced alarms with configurable lead time
- Auto-enable/restore DND around meetings
- Join meeting links directly from the alarm
- Snooze support
- Per-meeting setting overrides
- Nap alarm mode
- Calendar filter and exclusion rules

## Tech

- Kotlin, Jetpack Compose
- Min SDK 26, Target SDK 34
- WorkManager for background calendar sync
- ContentObserver for real-time calendar changes
