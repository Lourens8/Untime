package com.meetingalarm.alarm

object MeetingUrlParser {

    private val MEETING_HOSTS = listOf(
        "zoom.us",
        "meet.google.com",
        "teams.microsoft.com",
        "teams.live.com",
        "webex.com",
        "gotomeeting.com",
        "bluejeans.com",
        "chime.aws"
    )

    private val URL_REGEX = Regex("""https?://[^\s<>"{}|\\^`\[\]]+""")

    fun extractMeetingUrl(location: String?): String? {
        if (location.isNullOrBlank()) return null
        return URL_REGEX.findAll(location)
            .map { it.value }
            .firstOrNull { url ->
                MEETING_HOSTS.any { host -> url.contains(host, ignoreCase = true) }
            }
    }
}
