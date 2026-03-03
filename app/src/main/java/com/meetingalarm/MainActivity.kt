package com.meetingalarm

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import android.app.NotificationManager
import com.meetingalarm.alarm.AlarmScheduler
import com.meetingalarm.calendar.CalendarReader
import com.meetingalarm.calendar.CalendarStore
import com.meetingalarm.exclusion.ExclusionStore
import com.meetingalarm.model.Meeting
import com.meetingalarm.settings.MeetingOverrideStore
import com.meetingalarm.settings.SettingsStore
import com.meetingalarm.ui.CalendarFilterScreen
import com.meetingalarm.ui.MeetingListScreen
import com.meetingalarm.ui.MeetingSettingsScreen
import com.meetingalarm.ui.SettingsScreen
import com.meetingalarm.ui.theme.MeetingAlarmTheme

class MainActivity : ComponentActivity() {

    private lateinit var exclusionStore: ExclusionStore
    private lateinit var calendarReader: CalendarReader
    private lateinit var alarmScheduler: AlarmScheduler
    private lateinit var settingsStore: SettingsStore
    private lateinit var overrideStore: MeetingOverrideStore

    private val meetings = mutableStateListOf<Meeting>()
    private val hasCalendarPermission = mutableStateOf(false)
    private val showSettings = mutableStateOf(false)
    private val showCalendarFilter = mutableStateOf(false)
    private val selectedMeetingForSettings = mutableStateOf<Meeting?>(null)

    /** Tracks whether the ringtone picker result is for global settings or a per-meeting override. */
    private var ringtonePickerTarget: String? = null // null = global, else = meeting title key

    private val calendarPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCalendarPermission.value = granted
        if (granted) loadAndSync()
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Continue regardless — notifications are nice-to-have for the full-screen intent
    }

    private val ringtonePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uri = result.data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
        val target = ringtonePickerTarget
        if (target == null) {
            // Global setting
            settingsStore.setAlarmSoundUri(uri?.toString())
        } else {
            // Per-meeting override
            overrideStore.setAlarmSoundUri(target, uri?.toString())
        }
        ringtonePickerTarget = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        exclusionStore = ExclusionStore(this)
        settingsStore = SettingsStore(this)
        overrideStore = MeetingOverrideStore(this)
        calendarReader = CalendarReader(this, exclusionStore, settingsStore)
        alarmScheduler = AlarmScheduler(this)

        setContent {
            MeetingAlarmTheme {
                val meetingForSettings = selectedMeetingForSettings.value
                when {
                    meetingForSettings != null -> {
                        val key = meetingForSettings.title.trim().lowercase()
                        MeetingSettingsScreen(
                            meetingTitle = meetingForSettings.title,
                            settingsStore = settingsStore,
                            overrideStore = overrideStore,
                            onPickAlarmSound = {
                                val currentUri = overrideStore.getAlarmSoundUri(key)
                                launchRingtonePicker(currentUri, key)
                            },
                            currentOverrideSoundName = overrideStore.getAlarmSoundUri(key)?.let {
                                getRingtoneName(it)
                            },
                            onBack = {
                                selectedMeetingForSettings.value = null
                                loadAndSync()
                            }
                        )
                    }
                    showCalendarFilter.value -> {
                        val calendars = CalendarStore.getAvailableCalendars(this@MainActivity)
                        CalendarFilterScreen(
                            calendars = calendars,
                            selectedIds = settingsStore.getSelectedCalendarIds(),
                            onSelectionChanged = { ids ->
                                settingsStore.setSelectedCalendarIds(ids)
                            },
                            onBack = {
                                showCalendarFilter.value = false
                                loadAndSync()
                            }
                        )
                    }
                    showSettings.value -> {
                        SettingsScreen(
                            currentMinutesBefore = settingsStore.getMinutesBefore(),
                            onMinutesBeforeChanged = { minutes ->
                                settingsStore.setMinutesBefore(minutes)
                                loadAndSync()
                            },
                            currentAutoDismissSeconds = settingsStore.getAutoDismissSeconds(),
                            onAutoDismissChanged = { seconds ->
                                settingsStore.setAutoDismissSeconds(seconds)
                                loadAndSync()
                            },
                            currentSnoozeEnabled = settingsStore.getSnoozeEnabled(),
                            onSnoozeEnabledChanged = { enabled ->
                                settingsStore.setSnoozeEnabled(enabled)
                                loadAndSync()
                            },
                            currentSnoozeDuration = settingsStore.getSnoozeDurationSeconds(),
                            onSnoozeDurationChanged = { seconds ->
                                settingsStore.setSnoozeDurationSeconds(seconds)
                                loadAndSync()
                            },
                            currentAlarmSoundName = settingsStore.getAlarmSoundUri()?.let {
                                getRingtoneName(it)
                            } ?: "System default",
                            onPickAlarmSound = {
                                launchRingtonePicker(settingsStore.getAlarmSoundUri(), null)
                            },
                            onCalendarFilterClick = {
                                showSettings.value = false
                                showCalendarFilter.value = true
                            },
                            onBack = { showSettings.value = false }
                        )
                    }
                    hasCalendarPermission.value -> {
                        MeetingListScreen(
                            meetings = meetings,
                            onToggleExclusion = { meeting -> toggleExclusion(meeting) },
                            onSettingsClick = { showSettings.value = true },
                            onMeetingClick = { meeting ->
                                selectedMeetingForSettings.value = meeting
                            },
                            hasOverride = { meeting ->
                                overrideStore.hasAnyOverride(meeting.title)
                            }
                        )
                    }
                    else -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Calendar permission is required to read your meetings.",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { requestCalendarPermission() }) {
                                Text("Grant Permission")
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkPermissions()
    }

    private fun checkPermissions() {
        val calendarGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED

        hasCalendarPermission.value = calendarGranted

        if (calendarGranted) {
            loadAndSync()
        } else {
            requestCalendarPermission()
        }

        // Request notification permission on API 33+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Prompt for exact alarm permission on API 31+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!AlarmScheduler.canScheduleExactAlarms(this)) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }
        }

        // Prompt for DND access if not yet granted
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (!nm.isNotificationPolicyAccessGranted) {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
        }

        // Prompt for overlay permission (display over other apps) so the alarm screen appears reliably
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                data = Uri.parse("package:$packageName")
            }
            startActivity(intent)
        }
    }

    private fun requestCalendarPermission() {
        calendarPermissionLauncher.launch(Manifest.permission.READ_CALENDAR)
    }

    private fun loadAndSync() {
        val loaded = calendarReader.readTodayMeetings()
        meetings.clear()
        meetings.addAll(loaded)
        alarmScheduler.scheduleAll(loaded, settingsStore, overrideStore)
    }

    private fun toggleExclusion(meeting: Meeting) {
        if (meeting.isExcluded) {
            exclusionStore.remove(meeting.title)
        } else {
            exclusionStore.add(meeting.title)
        }
        loadAndSync()
    }

    private fun launchRingtonePicker(currentUri: String?, targetTitle: String?) {
        ringtonePickerTarget = targetTitle
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
            putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Alarm Sound")
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
            if (currentUri != null) {
                putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(currentUri))
            }
        }
        ringtonePickerLauncher.launch(intent)
    }

    private fun getRingtoneName(uriString: String): String {
        return try {
            val ringtone = RingtoneManager.getRingtone(this, Uri.parse(uriString))
            ringtone?.getTitle(this) ?: "Custom sound"
        } catch (_: Exception) {
            "Custom sound"
        }
    }
}
