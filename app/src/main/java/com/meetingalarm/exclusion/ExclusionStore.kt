package com.meetingalarm.exclusion

import android.content.Context
import java.io.File

class ExclusionStore(context: Context) {

    private val file = File(context.filesDir, "excluded_meetings.txt")

    fun load(): Set<String> {
        if (!file.exists()) return emptySet()
        return file.readLines()
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }
            .toSet()
    }

    fun add(name: String) {
        val key = name.trim().lowercase()
        val current = load()
        if (key in current) return
        file.appendText("$key\n")
    }

    fun remove(name: String) {
        val key = name.trim().lowercase()
        val remaining = load() - key
        file.writeText(remaining.joinToString("\n") { it } + if (remaining.isNotEmpty()) "\n" else "")
    }

    fun isExcluded(title: String): Boolean {
        return title.trim().lowercase() in load()
    }
}
