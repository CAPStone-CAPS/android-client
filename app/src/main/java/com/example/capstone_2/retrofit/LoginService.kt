package com.example.capstone_2.retrofit

import com.example.capstone_2.data.LoginSession
import com.example.capstone_2.data.SignupResult
import com.example.capstone_2.data.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST

data class UserRequest(
    val username: String,
    val password: String
)

data class NullableUserRequest(
    val username: String?,
    val password: String?
)

interface LoginService {
    @GET("/api/users/me")
    suspend fun getUser(@Header("accessToken") token: String): Response<User>

    @POST("/api/users/login")
    suspend fun login(@Body request: UserRequest): Response<LoginSession>

    @POST("/api/users/signup")
    suspend fun signup(@Body request: UserRequest): Response<SignupResult>

    @PATCH("/api/users/me")
    suspend fun editUser(@Header("accessToken") token: String, @Body request: NullableUserRequest): Response<User>

    // TODO 프로필 이미지 업로드 호출도 추가....
}