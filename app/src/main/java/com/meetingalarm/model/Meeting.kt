package com.meetingalarm.model

data class Meeting(
    val eventId: Long,
    val title: String,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val isExcluded: Boolean
)
