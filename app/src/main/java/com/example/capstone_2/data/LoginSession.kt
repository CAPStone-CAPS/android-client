package com.example.capstone_2.data

import com.google.gson.annotations.SerializedName

data class LoginSession (
    @SerializedName("accessToken")
    val accessToken: String,

    @SerializedName("refresh")
    val refresh: String
)

data class LoginResponse(
    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: LoginSession
)