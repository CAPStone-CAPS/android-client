package com.example.capstone_2.util

import android.app.usage.UsageEvents
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import com.example.capstone_2.data.AppTimeBlock
import com.example.capstone_2.data.AppUsageInfo
import com.example.capstone_2.data.UsageStatWithLabel
import com.example.capstone_2.data.AppInfoHelper
import java.util.*
import android.os.Build
import android.util.Log
import java.time.LocalDate
import java.time.ZoneId


fun getDetailedUsageInfoPerApp(context: Context, date: LocalDate): List<AppUsageInfo> {
    val usageStatsManager =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    val zoneId = ZoneId.systemDefault()
    val startTime = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
    val endTime = if (date == LocalDate.now()) System.currentTimeMillis()
    else date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()

    val events = usageStatsManager.queryEvents(startTime, endTime)
    val appUsageMap = mutableMapOf<String, Long>()
    val lastForegroundMap = mutableMapOf<String, Long>()
    val event = UsageEvents.Event()
    val helper = AppInfoHelper(context)

    while (events.hasNextEvent()) {
        events.getNextEvent(event)

        when (event.eventType) {
            UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                lastForegroundMap[event.packageName] = event.timeStamp
            }

            UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                val start = lastForegroundMap[event.packageName] ?: continue
                val duration = event.timeStamp - start
                if (duration > 0) {
                    appUsageMap[event.packageName] =
                        appUsageMap.getOrDefault(event.packageName, 0L) + duration
                }
                lastForegroundMap.remove(event.packageName)
            }
        }
    }

    val excludedPackages = listOf(
        "com.android.systemui",
        "com.google.android.apps.nexuslauncher",
        "com.sec.android.app.launcher"
    )

    return appUsageMap.mapNotNull { (packageName, totalTime) ->
        if (
            packageName.startsWith("com.android.") ||
            packageName.startsWith("com.samsung.") ||
            excludedPackages.contains(packageName)
        ) {
            return@mapNotNull null //  제외 대상
        }

        try {
            AppUsageInfo(
                packageName = packageName,
                appName = helper.getAppName(packageName),
                appIcon = helper.getAppIcon(packageName),
                totalTimeInForeground = totalTime
            )
        } catch (e: Exception) {
            null
        }
    }.sortedByDescending { it.totalTimeInForeground }
}

private fun getStartOfDayMillis(): Long {
    val cal = java.util.Calendar.getInstance()
    cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
    cal.set(java.util.Calendar.MINUTE, 0)
    cal.set(java.util.Calendar.SECOND, 0)
    cal.set(java.util.Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

private data class UsageTracking(
    var total: Long = 0L,
    var start: Long = 0L,
    var first: Long = 0L,
    var last: Long = 0L
)

fun getAppUsageStats(context: Context): UsageStatWithLabel {
    return try {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        val rawStats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_BEST,
            startTime,
            endTime
        ).orEmpty().filter {
            it.lastTimeUsed in startTime..endTime && it.totalTimeInForeground > 0
        }
        Log.d("USAGE_RAW", "=== queryUsageStats() 결과 (${rawStats.size}개) ===")
//        rawStats.forEach { stat ->
//            Log.d("USAGE_RAW", """
//                패키지명: ${stat.packageName}
//                최초 실행: ${stat.firstTimeStamp}
//                마지막 실행: ${stat.lastTimeStamp}
//                마지막 사용: ${stat.lastTimeUsed}
//                포그라운드 시간(ms): ${stat.totalTimeInForeground}
//            """.trimIndent())
//        }
        val helper = AppInfoHelper(context)

        val mergedStats = rawStats.groupBy { it.packageName }
            .mapNotNull { (packageName, stats) ->
                try {
                    AppUsageInfo(
                        packageName = packageName,
                        appName = helper.getAppName(packageName),
                        appIcon = helper.getAppIcon(packageName),
                        totalTimeInForeground = stats.sumOf { it.totalTimeInForeground }
                    )
                } catch (e: Exception) {
                    null
                }
            }
            .sortedByDescending { it.totalTimeInForeground }

        val total = mergedStats.sumOf { it.totalTimeInForeground }

        UsageStatWithLabel(mergedStats, total)
    } catch (e: Exception) {
        e.printStackTrace()
        UsageStatWithLabel(emptyList(), 0L)
    }
}

fun convertEventsToTimeGridMatrix(context: Context, date: LocalDate): List<Set<String>> {
    val appInfoHelper = AppInfoHelper(context)
    val result = MutableList(144) { mutableSetOf<String>() } // 10분 × 24시간

    val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    val zoneId = ZoneId.systemDefault()
    val startOfDay = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
    val endOfDay = date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
    val interval = 10 * 60 * 1000L // 10분 단위

    val events = usageStatsManager.queryEvents(startOfDay, endOfDay)
    val lastForegroundMap = mutableMapOf<String, Long>()
    val event = UsageEvents.Event()

    while (events.hasNextEvent()) {
        events.getNextEvent(event)

        when (event.eventType) {
            UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                lastForegroundMap[event.packageName] = event.timeStamp
            }

            UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                val start = lastForegroundMap[event.packageName] ?: continue
                val end = event.timeStamp
                lastForegroundMap.remove(event.packageName)

                if (start >= end) continue

                val appName = appInfoHelper.getAppName(event.packageName)

                val startIndex = ((start - startOfDay) / interval).toInt().coerceIn(0, 143)
                val endIndex = ((end - startOfDay) / interval).toInt().coerceIn(0, 143)

                for (i in startIndex..endIndex) {
                    result[i].add(appName)
                }
            }
        }
    }

    return result
}


fun getActualUsageTimePerApp(context: Context): Map<String, Long> {
    val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val startTime = calendar.timeInMillis
    val endTime = System.currentTimeMillis()

    val usageEvents = usageStatsManager.queryEvents(startTime, endTime)
    val usageMap = HashMap<String, Long>()
    val lastForegroundMap = HashMap<String, Long>()

    val event = UsageEvents.Event()

    while (usageEvents.hasNextEvent()) {
        usageEvents.getNextEvent(event)

        when (event.eventType) {
            UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                lastForegroundMap[event.packageName] = event.timeStamp
            }

            UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                val start = lastForegroundMap[event.packageName]
                if (start != null && start <= event.timeStamp) {
                    val duration = event.timeStamp - start
                    usageMap[event.packageName] = usageMap.getOrDefault(event.packageName, 0L) + duration
                    lastForegroundMap.remove(event.packageName)
                }
            }
        }
    }

    return usageMap
}

data class UsageSession(
    val packageName: String,
    val appName: String,
    val startTime: Long,
    val endTime: Long
)

fun getAppUsageSessions(context: Context, date: LocalDate): List<UsageSession> {
    val usageStatsManager =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    val zoneId = ZoneId.systemDefault()
    val startTime = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
    val endTime = date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()

    val events = usageStatsManager.queryEvents(startTime, endTime)
    val event = UsageEvents.Event()
    val helper = AppInfoHelper(context)

    val lastStartMap = mutableMapOf<String, Long>()
    val sessions = mutableListOf<UsageSession>()

    while (events.hasNextEvent()) {
        events.getNextEvent(event)

        when (event.eventType) {
            UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                lastStartMap[event.packageName] = event.timeStamp
            }

            UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                val start = lastStartMap[event.packageName] ?: continue
                val end = event.timeStamp
                lastStartMap.remove(event.packageName)

                if (end > start) {
                    sessions.add(
                        UsageSession(
                            packageName = event.packageName,
                            appName = helper.getAppName(event.packageName),
                            startTime = start,
                            endTime = end
                        )
                    )
                }
            }
        }
    }

    return sessions
}
