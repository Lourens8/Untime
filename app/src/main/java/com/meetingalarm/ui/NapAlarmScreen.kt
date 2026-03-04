package com.meetingalarm.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.meetingalarm.model.Meeting

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NapAlarmScreen(
    meetings: List<Meeting>,
    initialDurationMinutes: Int,
    onStartNap: (durationMinutes: Int, silenceMeetings: Boolean) -> Unit,
    onBack: () -> Unit
) {
    var durationMinutes by remember { mutableIntStateOf(initialDurationMinutes) }
    var showConflictDialog by remember { mutableStateOf(false) }
    var conflictingMeetings by remember { mutableStateOf<List<Meeting>>(emptyList()) }

    val onGoClicked = {
        val now = System.currentTimeMillis()
        val napEnd = now + durationMinutes * 60_000L
        val overlapping = meetings.filter { m ->
            !m.isExcluded && m.startTimeMillis < napEnd && m.endTimeMillis > now
        }
        if (overlapping.isNotEmpty()) {
            conflictingMeetings = overlapping
            showConflictDialog = true
        } else {
            onStartNap(durationMinutes, false)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nap Alarm") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Nap Duration",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Duration display
            val hours = durationMinutes / 60
            val mins = durationMinutes % 60
            val displayText = if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
            Text(
                text = displayText,
                style = MaterialTheme.typography.displayMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Stepper buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { if (durationMinutes > 5) durationMinutes -= 5 },
                    modifier = Modifier.size(64.dp)
                ) {
                    Text("-5", style = MaterialTheme.typography.titleMedium)
                }
                OutlinedButton(
                    onClick = { if (durationMinutes > 1) durationMinutes -= 1 },
                    modifier = Modifier.size(64.dp)
                ) {
                    Text("-1", style = MaterialTheme.typography.titleMedium)
                }
                OutlinedButton(
                    onClick = { if (durationMinutes < 480) durationMinutes += 1 },
                    modifier = Modifier.size(64.dp)
                ) {
                    Text("+1", style = MaterialTheme.typography.titleMedium)
                }
                OutlinedButton(
                    onClick = { if (durationMinutes < 480) durationMinutes += 5 },
                    modifier = Modifier.size(64.dp)
                ) {
                    Text("+5", style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = { onGoClicked() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Start Nap", style = MaterialTheme.typography.titleMedium)
            }
        }
    }

    if (showConflictDialog) {
        AlertDialog(
            onDismissRequest = { showConflictDialog = false },
            title = { Text("Meetings During Nap") },
            text = {
                Column {
                    Text("These meetings overlap with your nap:")
                    Spacer(modifier = Modifier.height(8.dp))
                    conflictingMeetings.forEach { m ->
                        Text(
                            text = "\u2022 ${m.title}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    showConflictDialog = false
                    onStartNap(durationMinutes, true)
                }) {
                    Text("Silence & Start Nap")
                }
            },
            dismissButton = {
                Column {
                    FilledTonalButton(onClick = {
                        showConflictDialog = false
                        onStartNap(durationMinutes, false)
                    }) {
                        Text("Start Nap Anyway")
                    }
                    TextButton(onClick = { showConflictDialog = false }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }
}
