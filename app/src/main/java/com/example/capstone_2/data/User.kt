package com.example.capstone_2.data

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id")
    val id: Int,

    @SerializedName("username")
    val username: String,

    @SerializedName("profile_image_url")
    val profile_image_url: String?
)