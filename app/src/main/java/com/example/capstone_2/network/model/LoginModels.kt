package com.example.capstone_2.network.model

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val message: String? = null,
    val data: LoginData? = null
)

data class LoginData(
    val accessToken: String? = null,
    val refresh: String? = null
)
