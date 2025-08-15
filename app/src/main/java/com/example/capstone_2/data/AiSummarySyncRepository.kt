package com.example.capstone_2.data
import java.time.ZoneId

import android.content.Context
import android.util.Log
import com.example.capstone_2.network.api.UsageApi
import com.example.capstone_2.retrofit.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AiSummarySyncRepository(private val context: Context) {
    private val api by lazy { RetrofitInstance.get(context).create(UsageApi::class.java) }

    suspend fun syncAndGetSummary(preferredDate: LocalDate? = null): Result<String> = withContext(Dispatchers.IO) {
        val date = preferredDate ?: LocalDate.now(ZoneId.of("Asia/Seoul"))

        // 1) 오늘 데이터 업로드 시도
        val uploaded = UsageCollector.uploadToday(context, date).getOrElse {
            return@withContext Result.failure(it)
        }
        Log.d("AI_SUMMARY", "업로드된 세션 수: $uploaded")

        // 2) 요약 호출 (오늘 → 없으면 404)
        val dateStr = date.format(DateTimeFormatter.ISO_DATE)
        val res = api.getSummary(dateStr)
        if (res.isSuccessful) {
            val msg = res.body()?.data?.message ?: res.body()?.message
            if (!msg.isNullOrBlank()) return@withContext Result.success(msg)
            return@withContext Result.failure(IllegalStateException("빈 요약"))
        } else if (res.code() == 404) {
            // 최근 7일 백오프
            for (offset in 1..7) {
                val d = date.minusDays(offset.toLong())
                val r = api.getSummary(d.format(DateTimeFormatter.ISO_DATE))
                if (r.isSuccessful) {
                    val msg = r.body()?.data?.message ?: r.body()?.message
                    if (!msg.isNullOrBlank()) return@withContext Result.success("[최근 ${offset}일 전 데이터]\n$msg")
                } else if (r.code() != 404) {
                    return@withContext Result.failure(RuntimeException("HTTP ${r.code()}"))
                }
            }
            return@withContext Result.failure(IllegalStateException("최근 7일 이내 요약 없음"))
        } else {
            return@withContext Result.failure(RuntimeException("HTTP ${res.code()}"))
        }
    }
}
