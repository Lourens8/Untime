package com.meetingalarm.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MeetingAlarmColorScheme = darkColorScheme(
    primary = TealAccent,
    onPrimary = TextOnTeal,
    primaryContainer = NavyTopBar,
    onPrimaryContainer = TextPrimary,
    secondary = TealDark,
    onSecondary = TextPrimary,
    secondaryContainer = NavyCard,
    onSecondaryContainer = TextPrimary,
    tertiary = GoldDivider,
    onTertiary = TextPrimary,
    tertiaryContainer = NavyCard,
    onTertiaryContainer = TextPrimary,
    background = NavyBackground,
    onBackground = TextPrimary,
    surface = NavyBackground,
    onSurface = TextPrimary,
    surfaceVariant = NavySurface,
    onSurfaceVariant = TextSecondary,
    outline = Color(0xFF4A5568),
    outlineVariant = Color(0xFF2D3748),
)

@Composable
fun MeetingAlarmTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MeetingAlarmColorScheme,
        content = content
    )
}
