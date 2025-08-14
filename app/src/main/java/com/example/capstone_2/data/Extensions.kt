package com.example.capstone_2.data

import android.util.Log
import com.example.capstone_2.DB.UsageSessionEntity
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

fun UsageSessionEntity.getLifestyleDate(): LocalDate {
    val time = Instant.ofEpochMilli(this.startTime)
        .atZone(ZoneId.of("Asia/Seoul"))
        .toLocalTime()

    val date = Instant.ofEpochMilli(this.startTime)
        .atZone(ZoneId.of("Asia/Seoul"))
        .toLocalDate()

    return if (time < LocalTime.of(6, 0)) date.minusDays(1) else date
}


fun UsageSessionEntity.getLifestyleEndDate(): LocalDate {
    val time = Instant.ofEpochMilli(this.endTime).atZone(ZoneId.of("Asia/Seoul")).toLocalTime()
    val date = Instant.ofEpochMilli(this.endTime).atZone(ZoneId.of("Asia/Seoul")).toLocalDate()
    return if (time < LocalTime.of(6, 0)) date.minusDays(1) else date
}
