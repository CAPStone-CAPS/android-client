package com.example.capstone_2.data

import com.example.capstone_2.DB.UsageSessionEntity

data class UsageRecordDto(
    val package_name: String,
    val app_name: String,
    val usage_time_ms: Long,
    val start_time: Long,
    val end_time: Long
)

fun UsageSessionEntity.toUsageRecordDto(appName: String): UsageRecordDto {
    val usageDuration = endTime - startTime

    return UsageRecordDto(
        package_name = this.packageName,
        app_name = appName,
        usage_time_ms = usageDuration,
        start_time = this.startTime,
        end_time = this.endTime
    )
}
