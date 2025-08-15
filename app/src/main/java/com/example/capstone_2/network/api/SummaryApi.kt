package com.example.capstone_2.network.api

import com.example.capstone_2.network.model.AiSummaryResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface SummaryApi {
    // date is optional, format YYYY-MM-DD; when null server may default to today.
    @GET("api/summary")
    suspend fun getSummary(@Query("date") date: String? = null): Response<AiSummaryResponse>
}
