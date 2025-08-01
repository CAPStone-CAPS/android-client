package com.example.capstone_2.data

import android.graphics.drawable.Drawable

// 앱 사용 정보
data class AppUsageInfo(
    val packageName: String,
    val appName: String,
    val appIcon: Drawable?,
    val totalTimeInForeground: Long,
    val firstTimeStamp: Long = 0L,
    val lastTimeStamp: Long = 0L
)

// 하루 144개 블럭으로 표현한 앱 사용 정보
data class AppTimeBlock(
    val appName: String,
    val usageBlocks: List<Boolean>  // 24시간 * 6 = 144개 (10분 단위)
)

// 사용 시간 총합 + 앱 리스트
data class UsageStatWithLabel(
    val usageStats: List<AppUsageInfo>,
    val totalTimeMillis: Long
)
