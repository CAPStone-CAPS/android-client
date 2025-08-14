package com.example.capstone_2.network

import com.example.capstone_2.data.AiSummaryResponse
import retrofit2.Response
import retrofit2.http.GET

interface SummaryService {
    @GET("/api/summary")
    suspend fun getSummary(): Response<AiSummaryResponse>
}
