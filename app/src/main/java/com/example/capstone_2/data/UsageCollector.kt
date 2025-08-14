package com.example.capstone_2.data

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.example.capstone_2.network.api.UsageApi
import com.example.capstone_2.network.model.UsageRecordRequest
import com.example.capstone_2.retrofit.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId

/**
 * 오늘(또는 지정한 날짜)의 사용 세션을 추출하여 서버로 업로드.
 * - 세션 정의: 동일 패키지의 Activity RESUMED ~ PAUSED/STOPPED 사이 구간
 * - 서버는 /api/usage/record 를 여러 번 받아 누적
 */
object UsageCollector {
    private const val TAG = "USAGE_COLLECTOR"

    private data class Session(
        val packageName: String,
        val appName: String,
        val start: Long,
        val end: Long
    ) {
        val duration: Long get() = (end - start).coerceAtLeast(0L)
    }

    private fun getAppName(context: Context, packageName: String): String {
        return try {
            val pm = context.packageManager
            val ai = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(ai).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName
        }
    }

    private fun dayRangeMillis(date: LocalDate): Pair<Long, Long> {
        val zone = ZoneId.of("Asia/Seoul")
        val start = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
        return start to end
    }

    suspend fun uploadToday(context: Context, date: LocalDate): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val (startMs, endMs) = dayRangeMillis(date)
            val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val events = usm.queryEvents(startMs, endMs)

            val openMap = HashMap<String, Long>() // package -> last RESUMED time
            val sessions = ArrayList<Session>()

            val ev = UsageEvents.Event()
            while (events.hasNextEvent()) {
                events.getNextEvent(ev)
                when (ev.eventType) {
                    UsageEvents.Event.ACTIVITY_RESUMED -> {
                        openMap[ev.packageName] = ev.timeStamp
                    }
                    UsageEvents.Event.ACTIVITY_PAUSED,
                    UsageEvents.Event.ACTIVITY_STOPPED -> {
                        val started = openMap.remove(ev.packageName)
                        if (started != null && ev.timeStamp > started) {
                            val appName = getAppName(context, ev.packageName)
                            sessions.add(Session(ev.packageName, appName, started, ev.timeStamp))
                        }
                    }
                }
            }

            // 열린 세션이 닫히지 않고 남아있다면, end를 dayEnd로 보정
            val dayEnd = endMs
            for ((pkg, started) in openMap) {
                val appName = getAppName(context, pkg)
                sessions.add(Session(pkg, appName, started, dayEnd))
            }

            if (sessions.isEmpty()) {
                Log.d(TAG, "업로드할 세션 없음")
                return@withContext Result.success(0)
            }

            val api = RetrofitInstance.get(context).create(UsageApi::class.java)
            var ok = 0
            for (s in sessions) {
                val body = UsageRecordRequest(
                    package_name = s.packageName,
                    app_name = s.appName,
                    usage_time_ms = s.duration,
                    start_time = s.start,
                    end_time = s.end
                )
                if (UsageUploader.safeRecord(context, body)) ok++
                else Log.e(TAG, "업로드 실패 for ${s.packageName} (404-resilient tried multiple paths)")
            }
            Log.d(TAG, "업로드 완료: ${ok}/${sessions.size}")
            Result.success(ok)
        } catch (e: SecurityException) {
            Log.e(TAG, "권한 부족: ${e.message}")
            Result.failure(IllegalStateException("UsageStats 권한 필요"))
        } catch (e: Exception) {
            Log.e(TAG, "업로드 오류: ${e.message}")
            Result.failure(e)
        }
    }
}
