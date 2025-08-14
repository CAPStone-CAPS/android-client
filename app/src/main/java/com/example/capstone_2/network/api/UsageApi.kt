package com.example.capstone_2.network.api

import com.example.capstone_2.network.model.AiSummaryResponse
import com.example.capstone_2.network.model.UsageRecordRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface UsageApi {
    // server might require trailing slash
    @POST("usage/record/")
    suspend fun recordUsage(@Body body: UsageRecordRequest): Response<Unit>

    @GET("summary/")
    suspend fun getSummary(@Query("date") date: String? = null): Response<AiSummaryResponse>
}
