package com.example.voicesns.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {
    fun formatDate(date: Date): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd h:mm a", Locale.getDefault())
        return dateFormat.format(date)
    }
}