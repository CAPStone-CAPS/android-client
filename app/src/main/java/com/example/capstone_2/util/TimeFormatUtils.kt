package com.example.capstone_2.util

fun formatMillis(millis: Long): String {
    val totalSeconds = millis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    return String.format("%02d:%02d", hours, minutes)
}
