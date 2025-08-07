package com.example.capstone_2.network

import com.example.capstone_2.data.UsageRecordDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface UsageApiService {

    @POST("/api/usage/usage/record")
    suspend fun sendUsageRecord(
        @Body record: UsageRecordDto
    ): Response<Map<String, Any?>>
}
