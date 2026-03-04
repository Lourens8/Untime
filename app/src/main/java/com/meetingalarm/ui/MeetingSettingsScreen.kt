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
import androidx.compose.material3.Divider
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
import com.meetingalarm.settings.MeetingOverrideStore
import com.meetingalarm.settings.SettingsStore

private val MINUTES_BEFORE_OPTIONS = listOf(0, 1, 2, 5, 10, 15)
private val AUTO_DISMISS_OPTIONS = listOf(30, 60, 120, 300, 0)
private val SNOOZE_DURATION_OPTIONS = listOf(30, 60, 120, 300)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingSettingsScreen(
    meetingTitle: String,
    settingsStore: SettingsStore,
    overrideStore: MeetingOverrideStore,
    onPickAlarmSound: () -> Unit,
    currentOverrideSoundName: String?,
    onBack: () -> Unit
) {
    val key = meetingTitle.trim().lowercase()

    var overrideMinutes by remember { mutableStateOf(overrideStore.getMinutesBefore(key) != null) }
    var minutesValue by remember { mutableIntStateOf(overrideStore.getMinutesBefore(key) ?: settingsStore.getMinutesBefore()) }

    var overrideAutoDismiss by remember { mutableStateOf(overrideStore.getAutoDismissSeconds(key) != null) }
    var autoDismissValue by remember { mutableIntStateOf(overrideStore.getAutoDismissSeconds(key) ?: settingsStore.getAutoDismissSeconds()) }

    var overrideSnoozeEnabled by remember { mutableStateOf(overrideStore.getSnoozeEnabled(key) != null) }
    var snoozeEnabledValue by remember { mutableStateOf(overrideStore.getSnoozeEnabled(key) ?: settingsStore.getSnoozeEnabled()) }

    var overrideSnoozeDuration by remember { mutableStateOf(overrideStore.getSnoozeDurationSeconds(key) != null) }
    var snoozeDurationValue by remember { mutableIntStateOf(overrideStore.getSnoozeDurationSeconds(key) ?: settingsStore.getSnoozeDurationSeconds()) }

    var overrideSound by remember { mutableStateOf(overrideStore.getAlarmSoundUri(key) != null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(meetingTitle, maxLines = 1) },
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
            Text(
                text = "Per-meeting overrides",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            // --- Minutes before ---
            OverrideSection(
                title = "Alarm before meeting",
                isOverridden = overrideMinutes,
                defaultDisplay = formatMinutesBefore(settingsStore.getMinutesBefore()),
                onToggleOverride = { enabled ->
                    overrideMinutes = enabled
                    if (enabled) overrideStore.setMinutesBefore(key, minutesValue)
                    else overrideStore.setMinutesBefore(key, null)
                }
            ) {
                MINUTES_BEFORE_OPTIONS.forEach { minutes ->
                    OverrideRadioRow(
                        label = formatMinutesBefore(minutes),
                        selected = minutesValue == minutes,
                        onClick = {
                            minutesValue = minutes
                            overrideStore.setMinutesBefore(key, minutes)
                        }
                    )
                }
            }

            SectionDivider()

            // --- Auto-dismiss ---
            OverrideSection(
                title = "Auto-dismiss",
                isOverridden = overrideAutoDismiss,
                defaultDisplay = formatAutoDismiss(settingsStore.getAutoDismissSeconds()),
                onToggleOverride = { enabled ->
                    overrideAutoDismiss = enabled
                    if (enabled) overrideStore.setAutoDismissSeconds(key, autoDismissValue)
                    else overrideStore.setAutoDismissSeconds(key, null)
                }
            ) {
                AUTO_DISMISS_OPTIONS.forEach { seconds ->
                    OverrideRadioRow(
                        label = formatAutoDismiss(seconds),
                        selected = autoDismissValue == seconds,
                        onClick = {
                            autoDismissValue = seconds
                            overrideStore.setAutoDismissSeconds(key, seconds)
                        }
                    )
                }
            }

            SectionDivider()

            // --- Snooze enabled ---
            OverrideSection(
                title = "Snooze enabled",
                isOverridden = overrideSnoozeEnabled,
                defaultDisplay = if (settingsStore.getSnoozeEnabled()) "On" else "Off",
                onToggleOverride = { enabled ->
                    overrideSnoozeEnabled = enabled
                    if (enabled) overrideStore.setSnoozeEnabled(key, snoozeEnabledValue)
                    else overrideStore.setSnoozeEnabled(key, null)
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Enable snooze", style = MaterialTheme.typography.bodyLarge)
                    Switch(
                        checked = snoozeEnabledValue,
                        onCheckedChange = {
                            snoozeEnabledValue = it
                            overrideStore.setSnoozeEnabled(key, it)
                        }
                    )
                }
            }

            SectionDivider()

            // --- Snooze duration ---
            OverrideSection(
                title = "Snooze duration",
                isOverridden = overrideSnoozeDuration,
                defaultDisplay = formatDuration(settingsStore.getSnoozeDurationSeconds()),
                onToggleOverride = { enabled ->
                    overrideSnoozeDuration = enabled
                    if (enabled) overrideStore.setSnoozeDurationSeconds(key, snoozeDurationValue)
                    else overrideStore.setSnoozeDurationSeconds(key, null)
                }
            ) {
                SNOOZE_DURATION_OPTIONS.forEach { seconds ->
                    OverrideRadioRow(
                        label = formatDuration(seconds),
                        selected = snoozeDurationValue == seconds,
                        onClick = {
                            snoozeDurationValue = seconds
                            overrideStore.setSnoozeDurationSeconds(key, seconds)
                        }
                    )
                }
            }

            SectionDivider()

            // --- Alarm sound ---
            OverrideSection(
                title = "Alarm sound",
                isOverridden = overrideSound,
                defaultDisplay = "System default",
                onToggleOverride = { enabled ->
                    overrideSound = enabled
                    if (!enabled) overrideStore.setAlarmSoundUri(key, null)
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPickAlarmSound() }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = currentOverrideSoundName ?: "System default",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Change",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun OverrideSection(
    title: String,
    isOverridden: Boolean,
    defaultDisplay: String,
    onToggleOverride: (Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Override",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Switch(
                checked = isOverridden,
                onCheckedChange = onToggleOverride,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
    if (isOverridden) {
        Spacer(modifier = Modifier.height(8.dp))
        content()
    } else {
        Text(
            text = "(default: $defaultDisplay)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 4.dp)
        )
    }
}

@Composable
private fun SectionDivider() {
    Spacer(modifier = Modifier.height(12.dp))
    Divider()
    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
private fun OverrideRadioRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 6.dp),
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

private fun formatMinutesBefore(minutes: Int): String =
    if (minutes == 0) "Exact (on time)" else "$minutes min before"

private fun formatAutoDismiss(seconds: Int): String = when (seconds) {
    0 -> "Never"
    30 -> "30 seconds"
    60 -> "1 minute"
    120 -> "2 minutes"
    300 -> "5 minutes"
    else -> "$seconds seconds"
}

private fun formatDuration(seconds: Int): String = when (seconds) {
    30 -> "30 seconds"
    60 -> "1 minute"
    120 -> "2 minutes"
    300 -> "5 minutes"
    else -> "$seconds seconds"
}
