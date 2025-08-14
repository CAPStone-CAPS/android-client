package com.example.capstone_2.util

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import androidx.compose.ui.graphics.Color
import android.util.Log
import com.example.capstone_2.DB.UsageSessionEntity
import com.example.capstone_2.data.AppInfoHelper
import com.example.capstone_2.data.AppUsageInfo
import com.example.capstone_2.data.UsageStatWithLabel
import com.example.capstone_2.data.getLifestyleDate
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.util.*
import kotlin.collections.set

data class AppEvent(
    val packageName: String,
    val className: String?,
    val eventType: Int,
    val timeStamp: Long,
)

data class BlockUsageEntry(
    val appName: String,
    val blockIndex: Int,
    val color: Color,
    val density: Int,
    val dayOfWeek: DayOfWeek
)

data class UsageSession(
    val packageName: String,
    val appName: String,
    val startTime: Long,
    val endTime: Long
)

fun getDetailedUsageInfoPerApp(context: Context, date: LocalDate): List<AppUsageInfo> {
    val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    val zoneId = ZoneId.of("Asia/Seoul")
    val startTime = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
    val endTime = if (date == LocalDate.now(ZoneId.of("Asia/Seoul"))) System.currentTimeMillis()
    else date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()

    val usageEvents = usageStatsManager.queryEvents(startTime, endTime)
    val allEvents = mutableListOf<AppEvent>()
    val appUsageMap = mutableMapOf<String, Long>()
    val lastForegroundMap = mutableMapOf<String, Long>()

    val event = UsageEvents.Event()

    while (usageEvents.hasNextEvent()) {
        usageEvents.getNextEvent(event)

        val copied = AppEvent(
            packageName = event.packageName,
            className = event.className,
            eventType = event.eventType,
            timeStamp = event.timeStamp
        )

        allEvents.add(copied)

        when (event.eventType) {
            UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                lastForegroundMap[event.packageName] = event.timeStamp
            }
            UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                val start = lastForegroundMap[event.packageName] ?: continue
                val end = event.timeStamp
                if (start <= end) {
                    val duration = end - start
                    appUsageMap[event.packageName] = appUsageMap.getOrDefault(event.packageName, 0L) + duration
                }
                lastForegroundMap.remove(event.packageName)
            }
        }
    }

    val eventsPerApp = allEvents.groupBy { it.packageName }
    val helper = AppInfoHelper(context)

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
            return@mapNotNull null
        }

        try {
            AppUsageInfo(
                packageName = packageName,
                appName = helper.getAppName(packageName),
                appIcon = helper.getAppIcon(packageName),
                totalTimeInForeground = totalTime,
                usageEvents = eventsPerApp[packageName] ?: emptyList()
            )
        } catch (e: Exception) {
            null
        }
    }.sortedByDescending { it.totalTimeInForeground }
}

fun convertEventsToBlockEntries(
    events: List<AppEvent>,
    date: LocalDate,
    context: Context
): List<BlockUsageEntry> {
    val appInfoHelper = AppInfoHelper(context)
    val appColorMap = generateAppColorMap(emptyList())

    val result = mutableListOf<BlockUsageEntry>()
    val zoneId = ZoneId.of("Asia/Seoul")
    val startOfDay = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
    val endOfDay = date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
    val interval = 10 * 60 * 1000L

    val lastForegroundMap = mutableMapOf<String, Long>()
    val blockUsageCounter = mutableMapOf<Pair<String, Int>, Int>()

    for (event in events) {
        if (event.timeStamp !in startOfDay until endOfDay) continue

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
                val color = appColorMap[appName] ?: Color.Gray

                val startIndex = ((start - startOfDay) / interval).toInt().coerceIn(0, 143)
                val endIndex = ((end - startOfDay) / interval).toInt().coerceIn(0, 143)

                for (i in startIndex..endIndex) {
                    val key = appName to i
                    blockUsageCounter[key] = blockUsageCounter.getOrDefault(key, 0) + 1
                }
            }
        }
    }

    val dayOfWeek = date.dayOfWeek
    for ((key, count) in blockUsageCounter) {
        val (appName, blockIndex) = key
        val color = appColorMap[appName] ?: Color.Gray
        val density = count.coerceIn(1, 5)

        result.add(
            BlockUsageEntry(
                appName = appName,
                blockIndex = blockIndex,
                color = color,
                density = density,
                dayOfWeek = dayOfWeek
            )
        )
    }

    return result
}

fun getAppUsageSessions(context: Context, date: LocalDate): List<UsageSession> {
    val usageStatsManager =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    val zoneId = ZoneId.of("Asia/Seoul")
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

fun convertSessionToTimeGridMatrix(sessions: List<UsageSessionEntity>, selectedDate: LocalDate): BooleanArray {
    val blockSets: List<Set<String>> = sessions
        .filter { it.getLifestyleDate() == selectedDate }
        .map { session ->
            val start = session.startTime
            val end = session.endTime
            val blocks = mutableSetOf<String>()
            var current = start
            while (current < end) {
                val minuteOfDay = Date(current).toInstant().atZone(ZoneId.of("Asia/Seoul")).toLocalTime().toSecondOfDay() / 60
                val blockIndex = (minuteOfDay - 360) / 10
                if (blockIndex in 0..143) {
                    blocks.add(blockIndex.toString())
                }
                current += 60_000
            }
            blocks
        }

    return BooleanArray(144) { index ->
        blockSets.any { blockSet -> index.toString() in blockSet }
    }
}
