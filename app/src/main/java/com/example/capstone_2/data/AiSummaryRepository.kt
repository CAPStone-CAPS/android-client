package com.example.capstone_2.data
import java.time.ZoneId

import android.util.Log
import android.content.Context
import com.example.capstone_2.network.api.SummaryApi
import com.example.capstone_2.retrofit.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AiSummaryRepository(private val context: Context) {
    private val api by lazy { RetrofitInstance.get(context).create(SummaryApi::class.java) }

    suspend fun fetch(date: String? = null): Result<String> = withContext(Dispatchers.IO) {
        try {
            val queryDate = date ?: LocalDate.now(ZoneId.of("Asia/Seoul")).format(DateTimeFormatter.ISO_DATE)
            Log.d("AI_SUMMARY", "요약 요청 시작 date=" + queryDate)
            val res = api.getSummary(queryDate)
            Log.d("AI_SUMMARY", "응답 코드: " + res.code())
            if (res.isSuccessful) {
                val body = res.body()
                Log.d("AI_SUMMARY", "본문: " + body)
                val msg = body?.data?.message ?: body?.message
                if (!msg.isNullOrBlank()) Result.success(msg)
                else Result.failure(IllegalStateException("빈 요약"))
            } else {
                val err = res.errorBody()?.string()
                Log.e("AI_SUMMARY", "에러 바디: " + err)
                Result.failure(RuntimeException("HTTP ${res.code()}"))
            }
        } catch (e: Exception) {
            Log.e("AI_SUMMARY", "예외: " + (e.message ?: e.toString()))
            Result.failure(e)
        }
    }
}
