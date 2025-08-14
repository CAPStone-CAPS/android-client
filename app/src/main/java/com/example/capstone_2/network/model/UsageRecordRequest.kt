package com.example.capstone_2.network.model

data class UsageRecordRequest(
    val package_name: String,
    val app_name: String,
    val usage_time_ms: Long,
    val start_time: Long,
    val end_time: Long
)
