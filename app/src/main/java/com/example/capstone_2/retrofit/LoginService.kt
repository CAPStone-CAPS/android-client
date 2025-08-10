package com.example.capstone_2.retrofit

import com.example.capstone_2.data.GetUserResponse
import com.example.capstone_2.data.LoginResponse
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
    suspend fun getUser(@Header("Authorization") bearerToken: String): Response<GetUserResponse>

    @POST("/api/users/login")
    suspend fun login(@Body request: UserRequest): Response<LoginResponse>

    @POST("/api/users/signup")
    suspend fun signup(@Body request: UserRequest): Response<SignupResult>

    @PATCH("/api/users/me")
    suspend fun editUser(@Header("Authorization") bearerToken: String, @Body request: NullableUserRequest): Response<GetUserResponse>

    // TODO 프로필 이미지 업로드 호출도 추가....
}