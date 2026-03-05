# Now Bar / Live Updates — Lock Screen Meeting Countdown

## Goal
Show the next meeting countdown directly in the Samsung Now Bar slot on the lock screen and AOD, replacing the space normally used by DND.

## Current Status
Not possible for third-party apps on One UI 7. Our persistent notification with `VISIBILITY_PUBLIC` + chronometer is the best available option — it shows on the lock screen only if the user has notification content visibility enabled.

## What to revisit

### Samsung One UI 8 (based on Android 16)
- Samsung confirmed Live Updates will be supported, allowing **any** third-party app to push real-time updates to the Now Bar, lock screen, and status bar.
- No whitelist required (unlike One UI 7).
- Track: https://www.androidauthority.com/one-ui-8-live-updates-support-3573794/

### Android 16 QPR2 — Lock Screen Widgets
- Jetpack Glance widgets can appear on the phone lock screen.
- Could build a countdown widget as an alternative to Now Bar.
- Track: https://android-developers.googleblog.com/2025/03/widgets-on-lock-screen-faq.html

## One UI 7 Now Bar — Technical Reference
For when the API opens up, here's what's needed:

### Manifest
```xml
<meta-data
    android:name="com.samsung.android.support.ongoing_activity"
    android:value="true" />
```

### Notification Bundle Extras
```kotlin
val extras = Bundle().apply {
    putInt("android.ongoingActivityNoti.style", 1)
    putString("android.ongoingActivityNoti.nowbarPrimaryInfo", "Next: Meeting Title")
    putString("android.ongoingActivityNoti.nowbarSecondaryInfo", "in 45 min")
    putInt("android.ongoingActivityNoti.nowbarChronometerPosition", 1)
}
```

### Requirements
- Must be an ongoing notification from a foreground service
- App must be on Samsung's whitelist (One UI 7) or use Live Updates API (One UI 8+)

### Source
https://akexorcist.dev/live-notifications-and-now-bar-in-samsung-one-ui-7-as-developer-en/
