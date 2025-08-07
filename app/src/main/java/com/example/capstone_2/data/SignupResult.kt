package com.example.capstone_2.data

import com.google.gson.annotations.SerializedName

data class SignupResult(
    @SerializedName("username")
    val username: String
)