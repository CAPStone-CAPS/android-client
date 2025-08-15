package com.example.capstone_2.network.api

import com.example.capstone_2.network.model.LoginRequest
import com.example.capstone_2.network.model.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface LoginApi {
    @POST("api/users/login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>
}
