package com.meetingalarm.alarm

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.meetingalarm.R
import com.meetingalarm.calendar.CalendarReader
import com.meetingalarm.exclusion.ExclusionStore
import com.meetingalarm.settings.MeetingOverrideStore
import com.meetingalarm.settings.SettingsStore

class AlarmActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private var autoDismissRunnable: Runnable? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var isNapAlarm = false
    private var meetingTitle = ""
    private var meetingEventId = 0L
    private var meetingEndTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Acquire a wake lock to force the screen on even during always-on display
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(
            PowerManager.FULL_WAKE_LOCK or
                    PowerManager.ACQUIRE_CAUSES_WAKEUP or
                    PowerManager.ON_AFTER_RELEASE,
            "meetingalarm:alarm_wakelock"
        ).apply { acquire(5 * 60 * 1000L) } // 5 minute timeout as safety net

        setShowWhenLocked(true)
        setTurnScreenOn(true)

        @Suppress("DEPRECATION")
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        // Dismiss the keyguard so the activity is fully visible
        val km = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        km.requestDismissKeyguard(this, null)

        setContentView(R.layout.activity_alarm)

        val title = intent.getStringExtra(EXTRA_MEETING_TITLE) ?: "Meeting"
        val eventId = intent.getLongExtra(EXTRA_EVENT_ID, 0)
        val endTime = intent.getLongExtra(EXTRA_END_TIME, 0)
        val location = intent.getStringExtra(EXTRA_LOCATION)
        val autoDismissSeconds = intent.getIntExtra(EXTRA_AUTO_DISMISS_SECONDS, 60)
        val snoozeDurationSeconds = intent.getIntExtra(EXTRA_SNOOZE_DURATION_SECONDS, 60)
        val snoozeEnabled = intent.getBooleanExtra(EXTRA_SNOOZE_ENABLED, true)
        val alarmSoundUri = intent.getStringExtra(EXTRA_ALARM_SOUND_URI)
        isNapAlarm = intent.getBooleanExtra(EXTRA_IS_NAP_ALARM, false)
        meetingTitle = title
        meetingEventId = eventId
        meetingEndTime = endTime

        findViewById<TextView>(R.id.tvMeetingTitle).text = title

        val tvLocation = findViewById<TextView>(R.id.tvLocation)
        if (!location.isNullOrBlank()) {
            tvLocation.text = location
            tvLocation.visibility = View.VISIBLE
        } else {
            tvLocation.visibility = View.GONE
        }

        // Join button
        val meetingUrl = MeetingUrlParser.extractMeetingUrl(location)
        val btnJoin = findViewById<Button>(R.id.btnJoin)
        if (meetingUrl != null) {
            btnJoin.visibility = View.VISIBLE
            btnJoin.setOnClickListener {
                cancelAutoDismiss()
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(meetingUrl))
                startActivity(browserIntent)
                dismissAlarm()
            }
        }

        // Snooze button
        val btnSnooze = findViewById<Button>(R.id.btnSnooze)
        if (snoozeEnabled && !isNapAlarm) {
            btnSnooze.visibility = View.VISIBLE
            btnSnooze.setOnClickListener {
                cancelAutoDismiss()
                AlarmScheduler(this).scheduleSnooze(
                    title = title,
                    eventId = eventId,
                    location = location,
                    delayMillis = snoozeDurationSeconds * 1000L,
                    autoDismissSeconds = autoDismissSeconds,
                    snoozeDurationSeconds = snoozeDurationSeconds,
                    snoozeEnabled = snoozeEnabled,
                    alarmSoundUri = alarmSoundUri
                )
                // Snooze does NOT enable DND
                AlarmService.stop(this)
                finish()
            }
        }

        // Dismiss button
        findViewById<Button>(R.id.btnDismiss).setOnClickListener {
            cancelAutoDismiss()
            dismissAlarm()
        }

        // Auto-dismiss timer
        if (autoDismissSeconds > 0) {
            autoDismissRunnable = Runnable { dismissAlarm() }
            handler.postDelayed(autoDismissRunnable!!, autoDismissSeconds * 1000L)
        }
    }

    private fun dismissAlarm() {
        cancelAutoDismiss()
        if (isNapAlarm) {
            // Restore DND instead of enabling it
            DndManager(this).restoreDnd()
            // Cancel nap notification
            OngoingNotificationManager.cancelNapNotification(this)
            // Mark nap as inactive and reschedule meeting alarms
            val settingsStore = SettingsStore(this)
            settingsStore.setNapActive(false)
            settingsStore.setNapEndTimeMillis(0)
            val exclusionStore = ExclusionStore(this)
            val overrideStore = MeetingOverrideStore(this)
            val calendarReader = CalendarReader(this, exclusionStore, settingsStore)
            val meetings = calendarReader.readTodayMeetings()
            AlarmScheduler(this).scheduleAll(meetings, settingsStore, overrideStore)
        } else {
            // Enable DND with countdown to meeting end
            if (meetingEndTime > System.currentTimeMillis()) {
                DndManager(this).enableDndUntil(meetingEndTime)
                OngoingNotificationManager.showMeetingNotification(
                    this, meetingTitle, meetingEndTime, meetingEventId
                )
            } else {
                DndManager(this).enableDnd()
            }
        }
        AlarmService.stop(this)
        finish()
    }

    private fun cancelAutoDismiss() {
        autoDismissRunnable?.let { handler.removeCallbacks(it) }
        autoDismissRunnable = null
    }

    override fun onDestroy() {
        cancelAutoDismiss()
        wakeLock?.let { if (it.isHeld) it.release() }
        wakeLock = null
        super.onDestroy()
    }

    @Deprecated("Use onBackPressedDispatcher")
    override fun onBackPressed() {
        // Block back button — user must tap DISMISS
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
    }
}
