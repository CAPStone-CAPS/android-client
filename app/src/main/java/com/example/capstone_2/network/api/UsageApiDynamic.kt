package com.example.capstone_2.network.api

import com.example.capstone_2.network.model.UsageRecordRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface UsageApiDynamic {
    @POST
    suspend fun recordUsage(@Url url: String, @Body body: UsageRecordRequest): Response<Unit>
}
