package com.example.capstone_2.retrofit

import com.example.capstone_2.data.LoginSession
import com.example.capstone_2.data.SignupResult
import com.example.capstone_2.data.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST

interface LoginService {
    @GET("/api/users/me")
    suspend fun getUser(): Response<User>

    @POST("/api/users/login")
    suspend fun login(@Body username: String, @Body password: String): Response<LoginSession>

    @POST("/api/users/signup")
    suspend fun signup(@Body username: String, @Body password: String): Response<SignupResult>

    @PATCH("/api/users/me")
    suspend fun editUser(@Body username: String?, @Body password: String?): Response<User>

    // TODO 프로필 이미지 업로드 호출도 추가....
}