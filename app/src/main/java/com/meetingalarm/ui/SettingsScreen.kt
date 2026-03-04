package com.meetingalarm.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val MINUTES_BEFORE_OPTIONS = listOf(0, 1, 2, 5, 10, 15)
private val AUTO_DISMISS_OPTIONS = listOf(30, 60, 120, 300, 0) // 0 = Never
private val SNOOZE_DURATION_OPTIONS = listOf(30, 60, 120, 300)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentMinutesBefore: Int,
    onMinutesBeforeChanged: (Int) -> Unit,
    currentAutoDismissSeconds: Int,
    onAutoDismissChanged: (Int) -> Unit,
    currentSnoozeEnabled: Boolean,
    onSnoozeEnabledChanged: (Boolean) -> Unit,
    currentSnoozeDuration: Int,
    onSnoozeDurationChanged: (Int) -> Unit,
    currentAlarmSoundName: String,
    onPickAlarmSound: () -> Unit,
    onCalendarFilterClick: () -> Unit,
    onBack: () -> Unit
) {
    var selectedMinutes by remember { mutableIntStateOf(currentMinutesBefore) }
    var selectedAutoDismiss by remember { mutableIntStateOf(currentAutoDismissSeconds) }
    var snoozeEnabled by remember { mutableStateOf(currentSnoozeEnabled) }
    var selectedSnoozeDuration by remember { mutableIntStateOf(currentSnoozeDuration) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top
        ) {
            // --- Alarm before meeting ---
            SectionHeader(
                title = "Alarm before meeting",
                subtitle = "How many minutes before a meeting should the alarm fire?"
            )
            MINUTES_BEFORE_OPTIONS.forEach { minutes ->
                val label = if (minutes == 0) "Exact (on time)" else "$minutes min before"
                RadioRow(
                    label = label,
                    selected = selectedMinutes == minutes,
                    onClick = {
                        selectedMinutes = minutes
                        onMinutesBeforeChanged(minutes)
                    }
                )
            }

            SectionDivider()

            // --- Auto-dismiss ---
            SectionHeader(
                title = "Auto-dismiss",
                subtitle = "Automatically dismiss the alarm after a set time."
            )
            AUTO_DISMISS_OPTIONS.forEach { seconds ->
                val label = when (seconds) {
                    0 -> "Never"
                    30 -> "30 seconds"
                    60 -> "1 minute"
                    120 -> "2 minutes"
                    300 -> "5 minutes"
                    else -> "$seconds seconds"
                }
                RadioRow(
                    label = label,
                    selected = selectedAutoDismiss == seconds,
                    onClick = {
                        selectedAutoDismiss = seconds
                        onAutoDismissChanged(seconds)
                    }
                )
            }

            SectionDivider()

            // --- Snooze ---
            SectionHeader(
                title = "Snooze",
                subtitle = "Allow snoozing the alarm to re-fire later."
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Enable snooze", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = snoozeEnabled,
                    onCheckedChange = {
                        snoozeEnabled = it
                        onSnoozeEnabledChanged(it)
                    }
                )
            }
            if (snoozeEnabled) {
                Text(
                    text = "Snooze duration",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
                SNOOZE_DURATION_OPTIONS.forEach { seconds ->
                    val label = when (seconds) {
                        30 -> "30 seconds"
                        60 -> "1 minute"
                        120 -> "2 minutes"
                        300 -> "5 minutes"
                        else -> "$seconds seconds"
                    }
                    RadioRow(
                        label = label,
                        selected = selectedSnoozeDuration == seconds,
                        onClick = {
                            selectedSnoozeDuration = seconds
                            onSnoozeDurationChanged(seconds)
                        }
                    )
                }
            }

            SectionDivider()

            // --- Alarm sound ---
            SectionHeader(
                title = "Alarm sound",
                subtitle = "Choose the sound played when the alarm fires."
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPickAlarmSound() }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(currentAlarmSoundName, style = MaterialTheme.typography.bodyLarge)
                }
                Text(
                    text = "Change",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            SectionDivider()

            // --- Calendar filter ---
            SectionHeader(
                title = "Calendar filter",
                subtitle = "Choose which calendars to show meetings from."
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCalendarFilterClick() }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Select calendars", style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "Configure",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Text(text = title, style = MaterialTheme.typography.titleMedium)
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = subtitle,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
private fun SectionDivider() {
    Spacer(modifier = Modifier.height(16.dp))
    HorizontalDivider()
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
private fun RadioRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
