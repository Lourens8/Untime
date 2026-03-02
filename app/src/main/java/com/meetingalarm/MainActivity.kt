package com.meetingalarm

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
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
import com.meetingalarm.alarm.DndManager
import com.meetingalarm.calendar.CalendarReader
import com.meetingalarm.exclusion.ExclusionStore
import com.meetingalarm.model.Meeting
import com.meetingalarm.ui.MeetingListScreen
import com.meetingalarm.ui.theme.MeetingAlarmTheme

class MainActivity : ComponentActivity() {

    private lateinit var exclusionStore: ExclusionStore
    private lateinit var calendarReader: CalendarReader
    private lateinit var alarmScheduler: AlarmScheduler

    private val meetings = mutableStateListOf<Meeting>()
    private val hasCalendarPermission = mutableStateOf(false)

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        exclusionStore = ExclusionStore(this)
        calendarReader = CalendarReader(this, exclusionStore)
        alarmScheduler = AlarmScheduler(this)

        setContent {
            MeetingAlarmTheme {
                if (hasCalendarPermission.value) {
                    MeetingListScreen(
                        meetings = meetings,
                        onToggleExclusion = { meeting -> toggleExclusion(meeting) }
                    )
                } else {
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
    }

    private fun requestCalendarPermission() {
        calendarPermissionLauncher.launch(Manifest.permission.READ_CALENDAR)
    }

    private fun loadAndSync() {
        val loaded = calendarReader.readTodayMeetings()
        meetings.clear()
        meetings.addAll(loaded)
        alarmScheduler.scheduleAll(loaded)
    }

    private fun toggleExclusion(meeting: Meeting) {
        if (meeting.isExcluded) {
            exclusionStore.remove(meeting.title)
        } else {
            exclusionStore.add(meeting.title)
        }
        loadAndSync()
    }
}
