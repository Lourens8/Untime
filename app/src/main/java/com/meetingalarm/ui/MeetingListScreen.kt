package com.meetingalarm.ui

import androidx.compose.foundation.background
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meetingalarm.model.Meeting
import com.meetingalarm.ui.theme.GoldDivider
import com.meetingalarm.ui.theme.TealAccent
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingListScreen(
    meetings: List<Meeting>,
    onToggleExclusion: (Meeting) -> Unit,
    onSettingsClick: () -> Unit = {},
    onNapClick: () -> Unit = {},
    onMeetingClick: (Meeting) -> Unit = {},
    hasOverride: (Meeting) -> Boolean = { false },
    napEndTimeMillis: Long = 0L,
    onEndNap: () -> Unit = {},
    onExtendNap: (Int) -> Unit = {}
) {
    var showNapControlDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Untime") },
                actions = {
                    IconButton(onClick = {
                        selectedTab = 2
                        onNapClick()
                    }) {
                        Text("z\u1DBB", style = MaterialTheme.typography.titleLarge)
                    }
                    IconButton(onClick = {
                        selectedTab = 3
                        onSettingsClick()
                    }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Text("\u2302", style = MaterialTheme.typography.titleLarge) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TealAccent,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Text("\u2611", style = MaterialTheme.typography.titleLarge) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TealAccent,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = {
                        selectedTab = 2
                        onNapClick()
                    },
                    icon = { Text("z\u1DBB", style = MaterialTheme.typography.titleMedium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TealAccent,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = {
                        selectedTab = 3
                        onSettingsClick()
                    },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TealAccent,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Nap countdown banner
            if (napEndTimeMillis > 0L) {
                NapCountdownBanner(
                    napEndTimeMillis = napEndTimeMillis,
                    onClick = { showNapControlDialog = true }
                )
            }

            if (meetings.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
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
                        .padding(horizontal = 16.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                    items(meetings, key = { it.eventId }) { meeting ->
                        MeetingItem(
                            meeting = meeting,
                            onToggle = { onToggleExclusion(meeting) },
                            onClick = { onMeetingClick(meeting) },
                            hasOverride = hasOverride(meeting)
                        )
                        HorizontalDivider(
                            color = GoldDivider,
                            thickness = 1.dp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
            }
        }
    }

    if (showNapControlDialog) {
        AlertDialog(
            onDismissRequest = { showNapControlDialog = false },
            title = { Text("Nap Timer") },
            text = { Text("End your nap early or extend the timer.") },
            confirmButton = {
                Button(onClick = {
                    showNapControlDialog = false
                    onEndNap()
                }) {
                    Text("End Nap")
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { showNapControlDialog = false }) {
                        Text("Cancel")
                    }
                    FilledTonalButton(onClick = {
                        showNapControlDialog = false
                        onExtendNap(15)
                    }) {
                        Text("+15m")
                    }
                    FilledTonalButton(onClick = {
                        showNapControlDialog = false
                        onExtendNap(30)
                    }) {
                        Text("+30m")
                    }
                }
            }
        )
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = !meeting.isExcluded,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(
                checkedColor = TealAccent,
                uncheckedColor = MaterialTheme.colorScheme.outline,
                checkmarkColor = MaterialTheme.colorScheme.surface
            )
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable { onClick() }
        ) {
            Text(
                text = meeting.title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isPast) "$timeText (passed)" else timeText,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isPast) MaterialTheme.colorScheme.onSurfaceVariant
                    else TealAccent
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

@Composable
private fun NapCountdownBanner(napEndTimeMillis: Long, onClick: () -> Unit) {
    var remainingMillis by remember { mutableLongStateOf(napEndTimeMillis - System.currentTimeMillis()) }

    LaunchedEffect(napEndTimeMillis) {
        while (true) {
            remainingMillis = napEndTimeMillis - System.currentTimeMillis()
            if (remainingMillis <= 0) break
            delay(1000)
        }
    }

    if (remainingMillis > 0) {
        val totalSeconds = (remainingMillis / 1000).toInt()
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        val timeText = if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%d:%02d", minutes, seconds)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.tertiaryContainer)
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Nap active  \u00B7  $timeText remaining",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                textAlign = TextAlign.Center
            )
        }
    }
}
