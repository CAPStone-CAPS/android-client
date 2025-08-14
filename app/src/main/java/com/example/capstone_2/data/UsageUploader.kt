package com.example.capstone_2.data

import android.content.Context
import android.util.Log
import com.example.capstone_2.network.api.UsageApiDynamic
import com.example.capstone_2.network.model.UsageRecordRequest
import com.example.capstone_2.retrofit.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl

object UsageUploader {
    private const val TAG = "USAGE_UPLOAD"

    suspend fun safeRecord(context: Context, body: UsageRecordRequest): Boolean =
        withContext(Dispatchers.IO) {
            val retrofit = RetrofitInstance.get(context)
            val api = retrofit.create(UsageApiDynamic::class.java)
            val base: HttpUrl = retrofit.baseUrl()

            // 경로 후보 (singular/plural + trailing slash 유무 + api 접두사 유무)
            val candidates = listOf(
                "usage/record/", "usage/record",
                "usage/records/", "usage/records",
                "api/usage/record/", "api/usage/record",
                "api/usage/records/", "api/usage/records"
            )

            for (p in candidates) {
                try {
                    // baseUrl과 합쳐 "정확히 어떤 URL로 보내는지"를 로그에 남김
                    val full = base.newBuilder().addPathSegments(p).build().toString()
                    Log.d("UPLOAD_TEST", "시도 URL: $full")

                    val res = api.recordUsage(full, body)   // @Url 에 절대 URL 전달
                    if (res.isSuccessful) {
                        Log.d("UPLOAD_TEST", "업로드 성공 @ $full")
                        return@withContext true
                    } else {
                        val err = try { res.errorBody()?.string() } catch (_: Throwable) { null } ?: ""
                        Log.e(TAG, "전송 실패: ${body.package_name}\nStatus: ${res.code()}, Error: $err")
                        // 404만 경로 문제로 보고 다음 후보로 계속 시도
                        if (res.code() !in listOf(404)) return@withContext false
                    }
                } catch (e: Exception) {
                    Log.e("UPLOAD_TEST", "요청 예외 @ $p: ${e.message}")
                    // 네트워크 예외는 다음 후보 계속
                }
            }
            false
        }
}
