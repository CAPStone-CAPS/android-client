package com.example.capstone_2.data

import android.app.usage.UsageEvents
import android.graphics.drawable.Drawable
import com.example.capstone_2.util.AppEvent

// 앱 사용 정보
data class AppUsageInfo(
    val packageName: String,
    val appName: String,
    val appIcon: Drawable?,
    val totalTimeInForeground: Long,
    val firstTimeStamp: Long = 0L,
    val lastTimeStamp: Long = 0L,
    val usageEvents: List<AppEvent>
)

// 하루 144개 블럭으로 표현한 앱 사용 정보
data class AppTimeBlock(
    val appName: String,
    val usageBlocks: BooleanArray, // size == 144
    val totalDurationMillis: Long
)

// 사용 시간 총합 + 앱 리스트
data class UsageStatWithLabel(
    val usageStats: List<AppUsageInfo>,
    val totalTimeMillis: Long
)
