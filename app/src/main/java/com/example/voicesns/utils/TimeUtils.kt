package com.example.voicesns.utils

import java.sql.Time
import java.util.Calendar
import java.util.Locale

object TimeUtils {
    fun formatDuration(duration: Int): String {
        val minutes = (duration / 1000) / 60
        val seconds = (duration / 1000) % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    fun formatElapsed(seconds: Int): String {
        val minutes = seconds / 60
        val displaySeconds = seconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, displaySeconds)
    }

    fun getSQLTime(hour: Int, minute: Int): Time {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        return Time(calendar.timeInMillis)
    }
}