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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.meetingalarm.model.Meeting
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingListScreen(
    meetings: List<Meeting>,
    onToggleExclusion: (Meeting) -> Unit,
    onSettingsClick: () -> Unit = {},
    onMeetingClick: (Meeting) -> Unit = {},
    hasOverride: (Meeting) -> Boolean = { false }
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meeting Alarm") },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        if (meetings.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No meetings today",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }
                items(meetings, key = { it.eventId }) { meeting ->
                    MeetingItem(
                        meeting = meeting,
                        onToggle = { onToggleExclusion(meeting) },
                        onClick = { onMeetingClick(meeting) },
                        hasOverride = hasOverride(meeting)
                    )
                }
                item { Spacer(modifier = Modifier.height(8.dp)) }
            }
        }
    }
}

@Composable
private fun MeetingItem(
    meeting: Meeting,
    onToggle: () -> Unit,
    onClick: () -> Unit,
    hasOverride: Boolean
) {
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val timeText = timeFormat.format(Date(meeting.startTimeMillis))
    val isPast = meeting.startTimeMillis <= System.currentTimeMillis()

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = !meeting.isExcluded,
                onCheckedChange = { onToggle() }
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onClick() }
            ) {
                Text(
                    text = meeting.title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isPast) "$timeText (passed)" else timeText,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isPast) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.primary
                    )
                    if (hasOverride) {
                        Text(
                            text = " \u2022 Custom",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
            if (meeting.isExcluded) {
                Text(
                    text = "Skipped",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }
    }
}
