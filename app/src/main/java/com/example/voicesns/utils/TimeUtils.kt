package com.example.voicesns.utils

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
}