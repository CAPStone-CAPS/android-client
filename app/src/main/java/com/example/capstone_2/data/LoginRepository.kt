package com.example.capstone_2.data

import android.content.Context
import com.example.capstone_2.network.api.LoginApi
import com.example.capstone_2.network.model.LoginRequest
import com.example.capstone_2.retrofit.RetrofitInstance
import com.example.capstone_2.util.TokenStore

class LoginRepository(private val context: Context) {
    private val api by lazy { RetrofitInstance.get(context).create(LoginApi::class.java) }

    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            val res = api.login(LoginRequest(email, password))
            if (res.isSuccessful) {
                val body = res.body()
                val access = body?.data?.accessToken
                val refresh = body?.data?.refresh
                if (!access.isNullOrBlank()) {
                    TokenStore.saveAccessToken(context, access)
                }
                if (!refresh.isNullOrBlank()) {
                    TokenStore.saveRefreshToken(context, refresh)
                }
                Result.success(Unit)
            } else {
                Result.failure(RuntimeException("HTTP ${res.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
