package com.example.capstone_2.network

import com.example.capstone_2.ui.AuthManager
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Reads access token from AuthManager so it stays in sync with existing code.
 * This avoids mismatch between TokenStore and AuthManager that caused 401 on /api/summary.
 */
class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = AuthManager.authToken
        val request = if (!token.isNullOrBlank()) {
            original.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }
        return chain.proceed(request)
    }
}
